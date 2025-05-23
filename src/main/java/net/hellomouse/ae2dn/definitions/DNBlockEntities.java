package net.hellomouse.ae2dn.definitions;

import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.core.definitions.BlockDefinition;
import appeng.core.definitions.DeferredBlockEntityType;
import net.hellomouse.ae2dn.AE2DistributedNetworks;
import net.hellomouse.ae2dn.RouteDistributorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class DNBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> DR =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, AE2DistributedNetworks.MOD_ID);
    private static final List<DeferredBlockEntityType<?>> BLOCK_ENTITY_TYPES = new ArrayList<>();

    public static List<DeferredBlockEntityType<?>> getBlockEntityTypes() {
        return Collections.unmodifiableList(BLOCK_ENTITY_TYPES);
    }

    public static final Supplier<BlockEntityType<RouteDistributorBlockEntity>> ROUTE_DISTRIBUTOR = create(
        "route_distributor",
        RouteDistributorBlockEntity.class,
        RouteDistributorBlockEntity::new,
        DNBlocks.ROUTE_DISTRIBUTOR_BLOCK
    );

    private static <T extends AEBaseBlockEntity> Supplier<BlockEntityType<T>> create(
        String id,
        Class<T> entityClass,
        BlockEntityFactory<T> factory,
        BlockDefinition<? extends AEBaseEntityBlock<T>> blockDefinition
    ) {
        var deferred = DR.register(id, () -> {
            var typeHolder = new AtomicReference<BlockEntityType<T>>();
            var type = BlockEntityType.Builder.of(
                (pos, state) -> factory.create(typeHolder.get(), pos, state),
                blockDefinition.block()
            ).build(null);
            typeHolder.set(type);

            AEBaseBlockEntity.registerBlockEntityItem(type, blockDefinition.asItem());
            blockDefinition.block().setBlockEntity(entityClass, type, null, null);
            return type;
        });
        var result = new DeferredBlockEntityType<>(entityClass, deferred);
        BLOCK_ENTITY_TYPES.add(result);
        return result;
    }

    @FunctionalInterface
    private interface BlockEntityFactory<T extends AEBaseBlockEntity> {
        T create(BlockEntityType<T> type, BlockPos pos, BlockState state);
    }
}
