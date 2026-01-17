package bitslayn.backport.availability;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class GraphicsCompat {
    public static void blit(GuiGraphics $this,
                            ResourceLocation resourceLocation,
                            int i,
                            int j,
                            int k,
                            int l,
                            float f,
                            float g,
                            int m,
                            int n,
                            int o,
                            int p) {
        $this.blit(resourceLocation, i, j, k, l, f, g, m, n, o, p);
    }

    public static void disableDepthTest() {
        RenderSystem.disableDepthTest();
    }

    public static IPoseStack pose(GuiGraphics gui) {
        return new PoseStackImpl(gui.pose());
    }

    public static int adjustColor(int color) {
        return color;
    }
}
