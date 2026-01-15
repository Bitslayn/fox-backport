package bitslayn.backport.forge;

import bitslayn.backport.FoxBackportMod;
import net.minecraftforge.fml.common.Mod;

@Mod(FoxBackportMod.MOD_ID)
public final class FoxBackportForge {
    public FoxBackportForge() {
        // Run our common setup.
        FoxBackportMod.init();
    }
}
