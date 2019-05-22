package JISA.Devices;

import JISA.Control.Synch;
import JISA.Util;

import java.io.IOException;

public interface TMeter extends Instrument {

    /**
     * Returns the temperature being reported by the thermometer.
     *
     * @return Temperature, in Kelvin
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    double getTemperature() throws IOException, DeviceException;

    /**
     * Sets the measurement range for temperature values. The smallest available range containing the specified value
     * will be selected if only discrete options are available.
     *
     * @param range The range to use, in Kelvin
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void setTemperatureRange(double range) throws IOException, DeviceException;

    /**
     * Returns the measurement range being used for temperature values.
     *
     * @return The range being used, in Kelvin
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    double getTemperatureRange() throws IOException, DeviceException;

    /**
     * Wait for the temperature to remain within a range of a given value for at least a specified amount of time.
     *
     * @param temperature Temperature to be in range of
     * @param pctMargin   Percentage range to stay within
     * @param duration    Minimum time needed to be in range.
     *
     * @throws IOException          Upon communications error
     * @throws DeviceException      Upon compatibility error
     * @throws InterruptedException Upon wait being interrupted
     */
    default void waitForStableTemperature(double temperature, double pctMargin, long duration) throws IOException, DeviceException, InterruptedException {
        Synch.waitForStableTarget(this::getTemperature, temperature, pctMargin, 1000, duration);
    }

    /**
     * Wait for the temperature to remain within a percentage range of any value for at least a specified amount of time.
     *
     * @param pctMargin Percentage range to stay within
     * @param duration  Minimum time needed to be in range.
     *
     * @throws IOException          Upon communications error
     * @throws DeviceException      Upon compatibility error
     * @throws InterruptedException Upon wait being interrupted
     */
    default void waitForStableTemperature(double pctMargin, long duration) throws IOException, DeviceException, InterruptedException {
        Synch.waitForParamStable(this::getTemperature, pctMargin, 1000, duration);
    }

}
