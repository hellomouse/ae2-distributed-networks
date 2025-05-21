package net.hellomouse.ae2dn.mixin;

import appeng.api.networking.IGridNode;
import appeng.blockentity.networking.ControllerBlockEntity;
import appeng.me.pathfinding.ControllerValidator;
import com.llamalad7.mixinextras.sugar.Local;
import net.hellomouse.ae2dn.extension.ControllerValidatorExt;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(ControllerValidator.class)
public class ControllerValidatorMixin implements ControllerValidatorExt {
    @Unique private Set<ControllerBlockEntity> ae2dn$unvisited = null;

    @Override
    public @Nullable Set<ControllerBlockEntity> ae2dn$getUnvisited() {
        return ae2dn$unvisited;
    }

    @Override
    public void ae2dn$setUnvisited(@Nullable Set<ControllerBlockEntity> unvisited) {
        ae2dn$unvisited = unvisited;
    }

    @Inject(
        method = "visitNode",
        at = @At(
            value = "FIELD",
            target = "Lappeng/me/pathfinding/ControllerValidator;found:I",
            opcode = Opcodes.PUTFIELD,
            ordinal = 0,
            shift = At.Shift.AFTER
        )
    )
    private void updateUnvisited(
        IGridNode node,
        CallbackInfoReturnable<Boolean> cir,
        @Local ControllerBlockEntity controller
    ) {
        if (this.ae2dn$unvisited != null) {
            this.ae2dn$unvisited.remove(controller);
        }
    }
}
