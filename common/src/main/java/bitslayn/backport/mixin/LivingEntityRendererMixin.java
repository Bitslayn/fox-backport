package bitslayn.backport.mixin;

import bitslayn.backport.duck.PopupMenuExt;
import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = LivingEntityRenderer.class, priority = 1100)
public class LivingEntityRendererMixin {
    // wow that's a lot.
    @Definition(id = "livingEntity", local = @Local(type = LivingEntity.class, argsOnly = true))
    @Definition(id = "getUUID", method = "Lnet/minecraft/world/entity/LivingEntity;getUUID()Ljava/util/UUID;")
    @Definition(id = "equals", method = "Ljava/util/UUID;equals(Ljava/lang/Object;)Z")
    @Expression("@(livingEntity.getUUID().equals(?))")
    @TargetHandler(
            mixin = "org.figuramc.figura.mixin.render.renderers.LivingEntityRendererMixin",
            name = "shouldShowName"
    )
    @ModifyExpressionValue(
            method = "@MixinSquared:Handler",
            at = @At("MIXINEXTRAS:EXPRESSION")
    )
    public boolean showName(boolean original) {
        return original && !PopupMenuExt.isSkull();
    }
}
