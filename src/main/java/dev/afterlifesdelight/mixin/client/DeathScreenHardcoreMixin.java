package dev.afterlifesdelight.mixin.client;

import java.util.List;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DeathScreen.class)
public abstract class DeathScreenHardcoreMixin {
    @Shadow
    @Final
    private boolean hardcore;

    @Shadow
    @Final
    private List<Button> exitButtons;

    @Inject(method = "init", at = @At("TAIL"))
    private void afterlifesDelight$renameHardcoreRespawnButton(CallbackInfo callback) {
        if (hardcore && !exitButtons.isEmpty()) {
            exitButtons.getFirst().setMessage(Component.translatable(
                    "afterlifes_delight.death_screen.enter_spirit_world"
            ));
        }
    }
}
