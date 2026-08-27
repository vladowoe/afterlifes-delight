package dev.afterlifesdelight.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

public final class AfterlifePieBlockItem extends BlockItem implements ResurrectionFood {
    private final ResurrectionEffect resurrectionEffect;

    public AfterlifePieBlockItem(
            Block block,
            Properties properties,
            ResurrectionEffect resurrectionEffect
    ) {
        super(block, properties);
        this.resurrectionEffect = resurrectionEffect;
    }

    @Override
    public ResurrectionEffect resurrectionEffect() {
        return resurrectionEffect;
    }
}
