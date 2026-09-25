package com.betalook.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.betalook.client.fog.BetaFog;
import com.betalook.config.BetaConfig;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;

/**
 * Mgla jak w b1.7.3: liniowa, zaczynajaca sie na 25% dystansu renderowania,
 * plus void fog ponizej y=20.
 *
 * W 26.x mgla ma dwie skladowe. environmentalStart/End to mgla srodowiskowa
 * (woda, lawa, slepota), renderDistanceStart/End to mgla dystansu. Vanilla
 * odsuwa te druga prawie na sam koniec zasiegu, przez co horyzont jest ostry.
 * Beta zaczynala ja na cwiartce dystansu -- stad jej charakterystyczna,
 * ciagle zamglona dal.
 *
 * Sygnatura zweryfikowana wzgledem Sodium (mixin/core/render/world/FogRendererMixin).
 */
@Mixin(FogRenderer.class)
public class FogRendererMixin {

    @Inject(method = "setupFog", at = @At("RETURN"))
    private void betalook$betaFog(Camera camera, int renderDistanceInChunks,
                                  DeltaTracker deltaTracker, float darkenWorldAmount,
                                  ClientLevel level,
                                  CallbackInfoReturnable<Vector4f> cir,
                                  @Local FogData fog) {
        if (!BetaConfig.betaFog) {
            return;
        }

        float renderDistanceBlocks = renderDistanceInChunks * 16.0F;
        BetaFog.Shape shape = BetaFog.surface(renderDistanceBlocks);

        float start = shape.start();
        float end = shape.end();

        if (BetaConfig.voidFog) {
            double eyeY = camera.getPosition().y;
            end = BetaFog.voidFogEnd(end, eyeY);
            start = Math.min(start, end * BetaFog.LINEAR_START_FACTOR);
        }

        fog.renderDistanceStart = start;
        fog.renderDistanceEnd = end;
    }
}
