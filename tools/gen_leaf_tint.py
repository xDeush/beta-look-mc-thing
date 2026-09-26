#!/usr/bin/env python3
"""
Robi zazielenione kopie lisci debu dla gatunkow, ktore nie maja barwienia.

    python3 tools/gen_leaf_tint.py <jar bety>

Po co to istnieje:

Tekstura lisci debu jest w ODCIENIACH SZAROSCI. Zielen dodaje dopiero
"tint source" przypisany do konkretnego BLOKU w kodzie gry. Dab, brzoza,
dzungla czy akacja go maja. Wisnia i blady dab -- nie, bo ich waniliowe
tekstury sa juz kolorowe same z siebie.

Skutek: skierowanie wisni na teksture debu daje liscie SZARE. Sam pack nie
ma jak dopisac blokowi zrodla barwy, wiec zamiast tego zapisujemy dla niego
osobna kopie tekstury, juz przemnozona przez betowa zielen.
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
OUT = ROOT / "resourcepack" / "assets" / "minecraft" / "textures" / "block"

# Ta sama stala co BetaColors.FOLIAGE po stronie moda.
BETA_FOLIAGE = (0x6C, 0x9E, 0x4B)

# Gatunki bez zrodla barwy w waniliowym kodzie.
UNTINTED = ["cherry_leaves", "pale_oak_leaves"]


def tint(image: Image.Image, color: tuple[int, int, int]) -> Image.Image:
    """Mnozy kanaly RGB przez kolor, zostawiajac alfe bez zmian."""
    r, g, b, a = image.convert("RGBA").split()
    r = r.point(lambda v: v * color[0] // 255)
    g = g.point(lambda v: v * color[1] // 255)
    b = b.point(lambda v: v * color[2] // 255)
    return Image.merge("RGBA", (r, g, b, a))


def leaf_tile_index() -> int:
    mapping = json.loads((ROOT / "tools" / "terrain_map.json").read_text())
    for key, value in mapping.items():
        if key.isdigit() and value == "block/oak_leaves":
            return int(key)
    sys.exit("W terrain_map.json nie ma wpisu na block/oak_leaves")


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("jar", type=pathlib.Path, help="jar bety")
    ap.add_argument("--tile", type=int, default=16)
    args = ap.parse_args()

    if not args.jar.is_file():
        sys.exit(f"Nie ma takiego pliku: {args.jar}")

    index = leaf_tile_index()

    with zipfile.ZipFile(args.jar) as jar:
        if "terrain.png" not in jar.namelist():
            sys.exit("W jarze nie ma terrain.png -- czy to jar bety?")
        with jar.open("terrain.png") as fh:
            terrain = Image.open(fh).convert("RGBA")

    per_row = terrain.width // args.tile
    x = (index % per_row) * args.tile
    y = (index // per_row) * args.tile
    tile = terrain.crop((x, y, x + args.tile, y + args.tile))

    if tile.getchannel("A").getextrema()[1] == 0:
        sys.exit(f"Kafelek {index} jest pusty -- popraw terrain_map.json "
                 f"(podglad: tools/index_atlas.py)")

    green = tint(tile, BETA_FOLIAGE)

    OUT.mkdir(parents=True, exist_ok=True)
    for name in UNTINTED:
        green.save(OUT / f"{name}.png")

    print(f"Zazielenione liscie z kafelka {index}: {', '.join(UNTINTED)}")
    print(f"Zapisano do {OUT}")


if __name__ == "__main__":
    main()
