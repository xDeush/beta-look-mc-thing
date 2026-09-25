# Resourcepack

## Uzycie w trzech krokach

```bat
:: 1. Ukryj wszystko, czego nie bylo w becie (czyta Twoj jar 26.2)
python tools\gen_hide_pack.py "%APPDATA%\.minecraft\versions\26.2\26.2.jar"

:: 2. Ujednolic drewno -- cherry, mangrove, bamboo... beda wygladac jak dab
python tools\gen_wood_overrides.py

:: 3. (opcjonalnie) Tekstury z Twojej kopii bety
pip install pillow
python tools\extract_beta_textures.py "%APPDATA%\.minecraft\versions\b1.7.3\b1.7.3.jar"

:: 4. Spakuj
python tools\build_pack.py
```

Powstaje `build\BetaLook.zip`. Wrzuc go do
`%APPDATA%\.minecraft\resourcepacks`, wejdz w grze w
**Opcje -> Pakiety zasobow** i przesun go na prawa strone.

### Czemu skrypt do pakowania, a nie zwykly zip

Klikniecie prawym na folderze `resourcepack` i "wyslij do -> folder
skompresowany" pakuje **folder**, a nie jego zawartosc. Minecraft widzi
wtedy zipa, w ktorym na wierzchu jest katalog `resourcepack`, nie znajduje
`pack.mcmeta` i pack **w ogole nie pojawia sie na liscie**. To najczestszy
blad przy recznym pakowaniu. `build_pack.py` pakuje zawartosc.

### Kroki 1 i 2 mozna robic bez moda

Sam pack juz ukrywa bloki i itemy oraz ujednolica drewno. Mod dokłada
mgle, swiatlo, animacje i ukrywanie mobow -- czyli to, czego pack nie potrafi.

## Ukrywanie vs podstawianie

Nie wszystko, czego nie bylo w becie, mozna po prostu ukryc. Bloki tworzace
teren -- granit, dioryt, andezyt, tuff, deepslate -- wystepuja w ziemi
masowo. Ukrycie ich robi **dziury w terenie**: patrzysz przez ziemie
w mgle i swiat wyglada na zniszczony, nie na betowy.

Dlatego sa dwie sciezki:

| | Co sie dzieje | Przyklady |
|---|---|---|
| **Podstawienie** | renderuje sie jak betowy odpowiednik | granite -> stone, deepslate_iron_ore -> iron_ore, coarse_dirt -> dirt, tall_grass -> short_grass |
| **Ukrycie** | nie renderuje sie wcale | sculk, amethyst, bloki z Endu, dekoracje |

Mapa podstawien siedzi w `tools/substitutions.json` i mozesz ja dopisywac.
Format: `"blok_wspolczesny": "blok_z_bety"`.

Jesli po wlaczeniu packa widzisz dziury w ziemi, to znaczy, ze jakis blok
tworzacy teren nie ma jeszcze podstawienia. Sprawdz, co to, klawiszem F3
(patrzac na krawedz dziury) i dopisz go do tego pliku.

## Skad tekstury

Z **twojej wlasnej** kopii `b1.7.3.jar`. Mojang nie pozwala redystrybuowac swoich
assetow, wiec w repo ich nie ma i nie bedzie -- sa tylko skrypty, ktore je wyjma.

Jar bety pobierzesz przez oficjalny launcher (zakladka Installations -> wlacz
"historical versions"), albo masz go juz w `~/.minecraft/versions/b1.7.3/`.

## Uklad tekstur w becie vs dzisiaj

To jest cale sedno problemu:

| Beta 1.7.3 | Dzisiaj |
|---|---|
| `terrain.png` -- jeden atlas 256x256, 256 kafli 16x16 | osobny plik PNG na kazdy blok |
| `gui/items.png` -- jeden atlas na wszystkie itemy | osobny plik na kazdy item |
| `mob/*.png` -- jeden plik na moba | katalog na moba, czesto kilka plikow |

Dlatego `tools/extract_beta_textures.py` **tnie atlasy** wg mapy indeksow
w `tools/terrain_map.json`. Mapa pokrywa obecnie 90 kafli terrain.png.
Brakujace dopisujesz tam sam -- format to `"<indeks kafla>": "<sciezka docelowa bez .png>"`.

Indeks kafla liczysz od 0, wierszami, 16 kafli na wiersz. Kafel w 3. wierszu
i 5. kolumnie to `2*16 + 4 = 36`.

## Ujednolicenie drewna

`tools/gen_wood_overrides.py` generuje modele, ktore kieruja cherry, mangrove,
bamboo, crimson, warped, acacia, dark oak, jungle i pale oak na tekstury debu.
Dziala na poziomie resourcepacka, wiec zadziala tez bez moda.

Mod robi to samo niezaleznie (`BetaContent.unifyWood`) -- dla przypadkow,
gdzie model jest generowany w kodzie, a nie z JSON-a.

## Gdy tekstura wyglada zle albo blok znika

Mapy indeksow (`tools/terrain_map.json`, `tools/items_map.json`) sa w czesci
zgadywane. Zly numer = wyciety nie ten kafelek, albo kafelek pusty -- a pusta
tekstura znaczy w grze NIEWIDZIALNY blok.

Dwie rzeczy temu zapobiegaja:

1. Skrypt **pomija puste kafelki** i wypisuje, ktore numery sa zle.
   Zla mapa nie moze juz cicho zrobic niewidzialnego bloku.
2. Podglad z numerami:

```bash
python3 tools/index_atlas.py <jar bety>                        # terrain.png
python3 tools/index_atlas.py <jar bety> --atlas gui/items.png  # itemy
```

Powstaje `build/<nazwa>_indexed.png`: atlas powiekszony 8x, czerwone numery
kafelkow, niebieskie obecne przypisania, szachownica pod spodem pokazuje
przezroczystosc. Widzisz, ze liscie sa pod 52, a w mapie masz 66 -- poprawiasz
jedna linijke.

## Czego skrypt jeszcze nie robi

- nie obsluguje animowanych tekstur (woda i lawa w becie byly generowane
  proceduralnie w kodzie, nie jako klatki PNG)
- nie generuje `pack.png` (ikonka packa na liscie -- czysto kosmetyczne)

## pack_format

`gen_hide_pack.py` odczytuje go z `version.json` w Twoim jarze i wpisuje
do `pack.mcmeta` sam. Wczesniej byla tam liczba wpisana na sztywno, co jest
najczestsza przyczyna komunikatu "pack jest niezgodny z ta wersja" --
Mojang podnosi ten numer niemal z kazdym wydaniem.
