package jisa.devices.camera.frame;

import java.io.IOException;
import java.io.OutputStream;
import java.util.stream.IntStream;

public class U16BitFrame implements Frame.UShortFrame<U16BitFrame> {

    protected final short[] data;
    protected final int     width;
    protected final int     height;

    protected long timestamp;

    public U16BitFrame(short[] data, int width, int height, long timestamp) {

        this.data   = data;
        this.width  = width;
        this.height = height;

        this.timestamp = timestamp;
    }

    public U16BitFrame(short[] data, int width, int height) {
        this(data, width, height, System.nanoTime());
    }

    public U16BitFrame(Short[] data, int width, int height, long timestamp) {

        this.data      = new short[data.length];
        this.width     = width;
        this.height    = height;
        this.timestamp = timestamp;

        for (int i = 0; i < data.length; i++) {
            this.data[i] = data[i];
        }

    }

    public U16BitFrame(Short[] data, int width, int height) {
        this(data, width, height, System.nanoTime());
    }

    @Override
    public U16BitFrame copy() {
        return new U16BitFrame(data.clone(), width, height, timestamp);
    }

    @Override
    public void copyFrom(U16BitFrame otherFrame) {
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
    public short signed(int x, int y) {
        return data[y * width + x];
    }

    @Override
    public int[] data() {
        return IntStream.range(0, data.length).map(i -> Short.toUnsignedInt(data[i])).toArray();
    }

    public short[] array() {
        return data;
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
    public U16BitFrame subFrame(int x, int y, int width, int height) {

        short[] subData = new short[width * height];

        for (int j = y; j < y + height; j++) {
            for (int i = x; i < x + width; i++) {
                subData[j * width + i] = signed(i, j);
            }
        }

        return new U16BitFrame(subData, width, height, timestamp);

    }

}
