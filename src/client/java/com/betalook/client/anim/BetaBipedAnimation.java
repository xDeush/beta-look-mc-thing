package com.betalook.client.anim;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

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
        parts.head().yRot = headYaw * Mth.DEG_TO_RAD;
        parts.head().xRot = headPitch * Mth.DEG_TO_RAD;

        parts.rightArm().xRot = Mth.cos(limbAngle * STEP_FREQ + Mth.PI)
                * ARM_AMPLITUDE * limbDistance * 0.5F;
        parts.leftArm().xRot = Mth.cos(limbAngle * STEP_FREQ)
                * ARM_AMPLITUDE * limbDistance * 0.5F;
        parts.rightArm().zRot = 0.0F;
        parts.leftArm().zRot = 0.0F;

        parts.rightLeg().xRot = Mth.cos(limbAngle * STEP_FREQ) * LEG_AMPLITUDE * limbDistance;
        parts.leftLeg().xRot = Mth.cos(limbAngle * STEP_FREQ + Mth.PI)
                * LEG_AMPLITUDE * limbDistance;
        parts.rightLeg().yRot = 0.0F;
        parts.leftLeg().yRot = 0.0F;

        if (riding) {
            parts.rightArm().xRot = -Mth.PI / 5.0F;
            parts.leftArm().xRot = -Mth.PI / 5.0F;
            parts.rightLeg().xRot = -Mth.PI * 2.0F / 5.0F;
            parts.leftLeg().xRot = -Mth.PI * 2.0F / 5.0F;
            parts.rightLeg().yRot = Mth.PI / 10.0F;
            parts.leftLeg().yRot = -Mth.PI / 10.0F;
        }

        if (rightItemPose != 0) {
            parts.rightArm().xRot = parts.rightArm().xRot * 0.5F
                    - Mth.PI / 10.0F * rightItemPose;
        }
        if (leftItemPose != 0) {
            parts.leftArm().xRot = parts.leftArm().xRot * 0.5F
                    - Mth.PI / 10.0F * leftItemPose;
        }

        parts.rightArm().yRot = 0.0F;
        parts.leftArm().yRot = 0.0F;

        // Bujanie rak w bezruchu -- charakterystyczne, wolne kolysanie bety.
        parts.rightArm().zRot += Mth.cos(age * 0.09F) * 0.05F + 0.05F;
        parts.leftArm().zRot -= Mth.cos(age * 0.09F) * 0.05F + 0.05F;
        parts.rightArm().xRot += Mth.sin(age * 0.067F) * 0.05F;
        parts.leftArm().xRot -= Mth.sin(age * 0.067F) * 0.05F;

        if (sneaking) {
            parts.body().xRot = 0.5F;
            parts.rightLeg().xRot -= 0.0F;
            parts.leftLeg().xRot -= 0.0F;
            parts.rightArm().xRot += 0.4F;
            parts.leftArm().xRot += 0.4F;
            parts.rightLeg().z = 4.0F;
            parts.leftLeg().z = 4.0F;
            parts.rightLeg().y = 9.0F;
            parts.leftLeg().y = 9.0F;
            parts.head().y = 1.0F;
        } else {
            parts.body().xRot = 0.0F;
            parts.rightLeg().z = 0.0F;
            parts.leftLeg().z = 0.0F;
            parts.rightLeg().y = 12.0F;
            parts.leftLeg().y = 12.0F;
            parts.head().y = 0.0F;
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

        float sin1 = Mth.sin(f * Mth.PI);
        float sin2 = Mth.sin(swingProgress * Mth.PI)
                * -(parts.head().xRot - 0.7F) * 0.75F;

        ModelPart arm = parts.rightArm();
        arm.xRot = (float) (arm.xRot - (sin1 * 1.2D + sin2));
        arm.yRot += parts.body().yRot * 2.0F;
        arm.zRot = Mth.sin(swingProgress * Mth.PI) * -0.4F;
    }

    /** Zestaw czesci modelu, zeby ta klasa nie zalezala od konkretnej klasy modelu. */
    public record Parts(ModelPart head, ModelPart body,
                        ModelPart rightArm, ModelPart leftArm,
                        ModelPart rightLeg, ModelPart leftLeg) {}
}
