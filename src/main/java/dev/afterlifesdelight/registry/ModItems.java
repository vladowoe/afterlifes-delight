package dev.afterlifesdelight.registry;

import dev.afterlifesdelight.AfterlifesDelight;
import dev.afterlifesdelight.item.AfterlifePieBlockItem;
import dev.afterlifesdelight.item.AfterlifeFoodItem;
import dev.afterlifesdelight.item.PestleItem;
import dev.afterlifesdelight.item.ResurrectionEffect;
import dev.afterlifesdelight.item.ResurrectionFood;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.food.FoodProperties;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AfterlifesDelight.MOD_ID);

    private static final FoodProperties BOWL_GHOST_FOOD = new FoodProperties.Builder()
            .nutrition(6)
            .saturationModifier(0.8F)
            .alwaysEdible()
            .usingConvertsTo(Items.BOWL)
            .build();
    private static final FoodProperties PIE_GHOST_FOOD = new FoodProperties.Builder()
            .nutrition(2)
            .saturationModifier(0.3F)
            .alwaysEdible()
            .fast()
            .build();

    public static final DeferredItem<PestleItem> WOODEN_PESTLE = ITEMS.registerItem(
            "wooden_pestle",
            properties -> new PestleItem(properties, 15),
            new Item.Properties()
                    .durability(59)
                    .attributes(PestleItem.createAttributes(Tiers.WOOD))
    );
    public static final DeferredItem<PestleItem> STONE_PESTLE = ITEMS.registerItem(
            "stone_pestle",
            properties -> new PestleItem(properties, 5),
            new Item.Properties()
                    .durability(131)
                    .attributes(PestleItem.createAttributes(Tiers.STONE))
    );
    public static final DeferredItem<PestleItem> IRON_PESTLE = ITEMS.registerItem(
            "iron_pestle",
            properties -> new PestleItem(properties, 14),
            new Item.Properties()
                    .durability(250)
                    .attributes(PestleItem.createAttributes(Tiers.IRON))
    );
    public static final DeferredItem<PestleItem> GOLDEN_PESTLE = ITEMS.registerItem(
            "golden_pestle",
            properties -> new PestleItem(properties, 22),
            new Item.Properties()
                    .durability(32)
                    .attributes(PestleItem.createAttributes(Tiers.GOLD))
    );
    public static final DeferredItem<PestleItem> DIAMOND_PESTLE = ITEMS.registerItem(
            "diamond_pestle",
            properties -> new PestleItem(properties, 10),
            new Item.Properties()
                    .durability(1561)
                    .attributes(PestleItem.createAttributes(Tiers.DIAMOND))
    );
    public static final DeferredItem<PestleItem> NETHERITE_PESTLE = ITEMS.registerItem(
            "netherite_pestle",
            properties -> new PestleItem(properties, 15),
            new Item.Properties()
                    .durability(2031)
                    .fireResistant()
                    .attributes(PestleItem.createAttributes(Tiers.NETHERITE))
    );

    public static final DeferredItem<Item> ECHO_DUST = ITEMS.registerSimpleItem("echo_dust");
    public static final DeferredItem<Item> NETHERITE_POWDER = ITEMS.registerSimpleItem("netherite_powder");
    public static final DeferredItem<Item> ENDER_SEASONING = ITEMS.registerSimpleItem("ender_seasoning");
    public static final DeferredItem<Item> ECHO_CRUST = ITEMS.registerSimpleItem("echo_crust");

    public static final DeferredItem<AfterlifeFoodItem> MEMORIAL_STEW = ITEMS.registerItem(
            "memorial_stew",
            properties -> new AfterlifeFoodItem(
                    properties.stacksTo(1).craftRemainder(Items.BOWL).food(BOWL_GHOST_FOOD),
                    ResurrectionEffect.MEMORIAL
            )
    );
    public static final DeferredItem<AfterlifeFoodItem> DEATHLESS_DEVOTION_STEW = ITEMS.registerItem(
            "deathless_devotion_stew",
            properties -> new AfterlifeFoodItem(
                    properties.stacksTo(1).craftRemainder(Items.BOWL).food(BOWL_GHOST_FOOD),
                    ResurrectionEffect.DEATHLESS_DEVOTION
            )
    );
    public static final DeferredItem<AfterlifeFoodItem> REJOINING_STEW = ITEMS.registerItem(
            "rejoining_stew",
            properties -> new AfterlifeFoodItem(
                    properties.stacksTo(1).craftRemainder(Items.BOWL).food(BOWL_GHOST_FOOD),
                    ResurrectionEffect.REJOINING
            )
    );
    public static final DeferredItem<AfterlifeFoodItem> UNBROKEN_REUNION_STEW = ITEMS.registerItem(
            "unbroken_reunion_stew",
            properties -> new AfterlifeFoodItem(
                    properties.stacksTo(1).craftRemainder(Items.BOWL).food(BOWL_GHOST_FOOD),
                    ResurrectionEffect.UNBROKEN_REUNION
            )
    );

    public static final DeferredItem<AfterlifeFoodItem> MEMORIAL_APPLE_PIE_SLICE = registerPieSlice(
            "memorial_apple_pie_slice",
            ResurrectionEffect.MEMORIAL
    );
    public static final DeferredItem<AfterlifeFoodItem> DEATHLESS_DEVOTION_APPLE_PIE_SLICE = registerPieSlice(
            "deathless_devotion_apple_pie_slice",
            ResurrectionEffect.DEATHLESS_DEVOTION
    );
    public static final DeferredItem<AfterlifeFoodItem> REJOINING_APPLE_PIE_SLICE = registerPieSlice(
            "rejoining_apple_pie_slice",
            ResurrectionEffect.REJOINING
    );
    public static final DeferredItem<AfterlifeFoodItem> UNBROKEN_REUNION_APPLE_PIE_SLICE = registerPieSlice(
            "unbroken_reunion_apple_pie_slice",
            ResurrectionEffect.UNBROKEN_REUNION
    );

    public static final DeferredItem<AfterlifePieBlockItem> MEMORIAL_APPLE_PIE = ITEMS.registerItem(
            "memorial_apple_pie",
            properties -> new AfterlifePieBlockItem(
                    ModBlocks.MEMORIAL_APPLE_PIE.get(),
                    properties,
                    ResurrectionEffect.MEMORIAL
            )
    );
    public static final DeferredItem<AfterlifePieBlockItem> DEATHLESS_DEVOTION_APPLE_PIE = ITEMS.registerItem(
            "deathless_devotion_apple_pie",
            properties -> new AfterlifePieBlockItem(
                    ModBlocks.DEATHLESS_DEVOTION_APPLE_PIE.get(),
                    properties,
                    ResurrectionEffect.DEATHLESS_DEVOTION
            )
    );
    public static final DeferredItem<AfterlifePieBlockItem> REJOINING_APPLE_PIE = ITEMS.registerItem(
            "rejoining_apple_pie",
            properties -> new AfterlifePieBlockItem(
                    ModBlocks.REJOINING_APPLE_PIE.get(),
                    properties,
                    ResurrectionEffect.REJOINING
            )
    );
    public static final DeferredItem<AfterlifePieBlockItem> UNBROKEN_REUNION_APPLE_PIE = ITEMS.registerItem(
            "unbroken_reunion_apple_pie",
            properties -> new AfterlifePieBlockItem(
                    ModBlocks.UNBROKEN_REUNION_APPLE_PIE.get(),
                    properties,
                    ResurrectionEffect.UNBROKEN_REUNION
            )
    );

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }

    public static boolean isAfterlifeFood(ItemStack stack) {
        return stack.getItem() instanceof ResurrectionFood;
    }

    private static DeferredItem<AfterlifeFoodItem> registerPieSlice(
            String name,
            ResurrectionEffect resurrectionEffect
    ) {
        return ITEMS.registerItem(
                name,
                properties -> new AfterlifeFoodItem(properties.food(PIE_GHOST_FOOD), resurrectionEffect)
        );
    }

    private ModItems() {
    }
}
