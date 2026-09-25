# Co zostalo

## Zrobione przez wykrywanie w czasie dzialania

Trzy moduly blokowaly sie na nazwach pol, ktorych nie dalo sie ustalic bez
zdekompilowanych zrodel. Zamiast czekac, kazdy z nich znajduje sobie to,
czego potrzebuje, sam:

1. **Lightmapa** -- `LightmapWriter` szuka w `LightmapRenderState` tablicy
   `int[256]` (16 poziomow nieba x 16 bloku) i do niej pisze. Jasnosc nieba
   odczytuje z komorki, ktora vanilla wlasnie policzyla, wiec nie potrzebuje
   nawet API pory dnia. Gdy pola nie ma -- loguje, co w tej klasie jest,
   i wylacza sie.

2. **Bujanie kamery** -- `WalkTracker` liczy dystans marszu i amplitude
   wlasnym licznikiem z pozycji gracza, wzorem z bety. Nie dotyka pol
   vanilli w ogole, wiec nie moze sie o nie rozbic.

3. **Chmury** -- `BetaClouds` szuka pola wysokosci PO WARTOSCI (192, 128,
   127 -- wysokosci uzywane przez Mojanga) i zmienia je tylko przy
   jednoznacznym trafieniu. Dwoch kandydatow albo zero: nie rusza niczego.

Gdy ktorys zamelduje w logu, ze nie trafil, ustaw `dump_classes=true`
w `config/betalook.properties`. Mod zapisze budowe wszystkich istotnych
klas do `config/betalook-classes.txt` -- i na tej podstawie da sie dopisac
brakujacy kawalek bez zgadywania.

## Do dokonczenia

3. **Smooth lighting** -- beta liczyla AO bez interpolacji po rogach,
   ktora weszla w 1.8. Flaga `beta_smooth_lighting` juz jest w configu.

4. **Czastki** -- beta miala mniej typow i inne krzywe zaniku.

5. **Ciecie `gui/items.png`** -- `extract_beta_textures.py` tnie na razie
   tylko `terrain.png` (90 kafli zmapowanych). Format mapy taki sam,
   dopisz `tools/items_map.json`.

6. **Niebo** -- gradient nieba w becie szedl z temperatury biomu.
   `SkyRenderer.renderSunriseAndSunset(PoseStack, float, int)` potwierdzony
   w zrodlach Iris, reszta nie.

## Do sprawdzenia przy pierwszym buildzie

Pelna tabela w [`SIGNATURES.md`](SIGNATURES.md). Najkrocej:

- `GameRenderer.bobView` i pola gracza `walkDist`/`walkDistO`/`bob`/`oBob`/`xRotO`
- pola `HumanoidRenderState`: `walkAnimationPos`, `walkAnimationSpeed`,
  `attackTime`, `isCrouching`, `xRot`, `yRot`, `isPassenger`

Jesli `HumanoidModelMixin` sie nie kompiluje, popraw te nazwy wg `genSources` --
reszta moda jest od nich niezalezna.

## Decyzje, ktore warto znac

**Ukrywanie blokow i itemow robi resourcepack, nie mod.** `BlockRenderDispatcher`
i `ItemRenderer` nie istnieja w 26.x, a nowa sciezka renderu jest wolana per quad.
Pack dziala niezaleznie od renderera, wiec lapie tez Sodium. To wyszlo lepiej
niz pierwotny plan.

**Klasy matematyczne nie zaleza od Minecrafta.** `BetaLightmap`, `BetaFog`
i `BetaColors` importuja wylacznie `java.*`. Dzieki temu `tools/run_tests.sh`
dziala bez gry, bez Gradle i bez sieci -- i to wlasnie ten test wylapal,
ze pierwsza wersja sprawdzala cieplote swiatla przy poziomie, gdzie wszystkie
kanaly i tak sa nasycone.
