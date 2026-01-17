package bitslayn.backport.availability;

import com.mojang.blaze3d.vertex.PoseStack;

public class PoseStackImpl implements IPoseStack {
    private final PoseStack inner;

    public PoseStackImpl(PoseStack inner) {
        this.inner = inner;
    }

    @Override
    public void pushPose() {
        inner.pushPose();
    }

    @Override
    public void translate(float x, float y, float z) {
        inner.translate(x, y, z);
    }

    @Override
    public void scale(float x, float y, float z) {
        inner.scale(x, y, z);
    }

    @Override
    public void popPose() {
        inner.popPose();
    }
}
