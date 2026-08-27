package dev.afterlifesdelight.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public final class GhostAmbientSound extends AbstractTickableSoundInstance {
    public GhostAmbientSound() {
        super(
                SoundEvents.AMBIENT_SOUL_SAND_VALLEY_LOOP.value(),
                SoundSource.AMBIENT,
                SoundInstance.createUnseededRandom()
        );
        looping = true;
        delay = 0;
        relative = true;
        volume = 0.01F;
    }

    @Override
    public void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null || !GhostAtmosphereClient.isTransitionActive()) {
            stop();
            return;
        }

        boolean nativeSoulSandValleyAmbience = minecraft.player.level()
                .getBiome(minecraft.player.blockPosition())
                .value()
                .getAmbientLoop()
                .map(sound -> sound.value() == SoundEvents.AMBIENT_SOUL_SAND_VALLEY_LOOP.value())
                .orElse(false);
        volume = nativeSoulSandValleyAmbience ? 0.0F : GhostAtmosphereClient.getAmbientVolume();
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }
}
