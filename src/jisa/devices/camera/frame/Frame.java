package jisa.devices.camera.frame;

import java.io.IOException;
import java.io.OutputStream;

public interface Frame<D> {

    /**
     * Returns a deep copy of this frame.
     *
     * @return Copy of this frame.
     */
    Frame<D> copy();

    /**
     * Returns the pixel value at the given x and y co-ordinates/indices.
     *
     * @param x X co-ordinate
     * @param y Y co-ordinate
     *
     * @return Pixel value
     */
    D get(int x, int y);

    /**
     * Returns all the pixels of this image in a 2D array.
     *
     * @return 2D array image
     */
    D[][] getImage();

    /**
     * Returns all the pixles of this image in a flat 1D array.
     *
     * @return 1D array image
     */
    D[] getData();

    /**
     * Returns the width of this image, in number of pixels.
     *
     * @return Number of pixels wide
     */
    int getWidth();

    /**
     * Returns the height of this image, in number of pixels.
     *
     * @return Number of pixels high
     */
    int getHeight();

    /**
     * Returns the total number of pixels in this image.
     *
     * @return Total number of pixels
     */
    int size();

    /**
     * Writes the contents of this frame to the given output stream.
     *
     * @param stream Stream to write to.
     */
    void writeToStream(OutputStream stream) throws IOException;

    /**
     * Returns an image containing a rectangular subsection of this image.
     *
     * @param x      Starting x co-ordinate
     * @param y      Starting y co-ordinate
     * @param width  Number of pixels wide
     * @param height Number of pixels tall
     *
     * @return Sub-image
     */
    Frame<D> subFrame(int x, int y, int width, int height);

    /**
     * Loop over each (x, y) value and the value of the pixel at each pair.
     *
     * @param action Action to perform on each iteration
     */
    default void forEach(TriConsumer<D> action) {

        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                action.accept(x, y, get(x, y));
            }
        }

    }

    interface ShortFrame extends Frame<Short> {

        /**
         * Directly returns the unboxed (memory-efficient) value for the specified pixel.
         *
         * @param x X co-ordinate of pixel
         * @param y X co-ordinate of pixel
         *
         * @return Unboxed value
         */
        short value(int x, int y);

        short[][] image();

        short[] data();

    }

    interface IntFrame extends Frame<Integer> {

        int value(int x, int y);

        int[][] image();

        int[] data();

    }

    interface TriConsumer<V> {

        void accept(int x, int y, V value);

    }

}
