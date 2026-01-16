package bitslayn.backport.mixin;

import bitslayn.backport.duck.PopupMenuExt;
import com.bawnorton.mixinsquared.TargetHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.gui.PopupMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// higher priority = after figura
@Mixin(value = Minecraft.class, priority = 1100)
public class MinecraftMixin {
    @Shadow
    public Entity cameraEntity;

    @SuppressWarnings("resource")
    @TargetHandler(
            mixin = "org.figuramc.figura.mixin.MinecraftMixin",
            name = "handleKeybinds"
    )
    @Inject(
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/figuramc/figura/avatar/AvatarManager;getAvatarForPlayer(Ljava/util/UUID;)Lorg/figuramc/figura/avatar/Avatar;"
            ),
            method = "@MixinSquared:Handler"
    )
    private void handleKeybinds(CallbackInfo ci, CallbackInfo ci2) {
        if (Configs.POPUP_BUTTON.keyBind.isDown()) {
            if (!PopupMenu.hasEntity()) {
                HitResult result = cameraEntity.pick(20d, 1f, false);
                if (result instanceof BlockHitResult blockHit) {
                    BlockPos pos = blockHit.getBlockPos();
                    BlockEntity block = cameraEntity.level().getBlockEntity(pos);

                    if (block instanceof SkullBlockEntity skull)
                        PopupMenuExt.setEntity(skull);
                }
            }
        }
    }
}
