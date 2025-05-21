package net.hellomouse.ae2dn.mixin;

import appeng.api.networking.IGrid;
import appeng.api.networking.pathing.ControllerState;
import appeng.blockentity.networking.ControllerBlockEntity;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.hellomouse.ae2dn.pathfinding.TrunkIssue;
import net.hellomouse.ae2dn.extension.PathingServiceExt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ControllerBlockEntity.class)
public class ControllerBlockEntityMixin {
    @ModifyExpressionValue(
        method = "updateState",
        at = @At(
            value = "INVOKE",
            target = "Lappeng/api/networking/pathing/IPathingService;getControllerState()Lappeng/api/networking/pathing/ControllerState;"
        )
    )
    private ControllerState overrideInvalidState(ControllerState original, @Local IGrid grid) {
        if (original == ControllerState.CONTROLLER_CONFLICT) {
            return original;
        }
        if (((PathingServiceExt) grid.getPathingService()).ae2dn$getTrunkIssue() != TrunkIssue.NONE) {
            return ControllerState.CONTROLLER_CONFLICT;
        }
        return original;
    }
}
