package com.betalook.client.light;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import com.betalook.BetaLook;

/**
 * Zapisuje betowa lightmape do stanu renderu, nie znajac nazwy pola.
 *
 * Nazwy pol w klasach Minecrafta zmieniaja sie z wersji na wersje, a tej
 * akurat nie dalo sie ustalic bez zdekompilowanych zrodel. Zamiast zgadywac
 * i wywalac kompilacje, szukamy pola PO TYPIE I ROZMIARZE: lightmapa to
 * tablica 256 intow (16 poziomow nieba x 16 poziomow bloku).
 *
 * Jesli takiego pola nie ma, modul sam sie wylacza i mowi o tym w logu --
 * gra dziala dalej, tylko bez cieplego swiatla.
 */
public final class LightmapWriter {
    private LightmapWriter() {}

    public static final int SIZE = 256;

    private static Field pixelField;
    private static boolean searched;
    private static boolean warned;

    /**
     * Znajduje pole trzymajace piksele lightmapy.
     *
     * @return pole albo null, gdy nie znaleziono
     */
    private static Field resolve(Class<?> stateClass) {
        if (searched) {
            return pixelField;
        }
        searched = true;

        for (Field field : stateClass.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            if (field.getType() != int[].class) {
                continue;
            }
            try {
                field.setAccessible(true);
                pixelField = field;
                BetaLook.LOGGER.info(
                        "Lightmapa: uzywam pola {}.{}",
                        stateClass.getSimpleName(), field.getName());
                return pixelField;
            } catch (RuntimeException e) {
                BetaLook.LOGGER.debug("Nie moge otworzyc pola {}", field.getName(), e);
            }
        }

        BetaLook.LOGGER.warn(
                "Lightmapa: nie znalazlem tablicy int[{}] w {} -- cieple swiatlo "
                        + "pochodni bedzie wylaczone. Pola w tej klasie: {}",
                SIZE, stateClass.getName(), describe(stateClass));
        return null;
    }

    private static String describe(Class<?> type) {
        StringBuilder sb = new StringBuilder();
        for (Field field : type.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers())) {
                sb.append(field.getType().getSimpleName())
                        .append(' ').append(field.getName()).append("; ");
            }
        }
        return sb.toString();
    }

    /**
     * Nadpisuje lightmape wartosciami z {@link BetaLightmap}.
     *
     * Jasnosc nieba odczytujemy z tego, co wlasnie policzyla vanilla --
     * dzieki temu nie musimy znac metody zwracajacej pore dnia. Bierzemy
     * kanal czerwony z pola (sky=15, block=0), czyli pelne swiatlo nieba
     * bez swiatla bloku.
     *
     * @return true, gdy udalo sie zapisac
     */
    public static boolean write(Object renderState, float gamma, boolean nether) {
        Field field = resolve(renderState.getClass());
        if (field == null) {
            return false;
        }

        try {
            Object value = field.get(renderState);
            if (!(value instanceof int[] pixels) || pixels.length < SIZE) {
                warnOnce("Pole {} nie trzyma tablicy {} intow", field.getName(), SIZE);
                return false;
            }

            float skyBrightness = sampleSkyBrightness(pixels);

            for (int sky = 0; sky < 16; sky++) {
                for (int block = 0; block < 16; block++) {
                    pixels[sky * 16 + block] =
                            BetaLightmap.color(sky, block, skyBrightness, 0.0F, gamma, nether);
                }
            }
            return true;
        } catch (IllegalAccessException | RuntimeException e) {
            warnOnce("Nie udalo sie zapisac lightmapy: {}", e.toString());
            return false;
        }
    }

    /**
     * Wyciaga jasnosc nieba z lightmapy policzonej przez vanille.
     *
     * Komorka (sky=15, block=0) to pelne swiatlo nieba bez swiatla bloku,
     * wiec jej jasnosc odpowiada porze dnia i pogodzie. To pozwala nam
     * reagowac na noc i burze bez znajomosci API swiata.
     */
    private static float sampleSkyBrightness(int[] pixels) {
        int argb = pixels[15 * 16];
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        return Math.max(Math.max(r, g), b) / 255.0F;
    }

    private static void warnOnce(String message, Object... args) {
        if (!warned) {
            warned = true;
            BetaLook.LOGGER.warn("Lightmapa: " + message, args);
        }
    }
}
