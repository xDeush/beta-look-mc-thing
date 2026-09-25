package com.betalook.registry;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * Zrodlo prawdy: co istnialo w Minecraft Beta 1.7.3.
 *
 * Wszystko spoza tych zbiorow jest traktowane jako "post-beta" i nie jest
 * renderowane. Logika, kolizje i AI zostaja nietkniete.
 *
 * Trzymamy ResourceLocation, a nie referencje do pol Blocks/Items -- dzieki temu
 * mod sie nie wywala, gdy Mojang przeniesie albo usunie pole miedzy wersjami.
 */
public final class BetaContent {
    private BetaContent() {}

    public static final Set<ResourceLocation> BLOCKS = ids(
            "air", "stone", "grass_block", "dirt", "cobblestone", "oak_planks",
            "oak_sapling", "bedrock", "water", "lava",
            "sand", "gravel", "gold_ore", "iron_ore", "coal_ore",
            "oak_log", "oak_leaves", "sponge", "glass",
            "lapis_ore", "lapis_block", "dispenser", "sandstone", "note_block",
            "white_bed", "powered_rail", "detector_rail", "sticky_piston",
            "cobweb", "short_grass", "dead_bush", "piston", "piston_head",
            "white_wool", "dandelion", "poppy", "brown_mushroom", "red_mushroom",
            "gold_block", "iron_block", "smooth_stone_slab", "bricks", "tnt",
            "bookshelf", "mossy_cobblestone", "obsidian", "torch", "wall_torch",
            "fire", "spawner", "oak_stairs", "chest", "redstone_wire",
            "diamond_ore", "diamond_block", "crafting_table", "wheat", "farmland",
            "furnace", "oak_sign", "oak_wall_sign", "oak_door", "ladder", "rail",
            "cobblestone_stairs", "lever", "stone_pressure_plate", "iron_door",
            "oak_pressure_plate", "redstone_ore", "redstone_torch",
            "redstone_wall_torch", "stone_button", "snow", "ice", "snow_block",
            "cactus", "clay", "sugar_cane", "jukebox", "oak_fence", "pumpkin",
            "netherrack", "soul_sand", "glowstone", "nether_portal",
            "jack_o_lantern", "carved_pumpkin", "cake", "repeater", "oak_trapdoor"
    );

    public static final Set<ResourceLocation> ENTITIES = ids(
            "player", "item", "experience_orb", "painting", "arrow", "snowball",
            "egg", "fireball", "small_fireball", "tnt", "falling_block",
            "boat", "oak_boat", "minecart", "chest_minecart", "furnace_minecart",
            "lightning_bolt", "item_frame",
            "pig", "sheep", "cow", "chicken", "squid", "wolf",
            "zombie", "skeleton", "creeper", "spider", "cave_spider",
            "slime", "ghast", "zombified_piglin", "enderman", "giant"
    );

    public static final Set<ResourceLocation> ITEMS = ids(
            "iron_shovel", "iron_pickaxe", "iron_axe", "flint_and_steel", "apple",
            "bow", "arrow", "coal", "charcoal", "diamond", "iron_ingot", "gold_ingot",
            "iron_sword", "wooden_sword", "wooden_shovel", "wooden_pickaxe",
            "wooden_axe", "stone_sword", "stone_shovel", "stone_pickaxe",
            "stone_axe", "diamond_sword", "diamond_shovel", "diamond_pickaxe",
            "diamond_axe", "stick", "bowl", "mushroom_stew", "golden_sword",
            "golden_shovel", "golden_pickaxe", "golden_axe", "string", "feather",
            "gunpowder", "wooden_hoe", "stone_hoe", "iron_hoe", "diamond_hoe",
            "golden_hoe", "wheat_seeds", "wheat", "bread",
            "leather_helmet", "leather_chestplate", "leather_leggings", "leather_boots",
            "chainmail_helmet", "chainmail_chestplate", "chainmail_leggings", "chainmail_boots",
            "iron_helmet", "iron_chestplate", "iron_leggings", "iron_boots",
            "diamond_helmet", "diamond_chestplate", "diamond_leggings", "diamond_boots",
            "golden_helmet", "golden_chestplate", "golden_leggings", "golden_boots",
            "flint", "porkchop", "cooked_porkchop", "painting", "golden_apple",
            "oak_sign", "oak_door", "bucket", "water_bucket", "lava_bucket",
            "minecart", "saddle", "iron_door", "redstone", "snowball", "oak_boat",
            "leather", "milk_bucket", "brick", "clay_ball", "sugar_cane", "paper",
            "book", "slime_ball", "chest_minecart", "furnace_minecart", "egg",
            "compass", "fishing_rod", "clock", "glowstone_dust", "cod", "cooked_cod",
            "ink_sac", "bone", "sugar", "cake", "white_bed", "repeater", "cookie",
            "filled_map", "shears", "music_disc_13", "music_disc_cat"
    );

