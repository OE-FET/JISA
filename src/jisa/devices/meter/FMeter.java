package jisa.devices.meter;

import jisa.devices.DeviceException;
import jisa.devices.ParameterList;

import java.io.IOException;

/**
 * Standard interface for instrument that provide frequency measurements.
 */
public interface FMeter extends Meter {

    static void addParameters(FMeter inst, Class target, ParameterList parameters) {

        parameters.addValue("Frequency Range [Hz]", inst::getFrequencyRange, 999.9, inst::setFrequencyRange);

    }

    @Override
    default double getValue() throws IOException, DeviceException {
        return getFrequency();
    }

    @Override
    default String getMeasuredQuantity() throws IOException, DeviceException {
        return "Frequency";
    }

    @Override
    default String getMeasuredUnits() throws IOException, DeviceException {
        return "Hz";
    }

    /**
     * Returns the frequency value currently being reported by the instrument, in Hz.
     *
     * @return Frequency, in Hz
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    double getFrequency() throws IOException, DeviceException;

    /**
     * Returns the frequency measurement range being used by the instrument, in Hz. A value of x indicates a range of
     * +/- x.
     *
     * @return Frequency range, in +/- Hz.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    double getFrequencyRange() throws IOException, DeviceException;

    /**
     * Sets the frequency measurement range to be used by the instrument. Will select the smallest range option that
     * contains the specified value.
     *
     * @param range Frequency range to use, in Hz.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    void setFrequencyRange(double range) throws IOException, DeviceException;

}
