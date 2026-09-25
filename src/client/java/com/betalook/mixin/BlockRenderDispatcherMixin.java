package com.betalook.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.betalook.client.render.PostBetaVisibility;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Bloki spoza bety nie trafiaja do mesha chunka.
 *
 * UWAGA: Sodium ma wlasny pipeline budowania chunkow i tego mixina NIE uzyje.
 * Z Sodium trzeba osobnego modulu przez jego API -- patrz docs/TODO.md.
 */
@Mixin(BlockRenderDispatcher.class)
public abstract class BlockRenderDispatcherMixin {

    @Inject(method = "renderBatched", at = @At("HEAD"), cancellable = true)
    private void betalook$hidePostBeta(BlockState state, BlockPos pos, BlockAndTintGetter level,
                                       PoseStack poseStack, VertexConsumer consumer,
                                       boolean checkSides, RandomSource random, CallbackInfo ci) {
        if (!PostBetaVisibility.shouldRender(state)) {
            ci.cancel();
        }
    }
}
