# Resourcepack

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

## Czego skrypt jeszcze nie robi

- nie tnie `gui/items.png` na pojedyncze itemy (trzeba dopisac mape jak dla terrain)
- nie obsluguje animowanych tekstur (woda/lawa w becie byly generowane
  proceduralnie w kodzie, nie jako klatki PNG)
- nie generuje `pack.png`
