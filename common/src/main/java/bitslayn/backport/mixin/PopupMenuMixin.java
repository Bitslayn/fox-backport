package bitslayn.backport.mixin;

import bitslayn.backport.availability.ItemCompat;
import bitslayn.backport.availability.VersionCompat;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.avatar.Badges;
import org.figuramc.figura.config.Configs;
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
import org.figuramc.figura.utils.ui.UIHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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

    @Shadow
    @Final
    private static int LENGTH;

    @Shadow
    @Final
    private static FiguraIdentifier ICONS;

    @Shadow
    private static int index;

    @Shadow
    @Final
    private static List<Pair<Component, Consumer<UUID>>> BUTTONS;

    @Shadow
    @Final
    private static FiguraIdentifier BACKGROUND;

    @Shadow
    @Final
    private static MutableComponent ERROR_WARN;

    @Shadow
    @Final
    private static MutableComponent VERSION_WARN;

    @Shadow
    @Final
    private static MutableComponent PERMISSION_WARN;

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

    /**
     * @author penguinencounter
     * @reason it's just so much easier than the alternative of 9 separate wrappers to prevent it from dying
     */
    @Overwrite
    public static void render(GuiGraphics gui) {
        if (!isEnabled()) return;

        Minecraft minecraft = Minecraft.getInstance();

        if (entity != null) {
            id = entity.getUUID();
            if (minecraft.player == null || (entity.isInvisibleTo(minecraft.player) && entity != minecraft.player)) {
                entity = null;
                id = null;
                return;
            }
        } else if (skull != null) {
            GameProfile profile = ItemCompat.getOwnerProfile(skull);
            id = profile != null ? profile.getId() : null;
            if (id == null || skull.isRemoved() || AvatarManager.getAvatarForPlayer(id) == null) {
                skull = null;
                id = null;
                return;
            }
        } else {
            id = null;
            return;
        }

        RenderSystem.disableDepthTest();
        PoseStack pose = gui.pose();
        pose.pushPose();

        // world to screen space
        FiguraVec4 vec;
        if (entity != null) {
            FiguraVec3 worldPos = FiguraVec3.fromVec3(entity.getPosition(VersionCompat.getFrameTime(minecraft)));
            worldPos.add(0f, entity.getBbHeight() + 0.1f, 0f);
            vec = MathUtils.worldToScreenSpace(worldPos);
        } else {
            FiguraVec3 blockPos = FiguraVec3.fromBlockPos(skull.getBlockPos());
            blockPos.add(0.5, 0.6, 0.5);
            vec = MathUtils.worldToScreenSpace(blockPos);
        }

        if (vec.z < 1) return; // too close

        Window window = minecraft.getWindow();
        double w = window.getGuiScaledWidth();
        double h = window.getGuiScaledHeight();
        double s = Configs.POPUP_SCALE.value * Math.max(Math.min(window.getHeight() * 0.035 / vec.w * (1 / window.getGuiScale()), Configs.POPUP_MAX_SIZE.value), Configs.POPUP_MIN_SIZE.value);

        pose.translate((vec.x + 1) / 2 * w, (vec.y + 1) / 2 * h, -100);
        pose.scale((float) (s * 0.5), (float) (s * 0.5), 1);

        // background
        int width = LENGTH * 18;

        UIHelper.enableBlend();
        int frame = Configs.REDUCED_MOTION.value ? 0 : (int) ((FiguraMod.ticks / 5f) % 4);
        gui.blit(BACKGROUND, width / -2, -24, width, 26, 0, frame * 26, width, 26, width, 104);

        // icons
        pose.translate(0f, 0f, -2f);
        UIHelper.enableBlend();
        for (int i = 0; i < LENGTH; i++)
            gui.blit(ICONS, width / -2 + (18 * i), -24, 18, 18, 18 * i, i == index ? 18 : 0, 18, 18, width, 36);

        // texts
        Font font = minecraft.font;

        Component title = BUTTONS.get(index).getFirst();

        PermissionPack tc = PermissionManager.get(id);
        MutableComponent permissionName = tc.getCategoryName().append(tc.hasChanges() ? "*" : "");

        Avatar avatar = AvatarManager.getAvatarForPlayer(id);
        MutableComponent name = avatar != null ? Component.literal(avatar.entityName) : entity.getName().copy();

        boolean error = false;
        boolean version = false;
        boolean noPermissions = false;

        Component badges = Badges.fetchBadges(id);
        if (!badges.getString().isEmpty())
            name.append(" ").append(badges);

        if (avatar != null) {
            error = avatar.scriptError;
            version = avatar.versionStatus > 0;
            noPermissions = !avatar.noPermissions.isEmpty();
        }

        // render texts
        UIHelper.renderOutlineText(gui, font, name, -font.width(name) / 2, -36, 0xFFFFFF, 0x202020);

        pose.scale(0.5f, 0.5f, 0.5f);
        pose.translate(0f, 0f, -1f);

        UIHelper.renderOutlineText(gui, font, permissionName, -font.width(permissionName) / 2, -54, 0xFFFFFF, 0x202020);
        gui.drawString(font, title, -width + 4, -12, 0xFFFFFF);

        if (error)
            UIHelper.renderOutlineText(gui, font, ERROR_WARN, -font.width(ERROR_WARN) / 2, 0, 0xFFFFFF, 0x202020);
        if (version)
            UIHelper.renderOutlineText(gui, font, VERSION_WARN, -font.width(VERSION_WARN) / 2, error ? font.lineHeight : 0, 0xFFFFFF, 0x202020);
        if (noPermissions)
            UIHelper.renderOutlineText(gui, font, PERMISSION_WARN, -font.width(PERMISSION_WARN) / 2, (error ? font.lineHeight : 0) + (version ? font.lineHeight : 0), 0xFFFFFF, 0x202020);

        // finish rendering
        pose.popPose();
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
