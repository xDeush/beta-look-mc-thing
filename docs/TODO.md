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

## Do sprawdzenia przy pierwszym buildzie

Pelna tabela w [`SIGNATURES.md`](SIGNATURES.md). Najkrocej:

- `GameRenderer.bobView` i pola gracza `walkDist`/`walkDistO`/`bob`/`oBob`/`xRotO`
- pola `HumanoidRenderState`: `walkAnimationPos`, `walkAnimationSpeed`,
  `attackTime`, `isCrouching`, `xRot`, `yRot`, `isPassenger`

Jesli `HumanoidModelMixin` sie nie kompiluje, popraw te nazwy wg `genSources` --
reszta moda jest od nich niezalezna.

## Niebo

`BetaSky` liczy kolor nieba wzorem z b1.7.3: temperatura biomu dzielona
przez 3, przyciecie do -1..1, konwersja HSV gdzie temperatura przesuwa
odcien i nasycenie, na koniec przyciemnienie pora dnia. Przy temperaturze 0
wychodzi 127,161,255 -- ten charakterystyczny, lekko fioletowawy blekit.

Wzor jest przetestowany offline (`tools/run_tests.sh`), ale hak
`ClientLevel.getSkyColor` nie jest zweryfikowany wzgledem 26.2, wiec mixin
siedzi w configu opcjonalnym.

## Smooth lighting: czego tu NIE ma

Sprawdzilem i nie odtwarzam beta-owego AO, bo nie ma czego odtwarzac:
algorytm smooth lightingu nie zmienil sie od bety w sposob, ktory dalby
sie sensownie zrekonstruowac. Napisanie modulu "beta AO" bylby placebo.

`beta_smooth_lighting` rozwiazuje za to konkretny blad, ktory powstaje
z polaczenia moda z packiem: blok spoza bety jest niewidzialny, ale bez
tego nadal zaciemnia sasiadow. W jaskini wygladalo to jak cien rzucany
przez powietrze. `BlockModelLighterMixin` zwraca dla takich blokow
"przezroczystosc swietlna" 1.0.

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
