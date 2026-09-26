package com.betalook.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.betalook.client.sky.BetaClouds;
import com.betalook.config.BetaConfig;

import net.minecraft.client.renderer.CloudRenderer;

/**
 * Chmury na wysokosci z bety.
 *
 * W b1.7.3 wisialy na y=108 -- nisko, jak sufit tuz nad glowa. Wspolczesnie
 * sa duzo wyzej i z ziemi widac je jako cienka warstwe przy horyzoncie.
 *
 * Wysokosc przychodzi jako pierwszy argument typu float do render(). Nie ma
 * jej w zadnym polu klasy, wiec podmieniamy sam argument.
 */
@Mixin(CloudRenderer.class)
public class CloudRendererMixin {

    @ModifyVariable(method = "render", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float betalook$lowerClouds(float height) {
        return BetaConfig.betaClouds ? BetaClouds.BETA_HEIGHT : height;
    }
}
