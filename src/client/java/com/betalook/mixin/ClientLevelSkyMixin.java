package com.betalook.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.betalook.client.sky.BetaSky;
import com.betalook.config.BetaConfig;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/**
 * Plaskie, betowe niebo bez wspolczesnego gradientu.
 *
 * Kolor liczy BetaSky z temperatury biomu pod kamera, dokladnie jak w b1.7.3.
 *
 * Wchodzimy na RETURN i przyciemniamy betowa barwe jasnoscia, ktora vanilla
 * wlasnie policzyla. Pierwsza wersja wolala getTimeOfDay i sie nie
 * kompilowala -- tej metody juz nie ma. Odczyt jasnosci z gotowego koloru
 * daje ten sam efekt i nie zalezy od zadnego API czasu ani pogody,
 * czyli od tych czesci silnika, ktore zmieniaja sie najczesciej.
 *
 * Nazwa getSkyColor nie jest zweryfikowana wzgledem 26.2, wiec mixin siedzi
 * w configu opcjonalnym -- nietrafiony injection zostawia wspolczesne niebo.
 */
@Mixin(ClientLevel.class)
public abstract class ClientLevelSkyMixin {

    @Inject(method = "getSkyColor", at = @At("RETURN"), cancellable = true)
    private void betalook$betaSkyColor(Vec3 cameraPos, float partialTick,
                                       CallbackInfoReturnable<Integer> cir) {
        if (!BetaConfig.betaSky) {
            return;
        }

        ClientLevel level = (ClientLevel) (Object) this;
        float temperature = level
                .getBiome(BlockPos.containing(cameraPos))
                .value()
                .getBaseTemperature();

        float daylight = BetaSky.daylightFromVanillaSky(cir.getReturnValue());
        cir.setReturnValue(BetaSky.skyColorWithDaylight(temperature, daylight));
    }
}
