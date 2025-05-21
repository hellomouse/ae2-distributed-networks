package net.hellomouse.ae2dn.extension;

import net.hellomouse.ae2dn.pathfinding.TrunkIssue;
import org.jetbrains.annotations.NotNull;

public interface PathingCalculationExt {
    /** Set whether the PathingCalculation should consider multi-controller rules */
    void ae2dn$setMultiController(boolean value);

    @NotNull TrunkIssue ae2dn$getTrunkIssue();
}
