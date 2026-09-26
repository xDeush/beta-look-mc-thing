package com.betalook.client.anim;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

/**
 * Liczy dystans marszu i amplitude bujania wlasnym licznikiem.
 *
 * Vanilla trzyma te wartosci w polach gracza, ale w 26.x nie nazywaja sie juz
 * walkDist/bob i nie udalo sie ustalic nowych nazw bez zdekompilowanych zrodel.
 * Zamiast na nie czekac, odtwarzamy je od zera z pozycji gracza -- wzorem
 * z bety, wiec wynik jest nawet blizszy oryginalowi niz odczyt z vanilli.
 *
 * W becie, co tick:
 *   walkDist += przebyty dystans poziomy * 0.6
 *   bob      += (min(predkosc * 4, 1) - bob) * 0.4
 *
 * Mnoznik 0.4 to wygladzanie: amplituda narasta i opada przez kilka tickow,
 * dlatego kamera nie zatrzymuje sie w miejscu w polowie kroku.
 */
public final class WalkTracker {
    private WalkTracker() {}

    private static double lastX;
    private static double lastZ;
    private static boolean primed;

    private static float walkDist;
    private static float prevWalkDist;
    private static float bob;
    private static float prevBob;

    private static long lastTickNanos;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(WalkTracker::tick);
    }

    private static void tick(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null) {
            primed = false;
            return;
        }

        Vec3 pos = player.position();

        if (!primed) {
            lastX = pos.x;
            lastZ = pos.z;
            primed = true;
            return;
        }

        double dx = pos.x - lastX;
        double dz = pos.z - lastZ;
        lastX = pos.x;
        lastZ = pos.z;

        float speed = (float) Math.sqrt(dx * dx + dz * dz);

        prevWalkDist = walkDist;
        walkDist += speed * 0.6F;

        // W powietrzu kamera nie buja -- tak samo jak w becie.
        float target = player.onGround() ? Math.min(speed * 4.0F, 1.0F) : 0.0F;

        prevBob = bob;
        bob += (target - bob) * 0.4F;

        lastTickNanos = System.nanoTime();
    }

    /**
     * Ulamek ticku, liczony z czasu od ostatniego ticku.
     *
     * Metoda bobView nie dostaje juz partialTick, a bez niego bujanie
     * skakaloby dwadziescia razy na sekunde zamiast plynac. Tick trwa 50 ms,
     * wiec wystarczy zmierzyc, ile z niego uplynelo.
     */
    public static float partialTick() {
        if (lastTickNanos == 0L) {
            return 0.0F;
        }
        float elapsed = (System.nanoTime() - lastTickNanos) / 50_000_000.0F;
        return elapsed < 0.0F ? 0.0F : (elapsed > 1.0F ? 1.0F : elapsed);
    }

    /** Dystans marszu z interpolacja miedzy tickami. */
    public static float walkDistance(float partialTick) {
        return prevWalkDist + (walkDist - prevWalkDist) * partialTick;
    }

    /** Amplituda bujania z interpolacja miedzy tickami. */
    public static float bobAmount(float partialTick) {
        return prevBob + (bob - prevBob) * partialTick;
    }
}
