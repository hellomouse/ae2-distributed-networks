package net.hellomouse.ae2dn;

import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNodeListener;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import appeng.core.AELog;
import net.hellomouse.ae2dn.extension.PathingServiceExt;
import net.hellomouse.ae2dn.RouteDistributorBlock.OperationalState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class RouteDistributorBlockEntity extends AENetworkedBlockEntity {
    private boolean isPrimary = false;

    public RouteDistributorBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        getMainNode().setIdlePowerUsage(Config.rdPassiveEnergyCost);
        getMainNode().setFlags(GridFlags.REQUIRE_CHANNEL);
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public void setPrimary(boolean value) {
        isPrimary = value;
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        if (reason == IGridNodeListener.State.GRID_BOOT) {
            if (!getMainNode().hasGridBooted()) {
                // grid rebooted, no longer primary
                setPrimary(false);
            }
            updateEnergyUsage();
        }
        markForUpdate();
    }

    /** Recalculate energy cost */
    private void updateEnergyUsage() {
        var node = getMainNode();
        if (node.isActive()) {
            var grid = getMainNode().getGrid();
            assert grid != null;
            var pathingServiceExt = ((PathingServiceExt) grid.getPathingService());
            if (isPrimary()) {
                var controllerStructures = pathingServiceExt.ae2dn$getControllerStructureCount();
                int multiplier;
                if (controllerStructures <= 1) {
                    // this ought not to happen
                    AELog.warn("RouteDistributor primary but controllers <= 1");
                    multiplier = 0;
                } else {
                    multiplier = controllerStructures - 1;
                }
                node.setIdlePowerUsage(Config.rdPassiveEnergyCost + multiplier * Config.rdPerControllerEnergyCost);
                return;
            }
        }
        // not active
        node.setIdlePowerUsage(Config.rdPassiveEnergyCost);
    }

    public OperationalState getOperationalState() {
        var node = getMainNode();
        if (!node.isPowered()) {
            return OperationalState.unpowered;
        }
        if (!node.isActive()) {
            return OperationalState.missing_channel;
        }
        if (!isPrimary()) {
            return OperationalState.standby;
        }
        return OperationalState.active;
    }
}
