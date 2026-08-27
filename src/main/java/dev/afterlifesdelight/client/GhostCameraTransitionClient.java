package dev.afterlifesdelight.client;

import dev.afterlifesdelight.AfterlifesDelight;
import dev.afterlifesdelight.config.AfterlifesClientConfig;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

@EventBusSubscriber(modid = AfterlifesDelight.MOD_ID, value = Dist.CLIENT)
public final class GhostCameraTransitionClient {
    private static final int ENTER_TICKS = 60;
    private static final int LEAVE_TICKS = 40;

    private static boolean active;
    private static boolean entering;
    private static int age;
    private static int duration;

    public static void start(boolean enteringGhostState) {
        if (!AfterlifesClientConfig.ENABLE_GHOST_TRANSITION_CAMERA.getAsBoolean()) {
            clear();
            return;
        }

        active = true;
        entering = enteringGhostState;
        age = 0;
        duration = entering ? ENTER_TICKS : LEAVE_TICKS;
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (!active) {
            return;
        }

        age++;
        if (age > duration) {
            clear();
        }
    }

    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        if (!active) {
            return;
        }

        float progress = progress(event.getPartialTick());
        float envelope = transitionEnvelope(progress);
        float intensity = AfterlifesClientConfig.GHOST_TRANSITION_CAMERA_INTENSITY.get().floatValue();
        float direction = entering ? 1.0F : -1.0F;
        float phase = progress * Mth.TWO_PI;

        float roll = direction * envelope * intensity
                * (2.35F * Mth.sin(phase * 1.65F) + 0.55F * Mth.sin(phase * 3.8F + 0.7F));
        float yaw = envelope * intensity * 0.72F * Mth.sin(phase * 1.2F + 1.1F);
        float pitch = direction * envelope * intensity * 0.58F * Mth.sin(phase * 1.45F - 0.45F);

        event.setRoll(event.getRoll() + roll);
        event.setYaw(event.getYaw() + yaw);
        event.setPitch(event.getPitch() + pitch);
    }

    @SubscribeEvent
    public static void onComputeFov(ViewportEvent.ComputeFov event) {
        if (!active) {
            return;
        }

        float progress = progress(event.getPartialTick());
        float envelope = transitionEnvelope(progress);
        float intensity = AfterlifesClientConfig.GHOST_TRANSITION_CAMERA_INTENSITY.get().floatValue();
        float direction = entering ? 1.0F : -1.0F;
        double breathing = Math.sin(progress * Math.PI * 5.0) * 0.0045;
        double shift = direction * envelope * 0.018 + envelope * breathing;
        event.setFOV(event.getFOV() * (1.0 + shift * intensity));
    }

    private static float progress(double partialTick) {
        return Mth.clamp((age + (float) partialTick) / (float) duration, 0.0F, 1.0F);
    }

    private static float transitionEnvelope(float progress) {
        return Mth.sin(progress * Mth.PI);
    }

    private static void clear() {
        active = false;
        age = 0;
        duration = 0;
    }

    private GhostCameraTransitionClient() {
    }
}
