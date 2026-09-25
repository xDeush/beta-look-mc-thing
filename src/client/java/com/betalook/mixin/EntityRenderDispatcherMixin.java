package com.betalook.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.betalook.client.render.PostBetaVisibility;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;

/**
 * Moby i encje spoza bety nie sa zglaszane do renderu. Nadal zyja i atakuja --
 * ukrywamy tylko obraz.
 *
 * Od 26.x render idzie przez stany (extract/submit), wiec nie dostajemy tu
 * encji, tylko jej stan. Na szczescie EntityRenderState niesie entityType,
 * co wystarcza do sprawdzenia, czy to tresc betowa.
 *
 * Sygnatura zweryfikowana wzgledem Iris
 * (mixin/entity_render_context/MixinEntityRenderDispatcher).
 */
@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {

    @Inject(method = "submit", at = @At("HEAD"), cancellable = true)
    private <S extends EntityRenderState> void betalook$hidePostBeta(
            S state, CameraRenderState cameraRenderState,
            double x, double y, double z,
            PoseStack poseStack, SubmitNodeCollector collector, CallbackInfo ci) {
        if (!PostBetaVisibility.shouldRender(state.entityType)) {
            ci.cancel();
        }
    }
}
