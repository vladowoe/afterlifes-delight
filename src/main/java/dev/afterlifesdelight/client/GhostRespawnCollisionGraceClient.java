package dev.afterlifesdelight.client;

import dev.afterlifesdelight.AfterlifesDelight;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = AfterlifesDelight.MOD_ID, value = Dist.CLIENT)
public final class GhostRespawnCollisionGraceClient {
    private static final int DURATION_TICKS = 40;

    private static int ticksRemaining;
    private static UUID appliedPlayerId;
    private static boolean previousNoPhysics;
    private static boolean previousNoGravity;

    public static void start() {
        ticksRemaining = DURATION_TICKS;
        apply(Minecraft.getInstance().player);
    }

    public static void clear() {
        restore(Minecraft.getInstance().player);
        ticksRemaining = 0;
    }

    @SubscribeEvent
    public static void onClientTickPre(ClientTickEvent.Pre event) {
        if (ticksRemaining <= 0) {
            return;
        }
        apply(Minecraft.getInstance().player);
    }

    @SubscribeEvent
    public static void onClientTickPost(ClientTickEvent.Post event) {
        if (ticksRemaining <= 0) {
            return;
        }

        ticksRemaining--;
        if (ticksRemaining == 0) {
            restore(Minecraft.getInstance().player);
        }
    }

    private static void apply(LocalPlayer player) {
        if (player == null) {
            return;
        }

        if (!player.getUUID().equals(appliedPlayerId)) {
            appliedPlayerId = player.getUUID();
            previousNoPhysics = player.noPhysics;
            previousNoGravity = player.isNoGravity();
        }

        player.noPhysics = true;
        player.setNoGravity(true);
        Vec3 movement = player.getDeltaMovement();
        player.setDeltaMovement(movement.x, 0.0D, movement.z);
        player.resetFallDistance();
    }

    private static void restore(LocalPlayer player) {
        if (player != null && player.getUUID().equals(appliedPlayerId)) {
            player.noPhysics = previousNoPhysics;
            player.setNoGravity(previousNoGravity);
        }
        appliedPlayerId = null;
        previousNoPhysics = false;
        previousNoGravity = false;
    }

    private GhostRespawnCollisionGraceClient() {
    }
}
