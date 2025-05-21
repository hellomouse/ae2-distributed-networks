package net.hellomouse.ae2dn;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@net.neoforged.fml.common.Mod(AE2DistributedNetworksMod.MOD_ID)
public class AE2DistributedNetworksMod {
    public static final String MOD_ID = "ae2dn";
    private static final Logger LOGGER = LogUtils.getLogger();

    public AE2DistributedNetworksMod(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Hello, world!");
    }
}
