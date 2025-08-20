package jisa.devices.meter;

import jisa.control.Sync;
import jisa.devices.DeviceException;
import jisa.devices.Instrument;
import jisa.devices.ParameterList;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Unified interface for thermometers
 */
public interface TMeter extends Instrument, Meter {

    static String getDescription() {
        return "Thermometer";
    }

    static void addParameters(TMeter inst, Class target, ParameterList parameters) {
        parameters.addValue("Temperature Range [K]", inst::getTemperatureRange, 999.9, inst::setTemperatureRange);
    }

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
        Sync.waitForStableTarget(this::getTemperature, temperature, pctMargin, 1000, duration);
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
        Sync.waitForParamStable(this::getTemperature, pctMargin, 1000, duration);
    }


    /**
     * Wait for the temperature to remain within a percentage range of any value for at least a specified amount of time, with max. time.
     *
     * @param pctMargin Percentage range to stay within
     * @param duration  Minimum time needed to be in range.
     * @param timeOut   Maximum time to wait before timing out.
     *
     * @throws IOException          Upon communications error.
     * @throws DeviceException      Upon compatibility error.
     * @throws InterruptedException Upon wait being interrupted.
     * @throws TimeoutException     Upon timing out.
     */
    default void waitForStableTemperature(double pctMargin, long duration, long timeOut) throws IOException, DeviceException, InterruptedException, TimeoutException {
        Sync.waitForParamStable(this::getTemperature, pctMargin, 1000, duration, timeOut);
    }

    /**
     * Wait for the temperature to remain within a range of a given value for at least a specified amount of time, with maximum time.
     *
     * @param temperature Temperature to be in range of.
     * @param pctMargin   Percentage range to stay within.
     * @param duration    Minimum time needed to be in range.
     * @param timeOut     Maximum time to wait before timing out.
     *
     * @throws IOException          Upon communications error.
     * @throws DeviceException      Upon compatibility error.
     * @throws InterruptedException Upon wait being interrupted.
     * @throws TimeoutException     Upon timing out.
     */
    default void waitForStableTemperature(double temperature, double pctMargin, long duration, long timeOut) throws IOException, DeviceException, InterruptedException, TimeoutException {
        Sync.waitForStableTarget(this::getTemperature, temperature, pctMargin, 1000, duration, timeOut);
    }

    @Override
    default double getIntegrationTime() throws IOException, DeviceException {
        return 0;
    }

    @Override
    default void setIntegrationTime(double intTime) throws IOException, DeviceException {

    }

}
