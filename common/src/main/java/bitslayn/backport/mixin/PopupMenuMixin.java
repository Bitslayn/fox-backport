package bitslayn.backport.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.gui.FiguraToast;
import org.figuramc.figura.gui.PopupMenu;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.math.vector.FiguraVec4;
import org.figuramc.figura.permissions.PermissionManager;
import org.figuramc.figura.permissions.PermissionPack;
import org.figuramc.figura.permissions.Permissions;
import org.figuramc.figura.utils.FiguraIdentifier;
import org.figuramc.figura.utils.FiguraText;
import org.figuramc.figura.utils.MathUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static bitslayn.backport.duck.PopupMenuExt.skull;

@Mixin(PopupMenu.class)
public abstract class PopupMenuMixin implements PopupMenuAccessor {
    @Shadow(remap = false)
    public static boolean isEnabled() {
        throw new AssertionError();
    }

    @Shadow
    private static Entity entity;

    @Shadow(remap = false)
    private static UUID id;

    @Definition(id = "BUTTONS", field = "Lorg/figuramc/figura/gui/PopupMenu;BUTTONS:Ljava/util/List;", remap = false)
    @Definition(id = "of", method = "Ljava/util/List;of(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/util/List;", remap = false)
    @Expression("BUTTONS = @(of(?, ?, ?, ?))")
    @ModifyExpressionValue(method = "<clinit>", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static List<Pair<Component, Consumer<UUID>>> insertVolumeButton(List<Pair<Component, Consumer<UUID>>> original) {
        ArrayList<Pair<Component, Consumer<UUID>>> writable = new ArrayList<>(original);
        writable.add(Pair.of(
                FiguraText.of("popup_menu.change_volume"), id -> {
                    PermissionPack pack = PermissionManager.get(id);

                    // maps volume to next of 100, 50, 0, 100...
                    int volume = pack.get(Permissions.VOLUME) / 50;
                    volume = (volume + 2) % 3 * 50;

                    pack.insert(Permissions.VOLUME, volume, FiguraMod.MOD_ID);
                    PermissionManager.saveToDisk();
                    FiguraToast.sendToast(FiguraText.of("toast.volume_change"), volume + "%");
                }
        ));
        return writable;
    }

    // it's easier to target the start of the method and copy the if statement
    // than inject after the if statement
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private static void attribution(GuiGraphics gui, CallbackInfo ci) {
        if (!isEnabled()) {
            ci.cancel();
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (entity != null) {
            id = entity.getUUID();
            if (mc.player == null || (entity.isInvisibleTo(mc.player) && entity != mc.player)) {
                entity = null;
                id = null;
                ci.cancel();
            }
        } else if (skull != null) {
            GameProfile profile = skull.getOwnerProfile();
            id = profile != null ? profile.getId() : null;
            if (id == null || skull.isRemoved() || AvatarManager.getAvatarForPlayer(id) == null) {
                skull = null;
                id = null;
                ci.cancel();
            }
        } else {
            id = null;
            ci.cancel();
        }
    }

    // and now we have to skip past all the original code
    @Definition(id = "entity", field = "Lorg/figuramc/figura/gui/PopupMenu;entity:Lnet/minecraft/world/entity/Entity;")
    @Expression("entity == null")
    @ModifyExpressionValue(method = "render", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static boolean bypass1(boolean original) {
        return false;
    }

    // we already know the ID from the attribution injection, so we want to not change it here
    @Redirect(method = "render", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;getUUID()Ljava/util/UUID;"
    ))
    private static UUID bypass2(Entity instance) {
        return id;
    }


    // defuse the original conditions and code
    @Definition(id = "minecraft", local = @Local(type = Minecraft.class, name = "minecraft"))
    @Definition(id = "player", field = "Lnet/minecraft/client/Minecraft;player:Lnet/minecraft/client/player/LocalPlayer;")
    @Expression("minecraft.player == null")
    @WrapOperation(method = "render", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static boolean removeOriginalCheck1(Object left, Object right, Operation<Boolean> original) {
        return false;
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;isInvisibleTo(Lnet/minecraft/world/entity/player/Player;)Z"))
    private static boolean removeOriginalCheck2(Entity instance, Player player, Operation<Boolean> original) {
        return false;
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getPosition(F)Lnet/minecraft/world/phys/Vec3;"))
    private static Vec3 removeOriginalCheck3(Entity instance, float d0, Operation<Vec3> original) {
        return Vec3.ZERO;
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getBbHeight()F"))
    private static float removeOriginalCheck4(Entity instance, Operation<Float> original) {
        return 0f;
    }

    // rendering
    @ModifyVariable(
            name = "vec",
            method = "render",
            at = @At(
                    value = "STORE",
                    ordinal = 0
            )
    )
    private static FiguraVec4 positioning(FiguraVec4 value, @Local(name = "minecraft") Minecraft minecraft) {
        if (entity != null) {
            FiguraVec3 worldPos = FiguraVec3.fromVec3(entity.getPosition(minecraft.getFrameTime()));
            worldPos.add(0f, entity.getBbHeight() + 0.1f, 0f);
            return MathUtils.worldToScreenSpace(worldPos);
        } else {
            FiguraVec3 blockPos = FiguraVec3.fromBlockPos(skull.getBlockPos());
            blockPos.add(0.5, 0.6, 0.5);
            return MathUtils.worldToScreenSpace(blockPos);
        }
    }

    // name
    @Definition(id = "entity", field = "Lorg/figuramc/figura/gui/PopupMenu;entity:Lnet/minecraft/world/entity/Entity;")
    @Definition(id = "getName", method = "Lnet/minecraft/world/entity/Entity;getName()Lnet/minecraft/network/chat/Component;")
    @Expression("@(entity.getName()).?()")
    @WrapOperation(method = "render", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static Component alternateName(Entity instance, Operation<Component> original) {
        Avatar avatar = AvatarManager.getAvatarForPlayer(id);
        return avatar != null ? Component.literal(avatar.entityName) : original.call(instance);
    }

    // clean up
    @Inject(method = "run", at = @At("TAIL"), remap = false)
    private static void clearSkull(CallbackInfo ci) {
        skull = null;
    }

    @WrapMethod(method = "hasEntity", remap = false)
    private static boolean hasEntity(Operation<Boolean> original) {
        return original.call() || skull != null;
    }

    @Inject(method = "setEntity", at = @At("TAIL"))
    private static void setEntityInj(Entity entity, CallbackInfo ci) {
        skull = null;
    }

    @Definition(id = "BACKGROUND", field = "Lorg/figuramc/figura/gui/PopupMenu;BACKGROUND:Lorg/figuramc/figura/utils/FiguraIdentifier;")
    @Expression("BACKGROUND = @(?)")
    @ModifyExpressionValue(method = "<clinit>", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static FiguraIdentifier adjustBackground(FiguraIdentifier original) {
        return new FiguraIdentifier("textures/gui/popup_2.png");
    }

    // @Redirect throws an error, which source code says "This should never happen"
    // well it happened :shrug:
    @Definition(id = "ICONS", field = "Lorg/figuramc/figura/gui/PopupMenu;ICONS:Lorg/figuramc/figura/utils/FiguraIdentifier;")
    @Expression("ICONS = @(?)")
    @ModifyExpressionValue(method = "<clinit>", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static FiguraIdentifier adjustIcons(FiguraIdentifier original) {
        return new FiguraIdentifier("textures/gui/popup_icons_2.png");
    }
}
