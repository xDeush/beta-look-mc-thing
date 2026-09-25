package com.betalook.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.betalook.client.anim.BetaViewBob;
import com.betalook.config.BetaConfig;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;

/** Bujanie kamery wg bety -- wieksza amplituda boczna i inna faza pochylenia. */
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    private void betalook$betaBob(PoseStack poseStack, float partialTick, CallbackInfo ci) {
        if (!BetaConfig.betaViewBobbing) {
            return;
        }
        if (!(Minecraft.getInstance().getCameraEntity() instanceof LocalPlayer player)) {
            return;
        }

        float delta = player.walkDist - player.walkDistO;
        float walked = -(player.walkDist + delta * partialTick);
        float amount = Mth.lerp(partialTick, player.oBob, player.bob);
        float pitch = Mth.lerp(partialTick, player.xRotO, player.getXRot());

        BetaViewBob.bobView(poseStack, walked, amount, pitch);
        ci.cancel();
    }
}
