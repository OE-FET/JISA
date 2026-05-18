package jisa.devices.camera.frame;

import io.jhdf.api.WritableDataset;
import io.jhdf.api.WritableGroup;

import java.util.Arrays;

public interface LongFrame<F extends LongFrame> extends Frame<Long, F, long[][]> {

    long value(int x, int y);

    @Deprecated
    default Long get(int x, int y) {
        return value(x, y);
    }

    default long[][] image() {

        long[][] image = new long[getHeight()][getWidth()];

        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                image[y][x] = value(x, y);
            }
        }

        return image;

    }

    default long[][] getImageArray() {
        return image();
    }

    default WritableDataset writeToHDF(WritableGroup group, String name) {
        return group.putDataset(name, image());
    }

    @Deprecated
    default Long[][] getImage() {

        Long[][] image = new Long[getHeight()][getWidth()];

        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                image[y][x] = value(x, y);
            }
        }

        return image;

    }

    long[] data();

    @Deprecated
    default Long[] getData() {
        return Arrays.stream(data()).boxed().toArray(Long[]::new);
    }

}
