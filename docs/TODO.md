# Co zostalo

## Do dokonczenia

1. **Lightmapa** -- matematyka gotowa i przetestowana, brakuje mixina.
   Instrukcja: [`LIGHTMAP.md`](LIGHTMAP.md). To najbardziej widoczna
   brakujaca rzecz: bez tego swiatlo pochodni jest biale, nie pomaranczowe.

2. **Chmury** -- w becie na y=108, wolniejsze, bez cieniowania.
   `CloudRenderer` istnieje i ma `TextureData`, ale nie ustalilem, skad
   bierze wysokosc. Zacznij od `genSources` na `CloudRenderer`.

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
