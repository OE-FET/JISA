package jisa.devices.camera.frame;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.stream.IntStream;

public class Mono32BitFrame implements Frame.IntFrame {

    protected final int[]     data;
    private final   int       width;
    private final   int       height;

    public Mono32BitFrame(int[] data, int width, int height) {

        this.data   = data;
        this.width  = width;
        this.height = height;

    }

    public Mono32BitFrame(Integer[] data, int width, int height) {

        this.data   = Arrays.stream(data).mapToInt(Integer::intValue).toArray();
        this.width  = width;
        this.height = height;

    }

    @Override
    public Mono32BitFrame copy() {
        return new Mono32BitFrame(data.clone(), width, height);
    }

    @Override
    public Integer get(int x, int y) {
        return data[y * width + x];
    }

    @Override
    public Integer[][] getImage() {

        Integer[][] image = new Integer[width][height];

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                image[i][j] = get(i, j);
            }
        }

        return image;

    }

    @Override
    public Integer[] getData() {
        return IntStream.of(data).boxed().toArray(Integer[]::new);
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
    public Mono32BitFrame subFrame(int x, int y, int width, int height) {

        int[] subData = new int[width * height];

        for (int i = x; i < x + width; i++) {
            for (int j = y; j < y + height; j++) {
                subData[j * width + i] = value(i, j);
            }
        }

        return new Mono32BitFrame(subData, width, height);

    }

    @Override
    public int value(int x, int y) {
        return data[y * width + x];
    }

    @Override
    public int[][] image() {

        int[][] image = new int[width][height];

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                image[i][j] = value(i, j);
            }
        }

        return image;

    }

    @Override
    public int[] data() {
        return data.clone();
    }

}
