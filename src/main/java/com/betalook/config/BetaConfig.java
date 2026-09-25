package com.betalook.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import com.betalook.BetaLook;
import com.betalook.registry.BetaContent;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Kazdy modul da sie wylaczyc osobno -- przy takiej liczbie ingerencji w render
 * to jedyny rozsadny sposob na diagnozowanie konfliktow z innymi modami.
 */
public final class BetaConfig {
    private static final Path FILE =
            FabricLoader.getInstance().getConfigDir().resolve("betalook.properties");

    public static boolean hidePostBetaBlocks = true;
    public static boolean hidePostBetaEntities = true;
    public static boolean hidePostBetaItems = true;
    public static boolean unifyWoodTextures = true;

    public static boolean betaFog = true;
    public static boolean betaLighting = true;
    public static boolean betaSky = true;
    public static boolean betaClouds = true;
    public static boolean betaBiomeColors = true;

    public static boolean betaAnimations = true;
    public static boolean betaViewBobbing = true;
    public static boolean betaHandSwing = true;

    /** Beta nie miala plynnego swiatla per-vertex w formie z 1.8+ -- osobny przelacznik. */
    public static boolean betaSmoothLighting = true;

    /** Void fog ponizej y=0 tak jak w becie. Domyslnie wl. */
    public static boolean voidFog = true;

    /**
     * Zapisz budowe klas renderu do config/betalook-classes.txt.
     *
     * Przydatne, gdy ktorys modul melduje w logu, ze nie znalazl pola --
     * plik mowi, co w tej wersji gry faktycznie istnieje.
     */
    public static boolean dumpClasses = false;

    private BetaConfig() {}

    public static void load() {
        Properties p = new Properties();
        if (Files.exists(FILE)) {
            try (var in = Files.newInputStream(FILE)) {
                p.load(in);
            } catch (IOException e) {
                BetaLook.LOGGER.warn("Nie udalo sie wczytac configu, uzywam domyslnych", e);
            }
        }
        hidePostBetaBlocks = bool(p, "hide_post_beta_blocks", hidePostBetaBlocks);
        hidePostBetaEntities = bool(p, "hide_post_beta_entities", hidePostBetaEntities);
        hidePostBetaItems = bool(p, "hide_post_beta_items", hidePostBetaItems);
        unifyWoodTextures = bool(p, "unify_wood_textures", unifyWoodTextures);
        betaFog = bool(p, "beta_fog", betaFog);
        betaLighting = bool(p, "beta_lighting", betaLighting);
        betaSky = bool(p, "beta_sky", betaSky);
        betaClouds = bool(p, "beta_clouds", betaClouds);
        betaBiomeColors = bool(p, "beta_biome_colors", betaBiomeColors);
        betaAnimations = bool(p, "beta_animations", betaAnimations);
        betaViewBobbing = bool(p, "beta_view_bobbing", betaViewBobbing);
        betaHandSwing = bool(p, "beta_hand_swing", betaHandSwing);
        betaSmoothLighting = bool(p, "beta_smooth_lighting", betaSmoothLighting);
        voidFog = bool(p, "void_fog", voidFog);
        dumpClasses = bool(p, "dump_classes", dumpClasses);
        BetaContent.invalidateCaches();
        save();
    }

    public static void save() {
        Properties p = new Properties();
        p.setProperty("hide_post_beta_blocks", String.valueOf(hidePostBetaBlocks));
        p.setProperty("hide_post_beta_entities", String.valueOf(hidePostBetaEntities));
        p.setProperty("hide_post_beta_items", String.valueOf(hidePostBetaItems));
        p.setProperty("unify_wood_textures", String.valueOf(unifyWoodTextures));
        p.setProperty("beta_fog", String.valueOf(betaFog));
        p.setProperty("beta_lighting", String.valueOf(betaLighting));
        p.setProperty("beta_sky", String.valueOf(betaSky));
        p.setProperty("beta_clouds", String.valueOf(betaClouds));
        p.setProperty("beta_biome_colors", String.valueOf(betaBiomeColors));
        p.setProperty("beta_animations", String.valueOf(betaAnimations));
        p.setProperty("beta_view_bobbing", String.valueOf(betaViewBobbing));
        p.setProperty("beta_hand_swing", String.valueOf(betaHandSwing));
        p.setProperty("beta_smooth_lighting", String.valueOf(betaSmoothLighting));
        p.setProperty("void_fog", String.valueOf(voidFog));
        p.setProperty("dump_classes", String.valueOf(dumpClasses));
        try {
            Files.createDirectories(FILE.getParent());
            try (var out = Files.newOutputStream(FILE)) {
                p.store(out, "BetaLook - odwzorowanie wygladu Minecraft Beta 1.7.3");
            }
        } catch (IOException e) {
            BetaLook.LOGGER.warn("Nie udalo sie zapisac configu", e);
        }
    }

    private static boolean bool(Properties p, String key, boolean def) {
        String v = p.getProperty(key);
        return v == null ? def : Boolean.parseBoolean(v.trim());
    }
}
