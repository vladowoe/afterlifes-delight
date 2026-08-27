package dev.afterlifesdelight.ghost;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.List;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public record GhostData(
        boolean ghost,
        String lastDeathDimension,
        double lastDeathX,
        double lastDeathY,
        double lastDeathZ,
        String lastSafeDimension,
        double lastSafeX,
        double lastSafeY,
        double lastSafeZ,
        boolean mayFlyBeforeGhost,
        boolean flyingBeforeGhost,
        String pendingResurrectionFood
) {
    public static final GhostData EMPTY = new GhostData(
            false,
            "",
            0.0,
            0.0,
            0.0,
            "",
            0.0,
            0.0,
            0.0,
            false,
            false,
            ""
    );

    public static final Codec<GhostData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("ghost", false).forGetter(GhostData::ghost),
            Codec.STRING.optionalFieldOf("last_death_dimension", "").forGetter(GhostData::lastDeathDimension),
            Codec.DOUBLE.optionalFieldOf("last_death_x", 0.0).forGetter(GhostData::lastDeathX),
            Codec.DOUBLE.optionalFieldOf("last_death_y", 0.0).forGetter(GhostData::lastDeathY),
            Codec.DOUBLE.optionalFieldOf("last_death_z", 0.0).forGetter(GhostData::lastDeathZ),
            Codec.STRING.optionalFieldOf("last_safe_dimension", "").forGetter(GhostData::lastSafeDimension),
            Codec.DOUBLE.optionalFieldOf("last_safe_x", 0.0).forGetter(GhostData::lastSafeX),
            Codec.DOUBLE.optionalFieldOf("last_safe_y", 0.0).forGetter(GhostData::lastSafeY),
            Codec.DOUBLE.optionalFieldOf("last_safe_z", 0.0).forGetter(GhostData::lastSafeZ),
            Codec.BOOL.optionalFieldOf("may_fly_before_ghost", false).forGetter(GhostData::mayFlyBeforeGhost),
            Codec.BOOL.optionalFieldOf("flying_before_ghost", false).forGetter(GhostData::flyingBeforeGhost),
            Codec.STRING.optionalFieldOf("pending_resurrection_food", "").forGetter(GhostData::pendingResurrectionFood)
    ).apply(instance, GhostData::new));

    public boolean hasLastDeathPosition() {
        return !lastDeathDimension.isBlank();
    }

    public boolean hasLastSafePosition() {
        return !lastSafeDimension.isBlank();
    }

    public Vec3 lastDeathPosition() {
        return new Vec3(lastDeathX, lastDeathY, lastDeathZ);
    }

    public Vec3 lastSafePosition() {
        return new Vec3(lastSafeX, lastSafeY, lastSafeZ);
    }

    public boolean hasPendingResurrectionFoods() {
        return !pendingResurrectionFood.isBlank();
    }

    public List<String> pendingResurrectionFoods() {
        if (pendingResurrectionFood.isBlank()) {
            return List.of();
        }

        return Arrays.stream(pendingResurrectionFood.split(","))
                .filter(itemId -> !itemId.isBlank())
                .toList();
    }

    public GhostData enterGhost(
            ResourceKey<Level> deathDimension,
            Vec3 deathPosition,
            boolean mayFly,
            boolean flying
    ) {
        String safeDimension = hasLastSafePosition()
                ? lastSafeDimension
                : deathDimension.location().toString();
        Vec3 safePosition = hasLastSafePosition() ? lastSafePosition() : deathPosition;

        return new GhostData(
                true,
                deathDimension.location().toString(),
                deathPosition.x,
                deathPosition.y,
                deathPosition.z,
                safeDimension,
                safePosition.x,
                safePosition.y,
                safePosition.z,
                mayFly,
                flying,
                pendingResurrectionFood
        );
    }

    public GhostData leaveGhost() {
        return new GhostData(
                false,
                lastDeathDimension,
                lastDeathX,
                lastDeathY,
                lastDeathZ,
                lastSafeDimension,
                lastSafeX,
                lastSafeY,
                lastSafeZ,
                mayFlyBeforeGhost,
                flyingBeforeGhost,
                ""
        );
    }

    public GhostData withLastSafePosition(ResourceKey<Level> dimension, Vec3 position) {
        return new GhostData(
                ghost,
                lastDeathDimension,
                lastDeathX,
                lastDeathY,
                lastDeathZ,
                dimension.location().toString(),
                position.x,
                position.y,
                position.z,
                mayFlyBeforeGhost,
                flyingBeforeGhost,
                pendingResurrectionFood
        );
    }

    public GhostData withPendingResurrectionFoods(List<String> itemIds) {
        return new GhostData(
                ghost,
                lastDeathDimension,
                lastDeathX,
                lastDeathY,
                lastDeathZ,
                lastSafeDimension,
                lastSafeX,
                lastSafeY,
                lastSafeZ,
                mayFlyBeforeGhost,
                flyingBeforeGhost,
                String.join(",", itemIds)
        );
    }

    public GhostData clearPendingResurrectionFood() {
        if (pendingResurrectionFood.isBlank()) {
            return this;
        }
        return new GhostData(
                ghost,
                lastDeathDimension,
                lastDeathX,
                lastDeathY,
                lastDeathZ,
                lastSafeDimension,
                lastSafeX,
                lastSafeY,
                lastSafeZ,
                mayFlyBeforeGhost,
                flyingBeforeGhost,
                ""
        );
    }
}
