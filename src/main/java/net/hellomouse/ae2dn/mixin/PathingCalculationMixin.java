package net.hellomouse.ae2dn.mixin;

import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.blockentity.networking.ControllerBlockEntity;
import appeng.me.GridConnection;
import appeng.me.GridNode;
import appeng.me.pathfinding.IPathItem;
import appeng.me.pathfinding.PathingCalculation;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.hellomouse.ae2dn.Config;
import net.hellomouse.ae2dn.pathfinding.ControllerInfo;
import net.hellomouse.ae2dn.pathfinding.ControllerInfo.SubtreeInfo;
import net.hellomouse.ae2dn.pathfinding.ControllerInfo.TrunkSearchState;
import net.hellomouse.ae2dn.SubnetManagerBlockEntity;
import net.hellomouse.ae2dn.pathfinding.TrunkIssue;
import net.hellomouse.ae2dn.extension.HasSubtreeInfo;
import net.hellomouse.ae2dn.extension.PathingCalculationExt;
import net.hellomouse.ae2dn.extension.PathingServiceExt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

@Mixin(PathingCalculation.class)
public abstract class PathingCalculationMixin implements PathingCalculationExt {
    /** Whether we should run multi-controller pathing as well */
    @Unique private boolean ae2dn$multiController = false;
    /** Multi-controller issues, if any exist */
    @Unique private TrunkIssue ae2dn$trunkIssue = TrunkIssue.NONE;

    /** Tracks SubtreeInfo objects for multi-controller routing. Null unless multiController was requested. */
    @Unique private Map<GridConnection, SubtreeInfo> ae2dn$subtreeInfoMap = null;
    /** Tracks ControllerInfo objects for multi-controller routing. Null unless multiController was requested. */
    @Unique private Map<IGridNode, ControllerInfo> ae2dn$controllerInfoMap = null;

    @Shadow @Final private IGrid grid;
    @Shadow @Final private Set<GridNode> channelNodes;
    @Shadow @Final private Set<IPathItem> visited;
    @Shadow protected abstract void enqueue(IPathItem pathItem, int queueIndex);

    @Unique
    @Override
    public void ae2dn$setMultiController(boolean value) {
        ae2dn$multiController = value;
    }

    @Unique
    @Override
    public @NotNull TrunkIssue ae2dn$getTrunkIssue() {
        return ae2dn$trunkIssue;
    }

    @Unique
    private @Nullable SubtreeInfo ae2dn$getSubtreeInfo(IPathItem node) {
        var ref = ((HasSubtreeInfo) node).ae2dn$getSubtreeInfo();
        if (ref == null) {
            return null;
        }
        var nc = ref.get();
        if (nc == null) {
            return null;
        }
        // ensure correct "epoch"
        if (nc.parent().owner != (Object) this) {
            return null;
        }
        return nc;
    }

    @Unique
    private void ae2dn$maybeDoTrunk(IPathItem current, IPathItem other) {
        var currentSt = ae2dn$getSubtreeInfo(current);
        if (currentSt == null || currentSt.trunkState != TrunkSearchState.SEARCHING) {
            return;
        }
        var otherSt = ae2dn$getSubtreeInfo(other);
        if (otherSt == null || otherSt.trunkState != TrunkSearchState.SEARCHING) {
            return;
        }
        if (currentSt.parent().members == otherSt.parent().members) {
            return;
        }

        // we are ok to trunk
        var currentNc = currentSt.parent();
        var otherNc = otherSt.parent();
        ControllerInfo.mergeMembers(currentNc, otherNc);
        currentSt.trunkState = TrunkSearchState.CONNECTED;
        otherSt.trunkState = TrunkSearchState.CONNECTED;
        var maxChannels = grid.getPathingService().getChannelMode().getCableCapacityFactor() * 32;

        // trunkState will be INVALID if there are already any channels assigned on this subtree
        // so we are free to allocate channels here
        var sideA = current;
        while (sideA != currentNc.controllerNode) {
            if (sideA instanceof GridConnection gc) {
                gc.setAdHocChannels(maxChannels);
            } else if (sideA instanceof GridNode gn) {
                gn.incrementChannelCount(maxChannels);
            }
            sideA = sideA.getControllerRoute();
        }
        var sideB = other;
        while (sideB != otherNc.controllerNode) {
            if (sideB instanceof GridConnection gc) {
                gc.setAdHocChannels(maxChannels);
            } else if (sideB instanceof GridNode gn) {
                gn.incrementChannelCount(maxChannels);
            }
            sideB = sideB.getControllerRoute();
        }
        // setControllerRoute sets usedChannels to 0 so we don't need to clear the rest
    }

