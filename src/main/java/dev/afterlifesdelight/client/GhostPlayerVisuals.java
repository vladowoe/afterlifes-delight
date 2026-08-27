package dev.afterlifesdelight.client;

import dev.afterlifesdelight.AfterlifesDelight;
import dev.afterlifesdelight.config.AfterlifesClientConfig;
import dev.afterlifesdelight.ghost.ModAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = AfterlifesDelight.MOD_ID, value = Dist.CLIENT)
public final class GhostPlayerVisuals {
    private static final int BODY_ALPHA = 90;
    private static final int BODY_RED = 190;
    private static final int BODY_GREEN = 226;
    private static final int BODY_BLUE = 255;
    private static final int GLOW_ALPHA = 38;
    private static final int GLOW_RED = 143;
    private static final int GLOW_GREEN = 222;
    private static final int GLOW_BLUE = 255;
    private static final int MINIMUM_GHOST_LIGHT = 11;

    @SubscribeEvent
    public static void addPlayerLayers(EntityRenderersEvent.AddLayers event) {
        for (var skin : event.getSkins()) {
            PlayerRenderer renderer = event.getSkin(skin);
            if (renderer != null) {
                renderer.addLayer(new GhostMotionTrailLayer(renderer));
                renderer.addLayer(new GhostGlowLayer(renderer));
                renderer.addLayer(new ResurrectionEchoLayer(renderer));
            }
        }
    }

    public static float intensity(Player player) {
        if (!AfterlifesClientConfig.ENABLE_GHOST_ATMOSPHERE.getAsBoolean()) {
            return 0.0F;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (player == minecraft.player) {
            return Mth.clamp(GhostAtmosphereClient.getVisualProgress(), 0.0F, 1.0F);
        }
        return player.getData(ModAttachments.GHOST_VISUAL_STATE.get()) ? 1.0F : 0.0F;
    }

    public static int bodyColor(float intensity) {
        return FastColor.ARGB32.color(
                Math.round(Mth.lerp(intensity, 255.0F, BODY_ALPHA)),
                Math.round(Mth.lerp(intensity, 255.0F, BODY_RED)),
                Math.round(Mth.lerp(intensity, 255.0F, BODY_GREEN)),
                BODY_BLUE
        );
    }

    public static int glowColor(float intensity) {
        return FastColor.ARGB32.color(
                Math.round(GLOW_ALPHA * intensity),
                GLOW_RED,
                GLOW_GREEN,
                GLOW_BLUE
        );
    }

    public static int ghostLight(int packedLight, float intensity) {
        int minimumLight = Math.round(MINIMUM_GHOST_LIGHT * intensity);
        return LightTexture.pack(
                Math.max(LightTexture.block(packedLight), minimumLight),
                Math.max(LightTexture.sky(packedLight), minimumLight)
        );
    }

    private GhostPlayerVisuals() {
    }
}
