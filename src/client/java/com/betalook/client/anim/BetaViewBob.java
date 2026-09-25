package com.betalook.client.anim;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.util.Mth;
import com.mojang.math.Axis;

/**
 * Bujanie kamery i zamach reka wg bety.
 *
 * Roznica wzgledem wspolczesnego klienta jest niewielka liczbowo, ale
 * odczuwalna: beta ma wieksza amplitude boczna i inne przesuniecie fazy
 * przy pochyleniu (0.2 rad), przez co chod "kolysze" mocniej.
 */
public final class BetaViewBob {
    private BetaViewBob() {}

    public static void bobView(PoseStack matrices, float walkDistance, float bobAmount,
                               float bobPitch) {
        float phase = -walkDistance * Mth.PI;

        matrices.translate(
                Mth.sin(phase) * bobAmount * 0.5F,
                -Math.abs(Mth.cos(phase) * bobAmount),
                0.0F);
        matrices.mulPose(Axis.POSITIVE_Z.rotationDegrees(
                Mth.sin(phase) * bobAmount * 3.0F));
        matrices.mulPose(Axis.POSITIVE_X.rotationDegrees(
                Math.abs(Mth.cos(phase - 0.2F) * bobAmount) * 5.0F));
        matrices.mulPose(Axis.POSITIVE_X.rotationDegrees(bobPitch));
    }

    /** Ruch trzymanego przedmiotu przy uderzeniu -- obwiednia sqrt z bety. */
    public static void swingItem(PoseStack matrices, float swingProgress) {
        float root = Mth.sqrt(swingProgress);
        float lift = Mth.sin(root * Mth.PI);

        matrices.translate(
                -Mth.sin(root * Mth.PI * 2.0F) * 0.4F,
                Mth.sin(root * Mth.PI * 2.0F) * 0.2F,
                -lift * 0.2F);
        matrices.mulPose(Axis.POSITIVE_Y.rotationDegrees(
                Mth.sin(root * Mth.PI) * -20.0F));
        matrices.mulPose(Axis.POSITIVE_Z.rotationDegrees(
                Mth.sin(root * Mth.PI) * -20.0F));
        matrices.mulPose(Axis.POSITIVE_X.rotationDegrees(
                Mth.sin(root * Mth.PI) * -80.0F));
    }
}
