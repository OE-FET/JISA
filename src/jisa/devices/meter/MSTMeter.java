package jisa.devices.meter;

import jisa.devices.DeviceException;
import jisa.devices.Instrument;
import jisa.devices.MultiInstrument;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface MSTMeter<T extends TMeter> extends MultiInstrument {

    /**
     * Returns a list of all thermometers provided by this instrument.
     *
     * @return List of thermometers.
     */
    List<T> getThermometers();

    /**
     * Returns the nth thermometer provided by this instrument (starting at 0).
     *
     * @param n Index of thermometer to get.
     *
     * @return Specified thermometer.
     */
    default T getThermometer(int n) {
        return getThermometers().get(n);
    }

    default List<? extends Instrument> getSubInstruments() {
        return getThermometers();
    }

    /**
     * Returns a map of temperature readings, keyed by the channel object they came from.
     *
     * @param channels Channels to read, if none specified then all are read.
     *
     * @return Readings map
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device incompatibility error
     */
    Map<T, Double> getTemperatures(T... channels) throws IOException, DeviceException;

}
