package dev.afterlifesdelight.registry;

import dev.afterlifesdelight.AfterlifesDelight;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(
            Registries.CREATIVE_MODE_TAB,
            AfterlifesDelight.MOD_ID
    );

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register(
            "main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.afterlifes_delight"))
                    .icon(() -> ModItems.ECHO_DUST.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.ECHO_DUST.get());
                        output.accept(ModItems.NETHERITE_POWDER.get());
                        output.accept(ModItems.ENDER_SEASONING.get());
                        output.accept(ModItems.ECHO_CRUST.get());

                        output.accept(ModItems.WOODEN_PESTLE.get());
                        output.accept(ModItems.STONE_PESTLE.get());
                        output.accept(ModItems.IRON_PESTLE.get());
                        output.accept(ModItems.GOLDEN_PESTLE.get());
                        output.accept(ModItems.DIAMOND_PESTLE.get());
                        output.accept(ModItems.NETHERITE_PESTLE.get());

                        output.accept(ModItems.MEMORIAL_STEW.get());
                        output.accept(ModItems.DEATHLESS_DEVOTION_STEW.get());
                        output.accept(ModItems.REJOINING_STEW.get());
                        output.accept(ModItems.UNBROKEN_REUNION_STEW.get());

                        output.accept(ModItems.MEMORIAL_APPLE_PIE.get());
                        output.accept(ModItems.MEMORIAL_APPLE_PIE_SLICE.get());
                        output.accept(ModItems.DEATHLESS_DEVOTION_APPLE_PIE.get());
                        output.accept(ModItems.DEATHLESS_DEVOTION_APPLE_PIE_SLICE.get());
                        output.accept(ModItems.REJOINING_APPLE_PIE.get());
                        output.accept(ModItems.REJOINING_APPLE_PIE_SLICE.get());
                        output.accept(ModItems.UNBROKEN_REUNION_APPLE_PIE.get());
                        output.accept(ModItems.UNBROKEN_REUNION_APPLE_PIE_SLICE.get());
                    })
                    .build()
    );

    public static void register(IEventBus modBus) {
        TABS.register(modBus);
    }

    private ModCreativeTabs() {
    }
}
