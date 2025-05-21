package net.hellomouse.ae2dn;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = AE2DistributedNetworksMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue GLOBAL_ENABLE = BUILDER
            .comment("Whether to enable mod functionality")
            .define("enable", true);

    private static final ModConfigSpec.BooleanValue REQUIRE_SUBNET_MANAGER = BUILDER
        .comment("Whether the Subnet Manager is required to allow multiple controllers on a network")
        .define("enable", true);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean globalEnable;
    public static boolean requireSubnetManager;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        globalEnable = GLOBAL_ENABLE.get();
        requireSubnetManager = REQUIRE_SUBNET_MANAGER.get();
    }
}
