package bitslayn.backport.availability;

import net.minecraft.client.Minecraft;

public class VersionCompat {
    public static float getFrameTime(Minecraft $this) {
        return $this.getFrameTime();
    }
}
