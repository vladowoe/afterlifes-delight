package dev.afterlifesdelight.ghost;

import com.mojang.serialization.Codec;
import dev.afterlifesdelight.AfterlifesDelight;
import java.util.function.Supplier;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModAttachments {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, AfterlifesDelight.MOD_ID);

    public static final Supplier<AttachmentType<GhostData>> GHOST_DATA = ATTACHMENTS.register(
            "ghost_data",
            () -> AttachmentType.builder(() -> GhostData.EMPTY)
                    .serialize(GhostData.CODEC)
                    .copyOnDeath()
                    .build()
    );

    public static final Supplier<AttachmentType<Boolean>> GHOST_VISUAL_STATE = ATTACHMENTS.register(
            "ghost_visual_state",
            () -> AttachmentType.builder(() -> false)
                    .serialize(Codec.BOOL)
                    .copyOnDeath()
                    .sync(ByteBufCodecs.BOOL)
                    .build()
    );

    public static final Supplier<AttachmentType<Boolean>> GHOST_RAGDOLL_COLLISION_GRACE = ATTACHMENTS.register(
            "ghost_ragdoll_collision_grace",
            () -> AttachmentType.builder(() -> false)
                    .sync(ByteBufCodecs.BOOL)
                    .build()
    );

    public static void register(IEventBus modBus) {
        ATTACHMENTS.register(modBus);
    }

    private ModAttachments() {
    }
}
