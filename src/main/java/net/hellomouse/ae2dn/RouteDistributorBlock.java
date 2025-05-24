package net.hellomouse.ae2dn;

import appeng.block.AEBaseEntityBlock;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.NotNull;

public class RouteDistributorBlock extends AEBaseEntityBlock<RouteDistributorBlockEntity> {
    public enum OperationalState implements StringRepresentable {
        /** No power */
        unpowered,
        /** Powered but no channel */
        missing_channel,
        /** Powered and has channel, currently standby */
        standby,
        /** Powered and has channel, currently active */
        active;

        @Override
        public @NotNull String getSerializedName() {
            return name();
        }
    }

    public static final EnumProperty<OperationalState> OPERATIONAL_STATE = EnumProperty.create("operational_state", OperationalState.class);

    public RouteDistributorBlock() {
        super(metalProps());
        registerDefaultState(defaultBlockState().setValue(OPERATIONAL_STATE, OperationalState.unpowered));
    }

    @Override
    protected BlockState updateBlockStateFromBlockEntity(
        BlockState currentState,
        RouteDistributorBlockEntity be
    ) {
        return currentState.setValue(OPERATIONAL_STATE, be.getOperationalState());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(OPERATIONAL_STATE);
    }
}