    @Inject(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lappeng/api/networking/IGrid;getMachineNodes(Ljava/lang/Class;)Ljava/lang/Iterable;",
            ordinal = 0
        )
    )
    private void initPre(IGrid grid, CallbackInfo ci) {
        // conveniently fetch this from our parent
        ae2dn$multiController = ((PathingServiceExt) grid.getPathingService()).ae2dn$hasMultipleControllers();
        if (!ae2dn$multiController) return;

        ae2dn$subtreeInfoMap = new HashMap<>();
        ae2dn$controllerInfoMap = new HashMap<>();

        for (var node : grid.getMachineNodes(ControllerBlockEntity.class)) {
            visited.add((IPathItem) node);
            // register this controller block
            var controllerInfo = new ControllerInfo((PathingCalculation) (Object) this, node);
            ae2dn$controllerInfoMap.put(node, controllerInfo);
            for (var gcc : node.getConnections()) {
                var gc = (GridConnection) gcc;
                if (!(gc.getOtherSide(node).getOwner() instanceof ControllerBlockEntity)) {
                    // start off BFS with all "exposed" controller faces
                    enqueue(gc, 0);
                    gc.setControllerRoute((GridNode) node);

                    var subtreeInfo = controllerInfo.forSubtree(gc);
                    ae2dn$subtreeInfoMap.put(gc, subtreeInfo);
                    ((HasSubtreeInfo) gc).ae2dn$setSubtreeInfo(subtreeInfo);
                } else {
                    // we've found another controller in the same multiblock, handle it now
                    var other = ae2dn$getSubtreeInfo(gc);
                    if (other == null) {
                        // this GridConnection is not claimed yet
                        var subtreeInfo = controllerInfo.forSubtree(gc);
                        ae2dn$subtreeInfoMap.put(gc, subtreeInfo);
                        ((HasSubtreeInfo) gc).ae2dn$setSubtreeInfo(subtreeInfo);
                    } else {
                        // do the merge
                        ControllerInfo.mergeMembers(other.parent(), controllerInfo);
                    }
                }
            }
        }
    }

    @Redirect(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lappeng/api/networking/IGrid;getMachineNodes(Ljava/lang/Class;)Ljava/lang/Iterable;",
            ordinal = 0
        )
    )
    private Iterable<IGridNode> skipConstructor(IGrid instance, Class<?> machineClass) {
        // skip the original constructor since we handle this here
        if (ae2dn$multiController) {
            return new Iterable<>() {
                @Override
                public @NotNull Iterator<IGridNode> iterator() {
                    return Collections.emptyIterator();
                }
            };
        }

        return instance.getMachineNodes(machineClass);
    }

    @ModifyExpressionValue(
        method = "processQueue",
        at = @At(value = "INVOKE", target = "Ljava/util/Set;contains(Ljava/lang/Object;)Z")
    )
    private boolean bfsWasVisited(
        boolean original,
        @Local(name = "i") IPathItem current,
        @Local(name = "pi") IPathItem other,
        @Local(argsOnly = true) int queueIndex
    ) {
        // try to establish a trunk
        if (ae2dn$multiController && queueIndex == 0 && other.getControllerRoute() != current) {
            ae2dn$maybeDoTrunk(current, other);
        }
        return original;
    }

    @ModifyExpressionValue(
        method = "processQueue",
        at = @At(
            value = "INVOKE",
            target = "Lappeng/me/pathfinding/PathingCalculation;tryUseChannel(Lappeng/me/GridNode;)Z"
        )
    )
    private boolean afterUseChannel(
        boolean worked,
        @Local(name = "i") IPathItem current,
        @Local(argsOnly = true) int queueIndex
    ) {
        // if a channel was assigned from this subtree, prevent trunk
        if (ae2dn$multiController && queueIndex == 0 && worked) {
            var subtreeInfo = ae2dn$getSubtreeInfo(current);
            if (subtreeInfo != null) {
                subtreeInfo.trunkState = TrunkSearchState.INVALID;
            }
        }
        return worked;
    }

    @Inject(
        method = "processQueue",
        at = @At(
            value = "INVOKE",
            target = "Lappeng/me/pathfinding/PathingCalculation;enqueue(Lappeng/me/pathfinding/IPathItem;I)V",
            ordinal = 0
        )
    )
    private void bfsAfterSetParent(
        Queue<IPathItem> oldOpen,
        int queueIndex,
        CallbackInfo ci,
        @Local(name = "i") IPathItem current,
        @Local(name = "pi") IPathItem other
    ) {
        // propagate SubtreeInfo down the subtree
        if (ae2dn$multiController && queueIndex == 0) {
            var parentSubtree = ae2dn$getSubtreeInfo(current);
            if (parentSubtree == null) {
                return;
            }
            if (parentSubtree.trunkState != TrunkSearchState.SEARCHING) {
                // no reason to proceed further
                return;
            }
            if (!(other instanceof GridConnection) && !other.hasFlag(GridFlags.DENSE_CAPACITY)) {
                // not valid to propagate trunk
                return;
            }
            if (other.hasFlag(GridFlags.CANNOT_CARRY_COMPRESSED)) {
                // disallow trunk connections over p2p tunnels
                return;
            }
            ((HasSubtreeInfo) other).ae2dn$setSubtreeInfo(parentSubtree);
        }
    }

    @Inject(
        method = "tryUseChannel",
        at = @At(
            value = "INVOKE",
            target = "Lit/unimi/dsi/fastutil/objects/Reference2IntOpenHashMap;getOrDefault(Ljava/lang/Object;I)I",
            ordinal = 0
        ),
        cancellable = true
    )
    private void preventChannelIfTrunk(CallbackInfoReturnable<Boolean> cir, @Local(name = "pi") GridNode node) {
        if (!ae2dn$multiController) return;
        var subtreeInfo = ae2dn$getSubtreeInfo(node);
        if (subtreeInfo != null && subtreeInfo.trunkState == TrunkSearchState.CONNECTED) {
            // this subtree is currently used by a trunk connection, don't allow channel assignment
            cir.setReturnValue(false);
        }
    }

    @Redirect(
        method = "propagateAssignments",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/List;add(Ljava/lang/Object;)Z",
            ordinal = 0
        )
    )
    @SuppressWarnings("SameReturnValue")
    private boolean skipDFSOnTrunk(List<Object> instance, Object obj) {
        if (ae2dn$multiController) {
            var gc = (GridConnection) obj;
            var subtreeInfo = ae2dn$subtreeInfoMap.get(gc);
            if (subtreeInfo != null && subtreeInfo.trunkState == TrunkSearchState.CONNECTED) {
                // this is a trunk, don't overwrite channels
                return true;
            }
        }

        instance.add(obj);
        return true;
    }

    @Inject(
        method = "compute",
        at = @At(
            value = "INVOKE",
            target = "Lappeng/me/pathfinding/PathingCalculation;propagateAssignments()V",
            ordinal = 0
        ),
        cancellable = true
    )
    private void postCompute(CallbackInfo ci) {
        if (ae2dn$multiController && ae2dn$controllerInfoMap != null) {
            // ensure all controllers are connected properly
            var firstCluster = ae2dn$controllerInfoMap.values().iterator().next();
            if (firstCluster.members.size() != ae2dn$controllerInfoMap.size()) {
                ae2dn$trunkIssue = TrunkIssue.TRUNK_CONNECTION_INVALID;
                ci.cancel();
                return;
            }

            smCheck: if (Config.requireSubnetManager) {
                // TODO: SubnetManagerBlockEntity.class
                for (var node : grid.getMachineNodes(SubnetManagerBlockEntity.class)) {
                    // require at least one subnet manager to have a channel
                    if (channelNodes.contains((GridNode) node)) {
                        break smCheck;
                    }
                }

                ae2dn$trunkIssue = TrunkIssue.MISSING_SUBNET_MANAGER;
                ci.cancel();
            }
        }
    }

    @ModifyReturnValue(method = "getChannelsInUse", at = @At("RETURN"))
    public int wrappedGetChannelsInUse(int previous) {
        if (ae2dn$trunkIssue != TrunkIssue.NONE) {
            return 0;
        }
        return previous;
    }

    @ModifyReturnValue(method = "getChannelsByBlocks", at = @At("RETURN"))
    public int wrappedGetChannelsByBlocks(int previous) {
        if (ae2dn$trunkIssue != TrunkIssue.NONE) {
            return 0;
        }
        return previous;
    }
}
