package dev.afterlifesdelight.client;

import dev.afterlifesdelight.AfterlifesDelight;
import dev.afterlifesdelight.ghost.ModAttachments;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = AfterlifesDelight.MOD_ID, value = Dist.CLIENT)
public final class GhostMotionTrailClient {
    public static final int[] ECHO_DELAYS = {6, 4, 2};
    private static final int HISTORY_LENGTH = 9;
    private static final int MAX_TRACKED_GHOSTS = 4;
    private static final double MAX_RENDER_DISTANCE_SQUARED = 32.0 * 32.0;
    private static final double TELEPORT_DISTANCE_SQUARED = 16.0 * 16.0;
    private static final Map<Integer, TrailHistory> HISTORIES = new HashMap<>();
    private static ClientLevel trackedLevel;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        AbstractClientPlayer observer = minecraft.player;
        if (level == null || observer == null) {
            clear();
            return;
        }

        if (level != trackedLevel) {
            clear();
            trackedLevel = level;
        }

        List<AbstractClientPlayer> candidates = new ArrayList<>();
        for (AbstractClientPlayer player : level.players()) {
            if (player.getData(ModAttachments.GHOST_VISUAL_STATE.get())
                    && player.distanceToSqr(observer) <= MAX_RENDER_DISTANCE_SQUARED) {
                candidates.add(player);
            }
        }
        candidates.sort(Comparator.comparingDouble(player -> player.distanceToSqr(observer)));

        Set<Integer> activePlayers = new HashSet<>();
        int trackedGhosts = Math.min(MAX_TRACKED_GHOSTS, candidates.size());
        for (int index = 0; index < trackedGhosts; index++) {
            AbstractClientPlayer player = candidates.get(index);
            activePlayers.add(player.getId());
            HISTORIES.computeIfAbsent(player.getId(), ignored -> new TrailHistory()).record(player.position());
        }
        HISTORIES.keySet().removeIf(entityId -> !activePlayers.contains(entityId));
    }

    public static Vec3 sample(AbstractClientPlayer player, int delay, float partialTick) {
        TrailHistory history = HISTORIES.get(player.getId());
        return history == null ? null : history.sample(delay, partialTick);
    }

    public static boolean isWithinRenderDistance(AbstractClientPlayer player) {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.player != null
                && player.distanceToSqr(minecraft.player) <= MAX_RENDER_DISTANCE_SQUARED;
    }

    private static void clear() {
        HISTORIES.clear();
        trackedLevel = null;
    }

    private static final class TrailHistory {
        private final Vec3[] positions = new Vec3[HISTORY_LENGTH];
        private int size;

        private void record(Vec3 position) {
            if (size > 0 && positions[0].distanceToSqr(position) > TELEPORT_DISTANCE_SQUARED) {
                size = 0;
            }

            int copyLength = Math.min(size, HISTORY_LENGTH - 1);
            if (copyLength > 0) {
                System.arraycopy(positions, 0, positions, 1, copyLength);
            }
            positions[0] = position;
            size = Math.min(size + 1, HISTORY_LENGTH);
        }

        private Vec3 sample(int delay, float partialTick) {
            if (delay < 1 || size <= delay + 1) {
                return null;
            }
            float interpolation = Math.max(0.0F, Math.min(1.0F, partialTick));
            return positions[delay + 1].lerp(positions[delay], interpolation);
        }
    }

    private GhostMotionTrailClient() {
    }
}
