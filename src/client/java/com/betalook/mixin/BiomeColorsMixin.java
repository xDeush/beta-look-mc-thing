package com.betalook.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.betalook.client.color.BetaColors;
import com.betalook.config.BetaConfig;

import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;

/**
 * W b1.7.3 woda byla WSZEDZIE tego samego koloru -- barwienie wody per biom
 * nie istnialo. To jedna z najbardziej rzucajacych sie w oczy roznic:
 * we wspolczesnej grze bagno ma brudnozielona wode, a ocean cieplych mórz
 * turkusowa. W becie kazda kaluza wygladala tak samo.
 */
@Mixin(BiomeColors.class)
public abstract class BiomeColorsMixin {

    @Inject(method = "getAverageWaterColor", at = @At("HEAD"), cancellable = true)
    private static void betalook$flatWater(BlockAndTintGetter level, BlockPos pos,
                                           CallbackInfoReturnable<Integer> cir) {
        if (BetaConfig.betaBiomeColors) {
            cir.setReturnValue(BetaColors.WATER);
        }
    }
}
