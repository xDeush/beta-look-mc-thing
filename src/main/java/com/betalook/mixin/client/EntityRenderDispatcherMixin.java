package com.betalook.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.betalook.client.render.PostBetaVisibility;

import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.Entity;

/** Moby i encje spoza bety po prostu sie nie rysuja. Nadal zyja i atakuja. */
@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private <E extends Entity> void betalook$hidePostBeta(E entity, double x, double y, double z,
                                                          float tickDelta,
                                                          net.minecraft.client.util.math.MatrixStack matrices,
                                                          net.minecraft.client.render.VertexConsumerProvider vertices,
                                                          int light, CallbackInfo ci) {
        if (!PostBetaVisibility.shouldRender(entity)) {
            ci.cancel();
        }
    }
}
