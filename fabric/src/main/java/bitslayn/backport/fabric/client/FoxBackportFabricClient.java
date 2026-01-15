package bitslayn.backport.fabric.client;

import bitslayn.backport.FoxBackportMod;
import net.fabricmc.api.ClientModInitializer;

public final class FoxBackportFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FoxBackportMod.init();
    }
}
