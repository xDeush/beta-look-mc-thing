package com.betalook.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.betalook.client.render.PostBetaVisibility;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Itemy spoza bety znikaja tez z reki, z ziemi i z ramek.
 *
 * Swiadomie NIE ukrywamy ich w ekwipunku -- gracz musi widziec, co ma,
 * inaczej nie da sie grac. Ukrywanie w GUI wlacza sie osobno przez
 * hide_post_beta_items, ale dotyczy tylko renderu w swiecie.
 */
@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

    @Inject(method = "renderStatic", at = @At("HEAD"), cancellable = true)
    private void betalook$hidePostBeta(ItemStack stack, ItemDisplayContext context,
                                       int light, int overlay, PoseStack poseStack,
                                       MultiBufferSource buffers,
                                       net.minecraft.world.level.Level level,
                                       int seed, CallbackInfo ci) {
        if (context == ItemDisplayContext.GUI) {
            return;
        }
        if (!PostBetaVisibility.shouldRender(stack)) {
            ci.cancel();
        }
    }
}
