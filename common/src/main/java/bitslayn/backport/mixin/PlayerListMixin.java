package bitslayn.backport.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.gui.widgets.lists.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

import java.util.Locale;
import java.util.UUID;

@Mixin(value = PlayerList.class, remap = false)
public class PlayerListMixin {
    @Shadow
    private String filter;

    @ModifyExpressionValue(
            method = "loadPlayers",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/String;isEmpty()Z"
            )
    )
    private boolean foxbackport$modifyFilter(boolean original) {
        return true;
    }

    // we can't 'continue' with a mixin; however, there is a convenient continue with conditional
    // just where we want it. let's use that...
    @ModifyExpressionValue(
            method = "loadPlayers",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;contains(Ljava/lang/Object;)Z"),
            slice = @Slice(
                    from = @At(
                            value = "INVOKE",
                            target = "Lorg/figuramc/figura/gui/widgets/SwitchButton;isToggled()Z"
                    ),
                    to = @At(
                            value = "INVOKE",
                            // enhanced 'for'
                            target = "Ljava/util/HashSet;iterator()Ljava/util/Iterator;"
                    )
            )
    )
    private boolean modifyContinue(boolean original,
                                   @Local(name = "avatar") Avatar avatar,
                                   @Local(name = "id") UUID id) {
        return original || (
                !avatar.entityName.toLowerCase(Locale.US).contains(filter.toLowerCase(Locale.US))
                        && !id.toString().contains(filter.toLowerCase(Locale.US))
        );
    }
}
