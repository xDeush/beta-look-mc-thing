# Co zostalo do zrobienia

## Krytyczne -- bez tego sie nie zbuduje

1. **Wersje w `gradle.properties`** sa placeholderami. Wpisz faktyczne
   `minecraft_version`, `yarn_mappings`, `loader_version`, `fabric_version`
   z https://fabricmc.net/develop/ dla wersji docelowej.

2. **Nazwy klas i metod w mixinach** pisalem wg ukladu klienta, jaki znam.
   Do zweryfikowania wzgledem faktycznych mappingow 26.2:
   - `BackgroundRenderer#applyFog` -- sygnatura i klasa `Fog` zmieniaja sie czesto
   - `LightmapTextureManager#update` oraz pola `image`/`texture`/`dirty`
   - `BipedEntityModel#setAngles` -- od 1.21.2 przyjmuje `BipedEntityRenderState`,
     a nazwy pol stanu (`limbFrequency`, `relativeHeadYaw`, ...) sa ruchome
   - `GameRenderer#bobView` oraz pola `horizontalSpeed`/`strideDistance` gracza
   - `BlockRenderManager#renderBlock` -- sygnatura
   - `EntityRenderDispatcher#render` -- sygnatura

   Najszybsza droga: `./gradlew genSources` i czytanie zdekompilowanego kodu.

## Wazne -- brakujace moduly

3. **Niebo i chmury** (`beta_sky`, `beta_clouds` w configu sa, mixina nie ma).
   Beta: chmury nizej (y=108), wolniejsze, bez cieniowania; gradient nieba
   liczony z temperatury biomu; brak koloru wschodu/zachodu w formie z 1.8+.

4. **Kolory biomow** -- `BetaColors` istnieje, ale nic go jeszcze nie wola.
   Potrzebny mixin na `BiomeColors` (jednolity kolor wody!) i na tinty trawy/lisci.

5. **Smooth lighting** -- `beta_smooth_lighting` w configu, brak implementacji.
   Beta liczyla AO inaczej (bez interpolacji po rogach w formie z 1.8).

6. **Ukrywanie itemow w GUI** -- `PostBetaVisibility.shouldRender(ItemStack)`
   nie jest jeszcze nigdzie podpiete.

7. **Czastki** -- beta miala mniej typow i inne krzywe zaniku.

## Do przemyslenia

8. Ujednolicenie drewna dziala na modelach, ale **mapy kolorow** (cherry ma
   rozowe liscie przez tint, nie przez teksture) moga wymagac osobnego mixina
   na `BlockColors`.

9. Kompatybilnosc z Sodium/Iris -- Sodium ma wlasny pipeline renderu chunkow,
   wiec `BlockRenderManagerMixin` go NIE zlapie. Potrzebny osobny modul
   przez Sodium API.
