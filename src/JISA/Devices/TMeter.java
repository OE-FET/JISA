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

    default void waitForStableTemperature(double temperature, double pctMargin, int duration) throws IOException, DeviceException, InterruptedException {
        Synch.waitForStableTarget(this::getTemperature, temperature, pctMargin, 1000, duration);
    }

}
