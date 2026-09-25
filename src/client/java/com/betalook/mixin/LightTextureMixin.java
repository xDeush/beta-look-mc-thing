package com.betalook.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.betalook.client.light.BetaLightmap;
import com.betalook.config.BetaConfig;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.world.level.ClientLevel;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;

/**
 * Podmieniamy cala zawartosc tekstury swiatla na wersje liczona wzorem z bety.
 * Stad bierze sie cieple, pomaranczowe swiatlo pochodni i brak wspolczesnego
 * tone-mappingu, ktory rozjasnia cienie.
 */
@Mixin(LightTexture.class)
public abstract class LightTextureMixin {

    @Shadow @Final private NativeImage lightPixels;
    @Shadow @Final private DynamicTexture lightTexture;
    @Shadow private boolean updateLightTexture;

    @Inject(method = "updateLightTexture", at = @At("HEAD"), cancellable = true)
    private void betalook$betaLightmap(float partialTick, CallbackInfo ci) {
        if (!BetaConfig.betaLighting || !this.updateLightTexture) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        ClientLevel level = client.level;
        if (level == null) {
            return;
        }

        float skyDarken = level.getSkyDarken(partialTick);
        float gamma = client.options.gamma().get().floatValue();
        boolean nether = level.dimensionType().effectsLocation()
                .equals(BuiltinDimensionTypes.NETHER_EFFECTS);

        for (int sky = 0; sky < 16; sky++) {
            for (int block = 0; block < 16; block++) {
                this.lightPixels.setPixel(block, sky,
                        BetaLightmap.color(sky, block, skyDarken, 0.0F, gamma, nether));
            }
        }

        this.lightTexture.upload();
        this.updateLightTexture = false;
        ci.cancel();
    }
}
