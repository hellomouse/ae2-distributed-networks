package net.hellomouse.ae2dn.mixin;

import appeng.api.networking.events.GridControllerChange;
import appeng.api.networking.pathing.ControllerState;
import appeng.blockentity.networking.ControllerBlockEntity;
import appeng.me.Grid;
import appeng.me.pathfinding.AdHocChannelUpdater;
import appeng.me.pathfinding.ControllerValidator;
import appeng.me.pathfinding.PathingCalculation;
import appeng.me.service.PathingService;
import com.llamalad7.mixinextras.sugar.Local;
import net.hellomouse.ae2dn.Config;
import net.hellomouse.ae2dn.pathfinding.TrunkIssue;
import net.hellomouse.ae2dn.extension.ControllerValidatorExt;
import net.hellomouse.ae2dn.extension.PathingServiceExt;
import net.hellomouse.ae2dn.extension.PathingCalculationExt;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("OverwriteAuthorRequired")
@Mixin(PathingService.class)
public class PathingServiceMixin implements PathingServiceExt {
    @Unique private TrunkIssue ae2dn$trunkIssue = TrunkIssue.NONE;
    @Unique private TrunkIssue ae2dn$oldTrunkIssue = TrunkIssue.NONE;
    /** How many distinct controller structures exist */
    @Unique private int ae2dn$controllerStructureCount;
    @Unique private ControllerState ae2dn$oldControllerState = null;

    @Shadow private boolean recalculateControllerNextTick;
    @Shadow @Final private Grid grid;
    @Shadow @Final private Set<ControllerBlockEntity> controllers;
    @Shadow private ControllerState controllerState;

    @Unique
    @Override
    public TrunkIssue ae2dn$getTrunkIssue() {
        return ae2dn$trunkIssue;
    }

    @Unique
    @Override
    public int ae2dn$getControllerStructureCount() {
        return ae2dn$controllerStructureCount;
    }

    @Inject(
        method = "onServerEndTick",
        at = @At("HEAD")
    )
    private void setOldStates(CallbackInfo ci) {
        // keep track of these to determine if we need to send updates for the controllers
        ae2dn$oldTrunkIssue = ae2dn$trunkIssue;
        ae2dn$oldControllerState = controllerState;
    }

    @Inject(
        method = "onServerEndTick",
        at = @At(
            value = "INVOKE",
            target = "Lappeng/me/service/PathingService;achievementPost()V",
            ordinal = 0
        )
    )
    private void maybePostGridControllerChange(CallbackInfo ci) {
        // if either trunkIssue or controllerState changed, fire an event
        if (ae2dn$oldTrunkIssue != ae2dn$trunkIssue || ae2dn$oldControllerState != controllerState) {
            grid.postEvent(new GridControllerChange());
        }
    }

    @Inject(
        method = "onServerEndTick",
        at = @At(
            value = "INVOKE",
            target = "Lappeng/me/pathfinding/PathingCalculation;getChannelsInUse()I",
            ordinal = 0
        )
    )
    private void afterPathingCalculation(CallbackInfo ci, @Local PathingCalculation calculation) {
        ae2dn$trunkIssue = ((PathingCalculationExt) calculation).ae2dn$getTrunkIssue();
        if (ae2dn$trunkIssue != TrunkIssue.NONE) {
            // remove all assigned channels
            grid.getPivot().beginVisit(new AdHocChannelUpdater(0));
        }
    }

    @Overwrite
    private void updateControllerState() {
        // why overwrite? because we pretty much replace the whole method anyway
        recalculateControllerNextTick = false;
        ae2dn$controllerStructureCount = 0;

        if (!Config.globalEnable) {
            controllerState = ControllerValidator.calculateState(controllers);
            if (controllerState == ControllerState.CONTROLLER_ONLINE) {
                ae2dn$controllerStructureCount = 1;
            }
            return;
        }

        if (controllers.isEmpty()) {
            controllerState = ControllerState.NO_CONTROLLER;
            return;
        }

        if (ControllerValidatorAccessor.hasControllerCross(controllers)) {
            controllerState = ControllerState.CONTROLLER_CONFLICT;
            return;
        }

        var unvisited = new HashSet<>(controllers);

        while (!unvisited.isEmpty()) {
            var next = unvisited.iterator().next();
            var nextNode = next.getGridNode();
            if (nextNode == null) {
                controllerState = ControllerState.CONTROLLER_CONFLICT;
                return;
            }

            // validate one controller structure
            var cv = ControllerValidatorAccessor.newInstance(next.getBlockPos());
            ((ControllerValidatorExt) cv).ae2dn$setUnvisited(unvisited);
            nextNode.beginVisit(cv);

            if (!cv.isValid()) {
                controllerState = ControllerState.CONTROLLER_CONFLICT;
                return;
            }

            ae2dn$controllerStructureCount++;
        }

        controllerState = ControllerState.CONTROLLER_ONLINE;
    }
}
