package com.betalook.client;

import net.fabricmc.api.ClientModInitializer;

public class BetaLookClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Celowo pusto.
        //
        // Byl tu listener przeladowania zasobow czyszczacy cache BetaContent.
        // Okazal sie zbedny: cache jest kluczowany po obiektach Block, Item
        // i EntityType, a te sa tworzone raz przy starcie gry i przeladowanie
        // resourcepacka ich nie rusza. Jedyne, co faktycznie uniewaznia cache,
        // to zmiana configu -- i to robi juz BetaConfig.load().
        //
        // Przy okazji pozbylismy sie zaleznosci od API resource-loadera,
        // ktore w 26.x przechodzi z v0 na v1 i zmienilo sygnature reload().
    }
}
