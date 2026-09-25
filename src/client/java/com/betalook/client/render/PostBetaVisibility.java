package com.betalook.client.render;

import com.betalook.config.BetaConfig;
import com.betalook.registry.BetaContent;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Jedno miejsce, ktore odpowiada na pytanie "czy to w ogole rysujemy".
 *
 * Ukrywamy WYLACZNIE render. Kolizje, fizyka, AI, redstone i logika serwera
 * zostaja nietkniete -- blok, ktorego nie widac, nadal blokuje ruch.
 * To swiadoma decyzja: inaczej swiat bylby niegrywalny.
 */
public final class PostBetaVisibility {
    private PostBetaVisibility() {}

    public static boolean shouldRender(BlockState state) {
        return !BetaConfig.hidePostBetaBlocks || BetaContent.isBeta(state.getBlock());
    }

    public static boolean shouldRender(Entity entity) {
        return !BetaConfig.hidePostBetaEntities || BetaContent.isBeta(entity.getType());
    }

    public static boolean shouldRender(ItemStack stack) {
        return !BetaConfig.hidePostBetaItems || stack.isEmpty()
                || BetaContent.isBeta(stack.getItem());
    }
}
