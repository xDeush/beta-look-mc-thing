package com.betalook.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.betalook.client.fog.BetaFog;
import com.betalook.config.BetaConfig;

import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Fog;
import net.minecraft.client.render.FogShape;
import net.minecraft.entity.Entity;

/**
 * Mgla liniowa startujaca na 25% dystansu renderowania + void fog ponizej y=20.
 *
 * Wspolczesny klient liczy mgle jako FogShape.CYLINDER z bardzo pozna krzywa;
 * beta miala plaska mgle sferyczna zaczynajaca sie o wiele blizej.
 */
@Mixin(BackgroundRenderer.class)
public abstract class BackgroundRendererMixin {

    @Inject(method = "applyFog", at = @At("RETURN"), cancellable = true)
    private static void betalook$betaFog(Camera camera, BackgroundRenderer.FogType fogType,
                                         net.minecraft.util.math.Vec3d color,
                                         float viewDistance, boolean thickFog, float tickDelta,
                                         CallbackInfoReturnable<Fog> cir) {
        if (!BetaConfig.betaFog) {
            return;
        }

        Entity entity = camera.getFocusedEntity();
        BetaFog.Shape shape = switch (camera.getSubmersionType()) {
            case WATER -> BetaFog.water();
            case LAVA -> BetaFog.lava();
            default -> entity != null && entity.getWorld().getDimension().hasCeiling()
                    ? BetaFog.nether()
                    : BetaFog.surface(viewDistance);
        };

        float start;
        float end;
        if (shape.exponential()) {
            // Silnik przyjmuje tylko mgle liniowa, wiec przeliczamy gestosc
            // wykladnicza na rownowazny przedzial: e^(-d*x) ~= 0 przy x ~ 4/d.
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

        cir.setReturnValue(new Fog(start, end, FogShape.SPHERE,
                (float) color.x, (float) color.y, (float) color.z, 1.0F));
    }
}
