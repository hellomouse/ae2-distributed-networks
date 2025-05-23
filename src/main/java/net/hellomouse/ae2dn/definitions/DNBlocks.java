package net.hellomouse.ae2dn.definitions;

import appeng.block.AEBaseBlockItem;
import appeng.core.MainCreativeTab;
import appeng.core.definitions.BlockDefinition;
import appeng.core.definitions.ItemDefinition;
import net.hellomouse.ae2dn.AE2DistributedNetworks;
import net.hellomouse.ae2dn.RouteDistributorBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class DNBlocks {
    public static final DeferredRegister.Blocks DR = DeferredRegister.createBlocks(AE2DistributedNetworks.MOD_ID);
    private static final List<BlockDefinition<?>> BLOCKS = new ArrayList<>();

    public static List<BlockDefinition<?>> getBlocks() {
        return Collections.unmodifiableList(BLOCKS);
    }

    public static final BlockDefinition<RouteDistributorBlock> ROUTE_DISTRIBUTOR_BLOCK = block(
        "Route Distributor",
        "route_distributor",
        RouteDistributorBlock::new,
        AEBaseBlockItem::new
    );

    private static <T extends Block> BlockDefinition<T> block(
        String englishName,
        String id,
        Supplier<T> blockSupplier,
        BiFunction<Block, Item.Properties, BlockItem> itemFactory
    ) {
        var deferredBlock = DR.register(id, blockSupplier);
        var deferredItem = DNItems.DR.register(id, () -> itemFactory.apply(deferredBlock.get(), new Item.Properties()));

        var itemDefinition = new ItemDefinition<>(englishName, deferredItem);
        // "borrowing" the AE2 creative tab
        MainCreativeTab.add(itemDefinition);
        var blockDefinition = new BlockDefinition<>(englishName, deferredBlock, itemDefinition);
        BLOCKS.add(blockDefinition);
        return blockDefinition;
    }
}
