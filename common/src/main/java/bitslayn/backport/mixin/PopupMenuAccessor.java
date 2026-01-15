package bitslayn.backport.mixin;

import net.minecraft.world.entity.Entity;
import org.figuramc.figura.gui.PopupMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PopupMenu.class)
public interface PopupMenuAccessor {
    @Accessor("entity")
    static void foxbackport$setEntity(Entity value) {
        throw new AssertionError();
    }
}
