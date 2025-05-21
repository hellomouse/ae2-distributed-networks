package net.hellomouse.ae2dn;

import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridFlags;
import appeng.blockentity.grid.AENetworkedPoweredBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class SubnetManagerBlockEntity extends AENetworkedPoweredBlockEntity {
    public SubnetManagerBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.setInternalMaxPower(8000);
        this.getMainNode().setIdlePowerUsage(10);
        this.getMainNode().setFlags(GridFlags.REQUIRE_CHANNEL);
    }

    @Override
    public InternalInventory getInternalInventory() {
        return InternalInventory.empty();
    }
}
