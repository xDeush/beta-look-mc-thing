package com.betalook.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.betalook.client.light.BetaLightmap;
import com.betalook.config.BetaConfig;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionTypes;

/**
 * Podmieniamy cala zawartosc tekstury swiatla na wersje liczona wzorem z bety.
 * To jest zrodlo "cieplego" swiatla pochodni i braku wspolczesnego tone-mappingu.
 */
@Mixin(LightmapTextureManager.class)
public abstract class LightmapTextureManagerMixin {

    @Shadow @Final private NativeImage image;
    @Shadow @Final private NativeImageBackedTexture texture;
    @Shadow private boolean dirty;

    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    private void betalook$betaLightmap(float tickDelta, CallbackInfo ci) {
        if (!BetaConfig.betaLighting || !this.dirty) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        World world = client.world;
        if (world == null) {
            return;
        }

        float sunBrightness = world.getStarBrightness(tickDelta) >= 0.0F
                ? world.getSkyBrightness(tickDelta)
                : 1.0F;
        float flicker = world.getLightningTicksLeft() > 0 ? 1.0F : sunBrightness * 0.95F + 0.05F;
        float gamma = client.options.getGamma().getValue().floatValue();
        boolean nether = world.getDimensionEntry().matchesKey(DimensionTypes.THE_NETHER);

        for (int sky = 0; sky < 16; sky++) {
            for (int block = 0; block < 16; block++) {
                this.image.setColorArgb(block, sky,
                        BetaLightmap.color(sky, block, flicker, 0.0F, gamma, nether));
            }
        }

        this.texture.upload();
        this.dirty = false;
        ci.cancel();
    }
}
