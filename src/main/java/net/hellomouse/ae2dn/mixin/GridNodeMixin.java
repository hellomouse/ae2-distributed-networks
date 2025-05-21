package net.hellomouse.ae2dn.mixin;

import appeng.me.GridNode;
import net.hellomouse.ae2dn.pathfinding.ControllerInfo.SubtreeInfo;
import net.hellomouse.ae2dn.extension.HasSubtreeInfo;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.lang.ref.WeakReference;

@Mixin(GridNode.class)
public class GridNodeMixin implements HasSubtreeInfo {
    @Unique
    @Nullable
    private WeakReference<SubtreeInfo> ae2dn$subtreeInfo = null;

    @Override
    public @Nullable WeakReference<SubtreeInfo> ae2dn$getSubtreeInfo() {
        return ae2dn$subtreeInfo;
    }

    @Override
    public void ae2dn$setSubtreeInfo(@Nullable SubtreeInfo subtreeInfo) {
        if (subtreeInfo == null) {
            this.ae2dn$subtreeInfo = null;
        } else {
            this.ae2dn$subtreeInfo = new WeakReference<>(subtreeInfo);
        }
    }
}
