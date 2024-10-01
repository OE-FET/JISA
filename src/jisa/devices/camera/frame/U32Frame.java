package jisa.devices.camera.frame;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

public class U32Frame implements Frame.UIntFrame<U32Frame> {

    protected final int[] data;
    protected final int   width;
    protected final int   height;

    protected long timestamp;

    public U32Frame(int[] data, int width, int height, long timestamp) {

        this.data      = data;
        this.width     = width;
        this.height    = height;
        this.timestamp = timestamp;

    }

    public U32Frame(int[] data, int width, int height) {
        this(data, width, height, System.nanoTime());
    }

    public U32Frame(Integer[] data, int width, int height, long timestamp) {

        this.data      = Arrays.stream(data).mapToInt(Integer::intValue).toArray();
        this.width     = width;
        this.height    = height;
        this.timestamp = timestamp;

    }

    public U32Frame(Integer[] data, int width, int height) {
        this(data, width, height, System.nanoTime());
    }

    @Override
    public U32Frame copy() {
        return new U32Frame(data.clone(), width, height, timestamp);
    }

    @Override
    public void copyFrom(U32Frame otherFrame) {
        System.arraycopy(otherFrame.data, 0, data, 0, data.length);
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
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int size() {
        return data.length;
    }

    @Override
    public void writeToStream(OutputStream stream) throws IOException {

        for (int value : data) {

            stream.write((byte) (value & 0xFF));
            stream.write((byte) ((value >> 8) & 0xFF));
            stream.write((byte) ((value >> 16) & 0xFF));
            stream.write((byte) ((value >> 24) & 0xFF));

        }

    }

    @Override
    public U32Frame subFrame(int x, int y, int width, int height) {

        int[] subData = new int[width * height];

        for (int i = x; i < x + width; i++) {
            for (int j = y; j < y + height; j++) {
                subData[j * width + i] = signed(i, j);
            }
        }

        return new U32Frame(subData, width, height, timestamp);

    }

    @Override
    public int signed(int x, int y) {
        return data[y * width + x];
    }

    @Override
    public long[][] image() {

        long[][] image = new long[width][height];

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                image[i][j] = value(i, j);
            }
        }

        return image;

    }

    @Override
    public long[] data() {
        return Arrays.stream(data).mapToLong(Integer::toUnsignedLong).toArray();
    }

    public int[] array() {
        return data;
    }

}
