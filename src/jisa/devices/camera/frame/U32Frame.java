package jisa.devices.camera.frame;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.stream.IntStream;

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
    public Long getMax() {
        return Integer.toUnsignedLong(0xFFFFFFFF);
    }

    @Override
    public int getARGB(int x, int y) {
        int value = signed(x, y) >> 24;
        return (255 << 24) | (value << 16) | (value << 8) | value;
    }

    @Override
    public int[] getARGBData() {

        return IntStream.of(data).map(v -> {
            int value = v >> 24;
            return (255 << 24) | (value << 16) | (value << 8) | value;
        }).toArray();

    }

    @Override
    public int[][] getARGBImage() {

        int[]   argb  = getARGBData();
        int[][] image = new int[width][height];

        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                char value = (char) (data[j * width + i] >> 8);
                image[i][j] = (255 << 24) | value << 16 | value << 8 | value;
            }
        }

        return image;

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
    public void writeToStream(DataOutputStream stream) throws IOException {

        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES * data.length);
        buffer.asIntBuffer().rewind().put(data);
        stream.write(buffer.rewind().array());

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
