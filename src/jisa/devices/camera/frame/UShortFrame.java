package jisa.devices.camera.frame;

import java.util.stream.IntStream;

public interface UShortFrame<F extends UShortFrame> extends IntFrame<F> {

    /**
     * Returns the raw (signed) short value for the given pixel.
     *
     * @param x X co-ordinate of pixel.
     * @param y X co-ordinate of pixel.
     * @return Signed value
     */
    short signed(int x, int y);

    /**
     * Directly returns the unboxed (memory-efficient) value for the specified pixel.
     *
     * @param x X co-ordinate of pixel.
     * @param y X co-ordinate of pixel.
     * @return Unboxed value.
     */
    default int value(int x, int y) {
        return Short.toUnsignedInt(signed(x, y));
    }

    default short[][] signedImage() {
        short[][] signed = new short[getHeight()][getWidth()];

        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                signed[y][x] = signed(x, y);
            }
        }

        return signed;

    }

    short[] array();

    default int[] data() {
        short[] array = array();
        return IntStream.range(0, array().length).map(i -> Short.toUnsignedInt(array[i])).toArray();
    }

}
