package dev.afterlifesdelight;

import dev.afterlifesdelight.config.AfterlifesConfig;
import dev.afterlifesdelight.config.AfterlifesClientConfig;
import dev.afterlifesdelight.ghost.ModAttachments;
import dev.afterlifesdelight.network.ModNetwork;
import dev.afterlifesdelight.registry.ModBlocks;
import dev.afterlifesdelight.registry.ModCreativeTabs;
import dev.afterlifesdelight.registry.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(AfterlifesDelight.MOD_ID)
public final class AfterlifesDelight {
    public static final String MOD_ID = "afterlifes_delight";

    public AfterlifesDelight(IEventBus modBus, ModContainer modContainer) {
        ModAttachments.register(modBus);
        ModBlocks.register(modBus);
        ModItems.register(modBus);
        ModCreativeTabs.register(modBus);
        modBus.addListener(ModNetwork::registerPayloads);
        modContainer.registerConfig(ModConfig.Type.COMMON, AfterlifesConfig.SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, AfterlifesClientConfig.SPEC);
    }
}
