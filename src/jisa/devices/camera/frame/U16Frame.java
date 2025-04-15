package jisa.devices.camera.frame;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.stream.IntStream;

public class U16Frame implements Frame.UShortFrame<U16Frame> {

    protected final short[] data;
    protected final int     width;
    protected final int     height;

    protected long timestamp;

    public U16Frame(short[] data, int width, int height, long timestamp) {

        this.data   = data;
        this.width  = width;
        this.height = height;

        this.timestamp = timestamp;
    }

    public U16Frame(short[] data, int width, int height) {
        this(data, width, height, System.nanoTime());
    }

    public U16Frame(Short[] data, int width, int height, long timestamp) {

        this.data      = new short[data.length];
        this.width     = width;
        this.height    = height;
        this.timestamp = timestamp;

        for (int i = 0; i < data.length; i++) {
            this.data[i] = data[i];
        }

    }

    public U16Frame(Short[] data, int width, int height) {
        this(data, width, height, System.nanoTime());
    }

    @Override
    public U16Frame copy() {
        return new U16Frame(data.clone(), width, height, timestamp);
    }

    @Override
    public void copyFrom(U16Frame otherFrame) {
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
    public void writeToStream(DataOutputStream stream) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(Short.BYTES * data.length);
        buffer.asShortBuffer().rewind().put(data);
        stream.write(buffer.rewind().array());
    }

    @Override
    public U16Frame subFrame(int x, int y, int width, int height) {

        short[] subData = new short[width * height];

        for (int j = y; j < y + height; j++) {
            for (int i = x; i < x + width; i++) {
                subData[j * width + i] = signed(i, j);
            }
        }

        return new U16Frame(subData, width, height, timestamp);

    }

}
