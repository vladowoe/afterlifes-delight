package dev.afterlifesdelight.client;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.logging.LogUtils;
import dev.afterlifesdelight.AfterlifesDelight;
import dev.afterlifesdelight.config.AfterlifesClientConfig;
import dev.afterlifesdelight.ghost.ModAttachments;
import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.FogType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import org.slf4j.Logger;

@EventBusSubscriber(modid = AfterlifesDelight.MOD_ID, value = Dist.CLIENT)
public final class GhostAtmosphereClient {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation GHOST_FILTER_EFFECT = ResourceLocation.fromNamespaceAndPath(
            AfterlifesDelight.MOD_ID,
            "shaders/post/ghost_filter.json"
    );
    private static final float FADE_IN_STEP = 1.0F / 60.0F;
    private static final float FADE_OUT_STEP = 1.0F / 40.0F;
    private static final float GHOST_FOG_START = 18.0F;
    private static final float GHOST_FOG_END = 152.0F;
    private static final int FILTER_RED = 57;
    private static final int FILTER_GREEN = 82;
    private static final int FILTER_BLUE = 104;

    private static float previousProgress;
    private static float progress;
    private static GhostAmbientSound ambientSound;
    private static PostChain ghostFilterEffect;
    private static int filterWidth = -1;
    private static int filterHeight = -1;
    private static boolean filterLoadFailed;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        previousProgress = progress;

        boolean atmosphereEnabled = AfterlifesClientConfig.ENABLE_GHOST_ATMOSPHERE.getAsBoolean();
        boolean ghost = minecraft.player != null
                && minecraft.player.getData(ModAttachments.GHOST_VISUAL_STATE.get());
        float target = atmosphereEnabled && ghost ? 1.0F : 0.0F;
        float step = target > progress ? FADE_IN_STEP : FADE_OUT_STEP;
        progress = Mth.approach(progress, target, step);

        if (minecraft.level == null || minecraft.player == null) {
            progress = 0.0F;
            previousProgress = 0.0F;
        }

