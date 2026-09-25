package com.betalook.client.anim;

import net.minecraft.client.model.ModelPart;
import net.minecraft.util.math.MathHelper;

/**
 * Poza humanoida dokladnie wg ModelBiped#setRotationAngles z b1.7.3.
 *
 * Czego tu swiadomie NIE MA, a jest we wspolczesnym kliencie:
 *  - pozy elytry, plywania, czolgania sie, riptide i dzierzenia tarczy,
 *  - osobnych krzywych dla lewej/prawej reki przy dwureku,
 *  - wygladzania kata ramion przy zmianie kierunku.
 * Beta miala jedna krzywa cosinusowa na konczyne i tyle.
 */
public final class BetaBipedAnimation {
    private BetaBipedAnimation() {}

    /** Czestotliwosc kroku. Ta stala nie zmienila sie od bety, ale zalezy od niej reszta. */
    public static final float STEP_FREQ = 0.6662F;

    public static final float ARM_AMPLITUDE = 2.0F;
    public static final float LEG_AMPLITUDE = 1.4F;

    /**
     * @param limbAngle    postep cyklu chodu
     * @param limbDistance amplituda ruchu (0 gdy stoi)
     * @param age          wiek encji w tickach (do bujania rak w bezruchu)
     * @param headYaw      w stopniach
     * @param headPitch    w stopniach
     */
    public static void apply(Parts parts, float limbAngle, float limbDistance, float age,
                             float headYaw, float headPitch, boolean riding, boolean sneaking,
                             int rightItemPose, int leftItemPose) {
        parts.head().yaw = headYaw * MathHelper.RADIANS_PER_DEGREE;
        parts.head().pitch = headPitch * MathHelper.RADIANS_PER_DEGREE;

        parts.rightArm().pitch = MathHelper.cos(limbAngle * STEP_FREQ + MathHelper.PI)
                * ARM_AMPLITUDE * limbDistance * 0.5F;
        parts.leftArm().pitch = MathHelper.cos(limbAngle * STEP_FREQ)
                * ARM_AMPLITUDE * limbDistance * 0.5F;
        parts.rightArm().roll = 0.0F;
        parts.leftArm().roll = 0.0F;

        parts.rightLeg().pitch = MathHelper.cos(limbAngle * STEP_FREQ) * LEG_AMPLITUDE * limbDistance;
        parts.leftLeg().pitch = MathHelper.cos(limbAngle * STEP_FREQ + MathHelper.PI)
                * LEG_AMPLITUDE * limbDistance;
        parts.rightLeg().yaw = 0.0F;
        parts.leftLeg().yaw = 0.0F;

        if (riding) {
            parts.rightArm().pitch = -MathHelper.PI / 5.0F;
            parts.leftArm().pitch = -MathHelper.PI / 5.0F;
            parts.rightLeg().pitch = -MathHelper.PI * 2.0F / 5.0F;
            parts.leftLeg().pitch = -MathHelper.PI * 2.0F / 5.0F;
            parts.rightLeg().yaw = MathHelper.PI / 10.0F;
            parts.leftLeg().yaw = -MathHelper.PI / 10.0F;
        }

        if (rightItemPose != 0) {
            parts.rightArm().pitch = parts.rightArm().pitch * 0.5F
                    - MathHelper.PI / 10.0F * rightItemPose;
        }
        if (leftItemPose != 0) {
            parts.leftArm().pitch = parts.leftArm().pitch * 0.5F
                    - MathHelper.PI / 10.0F * leftItemPose;
        }

        parts.rightArm().yaw = 0.0F;
        parts.leftArm().yaw = 0.0F;

        // Bujanie rak w bezruchu -- charakterystyczne, wolne kolysanie bety.
        parts.rightArm().roll += MathHelper.cos(age * 0.09F) * 0.05F + 0.05F;
        parts.leftArm().roll -= MathHelper.cos(age * 0.09F) * 0.05F + 0.05F;
        parts.rightArm().pitch += MathHelper.sin(age * 0.067F) * 0.05F;
        parts.leftArm().pitch -= MathHelper.sin(age * 0.067F) * 0.05F;

        if (sneaking) {
            parts.body().pitch = 0.5F;
            parts.rightLeg().pitch -= 0.0F;
            parts.leftLeg().pitch -= 0.0F;
            parts.rightArm().pitch += 0.4F;
            parts.leftArm().pitch += 0.4F;
            parts.rightLeg().pivotZ = 4.0F;
            parts.leftLeg().pivotZ = 4.0F;
            parts.rightLeg().pivotY = 9.0F;
            parts.leftLeg().pivotY = 9.0F;
            parts.head().pivotY = 1.0F;
        } else {
            parts.body().pitch = 0.0F;
            parts.rightLeg().pivotZ = 0.0F;
            parts.leftLeg().pivotZ = 0.0F;
            parts.rightLeg().pivotY = 12.0F;
            parts.leftLeg().pivotY = 12.0F;
            parts.head().pivotY = 0.0F;
        }
    }

    /**
     * Krzywa zamachu reka z bety. Wspolczesny klient uzywa innej obwiedni,
     * przez co cios wyglada na "miekszy".
     */
    public static void applySwing(Parts parts, float swingProgress) {
        if (swingProgress <= 0.0F) {
            return;
        }
        float f = 1.0F - swingProgress;
        f = f * f * f;
        f = 1.0F - f;

        float sin1 = MathHelper.sin(f * MathHelper.PI);
        float sin2 = MathHelper.sin(swingProgress * MathHelper.PI)
                * -(parts.head().pitch - 0.7F) * 0.75F;

        ModelPart arm = parts.rightArm();
        arm.pitch = (float) (arm.pitch - (sin1 * 1.2D + sin2));
        arm.yaw += parts.body().yaw * 2.0F;
        arm.roll = MathHelper.sin(swingProgress * MathHelper.PI) * -0.4F;
    }

    /** Zestaw czesci modelu, zeby ta klasa nie zalezala od konkretnej klasy modelu. */
    public record Parts(ModelPart head, ModelPart body,
                        ModelPart rightArm, ModelPart leftArm,
                        ModelPart rightLeg, ModelPart leftLeg) {}
}
