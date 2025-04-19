package jisa.devices.camera.frame;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class RGBFrame implements Frame<RGB, RGBFrame> {

    private final int[] argb;
    private final int   width;
    private final int   height;

    private long timestamp;

    public RGBFrame(RGB[] data, int width, int height, long timestamp) {

        this.width     = width;
        this.height    = height;
        this.timestamp = timestamp;
        this.argb      = Stream.of(data).mapToInt(RGB::getARGB).toArray();

    }

    public RGBFrame(RGB[] data, int width, int height) {
        this(data, width, height, System.nanoTime());
    }

    public RGBFrame(short[] red, short[] green, short[] blue, int width, int height, long timestamp) {

        this.width     = width;
        this.height    = height;
        this.argb      = IntStream.range(0, red.length).map(i -> (255 << 24) | (red[i] << 16) | (green[i] << 8) | (blue[i])).toArray();
        this.timestamp = timestamp;

    }

    public RGBFrame(short[] red, short[] green, short[] blue, int width, int height) {
        this(red, green, blue, width, height, System.nanoTime());
    }

    public RGBFrame(int[] argb, int width, int height, long timestamp) {
        this.width     = width;
        this.height    = height;
        this.timestamp = timestamp;
        this.argb      = argb;
    }

    @Override
    public RGBFrame copy() {
        return new RGBFrame(argb.clone(), width, height, timestamp);
    }

    @Override
    public void copyFrom(RGBFrame otherFrame) {
        System.arraycopy(otherFrame.argb, 0, this.argb, 0, this.argb.length);
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
    public RGB get(int x, int y) {
        return new RGB(getRed(x, y), getGreen(x, y), getBlue(x, y));
    }

    @Override
    public RGB getMax() {
        return new RGB(255, 255, 255);
    }

    public short getRed(int x, int y) {
        return (short) ((argb[y * width + x] >> 16) & 0xFF);
    }

    public short getGreen(int x, int y) {
        return (short) ((argb[y * width + x] >> 8) & 0xFF);
    }

    public short getBlue(int x, int y) {
        return (short) ((argb[y * width + x]) & 0xFF);
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

    public int[][] getARGBImage() {

        int[][] raw = new int[width][height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                raw[x][y] = argb[width * y + x];
            }
        }

        return raw;

    }

    @Override
    public RGB[] getData() {
        return IntStream.of(argb).mapToObj(RGB::new).toArray(RGB[]::new);
    }

    @Override
    public int getARGB(int x, int y) {
        return argb[y * width + x];
    }

    public int[] getARGBData() {
        return argb;
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
        return argb.length;
    }

    @Override
    public void writeToStream(DataOutputStream stream) throws IOException {

        stream.writeInt(width);
        stream.writeInt(height);
        stream.writeLong(timestamp);

        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES * argb.length);
        buffer.asIntBuffer().put(argb);

        stream.write(buffer.rewind().array());

    }

    @Override
    public RGBFrame subFrame(int x, int y, int width, int height) {

        int[] sub = new int[width * height];

        for (int xi = 0; xi < width; xi++) {
            for (int yi = 0; yi < height; yi++) {
                sub[yi * width + xi] = argb[width * (yi + y) + (xi + x)];
            }
        }

        return new RGBFrame(sub, width, height, timestamp);

    }

}
