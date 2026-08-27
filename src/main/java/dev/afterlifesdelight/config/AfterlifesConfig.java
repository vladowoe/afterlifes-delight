package dev.afterlifesdelight.config;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class AfterlifesConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    private static final float DEFAULT_SOUND_VOLUME = 1.0F;
    private static final float DEFAULT_SOUND_PITCH = 1.0F;
    private static final float MAX_SOUND_VOLUME = 4.0F;
    private static final float MIN_SOUND_PITCH = 0.1F;
    private static final float MAX_SOUND_PITCH = 4.0F;

    public static final ModConfigSpec.BooleanValue ALLOW_GHOST_FLIGHT = BUILDER
            .comment("Allow ghosts to fly using vanilla creative-flight controls.")
            .translation("afterlifes_delight.configuration.allowGhostFlight")
            .define("allowGhostFlight", true);

    public static final ModConfigSpec.DoubleValue GHOST_CORPSE_GRIP_DROP_CHANCE_PERCENT = BUILDER
            .comment(
                    "Chance per second that a Sable ragdoll corpse slips from a ghost's grip, in percent.",
                    "Set to 0 to let ghosts hold corpses normally, or 100 to always drop them after one second."
            )
            .translation("afterlifes_delight.configuration.ghostCorpseGripDropChancePercent")
            .defineInRange("ghostCorpseGripDropChancePercent", 35.0D, 0.0D, 100.0D);

    public static final ModConfigSpec.DoubleValue GHOST_CORPSE_GRIP_COOLDOWN_SECONDS = BUILDER
            .comment(
                    "How long a ghost must wait before grabbing a Sable ragdoll corpse again after it slips free.",
                    "Measured in seconds. Set to 0 to disable the cooldown."
            )
            .translation("afterlifes_delight.configuration.ghostCorpseGripCooldownSeconds")
            .defineInRange("ghostCorpseGripCooldownSeconds", 3.0D, 0.0D, 60.0D);

    public static final ModConfigSpec.ConfigValue<String> RESURRECTION_SOUND = BUILDER
            .comment(
                    "Sound event played when a ghost resurrects.",
                    "Format: sound_id volume pitch. Example: minecraft:entity.warden.sonic_boom 1.0 1.0.",
                    "Legacy sound_id-only values remain valid. Use none to disable the sound."
            )
            .translation("afterlifes_delight.configuration.resurrectionSound")
            .define(
                    "resurrectionSound",
                    "minecraft:entity.warden.sonic_boom 1.0 1.0",
                    AfterlifesConfig::isValidSoundSetting
            );

    public static final ModConfigSpec.ConfigValue<String> DEATH_SOUND = BUILDER
            .comment(
                    "Sound event played when a living player enters Ghost State.",
                    "Format: sound_id volume pitch. Example: minecraft:entity.warden.sonic_charge 1.0 1.0.",
                    "Legacy sound_id-only values remain valid. Use none to disable the sound."
            )
            .translation("afterlifes_delight.configuration.deathSound")
            .define(
                    "deathSound",
                    "minecraft:entity.warden.sonic_charge 1.0 1.0",
                    AfterlifesConfig::isValidSoundSetting
            );

    private static boolean isValidSoundSetting(Object value) {
        if (!(value instanceof String setting)) {
            return false;
        }

        String trimmed = setting.trim();
        return trimmed.equalsIgnoreCase("none") || parseSoundSetting(trimmed) != null;
    }

    public static SoundSetting parseSoundSetting(String setting) {
        String[] parts = setting.trim().split("\\s+");
        if (parts.length != 1 && parts.length != 3) {
            return null;
        }

        ResourceLocation soundId = ResourceLocation.tryParse(parts[0]);
        if (soundId == null) {
            return null;
        }

        float volume = DEFAULT_SOUND_VOLUME;
        float pitch = DEFAULT_SOUND_PITCH;
        if (parts.length == 3) {
            try {
                volume = Float.parseFloat(parts[1]);
                pitch = Float.parseFloat(parts[2]);
            } catch (NumberFormatException exception) {
                return null;
            }
        }

        if (!Float.isFinite(volume)
                || !Float.isFinite(pitch)
                || volume < 0.0F
                || volume > MAX_SOUND_VOLUME
                || pitch < MIN_SOUND_PITCH
                || pitch > MAX_SOUND_PITCH) {
            return null;
        }
        return new SoundSetting(soundId, volume, pitch);
    }

    public record SoundSetting(ResourceLocation soundId, float volume, float pitch) {
    }

    public static final ModConfigSpec SPEC = BUILDER.build();

    private AfterlifesConfig() {
    }
}
