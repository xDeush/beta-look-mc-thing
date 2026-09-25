package com.betalook.client.anim;

import org.joml.Quaternionf;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.util.Mth;

/**
 * Bujanie kamery wg bety.
 *
 * Liczbowo roznica wzgledem wspolczesnego klienta jest niewielka, ale
 * odczuwalna: beta ma wieksza amplitude boczna i przesuniecie fazy o 0.2 rad
 * przy pochyleniu, przez co chod kolysze mocniej i "ciezej".
 *
 * Obroty skladamy przez JOML zamiast przez klase Axis -- JOML jest w silniku
 * na pewno (Vector4f w FogRenderer), a Axis bywa przenoszony miedzy wersjami.
 */
public final class BetaViewBob {
    private BetaViewBob() {}

    public static void bobView(PoseStack poseStack, float walkDistance, float bobAmount,
                               float bobPitch) {
        float phase = -walkDistance * Mth.PI;

        poseStack.translate(
                Mth.sin(phase) * bobAmount * 0.5F,
                -Math.abs(Mth.cos(phase) * bobAmount),
                0.0F);

        rotateZ(poseStack, Mth.sin(phase) * bobAmount * 3.0F);
        rotateX(poseStack, Math.abs(Mth.cos(phase - 0.2F) * bobAmount) * 5.0F);
        rotateX(poseStack, bobPitch);
    }

    private static void rotateX(PoseStack poseStack, float degrees) {
        poseStack.mulPose(new Quaternionf().rotateX(degrees * Mth.DEG_TO_RAD));
    }

    private static void rotateZ(PoseStack poseStack, float degrees) {
        poseStack.mulPose(new Quaternionf().rotateZ(degrees * Mth.DEG_TO_RAD));
    }
}
