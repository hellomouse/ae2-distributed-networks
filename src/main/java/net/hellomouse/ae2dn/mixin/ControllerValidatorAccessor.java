package net.hellomouse.ae2dn.mixin;

import appeng.blockentity.networking.ControllerBlockEntity;
import appeng.me.pathfinding.ControllerValidator;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Contract;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Collection;

@Mixin(ControllerValidator.class)
public interface ControllerValidatorAccessor {
    @Invoker("<init>")
    @Contract() // no contract
    static ControllerValidator newInstance(BlockPos pos) {
        throw new AssertionError();
    }

    @Invoker("hasControllerCross")
    @Contract() // no contract
    static boolean hasControllerCross(Collection<ControllerBlockEntity> controllers) {
        throw new AssertionError();
    }
}
