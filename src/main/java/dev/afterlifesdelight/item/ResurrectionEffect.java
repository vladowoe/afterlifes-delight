package dev.afterlifesdelight.item;

public enum ResurrectionEffect {
    MEMORIAL(false, false),
    DEATHLESS_DEVOTION(true, false),
    REJOINING(false, true),
    UNBROKEN_REUNION(true, true);

    private final boolean followsThroughDeath;
    private final boolean returnsToDeath;

    ResurrectionEffect(boolean followsThroughDeath, boolean returnsToDeath) {
        this.followsThroughDeath = followsThroughDeath;
        this.returnsToDeath = returnsToDeath;
    }

    public boolean followsThroughDeath() {
        return followsThroughDeath;
    }

    public boolean returnsToDeath() {
        return returnsToDeath;
    }

    public byte ingredientFlags() {
        byte flags = 0;
        if (followsThroughDeath) {
            flags |= 1;
        }
        if (returnsToDeath) {
            flags |= 2;
        }
        return flags;
    }
}
