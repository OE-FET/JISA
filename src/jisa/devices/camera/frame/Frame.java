package jisa.devices.camera.frame;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.stream.IntStream;

public interface Frame<D, F extends Frame> {

    /**
     * Returns a deep copy of this frame.
     *
     * @return Copy of this frame.
     */
    F copy();

    /**
     * Copies the data from the given frame into this frame. The frames must have matching dimensions.
     *
     * @param otherFrame Frame to copy from.
     */
    void copyFrom(F otherFrame);

    /**
     * Returns the timestamp at which this frame was taken (or best approximation thereof) in nanoseconds.
     *
     * @return Timestamp, in nanoseconds.
     */
    long getTimestamp();

    /**
     * Sets the timestamp of this frame.
     *
     * @param timestamp Timestamp, in nanoseconds.
     */
    void setTimestamp(long timestamp);

    default LocalDateTime getTime() {
        long stamp = getTimestamp();
        return LocalDateTime.ofEpochSecond(stamp / 1000000, (int) (stamp % 1000000), ZoneOffset.UTC);
    }

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
    F subFrame(int x, int y, int width, int height);

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

    interface ShortFrame<F extends ShortFrame> extends Frame<Short, F> {

        /**
         * Use the value() method instead.
         *
         * @param x X co-ordinate.
         * @param y Y co-ordinate.
         *
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
         *
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

    interface IntFrame<F extends IntFrame> extends Frame<Integer, F> {

        int value(int x, int y);

        /**
         * Use the value() method instead.
         *
         * @param x X co-ordinate.
         * @param y Y co-ordinate.
         *
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

    interface UShortFrame<F extends UShortFrame> extends IntFrame<F> {

        /**
         * Returns the raw (signed) short value for the given pixel.
         *
         * @param x X co-ordinate of pixel.
         * @param y X co-ordinate of pixel.
         *
         * @return Signed value
         */
        short signed(int x, int y);

        /**
         * Directly returns the unboxed (memory-efficient) value for the specified pixel.
         *
         * @param x X co-ordinate of pixel.
         * @param y X co-ordinate of pixel.
         *
         * @return Unboxed value.
         */
        default int value(int x, int y) {
            return Short.toUnsignedInt(signed(x, y));
        }

        short[] array();

        default int[] data() {
            short[] array = array();
            return IntStream.range(0, array().length).map(i -> Short.toUnsignedInt(array[i])).toArray();
        }

    }

    interface LongFrame<F extends LongFrame> extends Frame<Long, F> {

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

    interface UIntFrame<F extends UIntFrame> extends LongFrame<F> {

        /**
         * Returns the raw (signed) short value for the given pixel.
         *
         * @param x X co-ordinate of pixel.
         * @param y X co-ordinate of pixel.
         *
         * @return Signed value
         */
        int signed(int x, int y);

        /**
         * Directly returns the unboxed (memory-efficient) value for the specified pixel.
         *
         * @param x X co-ordinate of pixel.
         * @param y X co-ordinate of pixel.
         *
         * @return Unboxed value.
         */
        default long value(int x, int y) {
            return Integer.toUnsignedLong(signed(x, y));
        }

        int[] array();

        default long[] data() {
            return Arrays.stream(array()).mapToLong(Integer::toUnsignedLong).toArray();
        }

        @Deprecated
        default Long[] getData() {
            return Arrays.stream(array()).mapToObj(Integer::toUnsignedLong).toArray(Long[]::new);
        }

    }

    interface TriConsumer<V> {

        void accept(int x, int y, V value);

    }

}
