package com.betalook.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.betalook.client.anim.BetaBipedAnimation;
import com.betalook.config.BetaConfig;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;

/**
 * Pelne nadpisanie pozy humanoida. Kasuje wszystkie pozy dodane po becie
 * (elytra, plywanie, czolganie, tarcza, riptide) -- gracz i moby ruszaja sie
 * dokladnie tak jak w b1.7.3.
 */
@Mixin(BipedEntityModel.class)
public abstract class BipedEntityModelMixin {

    @Shadow public ModelPart head;
    @Shadow public ModelPart body;
    @Shadow public ModelPart rightArm;
    @Shadow public ModelPart leftArm;
    @Shadow public ModelPart rightLeg;
    @Shadow public ModelPart leftLeg;

    @Inject(method = "setAngles", at = @At("RETURN"))
    private void betalook$betaPose(net.minecraft.client.render.entity.state.BipedEntityRenderState state,
                                   CallbackInfo ci) {
        if (!BetaConfig.betaAnimations) {
            return;
        }

        var parts = new BetaBipedAnimation.Parts(
                this.head, this.body, this.rightArm, this.leftArm, this.rightLeg, this.leftLeg);

        BetaBipedAnimation.apply(parts,
                state.limbFrequency, state.limbAmplitudeMultiplier, state.age,
                state.relativeHeadYaw, state.pitch,
                state.isInPose(net.minecraft.entity.EntityPose.SITTING),
                state.isInSneakingPose,
                0, 0);
        BetaBipedAnimation.applySwing(parts, state.handSwingProgress);
    }
}
