package com.betalook.client.render;

import com.betalook.config.BetaConfig;
import com.betalook.registry.BetaContent;

import net.minecraft.world.entity.EntityType;

/**
 * Odpowiada na pytanie "czy to w ogole rysujemy".
 *
 * Mod ukrywa tylko ENCJE. Bloki i itemy zalatwia resourcepack
 * (tools/gen_hide_pack.py), i to celowo:
 *
 *  - w 26.x render blokow idzie przez ModelBlockRenderer i BlockQuadOutput,
 *    czyli sciezke wywolywana per quad -- wpinanie sie tam kosztuje wiecej
 *    niz cala reszta moda razem wziety;
 *  - Sodium ma wlasny pipeline chunkow i tak czy inaczej ominalby mixin,
 *    a pack dziala niezaleznie od renderera;
 *  - pusty model to zero geometrii juz na etapie bakowania, wiec nic
 *    nie trafia nawet do bufora.
 *
 * Ukrywanie jest czysto wizualne: kolizje, fizyka, AI i redstone zostaja.
 */
public final class PostBetaVisibility {
    private PostBetaVisibility() {}

    public static boolean shouldRender(EntityType<?> type) {
        return !BetaConfig.hidePostBetaEntities || BetaContent.isBeta(type);
    }
}
