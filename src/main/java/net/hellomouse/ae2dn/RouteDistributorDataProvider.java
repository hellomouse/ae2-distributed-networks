package net.hellomouse.ae2dn;

import appeng.api.integrations.igtooltip.TooltipBuilder;
import appeng.api.integrations.igtooltip.TooltipContext;
import appeng.api.integrations.igtooltip.providers.BodyProvider;
import appeng.api.integrations.igtooltip.providers.ServerDataProvider;
import appeng.core.localization.GuiText;
import appeng.util.Platform;
import net.hellomouse.ae2dn.RouteDistributorBlock.OperationalState;
import net.hellomouse.ae2dn.extension.PathingServiceExt;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

@SuppressWarnings("UnstableApiUsage")
public class RouteDistributorDataProvider
    implements BodyProvider<RouteDistributorBlockEntity>, ServerDataProvider<RouteDistributorBlockEntity>
{
    private static final String TAG_ENERGY_USAGE = "rdEnergyConsumption";
    private static final String TAG_CONTROLLER_COUNT = "rdControllerCount";

    @Override
    public void buildTooltip(RouteDistributorBlockEntity object, TooltipContext context, TooltipBuilder tooltip) {
        var serverData = context.serverData();
        var blockState = object.getBlockState();
        var state = blockState.getValue(RouteDistributorBlock.OPERATIONAL_STATE);
        // the GridNodeStateDataProvider will handle unpowered and missing_channel cases
        if (state == OperationalState.standby) {
            tooltip.addLine(
                Component.translatable("waila.ae2dn.RouteDistributorStatus")
                    .append(Component.translatable("waila.ae2dn.RouteDistributorStatusStandby"))
            );
        } else if (state == OperationalState.active) {
            tooltip.addLine(
                Component.translatable("waila.ae2dn.RouteDistributorStatus")
                    .append(Component.translatable("waila.ae2dn.RouteDistributorStatusActive"))
            );
        }
        if (serverData.contains(TAG_CONTROLLER_COUNT, Tag.TAG_INT)) {
            var count = serverData.getInt(TAG_CONTROLLER_COUNT);
            tooltip.addLine(
                Component.translatable("waila.ae2dn.ControllerStructureCount", count)
            );
        }
        if (serverData.contains(TAG_ENERGY_USAGE, Tag.TAG_DOUBLE)) {
            var usage = serverData.getDouble(TAG_ENERGY_USAGE);
            tooltip.addLine(GuiText.PowerUsageRate.text(Platform.formatPower(usage, true)));
        }
    }

    @Override
    public void provideServerData(Player player, RouteDistributorBlockEntity object, CompoundTag serverData) {
        var node = object.getMainNode();
        var blockState = object.getBlockState();
        var state = blockState.getValue(RouteDistributorBlock.OPERATIONAL_STATE);
        if (node.isReady()) {
            if (state != OperationalState.unpowered) {
                //noinspection DataFlowIssue: node.isReady() checked
                serverData.putDouble(TAG_ENERGY_USAGE, node.getNode().getIdlePowerUsage());
            }
            if (state == OperationalState.standby || state == OperationalState.active) {
                //noinspection DataFlowIssue: node.isReady() checked
                var pathingServiceExt = (PathingServiceExt) node.getGrid().getPathingService();
                serverData.putInt(TAG_CONTROLLER_COUNT, pathingServiceExt.ae2dn$getControllerStructureCount());
            }
        }
    }
}
