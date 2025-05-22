package jisa.devices.camera.frame;

import io.jhdf.api.WritableDataset;
import io.jhdf.api.WritableGroup;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

public class U16RGBFrame implements Frame<U16RGB, U16RGBFrame> {

    protected final long[] argb;
    protected final int    width;
    protected final int    height;
    protected       long   timestamp;

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

    public long getLongARGB(int x, int y) {
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

    public long[][] getLongARGBImage() {

        long[][] image = new long[width][height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image[x][y] = argb[y * width + x];
            }
        }

        return image;

    }

    public WritableDataset writeToHDF(WritableGroup group, String name) {
        return group.putDataset(name, getLongARGBImage());
    }

    @Override
    public U16RGB[] getData() {
        return LongStream.of(argb).mapToObj(U16RGB::new).toArray(U16RGB[]::new);
    }

    @Override
    public int getARGB(int x, int y) {
        return (255 << 24) | (getR(x, y) << 16) | (getG(x, y) << 8) | getB(x, y);
    }

    @Override
    public void readARGBData(int[] destination) {

        long v;

        for (int i = 0; i < argb.length; i++) {

            v = argb[i];
            destination[i] = (int) (0xFF << 24| (v >> 32 & 0xFF00) << 8 | (v >> 16 & 0xFF00) | (v & 0xFF00) >> 8);

        }

    }

    @Override
    public int[][] getARGBImage() {

        int[]   argbData = getARGBData();
        int[][] image    = new int[width][height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image[x][y] = argbData[y * width + x];
            }
        }

        return image;

    }

    public short[][][] getRGBImage() {

        short[][][] output = new short[width][height][3];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                output[x][y][0] = getR(x, y);
                output[x][y][1] = getG(x, y);
                output[x][y][2] = getB(x, y);
            }
        }

        return output;

    }

    public int[][][] getLongRGBImage() {

        int[][][] output = new int[width][height][3];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                output[x][y][0] = getRedChar(x, y);
                output[x][y][1] = getGreenChar(x, y);
                output[x][y][2] = getBlueChar(x, y);
            }
        }

        return output;

    }

    public long[] getLongARGBData() {
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

        stream.writeInt(width);
        stream.writeInt(height);
        stream.writeInt(Long.BYTES);
        stream.writeLong(timestamp);

        ByteBuffer buffer = ByteBuffer.allocate(argb.length * Long.BYTES);
        buffer.asLongBuffer().put(argb);

        stream.write(buffer.array());

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
