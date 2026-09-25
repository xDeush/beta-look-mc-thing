package com.betalook.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.betalook.config.BetaConfig;
import com.betalook.registry.BetaContent;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockModelLighter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Ukryte bloki nie rzucaja cieni smooth lightingu.
 *
 * Algorytm AO jest w zasadzie ten sam od bety, wiec nie ma tu czego
 * "odtwarzac". Jest za to konkretny blad, ktory powstaje z polaczenia
 * moda z resourcepackiem: blok spoza bety jest niewidzialny, ale nadal
 * zaciemnia sasiadow. W jaskini wygladalo to jak cien rzucany przez
 * powietrze.
 *
 * getShadeBrightness zwraca "przezroczystosc swietlna" bloku: 1.0 znaczy
 * brak zaciemniania. Dla blokow, ktorych w becie nie bylo, zwracamy wlasnie
 * 1.0 -- skoro ich nie widac, nie powinny tez zostawiac sladu w swietle.
 *
 * Nazwa metody zweryfikowana wzgledem Fabric API
 * (fabric-renderer-indigo, AoCalculator).
 */
@Mixin(BlockModelLighter.Cache.class)
public class BlockModelLighterMixin {

    @Inject(method = "getShadeBrightness", at = @At("HEAD"), cancellable = true)
    private void betalook$noShadowFromHidden(BlockState state, BlockAndTintGetter level,
                                             BlockPos pos,
                                             CallbackInfoReturnable<Float> cir) {
        if (!BetaConfig.betaSmoothLighting || !BetaConfig.hidePostBetaBlocks) {
            return;
        }
        if (!BetaContent.isBeta(state.getBlock())) {
            cir.setReturnValue(1.0F);
        }
    }
}
