package com.betalook.mixin;

import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.betalook.client.fog.BetaFog;
import com.betalook.config.BetaConfig;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FogParameters;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FogType;

/**
 * Mgla liniowa startujaca na 25% dystansu renderowania + void fog ponizej y=20.
 *
 * Wspolczesny klient odsuwa mgle bardzo daleko i uzywa ksztaltu cylindrycznego.
 * Beta miala plaska, sferyczna mgle zaczynajaca sie duzo blizej -- dlatego nawet
 * w srodku dnia horyzont byl zamglony. Void fog Mojang usunal w 1.8.
 */
@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {

    @Inject(method = "setupFog", at = @At("RETURN"), cancellable = true)
    private static void betalook$betaFog(Camera camera, FogRenderer.FogMode fogMode,
                                         Vector4f color, float renderDistance,
                                         boolean thickFog, float partialTick,
                                         CallbackInfoReturnable<FogParameters> cir) {
        if (!BetaConfig.betaFog) {
            return;
        }

        FogType fluid = camera.getFluidInCamera();
        Entity entity = camera.getEntity();
        boolean ceiling = entity != null && entity.level().dimensionType().hasCeiling();

        BetaFog.Shape shape = switch (fluid) {
            case WATER -> BetaFog.water();
            case LAVA -> BetaFog.lava();
            default -> ceiling ? BetaFog.nether() : BetaFog.surface(renderDistance);
        };

        float start;
        float end;
        if (shape.exponential()) {
            // Silnik przyjmuje tylko przedzial liniowy, wiec przeliczamy gestosc
            // wykladnicza na rownowazny zasieg: e^(-d*x) jest juz nieodrozialne
            // od zera przy x ~ 4/d.
            end = 4.0F / shape.density();
            start = end * 0.1F;
        } else {
            start = shape.start();
            end = shape.end();
        }

        if (BetaConfig.voidFog && entity != null && !shape.exponential()) {
            end = BetaFog.voidFogEnd(end, entity.getEyeY());
            start = Math.min(start, end * BetaFog.LINEAR_START_FACTOR);
        }

        FogParameters vanilla = cir.getReturnValue();
        cir.setReturnValue(new FogParameters(start, end, vanilla.shape(),
                vanilla.red(), vanilla.green(), vanilla.blue(), vanilla.alpha()));
    }
}
