package bitslayn.backport.availability;

import com.mojang.blaze3d.opengl.GlStateManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;

import static net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED;

public class GraphicsCompat {

    public static void blit(GuiGraphics $this, ResourceLocation resourceLocation, int i, int j, int ignored, int ignored2, float f, float g, int m, int n, int o, int p) {
        $this.blit(GUI_TEXTURED, resourceLocation, i, j, f, g, m, n, m, n, o, p);
    }

    public static IPoseStack pose(GuiGraphics $this) {
        return new PoseStackImpl($this.pose());
    }

    public static void disableDepthTest() {
        GlStateManager._disableDepthTest();
    }

    public static int adjustColor(int color) {
        return (color & 0xfc000000) == 0 ? ARGB.opaque(color) : color;
    }
}
