package bitslayn.backport.neoforge;

import bitslayn.backport.FoxBackportMod;
import net.neoforged.fml.common.Mod;

@Mod(FoxBackportMod.MOD_ID)
public final class FoxBackportNeoForge {
    public FoxBackportNeoForge() {
        // Run our common setup.
        FoxBackportMod.init();
    }
}
