package com.betalook.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.betalook.client.render.PostBetaVisibility;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;

/** Moby i encje spoza bety sie nie rysuja. Nadal zyja, chodza i atakuja. */
@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private <E extends Entity> void betalook$hidePostBeta(E entity, double x, double y, double z,
                                                          float partialTick, PoseStack poseStack,
                                                          MultiBufferSource buffers, int light,
                                                          CallbackInfo ci) {
        if (!PostBetaVisibility.shouldRender(entity)) {
            ci.cancel();
        }
    }
}
