package jisa.devices.interfaces;

import jisa.Util;
import jisa.addresses.Address;
import jisa.control.Synch;
import jisa.devices.DeviceException;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public interface MSTC extends TC, MSTMeter, Output<MSTC> {

    String getOutputName();

    default Class<MSTC> getOutputClass() {
        return MSTC.class;
    }

    public static String getDescription() {
        return "Multi-Sensor Temperature Controller";
    }

    /**
     * Returns the temperature reported by the specified sensor
     *
     * @param sensor Sensor number
     *
     * @return Temperature, in Kelvin
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    double getTemperature(int sensor) throws IOException, DeviceException;

    /**
     * Returns the temperature reported by the control-loop sensor
     *
     * @return Temperature, in Kelvin
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    default double getTemperature() throws IOException, DeviceException {
        return getTemperature(getUsedSensor());
    }

    /**
     * Tells the controller to use the specified sensor for temperature control
     *
     * @param sensor Sensor number
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    void useSensor(int sensor) throws IOException, DeviceException;

    /**
     * Returns the number of the sensor being used for temperature control
     *
     * @return Sensor number
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    int getUsedSensor() throws IOException, DeviceException;

    /**
     * Returns the number of sensors the controller has.
     *
     * @return Number of sensors
     */
    int getNumSensors();

    /**
     * Returns a virtual TMeter object representing the specified temperature sensor.
     *
     * @param sensor Sensor number
     *
     * @return TMeter representing the sensor
     *
     * @throws DeviceException Upon compatibility error
     */
    default TMeter getSensor(int sensor) throws DeviceException {

        checkSensor(sensor);

        return new TMeter() {

            @Override
            public String getSensorName() {
                return MSTC.this.getSensorName(sensor);
            }

            @Override
            public double getTemperature() throws IOException, DeviceException {
                return MSTC.this.getTemperature(sensor);
            }

            @Override
            public double getTemperatureRange() throws IOException, DeviceException {
                return MSTC.this.getTemperatureRange(sensor);
            }

            @Override
            public void setTemperatureRange(double range) throws IOException, DeviceException {
                MSTC.this.setTemperatureRange(sensor, range);
            }

            @Override
            public String getIDN() throws IOException, DeviceException {
                return MSTC.this.getIDN();
            }

            @Override
            public void close() throws IOException, DeviceException {
                MSTC.this.close();
            }

            @Override
            public Address getAddress() {
                return null;
            }

        };

    }

    /**
     * Checks whether the given sensor number is valid, throws a DeviceException if not.
     *
     * @param sensor Sensor number to check
     *
     * @throws DeviceException Upon invalid sensor number being specified
     */
    default void checkSensor(int sensor) throws DeviceException {
        if (!Util.isBetween(sensor, 0, getNumSensors() - 1)) {
            throw new DeviceException("This temperature controller only has %d sensors.", getNumSensors());
        }
    }

    /**
     * Waits until the temperature reported by the specified sensor has remained within the specified percentage margin
     * of the specified temperature for at least the specified time.
     *
     * @param sensor      Sensor number
     * @param temperature Temperature target
     * @param pctMargin   Percentage margin
     * @param time        Duration, in seconds
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    default void waitForStableTemperature(int sensor, double temperature, double pctMargin, long time) throws IOException, DeviceException, InterruptedException {

        checkSensor(sensor);

        Synch.waitForStableTarget(
            () -> getTemperature(sensor),
            temperature,
            pctMargin,
            1000,
            time
        );

    }

    default List<Parameter<?>> getConfigurationParameters(Class<?> target) {

        LinkedList<Parameter<?>> parameters = new LinkedList<>();

        List<String> names = getSensors().stream().map(Sensor::getSensorName).collect(Collectors.toUnmodifiableList());
        parameters.add(new Parameter<>("Sensor", getSensorName(0), n -> useSensor(names.indexOf(n)), names.toArray(String[]::new)));
        parameters.addAll(TC.super.getConfigurationParameters(target));

        return parameters;

    }

    default Class<TMeter> getSensorClass() {
        return TMeter.class;
    }

}
