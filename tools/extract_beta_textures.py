#!/usr/bin/env python3
"""
Wypakowuje tekstury z TWOJEGO wlasnego jara Minecraft Beta 1.7.3
i sklada z nich wspolczesny resourcepack.

Repo nie zawiera i nie bedzie zawierac zadnych tekstur Mojanga --
musisz wskazac wlasna kopie jara (np. z .minecraft/versions/b1.7.3/).

    python3 tools/extract_beta_textures.py ~/.minecraft/versions/b1.7.3/b1.7.3.jar

Wynik: resourcepack/assets/minecraft/textures/...
"""
import argparse
import json
import pathlib
import sys
import zipfile

try:
    from PIL import Image
except ImportError:
    sys.exit("Potrzebny Pillow:  pip install pillow")

ROOT = pathlib.Path(__file__).resolve().parent.parent
OUT = ROOT / "resourcepack" / "assets" / "minecraft" / "textures"

# Pliki kopiowane 1:1 (nazwa w becie -> sciezka docelowa bez .png)
DIRECT = {
    "terrain/sun.png": "environment/sun",
    "terrain/moon.png": "environment/moon_phases",
    "misc/water.png": "misc/underwater",
    "particles.png": "particle/particles",
    "clouds.png": "environment/clouds",
    "mob/char.png": "entity/steve",
    "mob/pig.png": "entity/pig/pig",
    "mob/sheep.png": "entity/sheep/sheep",
    "mob/sheep_fur.png": "entity/sheep/sheep_fur",
    "mob/cow.png": "entity/cow/cow",
    "mob/chicken.png": "entity/chicken",
    "mob/squid.png": "entity/squid/squid",
    "mob/wolf.png": "entity/wolf/wolf",
    "mob/zombie.png": "entity/zombie/zombie",
    "mob/skeleton.png": "entity/skeleton/skeleton",
    "mob/creeper.png": "entity/creeper/creeper",
    "mob/spider.png": "entity/spider/spider",
    "mob/slime.png": "entity/slime/slime",
    "mob/ghast.png": "entity/ghast/ghast",
    "mob/pigzombie.png": "entity/zombie_pigman",
    "mob/enderman.png": "entity/enderman/enderman",
    "art/kz.png": "painting/paintings",
    "gui/items.png": "_atlas/items",
    "gui/gui.png": "gui/widgets",
    "gui/icons.png": "gui/icons",
}


def is_blank(tile_img) -> bool:
    """
    Czy kafelek jest w calosci przezroczysty.

    To straznik przed zla mapa indeksow: jesli wskazemy pusty kafelek,
    tekstura docelowa staje sie niewidzialna i blok znika z gry -- co
    wyglada jak blad moda, a jest blednym numerem w terrain_map.json.
    """
    alpha = tile_img.getchannel("A")
    return alpha.getextrema()[1] == 0


def slice_atlas(img, index, tile=16):
    per_row = img.width // tile
    x = (index % per_row) * tile
    y = (index // per_row) * tile
    return img.crop((x, y, x + tile, y + tile))


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("jar", type=pathlib.Path, help="sciezka do jara bety")
    ap.add_argument("--tile", type=int, default=16, help="rozmiar kafla w terrain.png")
    args = ap.parse_args()

    if not args.jar.is_file():
        sys.exit(f"Nie ma takiego pliku: {args.jar}")

    mapping = json.loads((ROOT / "tools" / "terrain_map.json").read_text())
    mapping = {int(k): v for k, v in mapping.items() if k.isdigit()}

    OUT.mkdir(parents=True, exist_ok=True)
    written = 0

    with zipfile.ZipFile(args.jar) as jar:
        names = set(jar.namelist())

        if "terrain.png" not in names:
            sys.exit("W jarze nie ma terrain.png -- czy to na pewno jar bety?")

        with jar.open("terrain.png") as fh:
            terrain = Image.open(fh).convert("RGBA")

        blank = []
        for index, dest in sorted(mapping.items()):
            piece = slice_atlas(terrain, index, args.tile)

            if is_blank(piece):
                blank.append((index, dest))
                continue

            target = OUT / f"{dest}.png"
            target.parent.mkdir(parents=True, exist_ok=True)
            piece.save(target)
            written += 1

        if blank:
            print()
            print(f"POMINIETO {len(blank)} pustych kafelkow -- zle numery "
                  f"w tools/terrain_map.json:")
            for index, dest in blank:
                print(f"  kafelek {index} -> {dest}")
            print("Zapisanie ich zrobiloby te bloki NIEWIDZIALNYMI w grze.")
            print("Zobacz wlasciwe numery: python3 tools/index_terrain.py <jar>")
            print()

        for src, dest in DIRECT.items():
            if src not in names:
                print(f"  pomijam (brak w jarze): {src}")
                continue
            target = OUT / f"{dest}.png"
            target.parent.mkdir(parents=True, exist_ok=True)
            with jar.open(src) as fh:
                Image.open(fh).convert("RGBA").save(target)
            written += 1

    print(f"Zapisano {written} tekstur do {OUT}")
    print("Atlas itemow (items.png) trzeba jeszcze pociac -- patrz docs/RESOURCEPACK.md")


if __name__ == "__main__":
    main()
