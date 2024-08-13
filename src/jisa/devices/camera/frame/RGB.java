package jisa.devices.camera.frame;

import javafx.scene.paint.Color;

public class RGB {

    private final short red;
    private final short green;
    private final short blue;

    public RGB(short red, short green, short blue) {
        this.red   = red;
        this.green = green;
        this.blue  = blue;
    }

    public short getRed() {
        return red;
    }

    public short getGreen() {
        return green;
    }

    public short getBlue() {
        return blue;
    }

    public Color toColour() {
        return new Color((red & 0xFF) / 255.0, (green & 0xFF) / 255.0, (blue & 0xFF) / 255.0, 1.0);
    }

}
