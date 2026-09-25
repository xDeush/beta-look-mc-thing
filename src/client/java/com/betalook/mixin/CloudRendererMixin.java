package com.betalook.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.betalook.client.sky.BetaClouds;
import com.betalook.config.BetaConfig;

import net.minecraft.client.renderer.CloudRenderer;

/**
 * Obniza chmury do poziomu z bety (y=108) zaraz po utworzeniu renderera.
 *
 * Nie znam nazwy pola wysokosci w 26.2, wiec BetaClouds szuka go po wartosci
 * i dziala tylko przy jednoznacznym trafieniu. Mixin siedzi w configu
 * opcjonalnym, wiec brak trafienia nie psuje gry.
 */
@Mixin(CloudRenderer.class)
public class CloudRendererMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void betalook$lowerClouds(CallbackInfo ci) {
        if (BetaConfig.betaClouds) {
            BetaClouds.apply(this);
        }
    }
}
