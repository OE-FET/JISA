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
    default void waitForStableTemperature(double temperature, double pctMargin, int duration) throws IOException, DeviceException, InterruptedException {
        Synch.waitForStableTarget(this::getTemperature, temperature, pctMargin, 1000, duration);
    }

    /**
     * Wait for the temperature to remain within a percentage range of any value for at least a specified amount of time.
     *
     * @param pctMargin   Percentage range to stay within
     * @param duration    Minimum time needed to be in range.
     *
     * @throws IOException          Upon communications error
     * @throws DeviceException      Upon compatibility error
     * @throws InterruptedException Upon wait being interrupted
     */
    default void waitForStableTemperature(double pctMargin, int duration) throws IOException, DeviceException, InterruptedException {
        Synch.waitForParamStable(this::getTemperature, pctMargin, 1000, duration);
    }

}
