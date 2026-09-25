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

SUBSTITUTIONS = {
    k: v for k, v in
    json.loads((pathlib.Path(__file__).resolve().parent / "substitutions.json").read_text()).items()
    if not k.startswith("_")
}

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


def pack_format(jar: zipfile.ZipFile) -> int | None:
    """
    Czyta wersje formatu resourcepacka z version.json w jarze gry.

    Zgadywanie tej liczby jest najczestsza przyczyna komunikatu
    "pack jest niezgodny z ta wersja" -- Mojang podnosi ja niemal
    z kazdym wydaniem, wiec lepiej ja odczytac niz wpisywac na sztywno.
    """
    try:
        with jar.open("version.json") as fh:
            meta = json.load(fh)
    except (KeyError, json.JSONDecodeError):
        return None

    version = meta.get("pack_version")
    if isinstance(version, dict):
        return version.get("resource")
    if isinstance(version, int):
        return version
    return None


def blockstate(block: str, beta: str) -> dict:
    """
    Blockstate kierujacy blok na model betowego odpowiednika.

    Bloki osiowe (bale, drewno, lodygi) musza zachowac obroty, inaczej kazdy
    bal lezy pionowo niezaleznie od tego, jak zostal postawiony.
    """
    model = f"minecraft:block/{beta}"

    if block.endswith(("_log", "_wood", "_stem", "_hyphae")):
        return {
            "variants": {
                "axis=y": {"model": model},
                "axis=z": {"model": model, "x": 90},
                "axis=x": {"model": model, "x": 90, "y": 90},
            }
        }

    # Pusty klucz wariantu lapie kazdy stan bloku, wiec nie musimy znac
    # jego wlasciwosci.
    return {"variants": {"": {"model": model}}}


def write(path: pathlib.Path, data: dict) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2) + "\n")


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("jar", type=pathlib.Path, help="jar wersji, na ktorej grasz")
    ap.add_argument("--dry-run", action="store_true",
                    help="tylko pokaz, ile czego zostaloby ukryte")
    ap.add_argument("--explain", metavar="BLOK",
                    help="powiedz, co pack robi z tym blokiem, i wyjdz")
    args = ap.parse_args()

    if not args.jar.is_file():
        sys.exit(f"Nie ma takiego pliku: {args.jar}")

    with zipfile.ZipFile(args.jar) as jar:
        all_blocks = read_ids(jar, "blockstates")
        # Od 1.21.4 definicje modeli itemow siedza w assets/minecraft/items/.
        item_defs = read_ids(jar, "items")
        legacy_items = read_ids(jar, "models/item") if not item_defs else set()
        fmt = pack_format(jar)

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

    # Bloki tworzace teren NIE sa ukrywane, tylko podstawiane betowym
    # odpowiednikiem. Ukrywanie granitu, deepslate czy andezytu robi
    # dziury w ziemi -- swiat wyglada na zniszczony, nie na betowy.
    substitute = {b: SUBSTITUTIONS[b] for b in all_blocks if b in SUBSTITUTIONS}

    # Drewno: wymuszamy blockstate, a nie tylko model.
    #
    # gen_wood_overrides.py nadpisuje models/block/cherry_planks.json, co
    # dziala tylko dopoki waniliowy blockstate wskazuje wlasnie na ten model.
    # Gdy wskazuje gdzie indziej, zmiane widac WYLACZNIE na ikonie w GUI
    # (bo item model idzie inna sciezka), a postawiony blok zostaje bez zmian.
    # Wskazanie wprost na model debu omija ten problem calkiem.
    for block in all_blocks:
        target = unify(block)
        if target == block or target not in all_blocks:
            continue
        if block.endswith(("_log", "_wood", "_stem", "_hyphae")):
            substitute.setdefault(block, target)
        elif block.endswith(("_planks", "_leaves")):
            substitute.setdefault(block, target)

    hide_blocks = sorted(
        b for b in all_blocks
        if not keep(b, BETA_BLOCKS) and b not in substitute)
    hide_items = sorted(
        i for i in (item_defs or legacy_items)
        if not keep(i, BETA_ITEMS) and i not in SUBSTITUTIONS)

    print(f"bloki w jarze:   {len(all_blocks):5d}  -> ukrywam {len(hide_blocks)},"
          f" podstawiam {len(substitute)}")
    print(f"itemy w jarze:   {len(item_defs or legacy_items):5d}  -> ukrywam {len(hide_items)}")

    if args.explain:
        name = args.explain
        print(f"blok: {name}")
        if name not in all_blocks:
            print("  NIE MA go w tym jarze -- zla nazwa albo zla wersja gry")
        elif name in substitute:
            print(f"  PODSTAWIANY -> renderuje sie jak {substitute[name]}")
            print(f"  plik: assets/minecraft/blockstates/{name}.json")
            print(f"  tresc: {json.dumps(blockstate(name, substitute[name]))}")
        elif keep(name, BETA_BLOCKS):
            print("  ZOSTAWIONY bez zmian -- jest na liscie blokow bety")
        else:
            print("  UKRYWANY -> pusty model, nic sie nie rysuje")
            print(f"  plik: assets/minecraft/blockstates/{name}.json")
        return

    if args.dry_run:
        print("\nukrywane:   ", ", ".join(hide_blocks[:12]))
        print("podstawiane:", ", ".join(f"{k}->{v}" for k, v in
                                        sorted(substitute.items())[:8]))
        print("itemy:      ", ", ".join(hide_items[:12]))
        return

    if fmt is None:
        print("UWAGA: nie odczytalem pack_format z version.json -- zostawiam "
              "to, co jest w pack.mcmeta. Jesli gra powie, ze pack jest "
              "niezgodny, popraw ta liczbe recznie.")
    else:
        write(PACK.parent.parent / "pack.mcmeta", {
            "pack": {
                "pack_format": fmt,
                "description": "BetaLook - wyglad Minecraft Beta 1.7.3",
            },
        })
        print(f"pack_format odczytany z jara: {fmt}")

    # Pusty model: brak "elements" znaczy zero geometrii, czyli nic sie nie rysuje.
    write(PACK / "models" / "block" / "betalook_empty.json", {})

    for block in hide_blocks:
        # Wariant "" lapie kazdy stan bloku niezaleznie od jego wlasciwosci,
        # wiec nie musimy znac schematu blockstate'a.
        write(PACK / "blockstates" / f"{block}.json",
              {"variants": {"": {"model": EMPTY_MODEL}}})

    for block, beta in sorted(substitute.items()):
        write(PACK / "blockstates" / f"{block}.json", blockstate(block, beta))

    for item in hide_items:
        if item_defs:
            # Celowo "minecraft:model" wskazujacy na pusty model, a nie typ
            # "minecraft:empty": model jest formatem, ktory na pewno istnieje
            # w tej wersji, a pusty model i tak daje zero geometrii.
            write(PACK / "items" / f"{item}.json",
                  {"model": {"type": "minecraft:model", "model": EMPTY_MODEL}})
        else:
            write(PACK / "models" / "item" / f"{item}.json", {})

    print(f"\nZapisano do {PACK}")
    print("Uwaga: ukryte bloki nadal blokuja ruch -- to tylko warstwa wizualna.")


if __name__ == "__main__":
    main()
