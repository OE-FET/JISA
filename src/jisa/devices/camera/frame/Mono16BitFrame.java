package jisa.devices.camera.frame;

import java.io.IOException;
import java.io.OutputStream;

public class Mono16BitFrame implements Frame.ShortFrame<Mono16BitFrame> {

    protected final short[] data;
    protected final int     width;
    protected final int     height;

    protected long timestamp;

    public Mono16BitFrame(short[] data, int width, int height, long timestamp) {

        this.data   = data;
        this.width  = width;
        this.height = height;

        this.timestamp = timestamp;
    }

    public Mono16BitFrame(short[] data, int width, int height) {
        this(data, width, height, System.nanoTime());
    }

    public Mono16BitFrame(Short[] data, int width, int height, long timestamp) {

        this.data      = new short[data.length];
        this.width     = width;
        this.height    = height;
        this.timestamp = timestamp;

        for (int i = 0; i < data.length; i++) {
            this.data[i] = data[i];
        }

    }

    public Mono16BitFrame(Short[] data, int width, int height) {
        this(data, width, height, System.nanoTime());
    }

    @Override
    public Mono16BitFrame copy() {
        return new Mono16BitFrame(data.clone(), width, height, timestamp);
    }

    @Override
    public void copyFrom(Mono16BitFrame otherFrame) {
        System.arraycopy(otherFrame.data, 0, data, 0, data.length);
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public Short get(int x, int y) {
        return data[y * width + x];
    }

    @Override
    public short value(int x, int y) {
        return data[y * width + x];
    }

    @Override
    public short[][] image() {

        short[][] image = new short[width][height];

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                image[i][j] = value(i, j);
            }
        }

        return image;

    }

    @Override
    public short[] data() {
        return data.clone();
    }

    @Override
    public Short[][] getImage() {

        Short[][] data = new Short[width][height];

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                data[i][j] = get(i, j);
            }
        }

        return data;

    }

    @Override
    public Short[] getData() {

        Short[] boxed = new Short[data.length];

        for (int i = 0; i < data.length; i++) {
            boxed[i] = data[i];
        }

        return boxed;

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

        for (short value : data) {
            stream.write((byte) (value & 0xFF));
            stream.write((byte) ((value >> 8) & 0xFF));
        }

    }

    @Override
    public Mono16BitFrame subFrame(int x, int y, int width, int height) {

        short[] subData = new short[width * height];

        for (int j = y; j < y + height; j++) {
            for (int i = x; i < x + width; i++) {
                subData[j * width + i] = value(i, j);
            }
        }

        return new Mono16BitFrame(subData, width, height, timestamp);

    }

}
