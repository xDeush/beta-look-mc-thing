# Co zostalo do zrobienia

## Zalatwione w tej turze

- Migracja na **Mojang mappings** -- Yarn nie istnieje od 26.1, loom juz nie remapuje
- `splitEnvironmentSourceSets()` -- kod klienta w `src/client`, inaczej dedyk sie wywala
- Java 25, `net.fabricmc.fabric-loom` (nowy plugin, bez `mappings`)
- Kolory biomow (jednolita woda z bety) i ukrywanie itemow w swiecie
- Resourcepack ukrywajacy wszystko post-betowe (`tools/gen_hide_pack.py`)

## Krytyczne -- do sprawdzenia przy pierwszym buildzie

Nie moglem tego zweryfikowac: kontener nie ma dostepu do `maven.fabricmc.net`
ani `libraries.minecraft.net`, wiec loom nie sciagnie Minecrafta i **nic sie
tu nie skompilowalo**. Kod jest pisany pod API, ktore znam, ale sygnatury
renderu Mojang rusza czesto. Przy pierwszym `./gradlew build` sprawdz:

| Mixin | Co moze sie nie zgadzac |
|---|---|
| `FogRendererMixin` | `FogRenderer.setupFog` i rekord `FogParameters` -- zmieniane w 1.21.x kilka razy |
| `LightTextureMixin` | pola `lightPixels` / `lightTexture` / `updateLightTexture` |
| `HumanoidModelMixin` | nazwy pol w `HumanoidRenderState` (`walkAnimationPos`, `yRot`, `attackTime`) |
| `GameRendererMixin` | pola gracza `walkDist` / `walkDistO` / `bob` / `oBob` |
| `BlockRenderDispatcherMixin` | sygnatura `renderBatched` |
| `ItemRendererMixin` | sygnatura `renderStatic` |

Jak cos nie pasuje: `./gradlew genSources`, otworz zdekompilowana klase,
popraw sygnature. Mixin z bledem wywali sie **glosno** przy starcie
(`defaultRequire: 1`), wiec sie nie przeoczy.

## Wazne -- czego nadal nie ma

1. **Niebo i chmury** (`beta_sky`, `beta_clouds` sa w configu, mixina nie ma).
   Beta: chmury na y=108, wolniejsze, bez cieniowania; gradient nieba z temperatury
   biomu. Nie pisalem tego na slepo, bo `CloudRenderer` przeszedl w 1.21.x
   przepisanie i zgadywanie sygnatur nie ma sensu bez zrodel.

2. **Smooth lighting** (`beta_smooth_lighting` w configu, brak implementacji).
   Beta liczyla AO bez interpolacji po rogach, ktora weszla w 1.8.

3. **Tinty trawy i lisci** -- `BetaColors.colorIndex` istnieje, ale nic go nie wola.
   Potrzebny mixin na `BlockColors` -- inaczej wisniowe liscie beda rozowe
   przez tint, mimo ze tekstura jest debowa.

4. **Czastki** -- beta miala mniej typow i inne krzywe zaniku.

5. **Ciecie `gui/items.png`** -- `extract_beta_textures.py` tnie tylko terrain.png.

## Kompatybilnosc

**Sodium omija `BlockRenderDispatcherMixin`** -- ma wlasny pipeline chunkow.
Dlatego `tools/gen_hide_pack.py` jest wazny: pack dziala niezaleznie od renderera.
Z Sodium ukrywanie blokow zalatwia pack, a mod zajmuje sie reszta.
