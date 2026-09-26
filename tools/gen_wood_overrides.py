#!/usr/bin/env python3
"""
Generuje pliki resourcepacka, ktore sprawiaja, ze KAZDY gatunek drewna
wyglada jak drewno z bety (oak / spruce / birch).

W b1.7.3 istnialy trzy tekstury bali i JEDNA tekstura desek, wiec cherry,
mangrove, bamboo, crimson, warped, acacia, dark oak, jungle i pale oak
dostaja modele wskazujace na tekstury debu.

    python3 tools/gen_wood_overrides.py
"""
import json
import pathlib

ROOT = pathlib.Path(__file__).resolve().parent.parent
MODELS = ROOT / "resourcepack" / "assets" / "minecraft" / "models" / "block"

# Gatunki, ktorych w becie nie bylo -> renderuja sie jako dab.
POST_BETA = [
    "cherry", "bamboo", "mangrove", "crimson", "warped",
    "acacia", "dark_oak", "jungle", "pale_oak",
]

# (suffix modelu, szablon, mapa textures wskazujaca na tekstury debu)
SHAPES = {
    "planks": ("minecraft:block/cube_all", {"all": "minecraft:block/oak_planks"}),
    "log": ("minecraft:block/cube_column",
            {"end": "minecraft:block/oak_log_top", "side": "minecraft:block/oak_log"}),
    "wood": ("minecraft:block/cube_column",
             {"end": "minecraft:block/oak_log", "side": "minecraft:block/oak_log"}),
    "stairs": ("minecraft:block/stairs", {
        "bottom": "minecraft:block/oak_planks",
        "top": "minecraft:block/oak_planks",
        "side": "minecraft:block/oak_planks"}),
    "slab": ("minecraft:block/slab", {
        "bottom": "minecraft:block/oak_planks",
        "top": "minecraft:block/oak_planks",
        "side": "minecraft:block/oak_planks"}),
    "fence_post": ("minecraft:block/fence_post", {"texture": "minecraft:block/oak_planks"}),
    "fence_side": ("minecraft:block/fence_side", {"texture": "minecraft:block/oak_planks"}),
    "door_bottom": ("minecraft:block/door_bottom_left",
                    {"bottom": "minecraft:block/oak_door_bottom",
                     "top": "minecraft:block/oak_door_top"}),
    "trapdoor_bottom": ("minecraft:block/template_orientable_trapdoor_bottom",
                        {"texture": "minecraft:block/oak_trapdoor"}),
}


def main():
    MODELS.mkdir(parents=True, exist_ok=True)
    count = 0
    for species in POST_BETA:
        for suffix, (parent, textures) in SHAPES.items():
            model = {"parent": parent, "textures": textures}
            path = MODELS / f"{species}_{suffix}.json"
            path.write_text(json.dumps(model, indent=2) + "\n")
            count += 1

    # Liscie: w becie byly tylko debowe, sosnowe i brzozowe.
    #
    # Wisni i bladego debu tu NIE ma. Ich bloki nie maja w waniliowym kodzie
    # zrodla barwy, wiec model wskazujacy na szara teksture debu dalby
    # szare liscie. Tym dwom podmieniamy sama teksture -- gen_leaf_tint.py.
    untinted = {"cherry", "pale_oak"}
    for species in POST_BETA:
        if species in untinted:
            continue
        model = {"parent": "minecraft:block/leaves",
                 "textures": {"all": "minecraft:block/oak_leaves"}}
        (MODELS / f"{species}_leaves.json").write_text(json.dumps(model, indent=2) + "\n")
        count += 1

    print(f"Wygenerowano {count} modeli w {MODELS}")
    print("Uwaga: nazwy wariantow (np. *_fence_post) sprawdz wzgledem wersji, "
          "na ktora budujesz -- Mojang bywa, ze je przemianowuje.")


if __name__ == "__main__":
    main()
