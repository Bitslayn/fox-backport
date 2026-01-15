package bitslayn.backport;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.figuramc.figura.gui.widgets.lists.PlayerList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public final class FoxBackportMod {
    public static final String MOD_ID = "foxbackport";
    public static final Logger LOGGER = LoggerFactory.getLogger(FoxBackportMod.class);

    public static void init() {

        bumpClasses();
    }

    @SuppressWarnings("unused")
    private static void bumpClasses() {
        // make mixin wake up and do the work ON THREAD
        Class<PlayerList> playerListClass = PlayerList.class;
    }
}
