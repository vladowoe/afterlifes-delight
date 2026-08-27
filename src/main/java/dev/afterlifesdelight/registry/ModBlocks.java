package dev.afterlifesdelight.registry;

import dev.afterlifesdelight.AfterlifesDelight;
import dev.afterlifesdelight.block.AfterlifePieBlock;
import dev.afterlifesdelight.item.ResurrectionEffect;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(AfterlifesDelight.MOD_ID);

    public static final DeferredBlock<AfterlifePieBlock> MEMORIAL_APPLE_PIE = BLOCKS.registerBlock(
            "memorial_apple_pie",
            properties -> new AfterlifePieBlock(
                    properties,
                    () -> ModItems.MEMORIAL_APPLE_PIE_SLICE.get(),
                    ResurrectionEffect.MEMORIAL
            ),
            BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE)
    );
    public static final DeferredBlock<AfterlifePieBlock> DEATHLESS_DEVOTION_APPLE_PIE = BLOCKS.registerBlock(
            "deathless_devotion_apple_pie",
            properties -> new AfterlifePieBlock(
                    properties,
                    () -> ModItems.DEATHLESS_DEVOTION_APPLE_PIE_SLICE.get(),
                    ResurrectionEffect.DEATHLESS_DEVOTION
            ),
            BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE)
    );
    public static final DeferredBlock<AfterlifePieBlock> REJOINING_APPLE_PIE = BLOCKS.registerBlock(
            "rejoining_apple_pie",
            properties -> new AfterlifePieBlock(
                    properties,
                    () -> ModItems.REJOINING_APPLE_PIE_SLICE.get(),
                    ResurrectionEffect.REJOINING
            ),
            BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE)
    );
    public static final DeferredBlock<AfterlifePieBlock> UNBROKEN_REUNION_APPLE_PIE = BLOCKS.registerBlock(
            "unbroken_reunion_apple_pie",
            properties -> new AfterlifePieBlock(
                    properties,
                    () -> ModItems.UNBROKEN_REUNION_APPLE_PIE_SLICE.get(),
                    ResurrectionEffect.UNBROKEN_REUNION
            ),
            BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE)
    );

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
    }

    private ModBlocks() {
    }
}
