package com.betalook.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.betalook.client.anim.BetaViewBob;
import com.betalook.client.anim.WalkTracker;
import com.betalook.config.BetaConfig;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;

/**
 * Bujanie kamery wg bety -- wieksza amplituda boczna i przesuniecie fazy
 * o 0.2 rad przy pochyleniu, przez co chod kolysze mocniej i ciezej.
 *
 * Dystans marszu bierzemy z WalkTracker, a nie z pol gracza: ich nazwy
 * zmienily sie w 26.x. Dzieki temu ten mixin nie zalezy od niczego poza
 * PoseStack i pitch gracza.
 *
 * Nazwa metody "bobView" nie jest zweryfikowana wzgledem 26.2, dlatego
 * ten mixin siedzi w betalook.optional.mixins.json z required=false --
 * nietrafiony injection wylacza tylko bujanie, nie wywala gry.
 */
@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    private void betalook$betaBob(PoseStack poseStack, float partialTick, CallbackInfo ci) {
        if (!BetaConfig.betaViewBobbing) {
            return;
        }
        if (!(Minecraft.getInstance().getCameraEntity() instanceof LocalPlayer player)) {
            return;
        }

        float walked = -WalkTracker.walkDistance(partialTick);
        float amount = WalkTracker.bobAmount(partialTick);
        float pitch = Mth.lerp(partialTick, player.xRotO, player.getXRot());

        BetaViewBob.bobView(poseStack, walked, amount, pitch);
        ci.cancel();
    }
}
