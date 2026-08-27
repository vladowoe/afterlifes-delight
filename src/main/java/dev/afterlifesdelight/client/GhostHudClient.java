package dev.afterlifesdelight.client;

import dev.afterlifesdelight.AfterlifesDelight;
import dev.afterlifesdelight.ghost.ModAttachments;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = AfterlifesDelight.MOD_ID, value = Dist.CLIENT)
public final class GhostHudClient {
    private static final Set<ResourceLocation> HIDDEN_GHOST_LAYERS = Set.of(
            VanillaGuiLayers.PLAYER_HEALTH,
            VanillaGuiLayers.FOOD_LEVEL,
            VanillaGuiLayers.AIR_LEVEL
    );

    @SubscribeEvent
    public static void onRenderGuiLayer(RenderGuiLayerEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null
                && minecraft.player.getData(ModAttachments.GHOST_VISUAL_STATE.get())
                && HIDDEN_GHOST_LAYERS.contains(event.getName())) {
            event.setCanceled(true);
        }
    }

    private GhostHudClient() {
    }
}
