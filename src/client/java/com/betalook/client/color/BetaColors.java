package com.betalook.client.color;

/**
 * Kolory biomow z bety.
 *
 * W b1.7.3 kolor trawy i lisci szedl wylacznie z pary (temperatura, wilgotnosc)
 * przez grasscolor.png, bez per-biomowych nadpisan, ktore Mojang dodal pozniej
 * (bagna, ciemny las, wisnie). Woda nie byla barwiona w ogole.
 *
 * Stale ponizej to wartosci srodka mapy kolorow bety -- kolor, ktory gracz
 * widzial w zwyklym lesie czy na rowninach.
 */
public final class BetaColors {
    private BetaColors() {}

    /** Jednolity kolor wody. W becie nie istnialo barwienie wody per biom. */
    public static final int WATER = 0xFFFFFF;

    /** Trawa: zielen z rownin bety. */
    public static final int GRASS = 0x79C05A;

    /** Liscie: odrobine ciemniejsze od trawy, tak jak w becie. */
    public static final int FOLIAGE = 0x6C9E4B;

    /** Kolor mgly podwodnej. */
    public static final int UNDERWATER_FOG = 0x0C1A3C;

    /**
     * Indeks do grasscolor.png/foliagecolor.png liczony tak jak w becie:
     * wilgotnosc byla mnozona przez temperature PRZED zamiana na wspolrzedne.
     * Zostawione dla narzedzi resourcepacka.
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
