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


def candidate_roots() -> list[pathlib.Path]:
    """
    Wszystkie miejsca, gdzie moze siedziec Minecraft.

    Nie kazdy trzyma gre w .minecraft. Prism, MultiMC, Modrinth App,
    CurseForge i GDLauncher maja wlasne katalogi, a czesc z nich w ogole
    nie kopiuje jara do instancji, tylko trzyma go raz w libraries/.
    Zamiast pytac uzytkownika, ktorego launchera uzywa, sprawdzamy wszystkie.
    """
    home = pathlib.Path.home()
    roots: list[pathlib.Path] = []

    system = platform.system()
    if system == "Windows":
        appdata = os.environ.get("APPDATA")
        local = os.environ.get("LOCALAPPDATA")
        if appdata:
            base = pathlib.Path(appdata)
            roots += [base / ".minecraft", base / "PrismLauncher",
                      base / "MultiMC", base / "ModrinthApp",
                      base / "com.modrinth.theseus", base / ".technic",
                      base / "gdlauncher_next", base / "ATLauncher"]
        if local:
            roots += [pathlib.Path(local) / "Packages"]
        roots += [home / "curseforge" / "minecraft",
                  home / "Documents" / "curseforge" / "minecraft"]
    elif system == "Darwin":
        support = home / "Library" / "Application Support"
        roots += [support / "minecraft", support / "PrismLauncher",
                  support / "ModrinthApp", support / "com.modrinth.theseus"]
    else:
        roots += [home / ".minecraft",
                  home / ".local" / "share" / "PrismLauncher",
                  home / ".local" / "share" / "multimc",
                  home / ".local" / "share" / "ModrinthApp",
                  home / ".var" / "app" / "org.prismlauncher.PrismLauncher"
                  / "data" / "PrismLauncher"]

    return [r for r in roots if r.is_dir()]


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


def jar_candidates(root: pathlib.Path) -> list[pathlib.Path]:
    """Miejsca, w ktorych launchery trzymaja waniliowy jar."""
    found: list[pathlib.Path] = []

    versions = root / "versions"
    if versions.is_dir():
        for folder in versions.iterdir():
            if folder.is_dir():
                found.append(folder / f"{folder.name}.jar")

    # Prism / MultiMC / Modrinth: jeden wspolny jar, nie kopia w instancji.
    libs = root / "libraries" / "com" / "mojang" / "minecraft"
    if libs.is_dir():
        found += [j for j in libs.glob("*/*.jar") if "client" in j.name]

    # Instancje z wlasnym .minecraft w srodku.
    for pattern in ("instances/*/minecraft/versions/*/*.jar",
                    "instances/*/.minecraft/versions/*/*.jar",
                    "profiles/*/versions/*/*.jar",
                    "Instances/*/versions/*/*.jar"):
        found += list(root.glob(pattern))

    return [j for j in found if j.is_file()]


def find_game_jar(roots: list[pathlib.Path], wanted: str | None) -> pathlib.Path | None:
    candidates: list[pathlib.Path] = []
    for root in roots:
        candidates += jar_candidates(root)

    if wanted:
        named = [j for j in candidates if wanted in j.parent.name or wanted in j.name]
        candidates = named

    vanilla = [j for j in candidates if is_vanilla_jar(j)]
    if not vanilla:
        return None
    return max(vanilla, key=lambda p: version_key(p.parent.name))


def is_beta_jar(path: pathlib.Path) -> bool:
    """Jar bety poznajemy po terrain.png -- atlasie, ktory zniknal w 1.5."""
    try:
        with zipfile.ZipFile(path) as jar:
            return "terrain.png" in jar.namelist()
    except (zipfile.BadZipFile, OSError):
        return False


def find_beta_jar(roots: list[pathlib.Path], given: str | None) -> pathlib.Path | None:
    if given:
        jar = pathlib.Path(given)
        return jar if jar.is_file() else None

    for root in roots:
        for jar in jar_candidates(root):
            if is_beta_jar(jar):
                return jar
    return None


def run(script: str, *args: str) -> bool:
    print(f"\n--- {script} {' '.join(args)}")
    result = subprocess.run([sys.executable, str(TOOLS / script), *args])
    return result.returncode == 0


class Tee:
    """Pisze jednoczesnie na ekran i do pliku, zeby log byl zawsze pod reka."""

    def __init__(self, stream, path: pathlib.Path):
        self.stream = stream
        self.file = path.open("w", encoding="utf-8")

    def write(self, text: str) -> int:
        self.stream.write(text)
        self.file.write(text)
        return len(text)

    def flush(self) -> None:
        self.stream.flush()
        self.file.flush()


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("--mc-dir")
    ap.add_argument("--version")
    ap.add_argument("--beta")
    ap.add_argument("--dry-run", action="store_true")
    args = ap.parse_args()

    log_path = ROOT / "setup-log.txt"
    sys.stdout = Tee(sys.__stdout__, log_path)
    sys.stderr = sys.stdout

    if args.mc_dir:
        roots = [pathlib.Path(args.mc_dir)]
        if not roots[0].is_dir():
            sys.exit(f"Nie ma takiego katalogu: {roots[0]}")
    else:
        roots = candidate_roots()

    if not roots:
        sys.exit(
            "Nie znalazlem Minecrafta w zadnej ze standardowych lokalizacji.\n"
            "Wskaz katalog recznie:\n"
            "  python tools/setup.py --mc-dir \"<sciezka>\"")

    print("Przeszukane:")
    for r in roots:
        print(f"  {r}")

    game_jar = find_game_jar(roots, args.version)
    if game_jar is None:
        print("\nZnalezione jary (zaden nie zawiera assetow gry):")
        any_jar = False
        for root in roots:
            for jar in jar_candidates(root):
                print(f"  {jar}")
                any_jar = True
        if not any_jar:
            print("  (zadnych)")
        sys.exit(
            "\nJary modloaderow nie zawieraja assetow -- potrzebny waniliowy.\n"
            "Odpal raz czysta wersje gry w launcherze, potem powtorz.\n"
            "Albo wskaz ja wprost:  python tools/setup.py --version <nazwa>")

    # mc_dir: tam, gdzie wgrywamy pack i mod. Bierzemy katalog gry, do ktorego
    # nalezy znaleziony jar, a nie pierwszy z brzegu z listy przeszukanej.
    mc_dir = game_jar.parent.parent.parent
    if not (mc_dir / "resourcepacks").is_dir() and (roots[0] / "resourcepacks").is_dir():
        mc_dir = roots[0]
    elif not (mc_dir / "resourcepacks").is_dir():
        mc_dir = roots[0]

    print(f"\nJar gry:    {game_jar}")
    print(f"Minecraft:  {mc_dir}")

    beta_jar = find_beta_jar(roots, args.beta)
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

    print()
    print(f"Caly ten log zapisalem tez w: {log_path}")


if __name__ == "__main__":
    main()
