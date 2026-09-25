package com.betalook.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.betalook.client.anim.BetaBipedAnimation;
import com.betalook.config.BetaConfig;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

/**
 * Nadpisuje poze humanoida po tym, jak vanilla policzy swoja.
 *
 * Kasuje wszystko, co doszlo po becie: elytre, plywanie, czolganie, tarcze,
 * riptide i osobne krzywe dla lewej/prawej reki. Beta miala jedna krzywa
 * cosinusowa na konczyne i nic poza tym -- stad jej sztywny, "drewniany" chod.
 */
@Mixin(HumanoidModel.class)
public abstract class HumanoidModelMixin {

    @Shadow public ModelPart head;
    @Shadow public ModelPart body;
    @Shadow public ModelPart rightArm;
    @Shadow public ModelPart leftArm;
    @Shadow public ModelPart rightLeg;
    @Shadow public ModelPart leftLeg;

    @Inject(method = "setupAnim", at = @At("RETURN"))
    private void betalook$betaPose(HumanoidRenderState state, CallbackInfo ci) {
        if (!BetaConfig.betaAnimations) {
            return;
        }

        var parts = new BetaBipedAnimation.Parts(
                this.head, this.body, this.rightArm, this.leftArm,
                this.rightLeg, this.leftLeg);

        BetaBipedAnimation.apply(parts,
                state.walkAnimationPos, state.walkAnimationSpeed, state.ageInTicks,
                state.yRot, state.xRot,
                state.isPassenger, state.isCrouching);
        BetaBipedAnimation.applySwing(parts, state.attackTime);
    }
}
