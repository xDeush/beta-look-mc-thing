package com.betalook.client.sky;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import com.betalook.BetaLook;

/**
 * Obniza chmury do poziomu z bety.
 *
 * W b1.7.3 chmury wisialy na y=108. Wspolczesnie sa duzo wyzej, przez co
 * z ziemi widac je jako cienka warstwe przy horyzoncie zamiast sufitu
 * tuz nad glowa.
 *
 * Nie znam nazwy pola trzymajacego wysokosc, wiec szukamy go PO WARTOSCI:
 * pole liczbowe, ktorego zawartosc odpowiada typowej wspolczesnej wysokosci
 * chmur. Dzialamy tylko wtedy, gdy kandydat jest DOKLADNIE JEDEN -- przy
 * dwoch albo zerowych nie ruszamy niczego i piszemy o tym w logu.
 * Lepiej nie zmienic nic, niz nadpisac losowe pole.
 */
public final class BetaClouds {
    private BetaClouds() {}

    public static final float BETA_HEIGHT = 108.0F;

    /** Wysokosci, jakich Mojang uzywal dla chmur w roznych wersjach. */
    private static final double[] KNOWN_HEIGHTS = {192.0D, 128.0D, 127.0D};

    private static boolean done;

    public static void apply(Object cloudRenderer) {
        if (done) {
            return;
        }
        done = true;

        List<Field> candidates = new ArrayList<>();
        for (Field field : cloudRenderer.getClass().getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            Class<?> type = field.getType();
            if (type != float.class && type != double.class && type != int.class) {
                continue;
            }
            try {
                field.setAccessible(true);
                double value = ((Number) field.get(cloudRenderer)).doubleValue();
                for (double known : KNOWN_HEIGHTS) {
                    if (Math.abs(value - known) < 0.001D) {
                        candidates.add(field);
                        break;
                    }
                }
            } catch (IllegalAccessException | RuntimeException e) {
                BetaLook.LOGGER.debug("Pomijam pole {}", field.getName(), e);
            }
        }

        if (candidates.size() != 1) {
            BetaLook.LOGGER.warn(
                    "Chmury: {} kandydatow na pole wysokosci -- nie ruszam niczego. "
                            + "Chmury zostana na wspolczesnej wysokosci.",
                    candidates.size());
            com.betalook.client.BetaDiagnostics.requestDump(
                    "chmury nie znalazly pola wysokosci");
            return;
        }

        Field field = candidates.get(0);
        try {
            if (field.getType() == float.class) {
                field.setFloat(cloudRenderer, BETA_HEIGHT);
            } else if (field.getType() == double.class) {
                field.setDouble(cloudRenderer, BETA_HEIGHT);
            } else {
                field.setInt(cloudRenderer, (int) BETA_HEIGHT);
            }
            BetaLook.LOGGER.info("Chmury: {} ustawione na y={}",
                    field.getName(), BETA_HEIGHT);
        } catch (IllegalAccessException | RuntimeException e) {
            BetaLook.LOGGER.warn("Chmury: nie udalo sie ustawic {}", field.getName(), e);
        }
    }
}
