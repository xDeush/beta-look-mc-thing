package com.betalook.client.anim;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

/**
 * Bujanie kamery i zamach reka wg bety.
 *
 * Roznica wzgledem wspolczesnego klienta jest niewielka liczbowo, ale
 * odczuwalna: beta ma wieksza amplitude boczna i inne przesuniecie fazy
 * przy pochyleniu (0.2 rad), przez co chod "kolysze" mocniej.
 */
public final class BetaViewBob {
    private BetaViewBob() {}

    public static void bobView(MatrixStack matrices, float walkDistance, float bobAmount,
                               float bobPitch) {
        float phase = -walkDistance * MathHelper.PI;

        matrices.translate(
                MathHelper.sin(phase) * bobAmount * 0.5F,
                -Math.abs(MathHelper.cos(phase) * bobAmount),
                0.0F);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(
                MathHelper.sin(phase) * bobAmount * 3.0F));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(
                Math.abs(MathHelper.cos(phase - 0.2F) * bobAmount) * 5.0F));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(bobPitch));
    }

    /** Ruch trzymanego przedmiotu przy uderzeniu -- obwiednia sqrt z bety. */
    public static void swingItem(MatrixStack matrices, float swingProgress) {
        float root = MathHelper.sqrt(swingProgress);
        float lift = MathHelper.sin(root * MathHelper.PI);

        matrices.translate(
                -MathHelper.sin(root * MathHelper.PI * 2.0F) * 0.4F,
                MathHelper.sin(root * MathHelper.PI * 2.0F) * 0.2F,
                -lift * 0.2F);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(
                MathHelper.sin(root * MathHelper.PI) * -20.0F));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(
                MathHelper.sin(root * MathHelper.PI) * -20.0F));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(
                MathHelper.sin(root * MathHelper.PI) * -80.0F));
    }
}
