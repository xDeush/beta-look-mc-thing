#!/usr/bin/env python3
"""
Generuje resourcepack, ktory UKRYWA wszystko, czego nie bylo w becie.

Czyta liste blokow i itemow z TWOJEGO jara Minecrafta (potrzebny, bo tylko on
wie, co w tej wersji w ogole istnieje), odejmuje liste bety z BetaContent.java
i dla reszty wypluwa puste modele.

    python3 tools/gen_hide_pack.py ~/.minecraft/versions/26.2/26.2.jar

Ukrywanie jest czysto wizualne -- blok nadal blokuje ruch. To samo robi mod,
ale pack dziala tez bez moda i lapie Sodium, ktory omija mixiny renderu.
"""
import argparse
import json
import pathlib
import sys
import zipfile

from beta_lists import BETA_BLOCKS, BETA_ITEMS, POST_BETA_WOOD

ROOT = pathlib.Path(__file__).resolve().parent.parent
PACK = ROOT / "resourcepack" / "assets" / "minecraft"

EMPTY_MODEL = "minecraft:block/betalook_empty"


def read_ids(jar: zipfile.ZipFile, folder: str) -> set[str]:
    """Nazwy plikow .json w danym katalogu assetow, bez rozszerzenia."""
    prefix = f"assets/minecraft/{folder}/"
    return {
        name[len(prefix):-len(".json")]
        for name in jar.namelist()
        if name.startswith(prefix) and name.endswith(".json") and "/" not in name[len(prefix):]
    }


def write(path: pathlib.Path, data: dict) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2) + "\n")


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("jar", type=pathlib.Path, help="jar wersji, na ktorej grasz")
    ap.add_argument("--dry-run", action="store_true",
                    help="tylko pokaz, ile czego zostaloby ukryte")
    args = ap.parse_args()

    if not args.jar.is_file():
        sys.exit(f"Nie ma takiego pliku: {args.jar}")

    with zipfile.ZipFile(args.jar) as jar:
        all_blocks = read_ids(jar, "blockstates")
        # Od 1.21.4 definicje modeli itemow siedza w assets/minecraft/items/.
        item_defs = read_ids(jar, "items")
        legacy_items = read_ids(jar, "models/item") if not item_defs else set()

    if not all_blocks:
        sys.exit("W jarze nie ma assets/minecraft/blockstates/ -- zly plik?")

    # Drewno post-betowe jest UJEDNOLICANE do debu, nie ukrywane -- inaczej
    # wisniowy las zniknalby zamiast wygladac na debowy.
    def unify(name: str) -> str:
        for species in POST_BETA_WOOD:
            if name.startswith(species + "_"):
                return "oak" + name[len(species):]
        return name

    def keep(name: str, beta: set[str]) -> bool:
        return name in beta or unify(name) in beta

    hide_blocks = sorted(b for b in all_blocks if not keep(b, BETA_BLOCKS))
    hide_items = sorted(i for i in (item_defs or legacy_items) if not keep(i, BETA_ITEMS))

    print(f"bloki w jarze:   {len(all_blocks):5d}  -> ukrywam {len(hide_blocks)}")
    print(f"itemy w jarze:   {len(item_defs or legacy_items):5d}  -> ukrywam {len(hide_items)}")

    if args.dry_run:
        print("\nprzykladowe bloki:", ", ".join(hide_blocks[:12]))
        print("przykladowe itemy:", ", ".join(hide_items[:12]))
        return

    # Pusty model: brak "elements" znaczy zero geometrii, czyli nic sie nie rysuje.
    write(PACK / "models" / "block" / "betalook_empty.json", {})

    for block in hide_blocks:
        # Wariant "" lapie kazdy stan bloku niezaleznie od jego wlasciwosci,
        # wiec nie musimy znac schematu blockstate'a.
        write(PACK / "blockstates" / f"{block}.json",
              {"variants": {"": {"model": EMPTY_MODEL}}})

    for item in hide_items:
        if item_defs:
            write(PACK / "items" / f"{item}.json",
                  {"model": {"type": "minecraft:empty"}})
        else:
            write(PACK / "models" / "item" / f"{item}.json", {})

    print(f"\nZapisano do {PACK}")
    print("Uwaga: ukryte bloki nadal blokuja ruch -- to tylko warstwa wizualna.")


if __name__ == "__main__":
    main()
