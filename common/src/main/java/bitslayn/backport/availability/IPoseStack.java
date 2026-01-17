package bitslayn.backport.availability;

// inter-version idea of a pose stack. only contains methods we actually need.
public interface IPoseStack {
    void pushPose();
    void translate(float x, float y, float z);
    void scale(float x, float y, float z);
    void popPose();
}
