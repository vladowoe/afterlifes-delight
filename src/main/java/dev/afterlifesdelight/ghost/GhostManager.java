package dev.afterlifesdelight.ghost;

import dev.afterlifesdelight.AfterlifesDelight;
import dev.afterlifesdelight.config.AfterlifesConfig;
import dev.afterlifesdelight.item.ResurrectionEffect;
import dev.afterlifesdelight.item.ResurrectionFood;
import dev.afterlifesdelight.network.GhostCameraTransitionPayload;
import dev.afterlifesdelight.network.ResurrectionEffectPayload;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.network.PacketDistributor;

public final class GhostManager {
    private static final int RESPAWN_GUARD_TICKS = 8;
    private static final int RAGDOLL_COLLISION_GRACE_TICKS = 40;
    private static final int RESURRECTION_FOOD_DELIVERY_DELAY_TICKS = 20;
    private static final double RESPAWN_GUARD_MAX_DISTANCE_SQUARED = 64.0;
    private static final Map<UUID, RespawnGuard> RESPAWN_GUARDS = new HashMap<>();
    private static final Map<UUID, Integer> RAGDOLL_COLLISION_GRACE_DELAYS = new HashMap<>();
    private static final Map<UUID, Integer> RESURRECTION_FOOD_DELIVERY_DELAYS = new HashMap<>();

    private static final ResourceLocation GHOST_FLIGHT_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(
            AfterlifesDelight.MOD_ID,
            "ghost_flight"
    );

    public static boolean isGhost(Player player) {
        return hasGhostRecord(player)
                && player.getData(ModAttachments.GHOST_VISUAL_STATE.get());
    }

    public static boolean hasGhostRecord(Player player) {
        return player.getData(ModAttachments.GHOST_DATA.get()).ghost();
    }

    public static boolean hasRagdollCollisionGrace(Player player) {
        return isGhost(player)
                && player.getData(ModAttachments.GHOST_RAGDOLL_COLLISION_GRACE.get());
    }

    public static GhostData getData(Player player) {
        return player.getData(ModAttachments.GHOST_DATA.get());
    }

    public static void prepareDeathFollowingFoodTransfer(ServerPlayer player) {
        GhostData data = getData(player);
        if (data.ghost()) {
            return;
        }

        List<String> transferredItemIds = new ArrayList<>(data.pendingResurrectionFoods());
        if (extractAllDeathFollowingFoods(player, transferredItemIds)) {
            player.setData(
                    ModAttachments.GHOST_DATA.get(),
                    data.withPendingResurrectionFoods(transferredItemIds)
            );
        }
    }

    public static void restoreDeathFollowingFoodAfterCanceledDeath(ServerPlayer player) {
        RESURRECTION_FOOD_DELIVERY_DELAYS.remove(player.getUUID());
        deliverPendingResurrectionFoods(player);
    }

    public static void recordDeath(ServerPlayer player) {
        clearRagdollCollisionGrace(player);
        GhostData current = getData(player);
        GhostData ghostData = current.enterGhost(
                player.level().dimension(),
                player.position(),
                player.mayFly(),
                player.getAbilities().flying
        );
        player.setData(ModAttachments.GHOST_DATA.get(), ghostData);
        player.setData(ModAttachments.GHOST_VISUAL_STATE.get(), false);
    }

    private static void disableGhost(ServerPlayer player) {
        RESURRECTION_FOOD_DELIVERY_DELAYS.remove(player.getUUID());
        deliverPendingResurrectionFoods(player);
        GhostData data = getData(player);
        if (!data.ghost()) {
            return;
        }

        GhostCorpseGrabCooldowns.clearAndSync(player);
        player.setData(ModAttachments.GHOST_DATA.get(), data.leaveGhost());
        player.setData(ModAttachments.GHOST_VISUAL_STATE.get(), false);
        RESPAWN_GUARDS.remove(player.getUUID());
        clearRagdollCollisionGrace(player);
        setGhostFlightModifier(player, false);
        player.getAbilities().flying = data.mayFlyBeforeGhost()
                && player.mayFly()
                && data.flyingBeforeGhost();
        player.onUpdateAbilities();
        player.clearFire();
        player.setAirSupply(player.getMaxAirSupply());
    }

