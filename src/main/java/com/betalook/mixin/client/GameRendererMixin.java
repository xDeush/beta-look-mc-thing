package com.betalook.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.betalook.client.anim.BetaViewBob;
import com.betalook.config.BetaConfig;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

/** Bujanie kamery wg bety -- wieksza amplituda boczna i inna faza. */
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    private void betalook$betaBob(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if (!BetaConfig.betaViewBobbing) {
            return;
        }
        if (!(MinecraftClient.getInstance().getCameraEntity() instanceof ClientPlayerEntity player)) {
            return;
        }

        float delta = player.horizontalSpeed - player.prevHorizontalSpeed;
        float walked = player.horizontalSpeed + delta * tickDelta;
        float amount = MathHelper.lerp(tickDelta, player.prevStrideDistance, player.strideDistance);
        float pitch = MathHelper.lerp(tickDelta, player.prevPitch, player.getPitch());

        BetaViewBob.bobView(matrices, walked, amount, pitch);
        ci.cancel();
    }
}
