package com.betalook.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.betalook.client.render.PostBetaVisibility;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.block.BlockRenderManager;

/**
 * Bloki spoza bety nie trafiaja do mesha chunka. Kolizje zostaja --
 * to czysto wizualne wyciecie.
 */
@Mixin(BlockRenderManager.class)
public abstract class BlockRenderManagerMixin {

    @Inject(method = "renderBlock", at = @At("HEAD"), cancellable = true)
    private void betalook$hidePostBeta(BlockState state, net.minecraft.util.math.BlockPos pos,
                                       net.minecraft.world.BlockRenderView world,
                                       net.minecraft.client.util.math.MatrixStack matrices,
                                       net.minecraft.client.render.VertexConsumer vertexConsumer,
                                       boolean cull, net.minecraft.util.math.random.Random random,
                                       CallbackInfo ci) {
        if (!PostBetaVisibility.shouldRender(state)) {
            ci.cancel();
        }
    }
}
