package jisa.enums;

/**
 * Enumeration of averaging modes
 */
public enum AMode {

    /**
     * No Averaging.
     */
    NONE,

    /**
     * Mean average, taking n data points each time.
     */
    MEAN_REPEAT,

    /**
     * Mean average, only taking one new data point each time and using the previous n-1 points.
     */
    MEAN_MOVING,

    /**
     * Median average, taking n data points each time.
     */
    MEDIAN_REPEAT,

    /**
     * Median average, only taking one new data point each time and using the previous n-1 points.
     */
    MEDIAN_MOVING

}
