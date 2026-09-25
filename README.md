# BetaLook

Sprawia, ze wspolczesny Minecraft wyglada jak **Beta 1.7.3**.

Projekt sklada sie z trzech warstw, bo samym resourcepackiem tego nie da sie zrobic:

| Warstwa | Co robi | Czemu nie da sie inaczej |
|---|---|---|
| **Mod (Fabric)** | mgla, silnik swiatla, animacje, ukrywanie tresci po-betowych | resourcepack nie ma dostepu do lightmapy ani do krzywych animacji |
| **Resourcepack** | tekstury z bety, ujednolicenie drewna | tekstury to czysty resourcepack |
| **(opcjonalnie) shadery** | tylko jesli grasz z Iris/Sodium | w vanilli silnik swiatla nadpisuje mod, shader jest niepotrzebny |

## Stan projektu

Kod jest napisany pod **Mojang mappings** i Fabric Loom 1.17 (Yarn nie istnieje
od 26.1 -- loom juz nie remapuje). Wersje w `gradle.properties` sa sprawdzone
na fabricmc.net.

**Nie zostal jednak skompilowany** -- pisalem go w kontenerze bez dostepu do
`maven.fabricmc.net`, wiec loom nie mial skad sciagnac Minecrafta. Sygnatury
mixinow moga wymagac drobnych poprawek; lista miejsc do sprawdzenia jest
w `docs/TODO.md`. Bledny mixin wywala sie glosno przy starcie, wiec znajdziesz
go od razu.

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

## Resourcepack: ukrywanie wszystkiego, co nowe

```bash
python3 tools/gen_hide_pack.py ~/.minecraft/versions/26.2/26.2.jar --dry-run  # podglad
python3 tools/gen_hide_pack.py ~/.minecraft/versions/26.2/26.2.jar           # generuj
```

Skrypt czyta liste blokow i itemow z twojego jara, odejmuje liste bety
(prosto z `BetaContent.java` -- jedno zrodlo prawdy) i dla reszty wypluwa
puste modele.

Drewno post-betowe jest **wyjete z ukrywania** -- cherry, mangrove, bamboo
itd. sa mapowane na dab, nie kasowane. Inaczej wisniowy las by zniknal
zamiast wygladac na debowy.

Ten pack jest tez jedynym sposobem na ukrywanie blokow **przy Sodium**,
ktory omija mixiny renderu chunkow.

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
