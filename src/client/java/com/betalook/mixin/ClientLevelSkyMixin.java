package com.betalook.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.betalook.client.sky.BetaSky;
import com.betalook.config.BetaConfig;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;

/**
 * Plaskie, betowe niebo bez wspolczesnego gradientu.
 *
 * Kolor liczy BetaSky z temperatury biomu, dokladnie jak w b1.7.3.
 * Temperature bierzemy z biomu pod kamera.
 *
 * Nazwa metody getSkyColor nie jest zweryfikowana wzgledem 26.2, dlatego
 * ten mixin siedzi w configu opcjonalnym -- nietrafiony injection zostawia
 * wspolczesne niebo, zamiast wywalac gre.
 */
@Mixin(ClientLevel.class)
public abstract class ClientLevelSkyMixin {

    @Inject(method = "getSkyColor", at = @At("HEAD"), cancellable = true)
    private void betalook$betaSkyColor(Vec3 cameraPos, float partialTick,
                                       CallbackInfoReturnable<Integer> cir) {
        if (!BetaConfig.betaSky) {
            return;
        }

        ClientLevel level = (ClientLevel) (Object) this;
        float temperature = level
                .getBiome(net.minecraft.core.BlockPos.containing(cameraPos))
                .value()
                .getBaseTemperature();
        float angle = level.getTimeOfDay(partialTick);

        cir.setReturnValue(BetaSky.skyColor(temperature, angle));
    }
}
