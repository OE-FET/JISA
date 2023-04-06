package jisa.devices.interfaces;

import jisa.Util;
import jisa.addresses.Address;
import jisa.control.Synch;
import jisa.devices.DeviceException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public interface MSTMeter extends TMeter, MultiInstrument {

    static String getDescription() {
        return "Multi-Sensor Thermometer";
    }

    @Override
    default List<Class<? extends Instrument>> getSubInstrumentTypes() {
        return List.of(TMeter.class);
    }

    @Override
    default <I extends Instrument> List<I> getSubInstruments(Class<I> type) {

        if (type.isAssignableFrom(TMeter.class)) {
            return (List<I>) getSensors();
        } else {
            return Collections.emptyList();
        }

    }

    @Override
    default <I extends Instrument> I getSubInstrument(Class<I> type, int index) {

        if (type.isAssignableFrom(TMeter.class)) {
            return (I) getSensor(index);
        } else {
            return null;
        }

    }

    String getName(int channel);

    /**
     * Returns the temperature being reported by the specified sensor.
     *
     * @param sensor Sensor to read
     *
     * @return Temperature, in Kelvin
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    double getTemperature(int sensor) throws IOException, DeviceException;

    /**
     * Returns the number of sensors the instrument has.
     *
     * @return Number of sensors
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    int getNumSensors();

    /**
     * Returns the temperature reported by the first sensor.
     *
     * @return Temperature, in Kelvin
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    default double getTemperature() throws IOException, DeviceException {
        return getTemperature(0);
    }

    /**
     * Returns temperature readings from all sensors as a List indexed by sensor number.
     *
     * @return List of temperatures, in Kelvin
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    default List<Double> getTemperatures() throws IOException, DeviceException {

        List<Double> values = new LinkedList<>();

        for (int i = 0; i < getNumSensors(); i++) {
            values.add(getTemperature(i));
        }

        return values;

    }

    /**
     * Sets the measurement range for temperature values. The smallest available range containing the specified value
     * will be selected if only discrete options are available.
     *
     * @param sensor The sensor this applies to
     * @param range  The range to use, in Kelvin
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void setTemperatureRange(int sensor, double range) throws IOException, DeviceException;

    default void setTemperatureRange(double range) throws IOException, DeviceException {
        setTemperatureRange(0, range);
    }

    /**
     * Returns the measurement range being used for temperature values.
     *
     * @param sensor The sensor this applies to
     *
     * @return The range being used, in Kelvin
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    double getTemperatureRange(int sensor) throws IOException, DeviceException;

    default double getTemperatureRange() throws IOException, DeviceException {
        return getTemperatureRange(0);
    }

    /**
     * Returns the specified sensor as its own thermometer object.
     *
     * @param sensor The sensor to return
     *
     * @return Sensor as thermometer
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    default TMeter getSensor(int sensor) {

        if (!Util.isBetween(sensor, 0, getNumSensors() - 1)) {
            return null;
        }

        return new TMeter() {

            @Override
            public String getName() {
                return MSTMeter.this.getName(sensor);
            }

            @Override
            public double getTemperature() throws IOException, DeviceException {
                return MSTMeter.this.getTemperature(sensor);
            }

            @Override
            public void setTemperatureRange(double range) throws IOException, DeviceException {
                MSTMeter.this.setTemperatureRange(sensor, range);
            }

            @Override
            public double getTemperatureRange() throws IOException, DeviceException {
                return MSTMeter.this.getTemperatureRange(sensor);
            }

            @Override
            public String getIDN() throws IOException, DeviceException {
                return MSTMeter.this.getIDN();
            }

            @Override
            public void close() throws IOException, DeviceException {
                MSTMeter.this.close();
            }

            @Override
            public Address getAddress() {
                return MSTMeter.this.getAddress();
            }

        };

    }

    default List<TMeter> getSensors() {

        List<TMeter> list = new ArrayList<>();

        for (int i = 0; i < getNumSensors(); i++) {
            list.add(getSensor(i));
        }

        return list;

    }

    default void waitForStableTemperature(int sensor, double temperature, double pctMargin, int duration) throws InterruptedException, DeviceException, IOException {
        Synch.waitForStableTarget(() -> getTemperature(sensor), temperature, pctMargin, 1000, duration);
    }

    default void checkSensor(int sensor) throws DeviceException, IOException {

        if (!Util.isBetween(sensor, 0, getNumSensors() - 1)) {
            throw new DeviceException("Sensor number %d does not exist", sensor);
        }

    }

}
