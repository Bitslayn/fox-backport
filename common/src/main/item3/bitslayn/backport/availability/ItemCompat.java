package bitslayn.backport.availability;

import net.minecraft.world.level.block.entity.SkullBlockEntity;

import java.util.UUID;

public class ItemCompat {
    public static UUID getOwnerProfileId(SkullBlockEntity $this) {
        if ($this.getOwnerProfile() != null) {
            return $this.getOwnerProfile().partialProfile().id();
        }
        return null;
    }
}
