package dev.afterlifesdelight.item;

import dev.afterlifesdelight.ghost.GhostManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class AfterlifeFoodItem extends Item implements ResurrectionFood {
    private final ResurrectionEffect resurrectionEffect;

    public AfterlifeFoodItem(Properties properties, ResurrectionEffect resurrectionEffect) {
        super(properties);
        this.resurrectionEffect = resurrectionEffect;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity consumer) {
        boolean resurrect = consumer instanceof ServerPlayer player && GhostManager.isGhost(player);
        ItemStack result = super.finishUsingItem(stack, level, consumer);

        if (resurrect && consumer instanceof ServerPlayer player) {
            GhostManager.resurrect(player, resurrectionEffect);
        }

        return result;
    }

    @Override
    public ResurrectionEffect resurrectionEffect() {
        return resurrectionEffect;
    }
}