    /**
     * Gatunki drewna, ktorych w becie nie bylo. Renderuja sie jako dab --
     * w b1.7.3 byly trzy tekstury bali i JEDNA tekstura desek.
     */
    public static final String[] POST_BETA_WOOD = {
            "cherry", "bamboo", "mangrove", "crimson", "warped",
            "acacia", "dark_oak", "jungle", "pale_oak"
    };

    public static ResourceLocation unifyWood(ResourceLocation id) {
        String path = id.getPath();
        for (String species : POST_BETA_WOOD) {
            if (path.startsWith(species + "_")) {
                return ResourceLocation.fromNamespaceAndPath(
                        id.getNamespace(), "oak" + path.substring(species.length()));
            }
        }
        return id;
    }

    // Testy widocznosci leca w petli renderu (setki tysiecy wywolan na klatke)
    // i to z watkow buildera chunkow, wiec cache musi byc wspolbiezny.
    private static final Map<Block, Boolean> BLOCK_CACHE = new ConcurrentHashMap<>();
    private static final Map<Item, Boolean> ITEM_CACHE = new ConcurrentHashMap<>();
    private static final Map<EntityType<?>, Boolean> ENTITY_CACHE = new ConcurrentHashMap<>();

    public static boolean isBeta(Block block) {
        return BLOCK_CACHE.computeIfAbsent(block,
                b -> isBetaId(BuiltInRegistries.BLOCK.getKey(b), BLOCKS));
    }

    public static boolean isBeta(Item item) {
        return ITEM_CACHE.computeIfAbsent(item, i -> {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(i);
            return isBetaId(id, ITEMS) || isBetaId(id, BLOCKS);
        });
    }

    /**
     * Drewno post-betowe NIE jest ukrywane -- jest ujednolicane do debu.
     * Bez tego cherry_planks bylby jednoczesnie mapowany na dab i niewidzialny,
     * co znaczy, ze wisniowy las po prostu by zniknal zamiast wygladac na debowy.
     */
    private static boolean isBetaId(ResourceLocation id, Set<ResourceLocation> set) {
        return set.contains(id) || set.contains(unifyWood(id));
    }

    public static boolean isBeta(EntityType<?> type) {
        return ENTITY_CACHE.computeIfAbsent(type,
                t -> ENTITIES.contains(BuiltInRegistries.ENTITY_TYPE.getKey(t)));
    }

    /** Wolane przy przeladowaniu zasobow i przy zmianie configu. */
    public static void invalidateCaches() {
        BLOCK_CACHE.clear();
        ITEM_CACHE.clear();
        ENTITY_CACHE.clear();
    }

    private static Set<ResourceLocation> ids(String... paths) {
        // Arrays.stream, nie Set.of -- Set.of rzuca przy duplikatach,
        // a te listy sa utrzymywane recznie.
        return Arrays.stream(paths)
                .map(ResourceLocation::withDefaultNamespace)
                .collect(Collectors.toUnmodifiableSet());
    }
}
