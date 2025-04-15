package jisa.devices.camera.frame;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

public class U16RGBFrame implements Frame<U16RGB, U16RGBFrame> {

    private final long[] argb;
    private final int    width;
    private final int    height;
    private       long   timestamp;

    public U16RGBFrame(char[] red, char[] green, char[] blue, int width, int height, long timestamp) {

        this.width     = width;
        this.height    = height;
        this.timestamp = timestamp;
        this.argb      = IntStream.range(0, red.length).mapToLong(i -> ((long) 255 << 48) | ((long) red[i] << 32) | ((long) green[i] << 16) | (blue[i])).toArray();

    }

    public U16RGBFrame(long[] argb, int width, int height, long timestamp) {

        this.argb      = argb;
        this.width     = width;
        this.height    = height;
        this.timestamp = timestamp;

    }

    @Override
    public U16RGBFrame copy() {
        return new U16RGBFrame(argb.clone(), width, height, timestamp);
    }

    @Override
    public void copyFrom(U16RGBFrame otherFrame) {

        System.arraycopy(otherFrame.argb, 0, argb, 0, argb.length);
        this.timestamp = otherFrame.timestamp;

    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public U16RGB get(int x, int y) {
        return new U16RGB(argb[y * width + x]);
    }

    public char getRedChar(int x, int y) {
        return (char) ((argb[y * width + x] >> 32) & 0xFFFF);
    }

    public char getGreenChar(int x, int y) {
        return (char) ((argb[y * width + x] >> 16) & 0xFFFF);
    }

    public char getBlueChar(int x, int y) {
        return (char) ((argb[y * width + x]) & 0xFF);
    }

    public long getARGB(int x, int y) {
        return argb[y * width + x];
    }

    public short getR(int x, int y) {
        return (short) ((255 * getRedChar(x, y)) / Character.MAX_VALUE);
    }

    public short getG(int x, int y) {
        return (short) ((255 * getGreenChar(x, y)) / Character.MAX_VALUE);
    }

    public short getB(int x, int y) {
        return (short) ((255 * getBlueChar(x, y)) / Character.MAX_VALUE);
    }

    @Override
    public U16RGB[][] getImage() {

        U16RGB[][] image = new U16RGB[width][height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image[x][y] = get(x, y);
            }
        }

        return image;

    }

    public long[][] getARGBImage() {

        long[][] image = new long[width][height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image[x][y] = argb[y * width + x];
            }
        }

        return image;

    }

    @Override
    public U16RGB[] getData() {
        return LongStream.of(argb).mapToObj(U16RGB::new).toArray(U16RGB[]::new);
    }

    public long[] getARGBData() {
        return argb.clone();
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
        return width * height;
    }

    @Override
    public void writeToStream(DataOutputStream stream) throws IOException {

        for (long pixel : argb) {

            for(int i = 7; i >= 0; --i) {
                stream.write((byte)((int)(pixel & 255L)));
                pixel >>= 8;
            }

        }

    }

    @Override
    public U16RGBFrame subFrame(int x, int y, int width, int height) {

        long[] sub = new long[width * height];

        for (int xi = 0; xi < width; xi++) {
            for (int yi = 0; yi < height; yi++) {
                sub[yi * width + xi] = argb[width * (yi + y) + (xi + x)];
            }
        }

        return new U16RGBFrame(sub, width, height, timestamp);
    }
}
