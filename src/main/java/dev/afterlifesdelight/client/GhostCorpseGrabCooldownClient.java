package dev.afterlifesdelight.client;

public final class GhostCorpseGrabCooldownClient {
    private static int ticksRemaining;

    public static void start(int durationTicks) {
        if (durationTicks <= 0) {
            ticksRemaining = 0;
            return;
        }
        ticksRemaining = Math.max(ticksRemaining, durationTicks);
    }

    public static boolean isActive() {
        return ticksRemaining > 0;
    }

    public static void tick() {
        if (ticksRemaining > 0) {
            ticksRemaining--;
        }
    }

    private GhostCorpseGrabCooldownClient() {
    }
}
