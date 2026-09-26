package com.betalook.client.sky;

/**
 * Kolor nieba wg b1.7.3.
 *
 * W becie niebo braly kolor z JEDNEJ liczby -- temperatury biomu -- przez
 * konwersje HSV. Nie bylo ani gradientu przy horyzoncie, ani per-biomowych
 * nadpisan, ktore Mojang dodal pozniej. Dlatego betowe niebo jest plaskie
 * i ma ten charakterystyczny, lekko fioletowawy odcien blekitu.
 *
 * Klasa nie zalezy od zadnej klasy Minecrafta -- da sie ja przetestowac
 * bez gry (patrz tools/test/BetaMathTest.java).
 */
public final class BetaSky {
    private BetaSky() {}

    /** Odcien nieba przy temperaturze 0. W becie stala zapisana na sztywno. */
    public static final float BASE_HUE = 0.6222222F;

    /**
     * Kolor nieba dla danej temperatury biomu, jako RGB w jednym incie.
     *
     * Wzor z bety: temperatura dzielona przez 3 i przyciakana do -1..1,
     * potem HSV gdzie temperatura przesuwa odcien i nasycenie. Cieplejszy
     * biom = odcien blizszy zieleni i mocniej nasycony.
     */
    public static int skyColorByTemperature(float temperature) {
        float t = temperature / 3.0F;
        t = clamp(t, -1.0F, 1.0F);
        return hsvToRgb(BASE_HUE - t * 0.05F, 0.5F + t * 0.1F, 1.0F);
    }

    /**
     * Pelny kolor nieba: barwa biomu przyciemniona pora dnia.
     *
     * @param temperature    temperatura biomu
     * @param celestialAngle kat sloneczny 0..1 (0 = poludnie)
     * @return RGB w jednym incie
     */
    public static int skyColor(float temperature, float celestialAngle) {
        float daylight = (float) Math.cos(celestialAngle * Math.PI * 2.0D) * 2.0F + 0.5F;
        return skyColorWithDaylight(temperature, clamp(daylight, 0.0F, 1.0F));
    }

    /**
     * Jasnosc dnia odczytana z koloru, ktory policzyla vanilla.
     *
     * Najmocniejszy kanal waniliowego nieba idzie w gore i w dol razem z pora
     * dnia i pogoda. Dzieki temu nie musimy wolac zadnego API czasu -- a to
     * wlasnie te metody Mojang przenosi i przemianowuje najczesciej.
     */
    public static float daylightFromVanillaSky(int vanillaRgb) {
        int r = (vanillaRgb >> 16) & 0xFF;
        int g = (vanillaRgb >> 8) & 0xFF;
        int b = vanillaRgb & 0xFF;
        return Math.max(Math.max(r, g), b) / 255.0F;
    }

    /** Barwa biomu przyciemniona zadanym wspolczynnikiem 0..1. */
    public static int skyColorWithDaylight(float temperature, float daylight) {
        daylight = clamp(daylight, 0.0F, 1.0F);
        int base = skyColorByTemperature(temperature);
        int r = (int) (((base >> 16) & 0xFF) * daylight);
        int g = (int) (((base >> 8) & 0xFF) * daylight);
        int b = (int) ((base & 0xFF) * daylight);
        return (r << 16) | (g << 8) | b;
    }

    /**
     * HSV na RGB, tak jak liczyl to MathHelper z bety.
     *
     * Wlasna implementacja zamiast java.awt.Color, bo ta ostatnia inaczej
     * zaokragla i nie jest dostepna wszedzie.
     */
    public static int hsvToRgb(float hue, float saturation, float value) {
        int sector = (int) (hue * 6.0F) % 6;
        float offset = hue * 6.0F - (int) (hue * 6.0F);
        float p = value * (1.0F - saturation);
        float q = value * (1.0F - offset * saturation);
        float t = value * (1.0F - (1.0F - offset) * saturation);

        float r;
        float g;
        float b;
        switch (sector) {
            case 0 -> { r = value; g = t; b = p; }
            case 1 -> { r = q; g = value; b = p; }
            case 2 -> { r = p; g = value; b = t; }
            case 3 -> { r = p; g = q; b = value; }
            case 4 -> { r = t; g = p; b = value; }
            default -> { r = value; g = p; b = q; }
        }

        return (channel(r) << 16) | (channel(g) << 8) | channel(b);
    }

    private static int channel(float v) {
        return (int) (clamp(v, 0.0F, 1.0F) * 255.0F);
    }

    private static float clamp(float v, float min, float max) {
        return v < min ? min : (v > max ? max : v);
    }
}
