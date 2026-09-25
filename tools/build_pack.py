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
import shutil
import sys
import zipfile

ROOT = pathlib.Path(__file__).resolve().parent.parent
SRC = ROOT / "resourcepack"
OUT = ROOT / "build" / "BetaLook.zip"


def main() -> None:
    if not (SRC / "pack.mcmeta").is_file():
        sys.exit(f"Brakuje {SRC / 'pack.mcmeta'} -- uruchom najpierw gen_hide_pack.py")

    textures = SRC / "assets" / "minecraft" / "textures"
    if not textures.is_dir():
        print("UWAGA: brak tekstur bety. Pack bedzie ukrywal nowe bloki")
        print("       i ujednolical drewno, ale tekstury zostana wspolczesne.")
        print("       Zeby to zmienic: tools/extract_beta_textures.py\n")

    OUT.parent.mkdir(parents=True, exist_ok=True)
    if OUT.exists():
        OUT.unlink()

    count = 0
    with zipfile.ZipFile(OUT, "w", zipfile.ZIP_DEFLATED) as zf:
        for path in sorted(SRC.rglob("*")):
            if path.is_file():
                # arcname wzgledem SRC -- dzieki temu pack.mcmeta laduje
                # w KORZENIU zipa, a nie w podfolderze.
                zf.write(path, path.relative_to(SRC).as_posix())
                count += 1

    size = OUT.stat().st_size / 1024
    print(f"Spakowano {count} plikow -> {OUT}  ({size:.0f} KB)")
    print()
    print("Wrzuc ten plik do:")
    print(r"  Windows:  %APPDATA%\.minecraft\resourcepacks")
    print("  Linux:    ~/.minecraft/resourcepacks")
    print("  macOS:    ~/Library/Application Support/minecraft/resourcepacks")


if __name__ == "__main__":
    main()
