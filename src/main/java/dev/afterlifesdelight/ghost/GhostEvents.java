package dev.afterlifesdelight.ghost;

import dev.afterlifesdelight.AfterlifesDelight;
import dev.afterlifesdelight.block.AfterlifePieBlock;
import dev.afterlifesdelight.compat.HardcoreLiteCompat;
import dev.afterlifesdelight.compat.SableGrabCleanup;
import dev.afterlifesdelight.registry.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.VanillaGameEvent;
import net.neoforged.neoforge.event.entity.EntityInvulnerabilityCheckEvent;
import net.neoforged.neoforge.event.entity.EntityMountEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.PlayerRespawnPositionEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = AfterlifesDelight.MOD_ID)
public final class GhostEvents {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onGhostDeathAttempt(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (GhostManager.isGhost(player)) {
                event.setCanceled(true);
                player.setHealth(Math.max(1.0F, player.getHealth()));
            } else if (HardcoreLiteCompat.shouldBecomeGhost(player)
                    && HardcoreLiteCompat.preserveLastHeartForGhost(player)) {
                GhostManager.prepareDeathFollowingFoodTransfer(player);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onConfirmedLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || GhostManager.isGhost(player)
                || !HardcoreLiteCompat.shouldBecomeGhost(player)
                || !HardcoreLiteCompat.preserveLastHeartForGhost(player)) {
            return;
        }

        if (event.isCanceled()) {
            GhostManager.restoreDeathFollowingFoodAfterCanceledDeath(player);
            HardcoreLiteCompat.restorePlayableGameMode(player);
        } else {
            GhostManager.recordDeath(player);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            event.getEntity().setData(
                    ModAttachments.GHOST_DATA.get(),
                    event.getOriginal().getData(ModAttachments.GHOST_DATA.get())
            );
            event.getEntity().setData(
                    ModAttachments.GHOST_VISUAL_STATE.get(),
                    false
            );
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerRespawnPosition(PlayerRespawnPositionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player
                && GhostManager.hasGhostRecord(player)
                && !GhostManager.isGhost(player)) {
            event.setDimensionTransition(GhostManager.createGhostRespawnTransition(
                    player,
                    event.getDimensionTransition()
            ));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && GhostManager.hasGhostRecord(player)) {
            HardcoreLiteCompat.restorePlayableGameMode(player);
            GhostManager.restoreAfterRespawn(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.isDeadOrDying()) {
            return;
        }

        if (GhostManager.isGhost(player)) {
            player.getServer().execute(() -> {
                HardcoreLiteCompat.restorePlayableGameMode(player);
                GhostManager.restoreGhostSession(player);
            });
        } else if (GhostManager.hasGhostRecord(player)) {
            player.getServer().execute(() -> {
                HardcoreLiteCompat.restorePlayableGameMode(player);
                GhostManager.restoreAfterRespawn(player);
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            GhostManager.onLogout(player);
            GhostCorpseGrabCooldowns.clear(player);
            SableGrabCleanup.clear(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && GhostManager.isGhost(player)) {
            GhostManager.applyGhostAbilities(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            SableGrabCleanup.tick(player);
            GhostManager.maintainGhost(player);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof Player player && GhostManager.isGhost(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onInvulnerabilityCheck(EntityInvulnerabilityCheckEvent event) {
        if (event.getEntity() instanceof Player player && GhostManager.isGhost(player)) {
            event.setInvulnerable(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onTargetChange(LivingChangeTargetEvent event) {
        if (event.getNewAboutToBeSetTarget() instanceof Player player && GhostManager.isGhost(player)) {
            event.setNewAboutToBeSetTarget(null);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onAttackEntity(AttackEntityEvent event) {
        if (GhostInteractionRules.isGhost(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        denyInteraction(event.getEntity(), event);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!GhostInteractionRules.isGhost(event.getEntity())) {
            return;
        }

        if (event.getItemStack().isEmpty()
                && event.getLevel().getBlockState(event.getPos()).getBlock() instanceof AfterlifePieBlock) {
            event.setUseBlock(TriState.TRUE);
            event.setUseItem(TriState.FALSE);
        } else if (ModItems.isAfterlifeFood(event.getItemStack())) {
            event.setUseBlock(TriState.FALSE);
        } else if (GhostInteractionRules.hasUsableAfterlifeFood(event.getEntity())) {
            event.setUseBlock(TriState.FALSE);
            event.setUseItem(TriState.FALSE);
        } else {
            event.setCancellationResult(InteractionResult.FAIL);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (GhostInteractionRules.isGhost(event.getEntity()) && !ModItems.isAfterlifeFood(event.getItemStack())) {
            event.setCancellationResult(InteractionResult.FAIL);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (GhostInteractionRules.isGhost(event.getEntity())) {
            event.setCancellationResult(InteractionResult.FAIL);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        if (GhostInteractionRules.isGhost(event.getEntity())) {
            event.setCancellationResult(InteractionResult.FAIL);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (GhostManager.isGhost(event.getPlayer())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof Player player
                && GhostManager.isGhost(player)
                && !(event.getPlacedBlock().getBlock() instanceof AfterlifePieBlock)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onFarmlandTrample(BlockEvent.FarmlandTrampleEvent event) {
        if (event.getEntity() instanceof Player player && GhostManager.isGhost(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onItemPickup(ItemEntityPickupEvent.Pre event) {
        if (GhostManager.isGhost(event.getPlayer())) {
            event.setCanPickup(ModItems.isAfterlifeFood(event.getItemEntity().getItem())
                    ? TriState.TRUE
                    : TriState.FALSE);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onContainerOpen(PlayerContainerEvent.Open event) {
        if (event.getEntity() instanceof ServerPlayer player && GhostManager.isGhost(player)) {
            player.getServer().execute(player::closeContainer);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMount(EntityMountEvent event) {
        if (event.isMounting()
                && event.getEntityMounting() instanceof Player player
                && GhostManager.isGhost(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onVanillaGameEvent(VanillaGameEvent event) {
        if (event.getCause() instanceof Player player && GhostManager.isGhost(player)) {
            event.setCanceled(true);
        }
    }

    private static void denyInteraction(Player player, ICancellableEvent event) {
        if (GhostInteractionRules.isGhost(player)) {
            event.setCanceled(true);
        }
    }

    private GhostEvents() {
    }
}
