package dev.afterlifesdelight.item;

public interface ResurrectionFood {
    ResurrectionEffect resurrectionEffect();

    default boolean followsThroughDeath() {
        return resurrectionEffect().followsThroughDeath();
    }
}
