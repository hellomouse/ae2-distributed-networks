package net.hellomouse.ae2dn;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = AE2DistributedNetworks.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue GLOBAL_ENABLE = BUILDER
            .comment("Whether to enable mod functionality")
            .define("enable", true);

    private static final ModConfigSpec.BooleanValue REQUIRE_ROUTE_DISTRIBUTOR = BUILDER
        .comment("Whether the route distributor is required to allow multiple controllers on a network")
        .define("requireRouteDistributor", true);

    private static final ModConfigSpec.ConfigValue<Integer> ROUTE_CONTROLLER_PASSIVE_ENERGY_COST = BUILDER
        .comment("Passive energy consumption of the route distributor block")
        .define("routeDistributorPassiveEnergyCost", 25);
    private static final ModConfigSpec.ConfigValue<Integer> ROUTE_DISTRIBUTOR_PER_CONTROLLER_ENERGY_COST = BUILDER
        .comment("How much energy the route distributor consumes per controller structure in the network beyond the first")
        .define("routeDistributorPerControllerEnergyCost", 100);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean globalEnable;
    public static boolean requireRouteDistributor;
    public static int rdPerControllerEnergyCost;
    public static int rdPassiveEnergyCost;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        globalEnable = GLOBAL_ENABLE.get();
        requireRouteDistributor = REQUIRE_ROUTE_DISTRIBUTOR.get();
        rdPerControllerEnergyCost = ROUTE_DISTRIBUTOR_PER_CONTROLLER_ENERGY_COST.get();
        rdPassiveEnergyCost = ROUTE_CONTROLLER_PASSIVE_ENERGY_COST.get();
    }
}
