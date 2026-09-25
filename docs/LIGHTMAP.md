# Lightmapa: ostatni brakujacy kawalek

Cieple, pomaranczowe swiatlo pochodni to najmocniej rozpoznawalna cecha bety.
**Matematyka jest gotowa i przetestowana** (`BetaLightmap`, `tools/run_tests.sh`),
brakuje tylko mixina, ktory wstrzyknie ja do gry.

## Czemu go nie ma

`LightTexture` zniklo w 26.x. Zastapil je
`net.minecraft.client.renderer.LightmapRenderStateExtractor` z metoda
`extract(LightmapRenderState, float)` -- to akurat potwierdzilem w zrodlach Iris.
Czego nie potwierdzilem: **jak `LightmapRenderState` trzyma piksele**. Bez tego
mixin nie ma gdzie zapisac wyniku, a zgadywanie nazw pol dalo by kod, ktory
sie nie kompiluje.

## Jak to dokonczyc (5 minut)

```bash
./gradlew genSources
```

Potem otworz `net/minecraft/client/renderer/state/LightmapRenderState.java`
i zobacz, w czym trzymane sa piksele (`NativeImage`? `int[]`? tekstura?).

Szkielet mixina -- uzupelnij tylko miejsce oznaczone `TODO`:

```java
package com.betalook.mixin;

import com.betalook.client.light.BetaLightmap;
import com.betalook.config.BetaConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.client.renderer.state.LightmapRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightmapRenderStateExtractor.class)
public class LightmapMixin {

    @Inject(method = "extract", at = @At("RETURN"))
    private void betalook$betaLightmap(LightmapRenderState state, float partialTick,
                                       CallbackInfo ci) {
        if (!BetaConfig.betaLighting) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        if (client.level == null) {
            return;
        }

        float skyDarken = client.level.getSkyDarken(partialTick);
        float gamma = client.options.gamma().get().floatValue();
        boolean nether = client.level.dimensionType().hasCeiling();

        for (int sky = 0; sky < 16; sky++) {
            for (int block = 0; block < 16; block++) {
                int argb = BetaLightmap.color(sky, block, skyDarken, 0.0F, gamma, nether);
                // TODO: zapisz argb do piksela (block, sky) w state.
                //       Nazwa pola/metody -- z genSources.
            }
        }
    }
}
```

Gotowy plik wrzuc do `src/client/java/com/betalook/mixin/` i dopisz
`"LightmapMixin"` do `src/client/resources/betalook.client.mixins.json`.

## Jak sprawdzic, czy dziala

Postaw pochodnie w ciemnej jaskini i odejdz kilka blokow. W polowie zasiegu
swiatla sciana ma byc **wyraznie pomaranczowa**, nie szara. Tuz przy pochodni
bedzie biala -- tak samo bylo w becie, bo wszystkie trzy kanaly dobijaja do 1.0.

Wartosci, ktore wypluwa przetestowana matematyka (swiatlo nieba = 0):

| poziom swiatla | R | G | B |
|---|---|---|---|
| 6 | 71 | 43 | 31 |
| 8 | 99 | 67 | 49 |
| 10 | 140 | 106 | 81 |
