package jisa.devices.meter;

import jisa.devices.DeviceException;
import jisa.experiment.IVPoint;

import java.io.IOException;

/**
 * Interface for defining the standard functionality of multi-meters.
 */
public interface IVMeter extends VMeter, IMeter {

    static String getDescription() {
        return "Multimeter";
    }

    @Override
    default double getValue() throws IOException, DeviceException {
        return getVoltage();
    }

    @Override
    default String getMeasuredQuantity() {
        return "Voltage";
    }

    @Override
    default String getMeasuredUnits() {
        return "V";
    }

    /**
     * Returns a combined current and voltage measurement as an IVPoint object.
     *
     * @return Combined measurement
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    default IVPoint getIVPoint() throws IOException, DeviceException {
        return new IVPoint(getVoltage(), getCurrent());
    }

    /**
     * Sets the ranges used for voltage and current measurements respectively.
     *
     * @param voltage Voltage range, in Volts
     * @param current Current range, in Amps
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    default void setRanges(double voltage, double current) throws IOException, DeviceException {
        setVoltageRange(voltage);
        setCurrentRange(current);
    }

    /**
     * Tells the multi-meter to use auto-ranging for both voltage and current measurements.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    default void useAutoRanges() throws IOException, DeviceException {
        useAutoVoltageRange();
        useAutoCurrentRange();
    }

}
