package bitslayn.backport.duck;

import net.minecraft.world.level.block.entity.SkullBlockEntity;

import static bitslayn.backport.mixin.PopupMenuAccessor.foxbackport$setEntity;

public class PopupMenuExt {
    public static SkullBlockEntity skull = null;

    public static void setEntity(SkullBlockEntity skull) {
        foxbackport$setEntity(null);
        PopupMenuExt.skull = skull;
    }

    public static boolean isSkull() {
        return skull != null;
    }
}
