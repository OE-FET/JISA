package jisa.devices.camera.frame;

import io.jhdf.api.WritableDataset;
import io.jhdf.api.WritableGroup;

import java.util.stream.IntStream;

public interface ShortFrame<F extends ShortFrame> extends Frame<Short, F, short[][]> {

    /**
     * Use the value() method instead.
     *
     * @param x X co-ordinate.
     * @param y Y co-ordinate.
     * @return Boxed pixel value (memory inefficient).
     */
    @Deprecated
    default Short get(int x, int y) {
        return value(x, y);
    }

    /**
     * Directly returns the unboxed (memory-efficient) value for the specified pixel.
     *
     * @param x X co-ordinate of pixel.
     * @param y X co-ordinate of pixel.
     * @return Unboxed value.
     */
    short value(int x, int y);

    /**
     * Returns a 2D array of unboxed pixel values (more memory efficient than the getImage() method).
     *
     * @return Unboxed array.
     */
    default short[][] image() {

        short[][] image = new short[getHeight()][getWidth()];

        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                image[y][x] = value(x, y);
            }
        }

        return image;

    }

    default short[][] getImageArray() {
        return image();
    }

    default WritableDataset writeToHDF(WritableGroup group, String name) {
        return group.putDataset(name, image());
    }

    /**
     * Use the image() method instead for a more memory-efficient implementation.
     *
     * @return Array of image in boxed values
     */
    @Deprecated
    default Short[][] getImage() {

        Short[][] image = new Short[getHeight()][getWidth()];

        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                image[y][x] = value(x, y);
            }
        }

        return image;

    }

    short[] data();

    /**
     * Use data() instead.
     *
     * @return Array of data as boxed values.
     */
    @Deprecated
    default Short[] getData() {
        short[] data = data();
        return IntStream.range(0, data.length).mapToObj(i -> data[i]).toArray(Short[]::new);
    }

    short[] array();

}
