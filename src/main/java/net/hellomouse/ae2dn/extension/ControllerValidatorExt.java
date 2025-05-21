package net.hellomouse.ae2dn.extension;

import appeng.blockentity.networking.ControllerBlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public interface ControllerValidatorExt {
    @Nullable Set<ControllerBlockEntity> ae2dn$getUnvisited();

    void ae2dn$setUnvisited(@Nullable Set<ControllerBlockEntity> unvisited);
}