        if (ambientSound != null && ambientSound.isStopped()) {
            ambientSound = null;
        }
        if (progress > 0.0F && ambientSound == null) {
            ambientSound = new GhostAmbientSound();
            minecraft.getSoundManager().play(ambientSound);
        }
    }

    @SubscribeEvent
    public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
        if (event.getCamera().getFluidInCamera() != FogType.NONE) {
            return;
        }

        float intensity = renderIntensity((float) event.getPartialTick());
        if (intensity <= 0.001F) {
            return;
        }

        float red = event.getRed();
        float green = event.getGreen();
        float blue = event.getBlue();
        float luminance = red * 0.2126F + green * 0.7152F + blue * 0.0722F;
        float coldRed = luminance * 0.70F;
        float coldGreen = luminance * 0.88F;
        float coldBlue = Math.min(1.0F, luminance * 1.10F + 0.025F);
        float blend = 0.52F * intensity;

        event.setRed(Mth.lerp(blend, red, coldRed));
        event.setGreen(Mth.lerp(blend, green, coldGreen));
        event.setBlue(Mth.lerp(blend, blue, coldBlue));
    }

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        if (event.getType() != FogType.NONE) {
            return;
        }

        float intensity = renderIntensity((float) event.getPartialTick());
        if (intensity <= 0.001F) {
            return;
        }

        float originalNear = event.getNearPlaneDistance();
        float originalFar = event.getFarPlaneDistance();
        float targetFar = Math.min(originalFar, GHOST_FOG_END);
        float targetNear = Math.min(GHOST_FOG_START, targetFar * 0.55F);

        event.setNearPlaneDistance(Mth.lerp(intensity, originalNear, targetNear));
        event.setFarPlaneDistance(Mth.lerp(intensity, originalFar, targetFar));
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null
                || minecraft.player == null
                || minecraft.gameRenderer.getMainCamera().getFluidInCamera() != FogType.NONE) {
            return;
        }

        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        float intensity = renderIntensity(partialTick);
        if (intensity <= 0.001F) {
            return;
        }

        applyGhostFilter(minecraft, partialTick, intensity);

        GuiGraphics graphics = event.getGuiGraphics();
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        int filterAlpha = Math.round(40.0F * intensity);
        graphics.fill(0, 0, width, height, argb(filterAlpha, FILTER_RED, FILTER_GREEN, FILTER_BLUE));

        int minimumDimension = Math.min(width, height);
        int bandThickness = Math.max(2, minimumDimension / 90);
        int bands = 7;
        for (int band = 0; band < bands; band++) {
            int offset = band * bandThickness;
            int alpha = Math.round(46.0F * intensity * (float) (bands - band) / (float) bands);
            int color = argb(alpha, 18, 29, 39);
            graphics.fill(offset, offset, width - offset, offset + bandThickness, color);
            graphics.fill(offset, height - offset - bandThickness, width - offset, height - offset, color);
            graphics.fill(offset, offset + bandThickness, offset + bandThickness, height - offset - bandThickness, color);
            graphics.fill(width - offset - bandThickness, offset + bandThickness, width - offset, height - offset - bandThickness, color);
        }
    }

    public static float getParticleIntensity() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.player.isUnderWater()) {
            return 0.0F;
        }
        return progress * visualIntensity();
    }

    public static float getAmbientVolume() {
        return Mth.clamp(
                progress * AfterlifesClientConfig.GHOST_AMBIENT_VOLUME.get().floatValue() * 2.0F,
                0.0F,
                1.0F
        );
    }

    public static boolean isTransitionActive() {
        return progress > 0.001F;
    }

    public static float getVisualProgress() {
        return progress * visualIntensity();
    }

    private static float renderIntensity(float partialTick) {
        return Mth.lerp(Mth.clamp(partialTick, 0.0F, 1.0F), previousProgress, progress) * visualIntensity();
    }

    private static float visualIntensity() {
        return AfterlifesClientConfig.GHOST_ATMOSPHERE_INTENSITY.get().floatValue();
    }

    private static void applyGhostFilter(Minecraft minecraft, float partialTick, float intensity) {
        RenderTarget mainTarget = minecraft.getMainRenderTarget();
        PostChain effect = getOrCreateGhostFilterEffect(minecraft, mainTarget);
        if (effect == null) {
            return;
        }

        effect.setUniform("Progress", intensity);
        effect.process(partialTick);
        mainTarget.bindWrite(true);
    }

    private static PostChain getOrCreateGhostFilterEffect(Minecraft minecraft, RenderTarget mainTarget) {
        if (filterLoadFailed) {
            return null;
        }

        try {
            if (ghostFilterEffect == null) {
                ghostFilterEffect = new PostChain(
                        minecraft.getTextureManager(),
                        minecraft.getResourceManager(),
                        mainTarget,
                        GHOST_FILTER_EFFECT
                );
                filterWidth = -1;
                filterHeight = -1;
            }

            if (filterWidth != mainTarget.width || filterHeight != mainTarget.height) {
                ghostFilterEffect.resize(mainTarget.width, mainTarget.height);
                filterWidth = mainTarget.width;
                filterHeight = mainTarget.height;
            }
            return ghostFilterEffect;
        } catch (IOException | JsonSyntaxException exception) {
            LOGGER.error("Failed to load the ghost color filter", exception);
            closeGhostFilterEffect();
            filterLoadFailed = true;
            return null;
        }
    }

    private static void closeGhostFilterEffect() {
        if (ghostFilterEffect != null) {
            ghostFilterEffect.close();
            ghostFilterEffect = null;
        }
        filterWidth = -1;
        filterHeight = -1;
    }

    private static int argb(int alpha, int red, int green, int blue) {
        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    private GhostAtmosphereClient() {
    }

    @EventBusSubscriber(modid = AfterlifesDelight.MOD_ID, value = Dist.CLIENT)
    public static final class ModEvents {
        @SubscribeEvent
        public static void registerReloadListener(RegisterClientReloadListenersEvent event) {
            event.registerReloadListener((ResourceManagerReloadListener) resourceManager -> {
                closeGhostFilterEffect();
                filterLoadFailed = false;
            });
        }

        private ModEvents() {
        }
    }
}
