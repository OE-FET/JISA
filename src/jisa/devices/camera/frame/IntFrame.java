package jisa.devices.camera.frame;

import io.jhdf.api.WritableDataset;
import io.jhdf.api.WritableGroup;

import java.util.Arrays;

public interface IntFrame<F extends IntFrame> extends Frame<Integer, F, int[][]> {

    int value(int x, int y);

    /**
     * Use the value() method instead.
     *
     * @param x X co-ordinate.
     * @param y Y co-ordinate.
     * @return Boxed pixel value (memory inefficient).
     */
    @Deprecated
    default Integer get(int x, int y) {
        return value(x, y);
    }

    default int[][] image() {

        int[][] image = new int[getHeight()][getWidth()];

        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                image[y][x] = value(x, y);
            }
        }

        return image;

    }

    default int[][] getImageArray() {
        return image();
    }

    /**
     * Use the image() method instead for a more memory-efficient implementation.
     *
     * @return Array of image in boxed values
     */
    @Deprecated
    default Integer[][] getImage() {

        Integer[][] image = new Integer[getHeight()][getWidth()];

        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                image[y][x] = value(x, y);
            }
        }

        return image;

    }

    default WritableDataset writeToHDF(WritableGroup group, String name) {
        return group.putDataset(name, image());
    }

    int[] data();

    /**
     * Use data() instead.
     *
     * @return Array of data as boxed values.
     */
    @Deprecated
    default Integer[] getData() {
        return Arrays.stream(data()).boxed().toArray(Integer[]::new);
    }

}
