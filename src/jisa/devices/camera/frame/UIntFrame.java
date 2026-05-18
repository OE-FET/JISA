package jisa.devices.camera.frame;

import java.util.Arrays;

public interface UIntFrame<F extends UIntFrame> extends LongFrame<F> {

    /**
     * Returns the raw (signed) short value for the given pixel.
     *
     * @param x X co-ordinate of pixel.
     * @param y X co-ordinate of pixel.
     * @return Signed value
     */
    int signed(int x, int y);

    /**
     * Directly returns the unboxed (memory-efficient) value for the specified pixel.
     *
     * @param x X co-ordinate of pixel.
     * @param y X co-ordinate of pixel.
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
