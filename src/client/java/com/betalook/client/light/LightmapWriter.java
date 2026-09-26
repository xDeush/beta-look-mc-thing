package com.betalook.client.light;

import java.lang.reflect.Field;

import org.joml.Vector3f;

import com.betalook.BetaLook;

/**
 * Ustawia betowa barwe swiatla bloku.
 *
 * Lightmapa nie jest juz tablica 256 pikseli -- w 26.x to zestaw parametrow,
 * z ktorych gra sklada teksture swiatla. Interesuje nas jeden:
 * blockLightTint, czyli barwa swiatla pochodni.
 *
 * Wspolczesnie jest niemal biala. W becie kanal zielony i niebieski opadaly
 * wyraznie szybciej niz czerwony, przez co plama swiatla byla pomaranczowa.
 * Proporcje bierzemy z BetaLightmap: przy polowie zasiegu swiatla wychodzi
 * tam R=99 G=67 B=49, czyli mniej wiecej 1.00 / 0.68 / 0.49.
 */
public final class LightmapWriter {
    private LightmapWriter() {}

    /** Barwa swiatla pochodni z bety, znormalizowana do kanalu czerwonego. */
    public static final Vector3f BETA_TORCH_TINT = new Vector3f(1.0F, 0.68F, 0.49F);

    private static Field tintField;
    private static boolean searched;
    private static boolean failed;

    private static Field resolve(Class<?> stateClass) {
        if (searched) {
            return tintField;
        }
        searched = true;

        try {
            tintField = stateClass.getDeclaredField("blockLightTint");
            tintField.setAccessible(true);
            BetaLook.LOGGER.info("Lightmapa: ustawiam blockLightTint na {}",
                    BETA_TORCH_TINT);
            return tintField;
        } catch (NoSuchFieldException | RuntimeException e) {
            BetaLook.LOGGER.warn(
                    "Lightmapa: brak pola blockLightTint w {} -- cieple swiatlo "
                            + "pochodni bedzie wylaczone", stateClass.getName(), e);
            com.betalook.client.BetaDiagnostics.requestDump(
                    "lightmapa nie znalazla pola blockLightTint");
            return null;
        }
    }

    /**
     * @return true, gdy udalo sie ustawic barwe
     */
    public static boolean write(Object renderState) {
        Field field = resolve(renderState.getClass());
        if (field == null) {
            return false;
        }

        try {
            field.set(renderState, new Vector3f(BETA_TORCH_TINT));
            return true;
        } catch (IllegalAccessException | RuntimeException e) {
            if (!failed) {
                failed = true;
                BetaLook.LOGGER.warn("Lightmapa: nie udalo sie ustawic barwy", e);
            }
            return false;
        }
    }
}
