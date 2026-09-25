package com.betalook.client.color;

/**
 * Kolory biomow z bety.
 *
 * Najbardziej rzucajaca sie w oczy roznica: w b1.7.3 WODA byla wszedzie
 * tego samego koloru -- nie istnialo barwienie wody per biom. Tak samo
 * liscie i trawa braly kolor wylacznie z pary (temperatura, wilgotnosc),
 * bez per-biomowych nadpisan, ktore Mojang dodal pozniej (bagna, ciemny las,
 * wisnie itd.).
 */
public final class BetaColors {
    private BetaColors() {}

    /** Jednolity kolor wody z bety. */
    public static final int WATER = 0xFFFFFF;

    /** Kolor mgly podwodnej z bety. */
    public static final int UNDERWATER_FOG = 0x0C1A3C;

    /** Kolor lisci sosny -- w becie stala, niezalezna od biomu. */
    public static final int SPRUCE_FOLIAGE = 0x619961;

    /** Kolor lisci brzozy -- j.w. */
    public static final int BIRCH_FOLIAGE = 0x80A755;

    /**
     * Zwraca indeks do grasscolor.png/foliagecolor.png tak, jak liczyla to beta.
     * Wilgotnosc byla mnozona przez temperature PRZED zamiana na wspolrzedne.
     */
    public static int colorIndex(double temperature, double rainfall) {
        double t = clamp01(temperature);
        double r = clamp01(rainfall) * t;
        int x = (int) ((1.0D - t) * 255.0D);
        int y = (int) ((1.0D - r) * 255.0D);
        return (y << 8) | x;
    }

    private static double clamp01(double v) {
        return v < 0.0D ? 0.0D : (v > 1.0D ? 1.0D : v);
    }
}
