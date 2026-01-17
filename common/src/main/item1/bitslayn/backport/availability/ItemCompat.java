package bitslayn.backport.availability;

import net.minecraft.world.level.block.entity.SkullBlockEntity;

import java.util.UUID;

public class ItemCompat {
    public static UUID getOwnerProfileId(SkullBlockEntity $this) {
        return $this.getOwnerProfile().getId();
    }
}
