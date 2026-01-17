package bitslayn.backport.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.figuramc.figura.model.FiguraModelPart;
import org.figuramc.figura.model.PartCustomization;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = {"org.figuramc.figura.model.rendering.ImmediateAvatarRenderer", "org.figuramc.figura.model.rendering.ImmediateFiguraRenderer"})
public class ImmediateAvatarRendererMixin {
    @Shadow
    @Final
    protected PartCustomization.PartCustomizationStack customizationStack;

    // FiguraVec3 pivot = custom.getPivot().copy().add(custom.getOffsetPivot());
    @Definition(id = "getPivot", method = "Lorg/figuramc/figura/model/PartCustomization;getPivot()Lorg/figuramc/figura/math/vector/FiguraVec3;")
    @Definition(id = "add", method = "Lorg/figuramc/figura/math/vector/FiguraVec3;add(Lorg/figuramc/figura/math/vector/FiguraVec3;)Lorg/figuramc/figura/math/vector/FiguraVec3;")
    @Expression("?.getPivot().?().add(?)")
    @Inject(method = "renderPart", at = @At("MIXINEXTRAS:EXPRESSION"))
    private void preCalculateLight(FiguraModelPart part,
                                   int[] remainingComplexity,
                                   boolean prevPredicate,
                                   CallbackInfoReturnable<Boolean> cir,
                                   @Share("preLight") LocalIntRef preLight,
                                   @Share("preOverlay") LocalIntRef preOverlay) {
        PartCustomization oldPeek = customizationStack.peek();
        preLight.set(oldPeek.light != null ? oldPeek.light : LightTexture.FULL_BRIGHT);
        preOverlay.set(oldPeek.overlay != null ? oldPeek.overlay : OverlayTexture.NO_OVERLAY);
    }

    @Inject(
            method = "renderPart",
            at = @At(value = "INVOKE", target = "Ljava/util/Collection;iterator()Ljava/util/Iterator;")
    )
    private void usePreCalculatedLight(FiguraModelPart part,
                                       int[] remainingComplexity,
                                       boolean prevPredicate,
                                       CallbackInfoReturnable<Boolean> cir,
                                       @Local(name = "light") LocalIntRef light,
                                       @Local(name = "overlay") LocalIntRef overlay,
                                       @Share("preLight") LocalIntRef preLight,
                                       @Share("preOverlay") LocalIntRef preOverlay) {
        light.set(preLight.get());
        overlay.set(preOverlay.get());
    }
}