    public static void restoreAfterRespawn(ServerPlayer player) {
        GhostData data = getData(player);
        if (!data.ghost()) {
            return;
        }

        // The respawn position was already replaced before the new player was
        // created, so only the ghost state and its short collision grace need to
        // be activated here. No second teleport packet is sent to the client.
        player.setData(ModAttachments.GHOST_VISUAL_STATE.get(), true);
        startRagdollCollisionGrace(player);
        RESPAWN_GUARDS.put(
                player.getUUID(),
                new RespawnGuard(player.serverLevel().dimension(), player.position(), RESPAWN_GUARD_TICKS)
        );
        applyGhostAbilities(player);
        clearMobTargets(player);
        PacketDistributor.sendToPlayer(player, new GhostCameraTransitionPayload(true));
        playConfiguredSound(player, AfterlifesConfig.DEATH_SOUND.get());
        RESURRECTION_FOOD_DELIVERY_DELAYS.put(
                player.getUUID(),
                RESURRECTION_FOOD_DELIVERY_DELAY_TICKS
        );
    }

    public static DimensionTransition createGhostRespawnTransition(
            ServerPlayer player,
            DimensionTransition original
    ) {
        GhostData data = getData(player);
        GhostRespawnTarget target = resolveGhostRespawnTarget(player, data);
        player.setData(
                ModAttachments.GHOST_DATA.get(),
                data.withLastSafePosition(target.level().dimension(), target.position())
        );

        return new DimensionTransition(
                target.level(),
                target.position(),
                Vec3.ZERO,
                original.yRot(),
                original.xRot(),
                original.postDimensionTransition()
        );
    }

    public static void restoreGhostSession(ServerPlayer player) {
        if (!isGhost(player)) {
            return;
        }
        clearRagdollCollisionGrace(player);
        applyGhostAbilities(player);
        player.setData(ModAttachments.GHOST_VISUAL_STATE.get(), true);
        RESURRECTION_FOOD_DELIVERY_DELAYS.remove(player.getUUID());
        deliverPendingResurrectionFoods(player);
    }

    public static void resurrectHere(
            ServerPlayer player,
            ResurrectionEffect resurrectionEffect
    ) {
        if (!isGhost(player)) {
            return;
        }

        completeResurrection(player, resurrectionEffect);
    }

    public static void resurrectAtLastDeath(
            ServerPlayer player,
            ResurrectionEffect resurrectionEffect
    ) {
        GhostData data = getData(player);
        if (!data.ghost()) {
            return;
        }

        teleportForRejoining(player, data);
        playChorusTeleportEffect(player);
        completeResurrection(player, resurrectionEffect);
    }

    public static void resurrect(ServerPlayer player, ResurrectionEffect resurrectionEffect) {
        if (resurrectionEffect.returnsToDeath()) {
            resurrectAtLastDeath(player, resurrectionEffect);
        } else {
            resurrectHere(player, resurrectionEffect);
        }
    }

    public static void applyGhostAbilities(ServerPlayer player) {
        if (!isGhost(player)) {
            return;
        }

        boolean allowFlight = AfterlifesConfig.ALLOW_GHOST_FLIGHT.getAsBoolean();
        boolean changed = setGhostFlightModifier(player, allowFlight);

        if (!player.mayFly() && player.getAbilities().flying) {
            player.getAbilities().flying = false;
            changed = true;
        }

        if (changed) {
            player.onUpdateAbilities();
        }
    }

    public static void maintainGhost(ServerPlayer player) {
        if (!isGhost(player)) {
            if (!hasGhostRecord(player)) {
                updateLastSafePosition(player);
            }
            return;
        }

        applyGhostAbilities(player);
        deliverPendingResurrectionFoodsWhenReady(player);
        tickRagdollCollisionGrace(player);
        enforceRespawnGuard(player);
        if (player.containerMenu != player.inventoryMenu) {
            player.closeContainer();
        }
        player.clearFire();
        player.setAirSupply(player.getMaxAirSupply());
        player.resetFallDistance();

        if (player.tickCount % 20 == 0) {
            clearMobTargets(player);
        }

        ServerLevel level = player.serverLevel();
        if (player.getY() < level.getMinBuildHeight() - 32) {
            teleportToSafePosition(player, getData(player));
            return;
        }

        updateLastSafePosition(player);
    }

