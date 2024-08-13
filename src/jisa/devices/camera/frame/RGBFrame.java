package jisa.devices.camera.frame;

import java.io.IOException;
import java.io.OutputStream;
import java.util.stream.IntStream;

public class RGBFrame implements Frame<RGB> {

    private final short[] red;
    private final short[] green;
    private final short[] blue;
    private final int     width;
    private final int     height;

    public RGBFrame(RGB[] data, int width, int height) {

        this.width  = width;
        this.height = height;
        this.red    = new short[data.length];
        this.green  = new short[data.length];
        this.blue   = new short[data.length];

        for (int i = 0; i < data.length; i++) {

            this.red[i]   = data[i].getRed();
            this.green[i] = data[i].getGreen();
            this.blue[i]  = data[i].getBlue();

        }

    }

    public RGBFrame(short[] red, short[] green, short[] blue, int width, int height) {

        this.width  = width;
        this.height = height;
        this.red    = red;
        this.green  = green;
        this.blue   = blue;

    }

    @Override
    public RGBFrame copy() {
        return new RGBFrame(red.clone(), green.clone(), blue.clone(), width, height);
    }

    @Override
    public RGB get(int x, int y) {
        int i = y * width + x;
        return new RGB(red[i], green[i], blue[i]);
    }

    public short getRed(int x, int y) {
        return red[y * width + x];
    }

    public short getGreen(int x, int y) {
        return green[y * width + x];
    }

    public short getBlue(int x, int y) {
        return blue[y * width + x];
    }

    @Override
    public RGB[][] getImage() {

        RGB[][] image = new RGB[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image[x][y] = get(x, y);
            }
        }

        return image;

    }

    @Override
    public RGB[] getData() {
        return IntStream.range(0, red.length).mapToObj(i -> new RGB(red[i], green[i], blue[i])).toArray(RGB[]::new);
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int size() {
        return red.length;
    }

    @Override
    public void writeToStream(OutputStream stream) throws IOException {

        for (int i = 0; i < red.length; i++) {

            stream.write((byte) (red[i] & 0xFF));
            stream.write((byte) ((red[i] >> 8) & 0xFF));

            stream.write((byte) (green[i] & 0xFF));
            stream.write((byte) ((green[i] >> 8) & 0xFF));

            stream.write((byte) (blue[i] & 0xFF));
            stream.write((byte) ((blue[i] >> 8) & 0xFF));

        }

    }

    @Override
    public RGBFrame subFrame(int x, int y, int width, int height) {

        short[] subRed   = new short[width * height];
        short[] subGreen = new short[width * height];
        short[] subBlue  = new short[width * height];

        for (int i = x; i < x + width; i++) {

            for (int j = y; j < y + height; j++) {

                subRed[j * width + i]   = getRed(i, j);
                subGreen[j * width + i] = getGreen(i, j);
                subBlue[j * width + i]  = getBlue(i, j);

            }

        }

        return new RGBFrame(subRed, subGreen, subBlue, width, height);

    }

}
