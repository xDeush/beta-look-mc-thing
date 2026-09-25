#!/usr/bin/env python3
"""
Rysuje terrain.png z numerami kafelkow, zebys mogl poprawic terrain_map.json.

    python3 tools/index_terrain.py ~/.minecraft/versions/b1.7.3/b1.7.3.jar

Wynik: build/terrain_indexed.png -- atlas powiekszony 8x, z numerem
na kazdym kafelku i aktualnym przypisaniem z terrain_map.json pod spodem.

Po co: mapa indeksow w tym repo jest w duzej czesci zgadywana. Zamiast
zgadywac dalej, otwierasz ten obrazek, widzisz gdzie faktycznie sa liscie,
i poprawiasz jedna linijke w terrain_map.json.
"""
import argparse
import json
import pathlib
import sys
import zipfile

try:
    from PIL import Image, ImageDraw
except ImportError:
    sys.exit("Potrzebny Pillow:  pip install pillow")

ROOT = pathlib.Path(__file__).resolve().parent.parent
OUT = ROOT / "build" / "terrain_indexed.png"
SCALE = 8


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("jar", type=pathlib.Path)
    ap.add_argument("--tile", type=int, default=16)
    args = ap.parse_args()

    if not args.jar.is_file():
        sys.exit(f"Nie ma takiego pliku: {args.jar}")

    mapping = json.loads((ROOT / "tools" / "terrain_map.json").read_text())
    mapping = {int(k): v for k, v in mapping.items() if k.isdigit()}

    with zipfile.ZipFile(args.jar) as jar:
        with jar.open("terrain.png") as fh:
            terrain = Image.open(fh).convert("RGBA")

    per_row = terrain.width // args.tile
    big = terrain.resize(
        (terrain.width * SCALE, terrain.height * SCALE), Image.NEAREST)

    # Szachownica pod spodem, zeby przezroczyste kafelki bylo widac jako puste.
    board = Image.new("RGBA", big.size, (255, 255, 255, 255))
    step = args.tile * SCALE // 2
    d = ImageDraw.Draw(board)
    for y in range(0, board.height, step):
        for x in range(0, board.width, step):
            if (x // step + y // step) % 2:
                d.rectangle([x, y, x + step, y + step], fill=(200, 200, 200, 255))
    board.alpha_composite(big)

    draw = ImageDraw.Draw(board)
    cell = args.tile * SCALE
    for index in range((terrain.width // args.tile) * (terrain.height // args.tile)):
        x = (index % per_row) * cell
        y = (index // per_row) * cell
        draw.rectangle([x, y, x + cell - 1, y + cell - 1], outline=(255, 0, 0, 255))
        label = str(index)
        draw.text((x + 3, y + 2), label, fill=(255, 0, 0, 255))
        if index in mapping:
            draw.text((x + 3, y + cell - 12),
                      mapping[index].split("/")[-1][:14], fill=(0, 0, 255, 255))

    OUT.parent.mkdir(parents=True, exist_ok=True)
    board.save(OUT)
    print(f"Zapisano {OUT}")
    print("Czerwone = numer kafelka. Niebieskie = obecne przypisanie.")
    print("Puste pola w szachownice = kafelek przezroczysty.")


if __name__ == "__main__":
    main()
