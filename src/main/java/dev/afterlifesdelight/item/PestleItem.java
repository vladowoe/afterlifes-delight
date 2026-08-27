package dev.afterlifesdelight.item;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public final class PestleItem extends Item {
    private final int enchantmentValue;

    public PestleItem(Properties properties, int enchantmentValue) {
        super(properties);
        this.enchantmentValue = enchantmentValue;
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return enchantmentValue;
    }

    public static ItemAttributeModifiers createAttributes(Tier tier) {
        return DiggerItem.createAttributes(tier, 1.0F, -2.8F);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        return true;
    }

    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
    }
}
