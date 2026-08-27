package dev.afterlifesdelight.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class AfterlifesClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLE_GHOST_ATMOSPHERE = BUILDER
            .comment("Enable the client-side ghost atmosphere.")
            .translation("afterlifes_delight.configuration.enableGhostAtmosphere")
            .define("enableGhostAtmosphere", true);

    public static final ModConfigSpec.DoubleValue GHOST_ATMOSPHERE_INTENSITY = BUILDER
            .comment("Visual intensity of ghost particles, fog, and the cold filter.")
            .translation("afterlifes_delight.configuration.ghostAtmosphereIntensity")
            .defineInRange("ghostAtmosphereIntensity", 1.0, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue GHOST_AMBIENT_VOLUME = BUILDER
            .comment("Volume multiplier for the Soul Sand Valley ghost ambience.")
            .translation("afterlifes_delight.configuration.ghostAmbientVolume")
            .defineInRange("ghostAmbientVolume", 0.35, 0.0, 1.0);

    public static final ModConfigSpec.BooleanValue ENABLE_GHOST_TRANSITION_CAMERA = BUILDER
            .comment("Enable camera sway when entering or leaving Ghost State.")
            .translation("afterlifes_delight.configuration.enableGhostTransitionCamera")
            .define("enableGhostTransitionCamera", true);

    public static final ModConfigSpec.DoubleValue GHOST_TRANSITION_CAMERA_INTENSITY = BUILDER
            .comment("Intensity of the Ghost State camera sway and FOV distortion.")
            .translation("afterlifes_delight.configuration.ghostTransitionCameraIntensity")
            .defineInRange("ghostTransitionCameraIntensity", 1.0, 0.0, 2.0);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private AfterlifesClientConfig() {
    }
}
