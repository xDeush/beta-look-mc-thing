package com.betalook.client;

import com.betalook.client.anim.WalkTracker;
import com.betalook.config.BetaConfig;

import net.fabricmc.api.ClientModInitializer;

public class BetaLookClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Wlasny licznik dystansu marszu -- pola gracza, z ktorych korzystala
        // wczesniejsza wersja, zmienily nazwy w 26.x.
        WalkTracker.register();

        if (BetaConfig.dumpClasses) {
            BetaDiagnostics.dump();
        }
    }
}
