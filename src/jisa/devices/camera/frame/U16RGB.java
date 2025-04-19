package jisa.devices.camera.frame;


public class U16RGB {

    private final long argb;

    public U16RGB(char red, char green, char blue) {
        argb = ((long) Character.MAX_VALUE << 48 | (long) red << 32 | (long) green << 16 | blue);
    }

    public U16RGB(long argb) {
        this.argb = argb;
    }

    public int getRed() {
        return (int) ((argb >> 32) & 0xFFFF);
    }

    public int getGreen() {
        return (int) ((argb >> 16) & 0xFFFF);
    }

    public int getBlue() {
        return (int) (argb & 0xFFFF);
    }

    public char getRedChar() {
        return (char) ((argb >> 32) & 0xFFFF);
    }

    public char getGreenChar() {
        return (char) ((argb >> 16) & 0xFFFF);
    }

    public char getBlueChar() {
        return (char) (argb & 0xFFFF);
    }

    public short getR() {
        return (short) ((255 * getRed()) / Character.MAX_VALUE);
    }

    public short getG() {
        return (short) ((255 * getGreen()) / Character.MAX_VALUE);
    }

    public short getB() {
        return (short) ((255 * getBlue()) / Character.MAX_VALUE);
    }

    public RGB toRGB() {
        return new RGB(getR(), getG(), getB());
    }

}
