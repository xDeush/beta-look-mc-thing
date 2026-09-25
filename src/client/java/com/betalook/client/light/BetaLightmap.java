package com.betalook.client.light;

/**
 * Tekstura swiatla (lightmap) 16x16 liczona wzorem z bety.
 *
 * Klasa celowo NIE zalezy od zadnej klasy Minecrafta -- dzieki temu da sie ja
 * skompilowac i przetestowac bez gry (patrz tools/test_beta_math.java).
 *
 * Trzy rzeczy odrozniaja beta lightmape od wspolczesnej:
 *  1. swiatlo bloku jest wyraznie cieplejsze -- kanal G i B sa tlumione
 *     nieliniowo, przez co pochodnia daje pomaranczowa plame, nie biala;
 *  2. nie ma nasycenia/night-vision ani modyfikatorow z efektow;
 *  3. krzywa jasnosci jest ta sama, ale gamma gracza wchodzi tylko
 *     jako proste rozjasnienie koncowe, bez wspolczesnego tone-mappingu.
 */
public final class BetaLightmap {
    private BetaLightmap() {}

    /** Minimalna jasnosc -- w becie 0.05, dlatego "czarne" jaskinie nie byly czarne. */
    public static final float AMBIENT = 0.05F;

    private static final float[] BRIGHTNESS_TABLE = new float[16];

    static {
        for (int i = 0; i < 16; i++) {
            float f = 1.0F - i / 15.0F;
            BRIGHTNESS_TABLE[i] = (1.0F - f) / (f * 3.0F + 1.0F) * (1.0F - AMBIENT) + AMBIENT;
        }
    }

    public static float brightness(int level) {
        return BRIGHTNESS_TABLE[clampLevel(level)];
    }

    /**
     * Zwraca kolor ARGB dla pary (poziom swiatla nieba, poziom swiatla bloku).
     *
     * @param skyLevel      0..15
     * @param blockLevel    0..15
     * @param sunBrightness jasnosc slonca 0..1 (pora dnia + pogoda)
     * @param torchFlicker  migotanie pochodni, ok. -0.1..0.1
     * @param gamma         suwak jasnosci gracza 0..1
     * @param nether        w Netherze swiatlo nieba jest stale i czerwonawe
     */
    public static int color(int skyLevel, int blockLevel, float sunBrightness,
                            float torchFlicker, float gamma, boolean nether) {
        float sky = brightness(skyLevel) * sunBrightness;
        float block = brightness(blockLevel) * (torchFlicker * 0.1F + 1.5F);

        float r;
        float g;
        float b;

        if (nether) {
            // Nether mial stale, ciemnoczerwone swiatlo tla.
            r = sky * 0.6F + 0.4F;
            g = sky * 0.6F + 0.4F;
            b = sky * 0.6F + 0.4F;
            r = r * 0.25F + 0.75F * 0.4F;
            g = g * 0.25F + 0.75F * 0.2F;
            b = b * 0.25F + 0.75F * 0.2F;
        } else {
            r = sky;
            g = sky;
            b = sky;
        }

        // Swiatlo bloku: R prawie liniowo, G slabiej, B najslabiej -> ciepla barwa.
        r += block;
        g += block * ((block * 0.6F + 0.4F) * 0.9F + 0.1F);
        b += block * (block * block * 0.6F + 0.4F);

        r = clamp01(r);
        g = clamp01(g);
        b = clamp01(b);

        // Gamma: w becie po prostu podnosila podloge jasnosci.
        if (gamma > 0.0F) {
            r = lift(r, gamma);
            g = lift(g, gamma);
            b = lift(b, gamma);
        }

        // Podloga, zeby nic nie bylo absolutnie czarne -- tak jak w becie.
        r = Math.max(r, AMBIENT);
        g = Math.max(g, AMBIENT);
        b = Math.max(b, AMBIENT);

        return 0xFF000000
                | ((int) (r * 255.0F) << 16)
                | ((int) (g * 255.0F) << 8)
                | (int) (b * 255.0F);
    }

    private static int clampLevel(int level) {
        return level < 0 ? 0 : (level > 15 ? 15 : level);
    }

    private static float clamp01(float v) {
        return v < 0.0F ? 0.0F : (v > 1.0F ? 1.0F : v);
    }

    private static float lift(float value, float gamma) {
        float lifted = 1.0F - (1.0F - value) * (1.0F - value) * (1.0F - value) * (1.0F - value);
        return value * (1.0F - gamma) + lifted * gamma;
    }
}
