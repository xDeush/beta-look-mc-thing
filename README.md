# BetaLook

Sprawia, ze wspolczesny Minecraft wyglada jak **Beta 1.7.3**.

Projekt sklada sie z trzech warstw, bo samym resourcepackiem tego nie da sie zrobic:

| Warstwa | Co robi | Czemu nie da sie inaczej |
|---|---|---|
| **Mod (Fabric)** | mgla, silnik swiatla, animacje, ukrywanie tresci po-betowych | resourcepack nie ma dostepu do lightmapy ani do krzywych animacji |
| **Resourcepack** | tekstury z bety, ujednolicenie drewna | tekstury to czysty resourcepack |
| **(opcjonalnie) shadery** | tylko jesli grasz z Iris/Sodium | w vanilli silnik swiatla nadpisuje mod, shader jest niepotrzebny |

## Stan projektu

Szkielet jest kompletny i spojny, ale **nie jest jeszcze skompilowany wzgledem 26.2** --
wersje w `gradle.properties` sa placeholderami, a nazwy klas/metod w mixinach trzeba
zweryfikowac wzgledem faktycznych mappingow yarn dla tej wersji. Patrz `docs/TODO.md`.

## Co juz jest

- `registry/BetaContent` -- pelne listy blokow, itemow i encji z b1.7.3 + mapa ujednolicania drewna
- `client/fog/BetaFog` -- mgla liniowa od 25% render distance, void fog ponizej y=20
- `client/light/BetaLightmap` -- oryginalny wzor na teksture swiatla (cieple swiatlo pochodni)
- `client/anim/BetaBipedAnimation` -- poza humanoida 1:1 z `ModelBiped` z bety
- `client/anim/BetaViewBob` -- bujanie kamery i zamach reka wg bety
- `client/render/PostBetaVisibility` -- jedno miejsce decydujace, co sie nie rysuje
- 6 mixinow spinajacych to z klientem
- `config/BetaConfig` -- kazdy modul osobno wylaczalny (`config/betalook.properties`)
- `tools/` -- skrypty do resourcepacka

## Tekstury

**Repo nie zawiera tekstur Mojanga i nie bedzie.** Wypakuj je z wlasnej kopii jara bety:

```bash
pip install pillow
python3 tools/extract_beta_textures.py ~/.minecraft/versions/b1.7.3/b1.7.3.jar
python3 tools/gen_wood_overrides.py
```

Szczegoly: [`docs/RESOURCEPACK.md`](docs/RESOURCEPACK.md).

## Build

```bash
./gradlew build      # -> build/libs/betalook-0.1.0.jar
./gradlew runClient  # test
```

## Ukrywanie tresci po-betowej

Domyslnie bloki, moby i itemy spoza bety **nie sa renderowane**, ale nadal istnieja:
kolizje, redstone, AI i logika serwera dzialaja normalnie. Niewidzialny blok wciaz
blokuje ruch -- to swiadoma decyzja, zeby swiat nie stal sie niegrywalny.

Zmiana w `config/betalook.properties`:

```properties
hide_post_beta_blocks=true
hide_post_beta_entities=true
hide_post_beta_items=true
```
