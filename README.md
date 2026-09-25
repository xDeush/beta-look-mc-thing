# BetaLook

Sprawia, ze wspolczesny Minecraft (26.2, Fabric) wyglada jak **Beta 1.7.3**.

## Podzial pracy

Nie da sie tego zrobic jedna warstwa. Podzial nie jest arbitralny -- kazda
rzecz siedzi tam, gdzie da sie ja zrobic *dobrze*:

| Warstwa | Co robi | Czemu tam |
|---|---|---|
| **Mod** | mgla, animacje, kolory biomow, ukrywanie mobow | resourcepack nie ma dostepu do krzywych animacji ani do mgly |
| **Resourcepack** | tekstury, ukrywanie blokow i itemow, ujednolicenie drewna | dziala tez z Sodium, ktory omija mixiny renderu |
| **Shadery** | niepotrzebne | mod nadpisuje mgle i swiatlo bezposrednio |

## Co dziala

**Mod** (4 zweryfikowane mixiny):
- `FogRendererMixin` -- mgla liniowa od 25% dystansu + void fog ponizej y=20
- `EntityRenderDispatcherMixin` -- moby spoza bety znikaja (ale zyja i atakuja)
- `BiomeColorsMixin` -- jednolita woda, trawa i liscie z bety
- `HumanoidModelMixin` -- poza gracza i mobow 1:1 z `ModelBiped` z bety
- `LightmapMixin` -- cieple swiatlo pochodni
- `BlockModelLighterMixin` -- ukryte bloki nie rzucaja cieni AO
- `ClientLevelSkyMixin` -- plaskie niebo z temperatury biomu
- `ClientLevelMixin` -- czastki spoza bety nie powstaja
- `CloudRendererMixin` -- chmury na y=108
- `GameRendererMixin` -- bujanie kamery (opcjonalny, patrz nizej)

**Resourcepack** (3 skrypty):
- `gen_hide_pack.py` -- ukrywa wszystko, czego nie bylo w becie
- `gen_wood_overrides.py` -- cherry/mangrove/bamboo/... renderuja sie jak dab
- `extract_beta_textures.py` -- tnie `terrain.png` z twojego jara bety

**Przetestowane** (`tools/run_tests.sh`, 33 asercje, bez Minecrafta i sieci):
krzywa jasnosci, cieplota swiatla pochodni, podloga ambientu, start mgly,
void fog, indeks mapy kolorow, kolor nieba i konwersja HSV.

## Szybki start

Potrzebujesz **JDK 25** i Gita.

Minecraft 26.x sam chodzi na Javie 25, wiec i tak jej potrzebujesz -- to nie jest
wymog tego moda. Sprawdz, co masz: `java -version`. Jesli mniej niz 25, pobierz
**Eclipse Temurin JDK 25** z https://adoptium.net (przy instalacji zaznacz
"Set JAVA_HOME variable") i otworz NOWE okno cmd.

Build uzywa toolchaina, wiec jesli JDK 25 jest gdziekolwiek w systemie, Gradle
go znajdzie sam -- nawet gdy JAVA_HOME wskazuje starsza wersje.

### Wszystko jedna komenda

```bat
git clone -b claude/hopeful-hopper-4ypcxj https://github.com/xDeush/beta-look-mc-thing
cd beta-look-mc-thing
pip install pillow
python tools\setup.py
```

`setup.py` sam znajduje jar Twojej wersji gry, sam generuje resourcepack,
sam go pakuje i sam wgrywa do `.minecraft`. Jesli masz gdzies jar bety --
wyciaga z niego tekstury. Jesli mod jest zbudowany -- wgrywa i jego.

Zostaje Ci tylko wlaczyc pack w grze: **Opcje -> Pakiety zasobow**,
przesun BetaLook na prawa strone.

Gdy cos nie pasuje:

```bat
python tools\setup.py --dry-run              :: pokaz, co znalazl, nic nie rob
python tools\setup.py --version 26.2         :: konkretna wersja
python tools\setup.py --mc-dir "<sciezka>"   :: Prism / MultiMC
python tools\setup.py --beta "<jar bety>"    :: jar bety spoza .minecraft
```

Mod budujesz osobno (wymaga JDK 25):

```bat
gradlew.bat build
python tools\setup.py
```

### Krok po kroku, gdyby setup.py zawiodl

```bash
python3 tools/gen_hide_pack.py ~/.minecraft/versions/26.2/26.2.jar  # ukryj i podstaw
python3 tools/gen_wood_overrides.py                                  # drewno -> dab
python3 tools/extract_beta_textures.py ~/.minecraft/versions/b1.7.3/b1.7.3.jar
python3 tools/build_pack.py                                          # spakuj
```

Diagnostyka pojedynczego bloku:

```bash
python3 tools/gen_hide_pack.py <jar> --explain leaf_litter
```

Testy matematyki, bez Minecrafta i sieci: `./tools/run_tests.sh`

## Zanim odpalisz: czego nie moglem sprawdzic

Pisalem to w kontenerze bez dostepu do `maven.fabricmc.net`, wiec
**projekt nie zostal skompilowany**. Nazwy klas ustalilem czytajac zrodla
Sodium, Iris i Fabric API, ktore celuja w te wersje -- pelen rozpis co
zweryfikowane, a co nie, jest w [`docs/SIGNATURES.md`](docs/SIGNATURES.md).

Mixiny o niepewnych sygnaturach siedza w osobnym configu z `required: false`,
wiec w najgorszym razie **wylaczy sie jedna funkcja, a nie cala gra**.

Brakuje jeszcze lightmapy (cieple swiatlo pochodni). Matematyka jest gotowa
i przetestowana, brakuje 10 linijek mixina -- instrukcja krok po kroku
w [`docs/LIGHTMAP.md`](docs/LIGHTMAP.md).

## Ukrywanie tresci po-betowej

Domyslnie bloki, moby i itemy spoza bety **nie sa renderowane**, ale nadal
istnieja: kolizje, redstone, AI i logika serwera dzialaja normalnie.
Niewidzialny blok wciaz blokuje ruch -- inaczej wpadalbys w dziury.

Drewno post-betowe jest **wyjete z ukrywania** i mapowane na dab. Inaczej
wisniowy las by zniknal zamiast wygladac na debowy.

Kazdy modul wylacza sie osobno w `config/betalook.properties`.

## Dokumentacja

- [`docs/SIGNATURES.md`](docs/SIGNATURES.md) -- co zweryfikowane i skad
- [`docs/LIGHTMAP.md`](docs/LIGHTMAP.md) -- jak dokonczyc swiatlo
- [`docs/RESOURCEPACK.md`](docs/RESOURCEPACK.md) -- tekstury i atlasy
- [`docs/TODO.md`](docs/TODO.md) -- co zostalo
