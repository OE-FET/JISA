package jisa.devices;

import java.io.IOException;

/**
 * Interface to define the standard functionality of an instrument that can source current.
 */
public interface ISource extends Instrument {

    /**
     * Sets the current to output.
     *
     * @param current Current to output, in Amps
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void setCurrent(double current) throws IOException, DeviceException;

    /**
     * Returns the current being output.
     *
     * @return Current being output, in Amps
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    double getCurrent() throws IOException, DeviceException;

    /**
     * Turns the current-source on.
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void turnOn() throws IOException, DeviceException;

    /**
     * Turns the current-source off.
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void turnOff() throws IOException, DeviceException;

    /**
     * Returns whether the current-source is on or off.
     *
     * @return Is it on?
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    boolean isOn() throws IOException, DeviceException;

}
