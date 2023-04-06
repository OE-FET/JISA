package jisa.devices.interfaces;

import jisa.devices.DeviceException;

import java.io.IOException;

/**
 * Interface to define the standard functionality of an instrument that can source voltage.
 */
public interface VSource extends Instrument, Switch {

    static String getDescription() {
        return "Voltage Source";
    }

    /**
     * Set the voltage to output.
     *
     * @param voltage Voltage to output, in Volts
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void setVoltage(double voltage) throws IOException, DeviceException;

    /**
     * Returns the voltage being output.
     *
     * @return Voltage being output, in Volts
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    double getVoltage() throws IOException, DeviceException;

    /**
     * Turns the voltage-source on.
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void turnOn() throws IOException, DeviceException;

    /**
     * Turns the voltage-source off.
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void turnOff() throws IOException, DeviceException;


    /**
     * Returns whether the voltage-source is on or off.
     *
     * @return Is it on?
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    boolean isOn() throws IOException, DeviceException;

}
