package bitslayn.backport.availability;

import com.mojang.authlib.GameProfile;
import net.minecraft.world.level.block.entity.SkullBlockEntity;

public class ItemCompat {
    public static GameProfile getOwnerProfile(SkullBlockEntity $this) {
        if ($this.getOwnerProfile() != null) {
            return $this.getOwnerProfile().gameProfile();
        }
        return null;
    }
}
