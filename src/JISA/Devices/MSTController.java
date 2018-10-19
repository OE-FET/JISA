package JISA.Devices;

import JISA.Addresses.InstrumentAddress;
import JISA.Control.Synch;
import JISA.Util;

import java.io.IOException;

public abstract class MSTController extends TController {

    protected int defaultSensor = 0;

    public MSTController(InstrumentAddress address) throws IOException {
        super(address);
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

    @Override
    public double getTemperature() throws IOException, DeviceException {
        return getTemperature(defaultSensor);
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

    public abstract int getNumSensors();

    public void checkSensor(int sensor) throws DeviceException {
        if (!Util.isBetween(sensor, 0, getNumSensors() - 1)) {
            throw new DeviceException("This temperature controller only has %d sensors.", getNumSensors());
        }
    }

    public void waitForStableTemperature(int sensor, double temperature, double pctMargin, long time) throws IOException, DeviceException {

        checkSensor(sensor);

        Synch.waitForStableTarget(
                () -> getTemperature(sensor),
                temperature,
                pctMargin,
                100,
                time
        );

    }

    public void waitForStableTemperature(int sensor, double temperature) throws IOException, DeviceException {
        waitForStableTemperature(sensor, temperature, 1.0, 10000);
    }

    public void waitForStableTemperature() throws IOException, DeviceException {
        waitForStableTemperature(getUsedSensor(), getTargetTemperature());
    }

    public void setTargetAndWait(double temperature) throws IOException, DeviceException {
        setTargetTemperature(temperature);
        waitForStableTemperature();
    }

}