    public static void onLogout(ServerPlayer player) {
        RESPAWN_GUARDS.remove(player.getUUID());
        clearRagdollCollisionGrace(player);
        RESURRECTION_FOOD_DELIVERY_DELAYS.remove(player.getUUID());
    }

    private static void startRagdollCollisionGrace(ServerPlayer player) {
        player.setData(ModAttachments.GHOST_RAGDOLL_COLLISION_GRACE.get(), true);
        RAGDOLL_COLLISION_GRACE_DELAYS.put(player.getUUID(), RAGDOLL_COLLISION_GRACE_TICKS);
        applyRagdollCollisionGrace(player);
    }

    private static void tickRagdollCollisionGrace(ServerPlayer player) {
        Integer ticksRemaining = RAGDOLL_COLLISION_GRACE_DELAYS.get(player.getUUID());
        if (ticksRemaining == null) {
            clearRagdollCollisionGrace(player);
            return;
        }

        if (ticksRemaining <= 1 || !isGhost(player)) {
            clearRagdollCollisionGrace(player);
            return;
        }

        applyRagdollCollisionGrace(player);
        RAGDOLL_COLLISION_GRACE_DELAYS.put(player.getUUID(), ticksRemaining - 1);
    }

    private static void applyRagdollCollisionGrace(ServerPlayer player) {
        player.noPhysics = true;
        player.setNoGravity(true);
        Vec3 movement = player.getDeltaMovement();
        player.setDeltaMovement(movement.x, 0.0D, movement.z);
        player.resetFallDistance();
    }

    private static void clearRagdollCollisionGrace(ServerPlayer player) {
        RAGDOLL_COLLISION_GRACE_DELAYS.remove(player.getUUID());
        if (player.getData(ModAttachments.GHOST_RAGDOLL_COLLISION_GRACE.get())) {
            player.setData(ModAttachments.GHOST_RAGDOLL_COLLISION_GRACE.get(), false);
        }
        player.noPhysics = false;
        player.setNoGravity(false);
    }

    private static void updateLastSafePosition(ServerPlayer player) {
        if (player.tickCount % 20 != 0 || !player.onGround() || player.isInLava()) {
            return;
        }

        GhostData data = getData(player);
        player.setData(
                ModAttachments.GHOST_DATA.get(),
                data.withLastSafePosition(player.level().dimension(), player.position())
        );
    }

    private static boolean extractAllDeathFollowingFoods(
            ServerPlayer player,
            List<String> transferredItemIds
    ) {
        boolean extracted = extractDeathFollowingFoods(player.getInventory().items, transferredItemIds);
        extracted |= extractDeathFollowingFoods(player.getInventory().offhand, transferredItemIds);
        if (extracted) {
            player.getInventory().setChanged();
        }
        return extracted;
    }

    private static boolean extractDeathFollowingFoods(
            Iterable<ItemStack> inventory,
            List<String> transferredItemIds
    ) {
        boolean extracted = false;
        for (ItemStack stack : inventory) {
            if (!isDeathFollowingFood(stack)) {
                continue;
            }

            String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            int count = stack.getCount();
            for (int item = 0; item < count; item++) {
                transferredItemIds.add(itemId);
            }
            stack.shrink(count);
            extracted = true;
        }
        return extracted;
    }

    private static boolean isDeathFollowingFood(ItemStack stack) {
        return stack.getItem() instanceof ResurrectionFood food && food.followsThroughDeath();
    }

    private static void deliverPendingResurrectionFoodsWhenReady(ServerPlayer player) {
        Integer ticksRemaining = RESURRECTION_FOOD_DELIVERY_DELAYS.get(player.getUUID());
        if (ticksRemaining == null) {
            return;
        }
        if (ticksRemaining > 1) {
            RESURRECTION_FOOD_DELIVERY_DELAYS.put(player.getUUID(), ticksRemaining - 1);
            return;
        }

        RESURRECTION_FOOD_DELIVERY_DELAYS.remove(player.getUUID());
        deliverPendingResurrectionFoods(player);
    }

