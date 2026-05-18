package jisa.devices.camera.frame;

import io.jhdf.api.WritableDataset;
import io.jhdf.api.WritableGroup;

import javax.imageio.ImageIO;
import java.awt.image.*;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;

public interface Frame<D, F extends Frame, R> {

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

    R getImageArray();

    /**
     * Returns the pixel value at the given x and y co-ordinates/indices.
     *
     * @param x X co-ordinate
     * @param y Y co-ordinate
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

    int getARGB(int x, int y);

    /**
     * Returns this frame's ARGB data as an array of integers.
     *
     * @return ARGB data.
     */
    default int[] getARGBData() {

        int[] data = new int[size()];
        readARGBData(data);
        return data;

    }

    default int[] getScaledARGBData() {

        int[] data = new int[size()];
        readARGBData(data);
        return data;

    }

    /**
     * Reads the frame's ARGB data into a given linear/1D array. This is for fast data transfer operations (such as populating
     * a PixelBuffer).
     *
     * @param destination Destination array to fill.
     */
    void readARGBData(int[] destination);


    /**
     * Reads the frame's ARGB data, normalised to the maximum pixel value, into a given linear/1D array. This is for fast data transfer operations (such as populating
     * a PixelBuffer).
     *
     * @param destination Destination array to fill.
     */
    void readScaledARGBData(int[] destination);

    /**
     * Returns the frame's ARGB values in the form of a 2D array corresponding to pixels.
     *
     * @return
     */
    default int[][] getARGBImage() {

        int     width  = getWidth();
        int     height = getHeight();
        int[]   data   = getARGBData();
        int[][] image  = new int[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image[y][x] = data[y * width + x];
            }
        }

        return image;

    }

    default byte[] getRGBBytes() {

        int[]  data   = getARGBData();
        byte[] output = new byte[data.length * 3];

        for (int i = 0; i < data.length; i++) {

            int pixel = data[i];

            output[i * 3]     = (byte) ((pixel >> 16) & 0xFF); // Red
            output[i * 3 + 1] = (byte) ((pixel >> 8) & 0xFF);  // Green
            output[i * 3 + 2] = (byte) (pixel & 0xFF);         // Blue
        }

        return output;
    }

    default byte[][] getPlanarRGBPlanes() {

        int[] argb = getARGBData();
        int   n    = argb.length;

        byte[] r = new byte[n];
        byte[] g = new byte[n];
        byte[] b = new byte[n];

        for (int i = 0; i < n; i++) {
            int pixel = argb[i];
            r[i] = (byte) ((pixel >> 16) & 0xFF);
            g[i] = (byte) ((pixel >> 8) & 0xFF);
            b[i] = (byte) (pixel & 0xFF);
        }

        return new byte[][]{r, g, b};
    }

    default byte[][][] getNPArray() {

        byte[][][] output = new byte[getHeight()][getWidth()][3];
        int[]      data   = getARGBData();
        ByteBuffer buffer = ByteBuffer.allocate(data.length * Integer.BYTES);

        buffer.asIntBuffer().put(data).rewind();

        for (int y = 0; y < getHeight(); y++) {

            for (int x = 0; x < getWidth(); x++) {
                buffer.get();
                buffer.get(output[y][x]);
            }

        }

        return output;

    }

    default byte[] getBGRBytes() {

        int[]      data   = getARGBData();
        ByteBuffer buffer = ByteBuffer.allocate(data.length * Integer.BYTES);
        buffer.asIntBuffer().put(data);

        byte[] output = new byte[data.length * 3];
        buffer.rewind();

        for (int i = 0; i < data.length; i++) {
            buffer.get();
            buffer.get(output, 3 * i + 2, 1);
            buffer.get(output, 3 * i + 1, 1);
            buffer.get(output, 3 * i + 0, 1);
        }

        return output;

    }

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
    void writeToStream(DataOutputStream stream) throws IOException;

    default BufferedImage toBufferedImage() {

        int[] argb = getARGBData();

        DataBuffer     rgbData    = new DataBufferInt(argb, argb.length);
        WritableRaster raster     = Raster.createPackedRaster(rgbData, getWidth(), getHeight(), getWidth(), new int[]{0xff0000, 0xff00, 0xff}, null);
        ColorModel     colorModel = new DirectColorModel(24, 0xff0000, 0xff00, 0xff);

        return new BufferedImage(colorModel, raster, false, null);

    }

    default void savePNG(String path) throws IOException {

        int[] argb = getARGBData();

        DataBuffer     rgbData    = new DataBufferInt(argb, argb.length);
        WritableRaster raster     = Raster.createPackedRaster(rgbData, getWidth(), getHeight(), getWidth(), new int[]{0xff0000, 0xff00, 0xff}, null);
        ColorModel     colorModel = new DirectColorModel(24, 0xff0000, 0xff00, 0xff);

        BufferedImage img = new BufferedImage(colorModel, raster, false, null);

        ImageIO.write(img, "png", new File(path));

    }

    /**
     * Returns an image containing a rectangular subsection of this image.
     *
     * @param x      Starting x co-ordinate
     * @param y      Starting y co-ordinate
     * @param width  Number of pixels wide
     * @param height Number of pixels tall
     * @return Sub-image
     */
    F subFrame(int x, int y, int width, int height);

    Map<String, Object> getAttributes();

    default int getAttributeInt(String key) {
        return (int) getAttributes().get(key);
    }

    default double getAttributeDouble(String key) {
        return (double) getAttributes().get(key);
    }

    default void setAttribute(String key, Object value) {
        getAttributes().put(key, value);
    }

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

    default WritableDataset writeToHDF(WritableGroup group, String name) {
        return group.putDataset(name, getARGBImage());
    }

    interface TriConsumer<V> {

        void accept(int x, int y, V value);

    }

}
