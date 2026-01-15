package bitslayn.backport.mixin;

import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;

// lower priority = before figura
@Mixin(value = LivingEntityRenderer.class, priority = 900)
public class LivingEntityRendererMixin {
//    @Inject(method = "shouldShowName(Lnet/minecraft/world/entity/LivingEntity;)Z")
}
