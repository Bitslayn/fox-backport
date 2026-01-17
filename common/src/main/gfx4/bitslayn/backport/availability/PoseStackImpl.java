package bitslayn.backport.availability;

import org.joml.Matrix3x2fStack;

class PoseStackImpl implements IPoseStack {
    private final Matrix3x2fStack inner;

    PoseStackImpl(Matrix3x2fStack inner) {
        this.inner = inner;
    }

    @Override
    public void pushPose() {
        inner.pushMatrix();
    }

    @Override
    public void translate(float x, float y, float z) {
        inner.translate(x, y);
    }

    @Override
    public void scale(float x, float y, float z) {
        inner.scale(x, y);
    }

    @Override
    public void popPose() {
        inner.popMatrix();
    }
}
