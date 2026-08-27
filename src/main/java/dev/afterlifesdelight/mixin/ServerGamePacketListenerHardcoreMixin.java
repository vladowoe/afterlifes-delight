package dev.afterlifesdelight.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.afterlifesdelight.ghost.GhostManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerHardcoreMixin {
    @Shadow
    public ServerPlayer player;

    @WrapOperation(
            method = "handleClientCommand",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/MinecraftServer;isHardcore()Z"
            )
    )
    private boolean afterlifesDelight$keepRespawnedGhostOutOfSpectator(
            MinecraftServer server,
            Operation<Boolean> original
    ) {
        return original.call(server) && !GhostManager.isGhost(player);
    }
}
