package com.betalook.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.betalook.client.anim.BetaViewBob;
import com.betalook.client.anim.WalkTracker;
import com.betalook.config.BetaConfig;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;

/**
 * Bujanie kamery wg bety -- wieksza amplituda boczna i przesuniecie fazy
 * o 0.2 rad przy pochyleniu, przez co chod kolysze mocniej i ciezej.
 *
 * Sygnatura bobView to (CameraRenderState, PoseStack). Pierwsza wersja
 * zakladala (PoseStack, float partialTick) i mixin odpadal przy starcie
 * z bledem o niezgodnym deskryptorze.
 *
 * Braku partialTick nie da sie obejsc przez API, wiec WalkTracker liczy
 * ulamek ticku sam, z czasu od ostatniego ticku. Bez tego bujanie
 * chodziloby w 20 krokach na sekunde zamiast plynnie.
 */
@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    private void betalook$betaBob(CameraRenderState cameraRenderState,
                                  PoseStack poseStack, CallbackInfo ci) {
        if (!BetaConfig.betaViewBobbing) {
            return;
        }
        if (!(Minecraft.getInstance().getCameraEntity() instanceof LocalPlayer player)) {
            return;
        }

        float partialTick = WalkTracker.partialTick();
        float walked = -WalkTracker.walkDistance(partialTick);
        float amount = WalkTracker.bobAmount(partialTick);
        float pitch = Mth.lerp(partialTick, player.xRotO, player.getXRot());

        BetaViewBob.bobView(poseStack, walked, amount, pitch);
        ci.cancel();
    }
}
