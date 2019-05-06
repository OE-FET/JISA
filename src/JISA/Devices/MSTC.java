package JISA.Devices;

import JISA.Addresses.Address;
import JISA.Control.Synch;
import JISA.Util;
import JISA.VISA.Driver;

import java.io.IOException;

public abstract class MSTC extends TC {

    public MSTC(Address address, Class<? extends Driver> prefDriver) throws IOException {
        super(address, prefDriver);
    }

    public MSTC(Address address) throws IOException {
        this(address, null);
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
    public abstract double getTemperature(int sensor) throws IOException, DeviceException;

    /**
     * Returns the temperature reported by the control-loop sensor
     *
     * @return Temperature, in Kelvin
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public double getTemperature() throws IOException, DeviceException {
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
    public abstract void useSensor(int sensor) throws IOException, DeviceException;

    /**
     * Returns the number of the sensor being used for temperature control
     *
     * @return Sensor number
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public abstract int getUsedSensor() throws IOException, DeviceException;

    /**
     * Returns the number of sensors the controller has.
     *
     * @return Number of sensors
     */
    public abstract int getNumSensors();

    /**
     * Returns a virtual TMeter object representing the specified temperature sensor.
     *
     * @param sensor Sensor number
     *
     * @return TMeter representing the sensor
     *
     * @throws DeviceException Upon compatibility error
     */
    public TMeter getSensor(int sensor) throws DeviceException {

        checkSensor(sensor);

        return new TMeter() {

            @Override
            public double getTemperature() throws IOException, DeviceException {
                return MSTC.this.getTemperature(sensor);
            }

            @Override
            public String getIDN() throws IOException {
                return MSTC.this.getIDN();
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
    protected void checkSensor(int sensor) throws DeviceException {
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
    public void waitForStableTemperature(int sensor, double temperature, double pctMargin, long time) throws IOException, DeviceException, InterruptedException {

        checkSensor(sensor);

        Synch.waitForStableTarget(
                () -> getTemperature(sensor),
                temperature,
                pctMargin,
                100,
                time
        );

    }

    /**
     * Waits until the temperature reported by the specified sensor has remained within 1% of the specified temperature
     * for at least 1 minute.
     *
     * @param sensor      Sensor number
     * @param temperature Temperature target
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public void waitForStableTemperature(int sensor, double temperature) throws IOException, DeviceException, InterruptedException {
        waitForStableTemperature(sensor, temperature, 1.0, 60000);
    }

    /**
     * Waits until the temperature reported by the control-loop sensor has remained within 1% of the target (set-point)
     * temperature for at least 1 minute.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public void waitForStableTemperature() throws IOException, DeviceException, InterruptedException {
        waitForStableTemperature(getUsedSensor(), getTargetTemperature());
    }

    /**
     * Sets the target (set-point) temperature and waits until the control loop sensor has reported a temperature
     * within 1% of that value for at least 1 minute.
     *
     * @param temperature Target temperature, in Kelvin
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public void setTargetAndWait(double temperature) throws IOException, DeviceException, InterruptedException {
        setTargetTemperature(temperature);
        waitForStableTemperature();
    }

}
