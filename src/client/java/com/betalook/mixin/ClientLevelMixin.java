package com.betalook.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.betalook.client.render.BetaParticles;
import com.betalook.config.BetaConfig;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;

/**
 * Czastki, ktorych w becie nie bylo, nie powstaja.
 *
 * Nazwa metody addParticle nie jest zweryfikowana wzgledem 26.2, dlatego
 * ten mixin siedzi w configu opcjonalnym -- nietrafiony injection wylacza
 * sam filtr czastek, nie gre.
 */
@Mixin(ClientLevel.class)
public class ClientLevelMixin {

    @Inject(method = "addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V",
            at = @At("HEAD"), cancellable = true)
    private void betalook$hidePostBetaParticles(ParticleOptions options,
                                                double x, double y, double z,
                                                double vx, double vy, double vz,
                                                CallbackInfo ci) {
        if (BetaConfig.betaParticles && !BetaParticles.isBeta(options)) {
            ci.cancel();
        }
    }
}
