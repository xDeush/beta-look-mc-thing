#!/usr/bin/env python3
"""
Robi wszystko jedna komenda:

    python tools/setup.py

Sam znajduje jar Twojej wersji gry, generuje resourcepack, pakuje go
i wgrywa do Minecrafta. Jesli masz gdzies jar bety, wyciaga tez z niego
tekstury. Jesli mod jest zbudowany, wgrywa i jego.

Nic nie nadpisuje poza wlasnym plikiem BetaLook.zip w resourcepacks.
Zadnych argumentow nie trzeba -- ale mozna podac:

    --mc-dir  <sciezka>   katalog .minecraft (gdy uzywasz Prism/MultiMC)
    --version <nazwa>     konkretna wersja zamiast automatycznej
    --beta    <jar>       jar bety do wyciagniecia tekstur
    --dry-run             pokaz, co by zrobil, i nic nie zapisuj
"""
import argparse
import os
import pathlib
import platform
import re
import shutil
import subprocess
import sys
import zipfile

ROOT = pathlib.Path(__file__).resolve().parent.parent
TOOLS = ROOT / "tools"


def default_mc_dir() -> pathlib.Path | None:
    system = platform.system()
    if system == "Windows":
        appdata = os.environ.get("APPDATA")
        return pathlib.Path(appdata) / ".minecraft" if appdata else None
    if system == "Darwin":
        return pathlib.Path.home() / "Library" / "Application Support" / "minecraft"
    return pathlib.Path.home() / ".minecraft"


def version_key(name: str) -> tuple:
    """Sortowanie wersji: 26.10 jest nowsze niz 26.2, a nie odwrotnie."""
    return tuple(int(p) for p in re.findall(r"\d+", name)) or (0,)


def is_vanilla_jar(path: pathlib.Path) -> bool:
    """
    Czy to waniliowy jar z assetami.

    Jary modloaderow (fabric-loader-..., forge-...) tez leza w versions,
    ale nie zawieraja assetow gry -- sa tylko cienka warstwa startowa.
    Zamiast zgadywac po nazwie, po prostu zagladamy do srodka.
    """
    try:
        with zipfile.ZipFile(path) as jar:
            names = jar.namelist()
            return any(n.startswith("assets/minecraft/blockstates/") for n in names)
    except (zipfile.BadZipFile, OSError):
        return False


def find_game_jar(mc_dir: pathlib.Path, wanted: str | None) -> pathlib.Path | None:
    versions = mc_dir / "versions"
    if not versions.is_dir():
        return None

    if wanted:
        jar = versions / wanted / f"{wanted}.jar"
        return jar if jar.is_file() else None

    candidates = []
    for folder in versions.iterdir():
        jar = folder / f"{folder.name}.jar"
        if jar.is_file() and is_vanilla_jar(jar):
            candidates.append(jar)

    if not candidates:
        return None
    return max(candidates, key=lambda p: version_key(p.parent.name))


def find_beta_jar(mc_dir: pathlib.Path, given: str | None) -> pathlib.Path | None:
    if given:
        jar = pathlib.Path(given)
        return jar if jar.is_file() else None

    versions = mc_dir / "versions"
    if not versions.is_dir():
        return None

    for folder in sorted(versions.iterdir()):
        if not folder.name.lower().startswith(("b1.", "beta", "a1.")):
            continue
        jar = folder / f"{folder.name}.jar"
        if jar.is_file():
            return jar
    return None


def run(script: str, *args: str) -> bool:
    print(f"\n--- {script} {' '.join(args)}")
    result = subprocess.run([sys.executable, str(TOOLS / script), *args])
    return result.returncode == 0


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("--mc-dir")
    ap.add_argument("--version")
    ap.add_argument("--beta")
    ap.add_argument("--dry-run", action="store_true")
    args = ap.parse_args()

    mc_dir = pathlib.Path(args.mc_dir) if args.mc_dir else default_mc_dir()
    if mc_dir is None or not mc_dir.is_dir():
        sys.exit(
            f"Nie znalazlem katalogu Minecrafta: {mc_dir}\n"
            "Podaj go recznie:  python tools/setup.py --mc-dir \"<sciezka>\"")

    print(f"Minecraft:  {mc_dir}")

    game_jar = find_game_jar(mc_dir, args.version)
    if game_jar is None:
        available = sorted(
            (p.name for p in (mc_dir / "versions").iterdir() if p.is_dir()),
            key=version_key) if (mc_dir / "versions").is_dir() else []
        sys.exit(
            "Nie znalazlem waniliowego jara z assetami.\n"
            f"Wersje w {mc_dir / 'versions'}:\n"
            + ("\n".join(f"  {v}" for v in available) or "  (pusto)") + "\n\n"
            "Jary modloaderow nie zawieraja assetow -- potrzebny jest waniliowy.\n"
            "Odpal raz czysta wersje w launcherze, albo wskaz ja:\n"
            "  python tools/setup.py --version <nazwa>")

    print(f"Jar gry:    {game_jar}")

    beta_jar = find_beta_jar(mc_dir, args.beta)
    print(f"Jar bety:   {beta_jar if beta_jar else '(brak -- tekstury zostana wspolczesne)'}")

    if args.dry_run:
        print("\n--dry-run: nic nie zapisuje.")
        return

    if not run("gen_hide_pack.py", str(game_jar)):
        sys.exit("gen_hide_pack.py nie przeszedl -- przerywam.")
    if not run("gen_wood_overrides.py"):
        sys.exit("gen_wood_overrides.py nie przeszedl -- przerywam.")
    if beta_jar and not run("extract_beta_textures.py", str(beta_jar)):
        print("UWAGA: wyciaganie tekstur nie przeszlo, ide dalej bez nich.")
    if not run("build_pack.py"):
        sys.exit("build_pack.py nie przeszedl -- przerywam.")

    packs = mc_dir / "resourcepacks"
    packs.mkdir(parents=True, exist_ok=True)
    shutil.copy2(ROOT / "build" / "BetaLook.zip", packs / "BetaLook.zip")
    print(f"\nPack wgrany:  {packs / 'BetaLook.zip'}")

    mod_jars = sorted((ROOT / "build" / "libs").glob("betalook-*.jar")) \
        if (ROOT / "build" / "libs").is_dir() else []
    mod_jars = [j for j in mod_jars if not j.name.endswith("-sources.jar")]
    if mod_jars:
        mods = mc_dir / "mods"
        mods.mkdir(parents=True, exist_ok=True)
        shutil.copy2(mod_jars[-1], mods / mod_jars[-1].name)
        print(f"Mod wgrany:   {mods / mod_jars[-1].name}")
    else:
        print("Mod:          nie zbudowany (uruchom gradlew build) -- pomijam")

    print()
    print("Zostalo tylko wlaczyc pack w grze:")
    print("  Opcje -> Pakiety zasobow -> przesun BetaLook na prawa strone")
    if not beta_jar:
        print()
        print("Tekstur bety nie znalazlem. Odpal raz b1.7.3 w launcherze")
        print("(Installations -> wlacz 'historical versions'), potem powtorz.")


if __name__ == "__main__":
    main()
