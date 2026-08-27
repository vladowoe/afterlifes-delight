package dev.afterlifesdelight.mixin;

import dev.afterlifesdelight.ghost.GhostManager;
import java.util.OptionalInt;
import java.util.function.Consumer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMenuMixin {
    @Inject(
            method = "openMenu(Lnet/minecraft/world/MenuProvider;Ljava/util/function/Consumer;)Ljava/util/OptionalInt;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void afterlifesDelight$denyMenus(
            MenuProvider menuProvider,
            Consumer<RegistryFriendlyByteBuf> extraDataWriter,
            CallbackInfoReturnable<OptionalInt> callback
    ) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        if (GhostManager.isGhost(player)) {
            callback.setReturnValue(OptionalInt.empty());
        }
    }
}
