package net.hellomouse.ae2dn.extension;

import net.hellomouse.ae2dn.pathfinding.TrunkIssue;

public interface PathingServiceExt {
    TrunkIssue ae2dn$getTrunkIssue();
    int ae2dn$getControllerStructureCount();

    default boolean ae2dn$hasMultipleControllers() {
        return ae2dn$getControllerStructureCount() > 1;
    }
}
