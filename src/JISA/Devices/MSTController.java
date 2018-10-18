package JISA.Devices;

import JISA.Addresses.InstrumentAddress;

import java.io.IOException;

public abstract class MSTController extends TController {

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

    @Override
    public double getTemperature() throws IOException, DeviceException {
        return getTemperature(0);
    }

}
