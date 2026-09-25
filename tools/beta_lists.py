"""
Czyta listy bety prosto z BetaContent.java.

Dzieki temu jest JEDNO zrodlo prawdy: dopisujesz blok w Javie, a skrypty
resourcepacka od razu o nim wiedza. Bez tego listy rozjechalyby sie
po pierwszej poprawce.
"""
import pathlib
import re

ROOT = pathlib.Path(__file__).resolve().parent.parent
SOURCE = ROOT / "src" / "main" / "java" / "com" / "betalook" / "registry" / "BetaContent.java"


def _extract(field: str) -> set[str]:
    text = SOURCE.read_text()
    match = re.search(
        rf"Set<\w+>\s+{field}\s*=\s*ids\((.*?)\);", text, re.S)
    if not match:
        raise SystemExit(f"Nie znalazlem pola {field} w {SOURCE}")
    body = match.group(1)
    # Wytnij komentarze, potem wyciagnij literaly stringow.
    body = re.sub(r"//.*?$", "", body, flags=re.M)
    return set(re.findall(r'"([a-z0-9_]+)"', body))


def _wood_species() -> list[str]:
    text = SOURCE.read_text()
    match = re.search(r"POST_BETA_WOOD\s*=\s*\{(.*?)\};", text, re.S)
    if not match:
        raise SystemExit("Nie znalazlem POST_BETA_WOOD")
    return re.findall(r'"([a-z0-9_]+)"', match.group(1))


BETA_BLOCKS = _extract("BLOCKS")
BETA_ITEMS = _extract("ITEMS") | BETA_BLOCKS
POST_BETA_WOOD = _wood_species()

if __name__ == "__main__":
    print(f"bloki bety:  {len(BETA_BLOCKS)}")
    print(f"itemy bety:  {len(BETA_ITEMS)}")
    print(f"drewno post-beta: {POST_BETA_WOOD}")
