#!/usr/bin/env python3
"""
Pakuje resourcepack do zipa gotowego do wrzucenia w Minecrafta.

    python3 tools/build_pack.py

Wynik: build/BetaLook.zip

Robi to skryptem, a nie "kliknij prawym -> wyslij do -> folder skompresowany",
bo to drugie pakuje FOLDER, a nie jego zawartosc. Minecraft wtedy widzi zip,
w ktorym na wierzchu jest katalog "resourcepack", nie znajduje pack.mcmeta
i pack w ogole nie pojawia sie na liscie.
"""
import pathlib
import sys
import zipfile

ROOT = pathlib.Path(__file__).resolve().parent.parent
SRC = ROOT / "resourcepack"
DEFAULT_OUT = "BetaLook.zip"


def main() -> None:
    force = "--force" in sys.argv
    overlay = "--overlay" in sys.argv

    name = DEFAULT_OUT
    if "--out" in sys.argv:
        name = sys.argv[sys.argv.index("--out") + 1]
    out = ROOT / "build" / name

    if not (SRC / "pack.mcmeta").is_file():
        sys.exit(f"Brakuje {SRC / 'pack.mcmeta'} -- uruchom najpierw gen_hide_pack.py")

    # Bez blockstates pack nie ukrywa ani nie podstawia NICZEGO -- zawiera
    # wtedy same nadpisania modeli drewna, ktore dzialaja polowicznie.
    # Wczesniej taki pack pakowal sie bez slowa i w grze wygladal na zepsuty.
    blockstates = SRC / "assets" / "minecraft" / "blockstates"
    if not force and (not blockstates.is_dir() or not any(blockstates.glob("*.json"))):
        sys.exit(
            "BLAD: brak assets/minecraft/blockstates -- pack nie ukrywalby\n"
            "      ani nie podstawial zadnego bloku.\n"
            "\n"
            "Uruchom najpierw:\n"
            "  python tools/gen_hide_pack.py <sciezka do jara Twojej wersji gry>\n"
            "\n"
            "Nie wiesz, gdzie jest jar? Zobacz, jakie masz wersje:\n"
            "  dir /b \"%APPDATA%\\.minecraft\\versions\"\n"
            "\n"
            "Jesli swiadomie chcesz pack z samymi teksturami: --force")

    textures = SRC / "assets" / "minecraft" / "textures"
    if overlay:
        # Jedyny dozwolony wyjatek: zazielenione liscie. Bez nich wisnia
        # jest szara, a pack nie ma innego sposobu, zeby to naprawic.
        allowed = {"cherry_leaves.png", "pale_oak_leaves.png"}
        extra = [f for f in textures.rglob("*") if f.is_file()
                 and f.name not in allowed] if textures.is_dir() else []
        if extra:
            sys.exit(f"BLAD: tryb nakladki, a w resourcepack/ jest "
                     f"{len(extra)} tekstur spoza wyjatkow, np.\n"
                     f"      {extra[0].name}\n"
                     "      Nakladka ma NIE zawierac tekstur, zeby pack\n"
                     "      pod spodem mogl je dostarczyc.")
        print("Tryb nakladki: tylko ukrywanie, podstawianie "
              "i zazielenione liscie.\n")
    elif not textures.is_dir():
        print("UWAGA: brak tekstur bety. Pack bedzie ukrywal nowe bloki")
        print("       i ujednolical drewno, ale tekstury zostana wspolczesne.")
        print("       Zeby to zmienic: tools/extract_beta_textures.py\n")

    out.parent.mkdir(parents=True, exist_ok=True)
    if out.exists():
        out.unlink()

    count = 0
    with zipfile.ZipFile(out, "w", zipfile.ZIP_DEFLATED) as zf:
        for path in sorted(SRC.rglob("*")):
            if path.is_file():
                # arcname wzgledem SRC -- dzieki temu pack.mcmeta laduje
                # w KORZENIU zipa, a nie w podfolderze.
                zf.write(path, path.relative_to(SRC).as_posix())
                count += 1

    size = out.stat().st_size / 1024
    print(f"Spakowano {count} plikow -> {out}  ({size:.0f} KB)")
    print()
    print("Wrzuc ten plik do:")
    print(r"  Windows:  %APPDATA%\.minecraft\resourcepacks")
    print("  Linux:    ~/.minecraft/resourcepacks")
    print("  macOS:    ~/Library/Application Support/minecraft/resourcepacks")


if __name__ == "__main__":
    main()
