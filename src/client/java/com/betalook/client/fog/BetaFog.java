package com.betalook.client.fog;

/**
 * Mgla tak jak w b1.7.3.
 *
 * Bez zaleznosci od klas Minecrafta -- testowalne poza gra.
 *
 * Roznice wzgledem wspolczesnej mgly, ktore tu odtwarzamy:
 *  - na powierzchni mgla jest LINIOWA i zaczyna sie na 25% render distance,
 *    a nie na ~70-90% jak dzis (dlatego beta wyglada na "zamglona" nawet w dzien);
 *  - pod woda/lawa mgla jest wykladnicza o stalej gestosci;
 *  - Nether to jednolita wykladnicza mgla bez zaleznosci od render distance;
 *  - ponizej y=20 wlacza sie void fog, ktory Mojang usunal w 1.8.
 */
public final class BetaFog {
    private BetaFog() {}

    /** W becie mgla startowala na cwiartce dystansu renderowania. */
    public static final float LINEAR_START_FACTOR = 0.25F;

    public static final float WATER_DENSITY = 0.1F;
    public static final float LAVA_DENSITY = 2.0F;
    public static final float NETHER_DENSITY = 0.1F;

    /** Powyzej tego Y void fog nie wystepuje wcale. */
    public static final double VOID_FOG_TOP = 20.0D;

    public record Shape(boolean exponential, float density, float start, float end) {
        public static Shape linear(float start, float end) {
            return new Shape(false, 0.0F, start, end);
        }

        public static Shape exp(float density) {
            return new Shape(true, density, 0.0F, 0.0F);
        }
    }

    public static Shape surface(float renderDistanceBlocks) {
        return Shape.linear(renderDistanceBlocks * LINEAR_START_FACTOR, renderDistanceBlocks);
    }

    public static Shape water() {
        return Shape.exp(WATER_DENSITY);
    }

    public static Shape lava() {
        return Shape.exp(LAVA_DENSITY);
    }

    public static Shape nether() {
        return Shape.exp(NETHER_DENSITY);
    }

    /**
     * Mnoznik koloru mgly dla void fog.
     *
     * W becie liczone bylo z "brightness" na poziomie gracza podniesionej do
     * czwartej potegi i skalowanej wysokoscia. Im nizej i im ciemniej, tym
     * mocniej kolor mgly zbiega do czerni.
     *
     * @param y            wysokosc oka gracza
     * @param skyBrightness jasnosc nieba w tym punkcie, 0..1
     * @return mnoznik 0..1 do przemnozenia przez kolor mgly
     */
    public static float voidFogMultiplier(double y, float skyBrightness) {
        double relative = y / VOID_FOG_TOP;
        if (relative >= 1.0D) {
            return 1.0F;
        }
        if (relative < 0.0D) {
            relative = 0.0D;
        }

        float b = skyBrightness * skyBrightness * skyBrightness * skyBrightness;
        float base = (float) relative * (1.0F - b) + b;
        return base < 0.0F ? 0.0F : (base > 1.0F ? 1.0F : base);
    }

    /**
     * Gestosc mgly zwiekszana przez void fog. Zwraca dystans koncowy mgly,
     * skrocony w miare schodzenia ponizej y=20.
     */
    public static float voidFogEnd(float normalEnd, double y) {
        if (y >= VOID_FOG_TOP) {
            return normalEnd;
        }
        double relative = Math.max(y, 0.0D) / VOID_FOG_TOP;
        // Przy y=0 widocznosc spada do ~15% normalnej -- to mniej wiecej to,
        // co gracz widzial w becie na bedrocku.
        float factor = (float) (0.15D + 0.85D * relative);
        return normalEnd * factor;
    }
}
