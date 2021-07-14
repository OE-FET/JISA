package jisa.devices.interfaces;

import jisa.addresses.Address;
import jisa.control.Synch;
import jisa.Util;
import jisa.devices.DeviceException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public interface MSTMeter extends TMeter, MultiSensor<TMeter> {

    public static String getDescription() {
        return "Multi-Sensor Thermometer";
    }

    default Class<TMeter> getSensorClass() {
        return TMeter.class;
    }

    void setSensorType(int sensor, SensorType type) throws IOException, DeviceException;

    SensorType getSensorType(int sensor) throws IOException, DeviceException;

    default void setSensorType(SensorType type) throws IOException, DeviceException {

        for (int i = 0; i < getNumSensors(); i++) {
            setSensorType(i, type);
        }

    }

    default SensorType getSensorType() throws IOException, DeviceException {
        return getSensorType(0);
    }

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
    default TMeter getSensor(int sensor) throws IOException, DeviceException {

        if (!Util.isBetween(sensor, 0, getNumSensors() - 1)) {
            throw new DeviceException("Sensor %d does not exist.", sensor);
        }

        return new TMeter() {

            @Override
            public String getSensorName() {
                return MSTMeter.this.getSensorName(sensor);
            }

            @Override
            public void setSensorType(SensorType type) throws IOException, DeviceException {
                MSTMeter.this.setSensorType(sensor, type);
            }

            @Override
            public SensorType getSensorType() throws IOException, DeviceException {
                return MSTMeter.this.getSensorType(sensor);
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


    @Override
    default List<TMeter> getSensors() {

        List<TMeter> list = new ArrayList<>();

        for (int i = 0; i < getNumSensors(); i++) {
            try {
                list.add(getSensor(i));
            } catch (IOException | DeviceException e) {
                e.printStackTrace();
            }
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
