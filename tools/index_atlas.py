#!/usr/bin/env python3
"""
Rysuje atlas bety z numerami kafelkow, zebys mogl poprawic mape indeksow.

    python3 tools/index_atlas.py <jar bety>                          # terrain.png
    python3 tools/index_atlas.py <jar bety> --atlas gui/items.png    # itemy

Wynik: build/<nazwa>_indexed.png -- atlas powiekszony 8x, z numerem na kazdym
kafelku i aktualnym przypisaniem pod spodem, na szachownicy (zeby bylo widac,
ktore kafelki sa przezroczyste).

Po co: mapy indeksow w tym repo sa w czesci zgadywane. Zamiast zgadywac dalej,
otwierasz obrazek, widzisz gdzie faktycznie sa liscie albo kilof, i poprawiasz
jedna linijke w terrain_map.json / items_map.json.
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
SCALE = 8

MAPS = {
    "terrain.png": "terrain_map.json",
    "gui/items.png": "items_map.json",
}


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("jar", type=pathlib.Path)
    ap.add_argument("--atlas", default="terrain.png", choices=sorted(MAPS))
    ap.add_argument("--tile", type=int, default=16)
    args = ap.parse_args()

    out = ROOT / "build" / (
        pathlib.Path(args.atlas).stem + "_indexed.png")

    if not args.jar.is_file():
        sys.exit(f"Nie ma takiego pliku: {args.jar}")

    mapping = json.loads((ROOT / "tools" / MAPS[args.atlas]).read_text())
    mapping = {int(k): v for k, v in mapping.items() if k.isdigit()}

    with zipfile.ZipFile(args.jar) as jar:
        if args.atlas not in jar.namelist():
            sys.exit(f"W jarze nie ma {args.atlas} -- czy to jar bety?")
        with jar.open(args.atlas) as fh:
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

    out.parent.mkdir(parents=True, exist_ok=True)
    board.save(out)
    print(f"Zapisano {out}")
    print("Czerwone = numer kafelka. Niebieskie = obecne przypisanie.")
    print("Puste pola w szachownice = kafelek przezroczysty.")


if __name__ == "__main__":
    main()
