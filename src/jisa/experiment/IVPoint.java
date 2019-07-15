package jisa.experiment;

/**
 * Structure to hold a current-voltage data-point.
 */
public class IVPoint {

    /**
     * Voltage value of I-V data-point
     */
    public double voltage;

    /**
     * Current value of I-V data-point
     */
    public double current;

    /**
     * Creates an IVPoint object with voltage V and current I.
     *
     * @param V Voltage
     * @param I Current
     */
    public IVPoint(double V, double I) {
        voltage = V;
        current = I;
    }


}