    private static void deliverPendingResurrectionFoods(ServerPlayer player) {
        GhostData data = getData(player);
        if (!data.hasPendingResurrectionFoods()) {
            return;
        }

        List<ItemStack> transferredFoods = data.pendingResurrectionFoods().stream()
                .map(GhostManager::createPendingResurrectionFood)
                .filter(stack -> !stack.isEmpty())
                .toList();
        player.setData(ModAttachments.GHOST_DATA.get(), data.clearPendingResurrectionFood());
        if (transferredFoods.isEmpty()) {
            return;
        }

        for (ItemStack transferred : transferredFoods) {
            if (!player.getInventory().add(transferred)) {
                ItemEntity itemEntity = new ItemEntity(
                        player.serverLevel(),
                        player.getX(),
                        player.getY() + 0.5,
                        player.getZ(),
                        transferred
                );
                itemEntity.setNoPickUpDelay();
                player.serverLevel().addFreshEntity(itemEntity);
            }
        }
        player.inventoryMenu.broadcastChanges();
        playChorusTeleportEffect(player);
    }

    private static void playChorusTeleportEffect(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        level.broadcastEntityEvent(player, (byte) 46);
        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.CHORUS_FRUIT_TELEPORT,
                SoundSource.PLAYERS
        );
    }

    private static ItemStack createPendingResurrectionFood(String itemId) {
        ResourceLocation location = ResourceLocation.tryParse(itemId);
        if (location == null) {
            return ItemStack.EMPTY;
        }

        Item item = BuiltInRegistries.ITEM.get(location);
        return item instanceof ResurrectionFood food && food.followsThroughDeath()
                ? new ItemStack(item)
                : ItemStack.EMPTY;
    }

    private static void completeResurrection(
            ServerPlayer player,
            ResurrectionEffect resurrectionEffect
    ) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(
                player,
                new ResurrectionEffectPayload(
                        player.getId(),
                        resurrectionEffect.ingredientFlags(),
                        player.getRandom().nextLong()
                )
        );
        PacketDistributor.sendToPlayer(player, new GhostCameraTransitionPayload(false));
        playConfiguredSound(player, AfterlifesConfig.RESURRECTION_SOUND.get());
        disableGhost(player);
        player.setHealth(player.getMaxHealth());
        player.getFoodData().setFoodLevel(20);
        player.getFoodData().setSaturation(5.0F);
        player.getFoodData().setExhaustion(0.0F);
        player.clearFire();
        player.setAirSupply(player.getMaxAirSupply());
        player.resetFallDistance();
    }

    private static void playConfiguredSound(ServerPlayer player, String configuredSound) {
        if (configuredSound.equalsIgnoreCase("none")) {
            return;
        }

        AfterlifesConfig.SoundSetting soundSetting = AfterlifesConfig.parseSoundSetting(configuredSound);
        if (soundSetting == null) {
            return;
        }
        BuiltInRegistries.SOUND_EVENT.getOptional(soundSetting.soundId()).ifPresent(sound -> player.serverLevel().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                sound,
                SoundSource.PLAYERS,
                soundSetting.volume(),
                soundSetting.pitch()
        ));
    }

    private static void clearMobTargets(ServerPlayer player) {
        player.serverLevel()
                .getEntitiesOfClass(
                        Mob.class,
                        player.getBoundingBox().inflate(128.0),
                        mob -> mob.getTarget() == player
                )
                .forEach(mob -> mob.setTarget(null));
    }

    private static void enforceRespawnGuard(ServerPlayer player) {
        RespawnGuard guard = RESPAWN_GUARDS.get(player.getUUID());
        if (guard == null) {
            return;
        }

        if (!isGhost(player) || guard.ticksRemaining() <= 0) {
            RESPAWN_GUARDS.remove(player.getUUID());
            return;
        }

        boolean wrongDimension = !player.serverLevel().dimension().equals(guard.dimension());
        boolean movedByExternalRespawn = player.position().distanceToSqr(guard.position())
                > RESPAWN_GUARD_MAX_DISTANCE_SQUARED;

        if (wrongDimension || movedByExternalRespawn) {
            ServerLevel targetLevel = player.getServer().getLevel(guard.dimension());
            if (targetLevel != null) {
                teleport(player, targetLevel, guard.position());
            }
        }

        if (guard.ticksRemaining() == 1) {
            RESPAWN_GUARDS.remove(player.getUUID());
        } else {
            RESPAWN_GUARDS.put(player.getUUID(), guard.nextTick());
        }
    }

    private static boolean setGhostFlightModifier(ServerPlayer player, boolean enabled) {
        AttributeInstance creativeFlight = player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT);
        if (creativeFlight == null) {
            return false;
        }

        if (enabled) {
            if (creativeFlight.hasModifier(GHOST_FLIGHT_MODIFIER_ID)) {
                return false;
            }
            creativeFlight.addTransientModifier(new AttributeModifier(
                    GHOST_FLIGHT_MODIFIER_ID,
                    1.0,
                    AttributeModifier.Operation.ADD_VALUE
            ));
            return true;
        }

        return creativeFlight.removeModifier(GHOST_FLIGHT_MODIFIER_ID);
    }

    private static GhostRespawnTarget resolveGhostRespawnTarget(ServerPlayer player, GhostData data) {
        if (data.hasLastDeathPosition()) {
            ServerLevel deathLevel = getLevel(player, data.lastDeathDimension());
            if (deathLevel != null && data.lastDeathY() >= deathLevel.getMinBuildHeight() - 16) {
                return new GhostRespawnTarget(
                        deathLevel,
                        findNonCollidingPosition(deathLevel, data.lastDeathPosition())
                );
            }
        }

        ServerLevel safeLevel = data.hasLastSafePosition()
                ? getLevel(player, data.lastSafeDimension())
                : player.serverLevel();
        if (safeLevel == null) {
            safeLevel = player.serverLevel();
        }

        Vec3 requested = data.hasLastSafePosition()
                ? data.lastSafePosition()
                : Vec3.atBottomCenterOf(safeLevel.getSharedSpawnPos());
        return new GhostRespawnTarget(safeLevel, findNonCollidingPosition(safeLevel, requested));
    }

    private static void teleportForRejoining(ServerPlayer player, GhostData data) {
        ServerLevel deathLevel = data.hasLastDeathPosition()
                ? getLevel(player, data.lastDeathDimension())
                : null;
        if (deathLevel != null) {
            Optional<Vec3> deathTarget = findSafeResurrectionPosition(deathLevel, data.lastDeathPosition());
            if (deathTarget.isPresent()) {
                teleport(player, deathLevel, deathTarget.get());
                return;
            }
        }

        ServerLevel safeLevel = data.hasLastSafePosition()
                ? getLevel(player, data.lastSafeDimension())
                : null;
        if (safeLevel != null) {
            Optional<Vec3> safeTarget = findSafeResurrectionPosition(safeLevel, data.lastSafePosition());
            if (safeTarget.isPresent()) {
                teleport(player, safeLevel, safeTarget.get());
                return;
            }
        }

        ServerLevel fallbackLevel = deathLevel != null ? deathLevel : player.serverLevel();
        Vec3 spawn = Vec3.atBottomCenterOf(fallbackLevel.getSharedSpawnPos());
        Vec3 target = findSafeResurrectionPosition(fallbackLevel, spawn).orElse(spawn);
        teleport(player, fallbackLevel, target);
    }

    private static void teleportToSafePosition(ServerPlayer player, GhostData data) {
        ServerLevel targetLevel = data.hasLastSafePosition()
                ? getLevel(player, data.lastSafeDimension())
                : player.serverLevel();
        if (targetLevel == null) {
            targetLevel = player.serverLevel();
        }

        Vec3 requested = data.hasLastSafePosition()
                ? data.lastSafePosition()
                : Vec3.atBottomCenterOf(targetLevel.getSharedSpawnPos());
        Vec3 target = findNonCollidingPosition(targetLevel, requested);
        teleport(player, targetLevel, target);
    }

    private static ServerLevel getLevel(ServerPlayer player, String dimensionId) {
        ResourceLocation location = ResourceLocation.tryParse(dimensionId);
        if (location == null || player.getServer() == null) {
            return null;
        }
        ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, location);
        return player.getServer().getLevel(key);
    }

    private static void teleport(ServerPlayer player, ServerLevel level, Vec3 target) {
        player.teleportTo(level, target.x, target.y, target.z, Set.of(), player.getYRot(), player.getXRot());
        player.setDeltaMovement(Vec3.ZERO);
        player.resetFallDistance();
    }

    private static Vec3 findNonCollidingPosition(ServerLevel level, Vec3 requested) {
        BlockPos origin = BlockPos.containing(requested);
        if (isNonColliding(level, origin)) {
            return requested;
        }

        for (int radius = 1; radius <= 4; radius++) {
            for (int yOffset = -2; yOffset <= 4; yOffset++) {
                for (int xOffset = -radius; xOffset <= radius; xOffset++) {
                    for (int zOffset = -radius; zOffset <= radius; zOffset++) {
                        if (Math.abs(xOffset) != radius && Math.abs(zOffset) != radius) {
                            continue;
                        }
                        BlockPos candidate = origin.offset(xOffset, yOffset, zOffset);
                        if (isNonColliding(level, candidate)) {
                            return Vec3.atBottomCenterOf(candidate);
                        }
                    }
                }
            }
        }

        return Vec3.atBottomCenterOf(origin.above());
    }

    private static Optional<Vec3> findSafeResurrectionPosition(ServerLevel level, Vec3 requested) {
        BlockPos origin = BlockPos.containing(requested);
        int[] verticalOffsets = {0, 1, -1, 2, -2, 3, -3, 4, -4, 5, 6};

        for (int radius = 0; radius <= 6; radius++) {
            for (int yOffset : verticalOffsets) {
                for (int xOffset = -radius; xOffset <= radius; xOffset++) {
                    for (int zOffset = -radius; zOffset <= radius; zOffset++) {
                        if (radius > 0
                                && Math.abs(xOffset) != radius
                                && Math.abs(zOffset) != radius) {
                            continue;
                        }

                        BlockPos candidate = origin.offset(xOffset, yOffset, zOffset);
                        if (isSafeForResurrection(level, candidate)) {
                            return Optional.of(Vec3.atBottomCenterOf(candidate));
                        }
                    }
                }
            }
        }

        return Optional.empty();
    }

    private static boolean isSafeForResurrection(ServerLevel level, BlockPos position) {
        if (position.getY() <= level.getMinBuildHeight()
                || position.getY() + 1 >= level.getMaxBuildHeight()
                || !level.getWorldBorder().isWithinBounds(position)) {
            return false;
        }

        BlockState feet = level.getBlockState(position);
        BlockState head = level.getBlockState(position.above());
        BlockState floor = level.getBlockState(position.below());

        return feet.getCollisionShape(level, position).isEmpty()
                && head.getCollisionShape(level, position.above()).isEmpty()
                && !floor.getCollisionShape(level, position.below()).isEmpty()
                && feet.getFluidState().isEmpty()
                && head.getFluidState().isEmpty()
                && !isHazardous(feet)
                && !isHazardous(head)
                && !isHazardous(floor);
    }

    private static boolean isHazardous(BlockState state) {
        return state.is(Blocks.FIRE)
                || state.is(Blocks.SOUL_FIRE)
                || state.is(Blocks.POWDER_SNOW)
                || state.is(Blocks.CACTUS)
                || state.is(Blocks.MAGMA_BLOCK)
                || state.is(Blocks.CAMPFIRE)
                || state.is(Blocks.SOUL_CAMPFIRE)
                || state.is(Blocks.SWEET_BERRY_BUSH);
    }

    private static boolean isNonColliding(ServerLevel level, BlockPos position) {
        return level.getBlockState(position).getCollisionShape(level, position).isEmpty()
                && level.getBlockState(position.above()).getCollisionShape(level, position.above()).isEmpty();
    }

    private record RespawnGuard(ResourceKey<Level> dimension, Vec3 position, int ticksRemaining) {
        private RespawnGuard nextTick() {
            return new RespawnGuard(dimension, position, ticksRemaining - 1);
        }
    }

    private record GhostRespawnTarget(ServerLevel level, Vec3 position) {
    }

    private GhostManager() {
    }
}
