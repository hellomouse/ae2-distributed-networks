package net.hellomouse.ae2dn.mixin;

import appeng.api.integrations.igtooltip.TooltipBuilder;
import appeng.api.integrations.igtooltip.TooltipContext;
import appeng.integration.modules.igtooltip.parts.ChannelDataProvider;
import appeng.me.service.PathingService;
import appeng.parts.networking.IUsedChannelProvider;
import com.llamalad7.mixinextras.sugar.Local;
import net.hellomouse.ae2dn.pathfinding.TrunkIssue;
import net.hellomouse.ae2dn.extension.PathingServiceExt;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChannelDataProvider.class)
public class ChannelDataProviderMixin {
    @Shadow @Final private static String TAG_ERROR;

    @Inject(
        method = "buildTooltip(Lappeng/parts/networking/IUsedChannelProvider;Lappeng/api/integrations/igtooltip/TooltipContext;Lappeng/api/integrations/igtooltip/TooltipBuilder;)V",
        at = @At(
            value = "INVOKE",
            target = "Lappeng/integration/modules/igtooltip/parts/ChannelDataProvider$ChannelError;valueOf(Ljava/lang/String;)Lappeng/integration/modules/igtooltip/parts/ChannelDataProvider$ChannelError;"
        ),
        cancellable = true
    )
    private void handleTrunkError(IUsedChannelProvider object, TooltipContext context, TooltipBuilder tooltip, CallbackInfo ci) {
        var serverData = context.serverData();
        var error = serverData.getString(TAG_ERROR);
        if (error.equals(TrunkIssue.TRUNK_CONNECTION_INVALID.name())) {
            tooltip.addLine(
                Component.translatable("waila.ae2dn.ErrorControllerTrunkInvalid")
                    .withStyle(ChatFormatting.RED)
            );
            ci.cancel();
        } else if (error.equals(TrunkIssue.MISSING_SUBNET_MANAGER.name())) {
            tooltip.addLine(
                Component.translatable("waila.ae2dn.ErrorMissingSubnetManager")
                    .withStyle(ChatFormatting.RED)
            );
            ci.cancel();
        }
    }

    @Inject(
        method = "provideServerData(Lnet/minecraft/world/entity/player/Player;Lappeng/parts/networking/IUsedChannelProvider;Lnet/minecraft/nbt/CompoundTag;)V",
        at = @At(
            value = "INVOKE_ASSIGN",
            target = "Lappeng/me/service/PathingService;getControllerState()Lappeng/api/networking/pathing/ControllerState;",
            ordinal = 0
        )
    )
    private void sendTrunkError(
        CallbackInfo ci,
        @Local PathingService pathingService,
        @Local(argsOnly = true) CompoundTag serverData
    ) {
        var issue = ((PathingServiceExt) pathingService).ae2dn$getTrunkIssue();
        if (issue != TrunkIssue.NONE) {
            serverData.putString(TAG_ERROR, issue.name());
        }
    }
}
