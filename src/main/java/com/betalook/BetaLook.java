package com.betalook;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.betalook.config.BetaConfig;

import net.fabricmc.api.ModInitializer;

public class BetaLook implements ModInitializer {
    public static final String MOD_ID = "betalook";
    public static final Logger LOGGER = LoggerFactory.getLogger("BetaLook");

    @Override
    public void onInitialize() {
        BetaConfig.load();
        LOGGER.info("BetaLook zaladowany -- cel: Minecraft Beta 1.7.3");
    }
}
