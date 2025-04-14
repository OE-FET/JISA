package jisa.devices.camera.frame;

public class RGB {

    private final int argb;

    public RGB(byte red, byte green, byte blue) {
        this.argb = (255 << 24) | (red << 16) | (green << 8) | blue;
    }

    public RGB(short red, short green, short blue) {
        this.argb = (255 << 24) | (red << 16) | (green << 8) | blue;
    }

    public RGB(int red, int green, int blue) {
        this((short) red, (short) green, (short) blue);
    }

    public RGB(int argb) {
        this.argb = argb;
    }

    public short getRed() {
        return (short) ((argb >> 16) & 0xFF);
    }

    public short getGreen() {
        return (short) ((argb >> 8) & 0xFF);
    }

    public short getBlue() {
        return (short) (argb & 0xFF);
    }

    public int getARGB() {
        return argb;
    }

}
