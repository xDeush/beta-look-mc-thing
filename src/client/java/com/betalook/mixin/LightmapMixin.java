package com.betalook.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.betalook.client.light.LightmapWriter;
import com.betalook.config.BetaConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.client.renderer.state.LightmapRenderState;

/**
 * Cieple swiatlo pochodni z bety.
 *
 * Wchodzimy PO tym, jak vanilla policzy swoja lightmape, i nadpisujemy ja
 * wzorem z b1.7.3. Klasa i metoda zweryfikowane wzgledem Iris
 * (mixin/MixinLightTexture); uklad pol w stanie renderu odnajduje
 * refleksja w LightmapWriter, bo tej akurat nie dalo sie ustalic.
 */
@Mixin(LightmapRenderStateExtractor.class)
public class LightmapMixin {

    @Inject(method = "extract", at = @At("RETURN"))
    private void betalook$betaLightmap(LightmapRenderState state, float partialTick,
                                       CallbackInfo ci) {
        if (!BetaConfig.betaLighting) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        if (client.level == null) {
            return;
        }

        float gamma = client.options.gamma().get().floatValue();
        boolean nether = client.level.dimensionType().hasCeiling();

        LightmapWriter.write(state, gamma, nether);
    }
}
