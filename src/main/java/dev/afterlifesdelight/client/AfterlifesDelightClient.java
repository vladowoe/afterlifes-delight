package dev.afterlifesdelight.client;

import dev.afterlifesdelight.AfterlifesDelight;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = AfterlifesDelight.MOD_ID, dist = Dist.CLIENT)
public final class AfterlifesDelightClient {
    public AfterlifesDelightClient(ModContainer modContainer) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, AfterlifesDelightClient::createConfigScreen);
    }

    private static Screen createConfigScreen(ModContainer modContainer, Screen parent) {
        return new ConfigurationScreen(modContainer, parent);
    }
}
