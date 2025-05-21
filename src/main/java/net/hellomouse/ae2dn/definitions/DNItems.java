package net.hellomouse.ae2dn.definitions;

import appeng.core.definitions.ItemDefinition;
import net.hellomouse.ae2dn.AE2DistributedNetworks;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DNItems {
    public static final DeferredRegister.Items DR = DeferredRegister.createItems(AE2DistributedNetworks.MOD_ID);
    private static final List<ItemDefinition<?>> ITEMS = new ArrayList<>();

    public static List<ItemDefinition<?>> getItems() {
        return Collections.unmodifiableList(ITEMS);
    }
}
