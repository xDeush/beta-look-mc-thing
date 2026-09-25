package com.betalook.client.render;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

/**
 * Czastki, ktore istnialy w b1.7.3.
 *
 * Beta miala ich kilkanascie. Dzisiaj jest ich ponad setka -- iskry fajerwerkow,
 * czastki sculka, zarodniki, dym z ogniska, czastki totemu. Wszystkie one
 * od razu zdradzaja, ze to nie jest stary Minecraft, nawet gdy bloki i swiatlo
 * juz wygladaja jak trzeba.
 */
public final class BetaParticles {
    private BetaParticles() {}

    /** Nazwy wspolczesne odpowiadajace czastkom z bety. */
    public static final Set<Identifier> BETA = Arrays.stream(new String[] {
            "smoke", "large_smoke", "flame", "lava",
            "splash", "bubble", "underwater", "rain",
            "crit", "explosion", "explosion_emitter", "poof",
            "portal", "dust", "note",
            "block", "item", "item_snowball",
            "cloud", "effect", "instant_effect", "entity_effect",
    }).map(Identifier::withDefaultNamespace).collect(Collectors.toUnmodifiableSet());

    private static final Map<ParticleType<?>, Boolean> CACHE = new ConcurrentHashMap<>();

    public static boolean isBeta(ParticleOptions options) {
        return CACHE.computeIfAbsent(options.getType(),
                type -> BETA.contains(BuiltInRegistries.PARTICLE_TYPE.getKey(type)));
    }
}
