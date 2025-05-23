package net.hellomouse.ae2dn;

import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridFlags;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class RouteDistributorBlockEntity extends AENetworkedBlockEntity {
    public RouteDistributorBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.getMainNode().setIdlePowerUsage(100);
        this.getMainNode().setFlags(GridFlags.REQUIRE_CHANNEL);
    }
}
