package com.betalook.client;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;

import com.betalook.BetaLook;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Spisuje budowe klas renderu do pliku.
 *
 * Kilka modulow tego moda musi trafic w pola, ktorych nazwy zmieniaja sie
 * miedzy wersjami. Zamiast prosic kogokolwiek o grzebanie w zdekompilowanych
 * zrodlach, mod sam zapisuje, co widzi, do
 * config/betalook-classes.txt -- i na tej podstawie da sie dopisac
 * brakujace moduly bez zgadywania.
 *
 * Wlaczane przez dump_classes=true w config/betalook.properties.
 */
public final class BetaDiagnostics {
    private BetaDiagnostics() {}

    private static final String[] INTERESTING = {
            "net.minecraft.client.renderer.state.LightmapRenderState",
            "net.minecraft.client.renderer.LightmapRenderStateExtractor",
            "net.minecraft.client.renderer.CloudRenderer",
            "net.minecraft.client.renderer.SkyRenderer",
            "net.minecraft.client.player.LocalPlayer",
            "net.minecraft.world.entity.Entity",
            "net.minecraft.client.renderer.GameRenderer",
            "net.minecraft.client.renderer.entity.state.HumanoidRenderState",
    };

    public static void dump() {
        Path out = FabricLoader.getInstance().getConfigDir()
                .resolve("betalook-classes.txt");
        try {
            Files.createDirectories(out.getParent());
            try (Writer w = Files.newBufferedWriter(out)) {
                w.write("BetaLook -- budowa klas renderu w tej wersji gry\n");
                w.write("Wyslij ten plik, jesli ktorys modul sie nie wlacza.\n\n");
                for (String name : INTERESTING) {
                    describe(w, name);
                }
            }
            BetaLook.LOGGER.info("Zapisalem budowe klas do {}", out);
        } catch (IOException e) {
            BetaLook.LOGGER.warn("Nie udalo sie zapisac diagnostyki", e);
        }
    }

    private static void describe(Writer w, String className) throws IOException {
        w.write("=== " + className + "\n");
        Class<?> type;
        try {
            type = Class.forName(className);
        } catch (ClassNotFoundException e) {
            w.write("  (klasa nie istnieje w tej wersji)\n\n");
            return;
        }

        for (Field field : type.getDeclaredFields()) {
            w.write("  pole   " + field.getType().getSimpleName()
                    + " " + field.getName() + "\n");
        }
        for (Method method : type.getDeclaredMethods()) {
            StringBuilder params = new StringBuilder();
            for (Class<?> p : method.getParameterTypes()) {
                if (params.length() > 0) {
                    params.append(", ");
                }
                params.append(p.getSimpleName());
            }
            w.write("  metoda " + method.getReturnType().getSimpleName()
                    + " " + method.getName() + "(" + params + ")\n");
        }
        w.write("\n");
    }
}
