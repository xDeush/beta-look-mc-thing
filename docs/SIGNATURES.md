# Sygnatury: co zweryfikowane, co nie

Minecraft 26.x przeszedl przepisanie renderu na model extract/submit.
Znikly klasy, w ktore celowala pierwsza wersja tego moda. Ponizej stan
faktyczny, ustalony przez przeczytanie zrodel dwoch modow, ktore celuja
w te wersje: **Sodium** (`CaffeineMC/sodium`) i **Iris** (`IrisShaders/Iris`),
plus **Fabric API** (`FabricMC/fabric`).

## Klasy, ktore JUZ NIE ISTNIEJA

| Bylo | Jest teraz |
|---|---|
| `BlockRenderDispatcher` | `ModelBlockRenderer` + `BlockQuadOutput` + `BlockStateModel` |
| `ItemRenderer` | `ItemStackRenderState` + `ItemModelResolver` |
| `LightTexture` | `LightmapRenderStateExtractor` + `state.LightmapRenderState` |
| `BackgroundRenderer` | `renderer.fog.FogRenderer` + `renderer.fog.FogData` |
| `ResourceLocation` | `net.minecraft.resources.Identifier` |

Dlatego ukrywanie **blokow i itemow zrobil resourcepack, nie mod**. To nie jest
obejscie -- to lepsza decyzja: render blokow idzie teraz przez sciezke wolana
per quad, Sodium i tak ma wlasny pipeline i ominalby mixin, a pusty model
wycina geometrie juz przy bakowaniu.

## Zweryfikowane

| Co | Sygnatura | Zrodlo |
|---|---|---|
| mgla | `FogRenderer.setupFog(Camera, int, DeltaTracker, float, ClientLevel)` -> `Vector4f`, lokalna `FogData` z polami `renderDistanceStart/End`, `environmentalStart/End`, `color` | Sodium `mixin/core/render/world/FogRendererMixin` |
| encje | `EntityRenderDispatcher.submit(S, CameraRenderState, double, double, double, PoseStack, SubmitNodeCollector)`, `EntityRenderState.entityType` | Iris `mixin/entity_render_context/MixinEntityRenderDispatcher` |
| kolory | `BiomeColors.getAverageWaterColor/getAverageGrassColor/getAverageFoliageColor(BlockAndTintGetter, BlockPos)` | Sodium `DefaultColorProviders`, `FluidRendererImpl` |
| rejestry | `BuiltInRegistries.BLOCK/ITEM/ENTITY_TYPE.getKey(...)` -> `Identifier` | Iris, Fabric API |
| `Identifier` | `fromNamespaceAndPath`, `withDefaultNamespace`, `parse` | 500+ uzyc w Fabric API |
| model | `HumanoidModel`, `ModelPart`, `Model.setupAnim(S)` | Fabric API `TransformCopyingModel` |
| lightmapa | klasa `LightmapRenderStateExtractor`, metoda `extract(LightmapRenderState, float)` | Iris `MixinLightTexture` |

## NIEzweryfikowane -- sprawdz przy pierwszym buildzie

| Co | Czego nie wiem |
|---|---|
| `GameRenderer.bobView` | czy metoda nadal tak sie nazywa; pola gracza `walkDist`, `walkDistO`, `bob`, `oBob`, `xRotO` |
| `HumanoidRenderState` | potwierdzone: `ageInTicks`, `bodyRot`, `id`, `entityType`. Zakladane: `walkAnimationPos`, `walkAnimationSpeed`, `attackTime`, `isCrouching`, `xRot`, `yRot`, `isPassenger` |
| `LightmapRenderState` | pola przechowujace piksele -- patrz `docs/LIGHTMAP.md` |

Mixiny niezweryfikowane siedza w osobnym configu
`betalook.optional.mixins.json` z `required: false`, wiec nietrafiony
injection **wylacza tylko dana funkcje zamiast wywalac gre**.

Najszybsza weryfikacja u siebie: `./gradlew genSources`, potem czytanie
zdekompilowanej klasy.

## Czego nie zrobilem i dlaczego

Nie sciagnalem zadnego repo z dekompilowanym Minecraftem, choc GitHub jest
ich pelen i rozwiazaloby to wszystkie znaki zapytania powyzej. To redystrybucja
kodu Mojanga. Legalna droga do tych samych zrodel to `./gradlew genSources`
na twoim wlasnym jarze -- zajmuje minute i daje dokladnie to samo.
