package com.betalook.client.render;

import com.betalook.config.BetaConfig;
import com.betalook.registry.BetaContent;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

/**
 * Jedno miejsce, ktore odpowiada na pytanie "czy to w ogole rysujemy".
 *
 * Ukrywamy WYLACZNIE render. Kolizje, fizyka, AI, redstone i logika serwera
 * zostaja nietkniete -- blok, ktorego nie widac, nadal blokuje ruch.
 */
public final class PostBetaVisibility {
    private PostBetaVisibility() {}

    public static boolean shouldRender(BlockState state) {
        if (!BetaConfig.hidePostBetaBlocks) {
            return true;
        }
        return BetaContent.isBeta(state.getBlock());
    }

    public static boolean shouldRender(Entity entity) {
        if (!BetaConfig.hidePostBetaEntities) {
            return true;
        }
        return BetaContent.isBeta(entity.getType());
    }

    public static boolean shouldRender(ItemStack stack) {
        if (!BetaConfig.hidePostBetaItems || stack.isEmpty()) {
            return true;
        }
        return BetaContent.isBeta(stack.getItem());
    }
}
