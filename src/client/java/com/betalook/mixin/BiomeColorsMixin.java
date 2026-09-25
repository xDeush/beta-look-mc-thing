package com.betalook.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.betalook.client.color.BetaColors;
import com.betalook.config.BetaConfig;

import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;

/**
 * W b1.7.3 woda byla WSZEDZIE tego samego koloru -- barwienie wody per biom
 * nie istnialo. To jedna z najmocniej rzucajacych sie w oczy roznic: dzis
 * bagno ma brudnozielona wode, a cieple morze turkusowa. W becie kazda
 * kaluza wygladala identycznie.
 *
 * Liscie sosny i brzozy tez mialy stale kolory, niezalezne od biomu --
 * dlatego wisniowe liscie, mimo debowej tekstury z packa, bez tego mixina
 * bylyby rozowe przez tint.
 *
 * Nazwy metod zweryfikowane wzgledem Sodium (DefaultColorProviders,
 * FluidRendererImpl).
 */
@Mixin(BiomeColors.class)
public class BiomeColorsMixin {

    @Inject(method = "getAverageWaterColor", at = @At("HEAD"), cancellable = true)
    private static void betalook$flatWater(BlockAndTintGetter level, BlockPos pos,
                                           CallbackInfoReturnable<Integer> cir) {
        if (BetaConfig.betaBiomeColors) {
            cir.setReturnValue(BetaColors.WATER);
        }
    }

    @Inject(method = "getAverageFoliageColor", at = @At("HEAD"), cancellable = true)
    private static void betalook$flatFoliage(BlockAndTintGetter level, BlockPos pos,
                                             CallbackInfoReturnable<Integer> cir) {
        if (BetaConfig.betaBiomeColors) {
            cir.setReturnValue(BetaColors.FOLIAGE);
        }
    }

    @Inject(method = "getAverageGrassColor", at = @At("HEAD"), cancellable = true)
    private static void betalook$flatGrass(BlockAndTintGetter level, BlockPos pos,
                                           CallbackInfoReturnable<Integer> cir) {
        if (BetaConfig.betaBiomeColors) {
            cir.setReturnValue(BetaColors.GRASS);
        }
    }
}
