package net.hellomouse.ae2dn.extension;

import net.hellomouse.ae2dn.pathfinding.ControllerInfo.SubtreeInfo;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

public interface HasSubtreeInfo {
    @Nullable
    WeakReference<SubtreeInfo> ae2dn$getSubtreeInfo();

    void ae2dn$setSubtreeInfo(@Nullable SubtreeInfo subtreeInfo);
}
