package com.betalook.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.betalook.client.light.LightmapWriter;
import com.betalook.config.BetaConfig;

import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.client.renderer.state.LightmapRenderState;

/**
 * Cieple swiatlo pochodni z bety.
 *
 * Wchodzimy po tym, jak vanilla policzy swoje parametry swiatla, i podmieniamy
 * barwe swiatla bloku. Nie ruszamy jasnosci ani swiatla nieba -- pora dnia,
 * pogoda i efekty maja dzialac dalej normalnie.
 */
@Mixin(LightmapRenderStateExtractor.class)
public class LightmapMixin {

    @Inject(method = "extract", at = @At("RETURN"))
    private void betalook$warmTorchLight(LightmapRenderState state, float partialTick,
                                         CallbackInfo ci) {
        if (BetaConfig.betaLighting) {
            LightmapWriter.write(state);
        }
    }
}
