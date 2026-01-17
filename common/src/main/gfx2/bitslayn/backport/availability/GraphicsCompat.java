package bitslayn.backport.availability;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class GraphicsCompat {
    public static void blit(GuiGraphics $this, ResourceLocation resourceLocation, int i, int j, int ignored, int ignored2, float f, float g, int m, int n, int o, int p) {
        $this.blit(RenderType::guiTexturedOverlay, resourceLocation, i, j, f, g, m, n, m, n, o, p);
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
