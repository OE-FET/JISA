package JISA.Devices;

import JISA.Addresses.InstrumentAddress;
import JISA.Control.Synch;
import JISA.VISA.VISADevice;

import java.io.IOException;

/**
 * Abstract class to define the standard functionality of temperature controllers
 */
public abstract class TController extends VISADevice {

    /**
     * Connects to the temperature controller at the given address, returning an instrument object to control it.
     *
     * @param address Address of instrument
     *
     * @throws IOException Upon communications error
     */
    public TController(InstrumentAddress address) throws IOException {
        super(address);
    }

    /**
     * Sets the target temperature of the temperature controller.
     *
     * @param temperature Target temperature, in Kelvin
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public abstract void setTargetTemperature(double temperature) throws IOException, DeviceException;

    /**
     * Returns the temperature measured by the controller.
     *
     * @return Temperature, in Kelvin
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public abstract double getTemperature() throws IOException, DeviceException;

    /**
     * Returns the target temperature set on the controller.
     *
     * @return Target temperature, in Kelvin
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public abstract double getTargetTemperature() throws IOException, DeviceException;

    /**
     * Returns the heater output power percentage.
     *
     * @return Heater power, percentage of max
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public abstract double getHeaterPower() throws IOException, DeviceException;

    /**
     * Returns the gas flow in whatever units the controller uses
     *
     * @return Gas flow (arbitrary units)
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public abstract double getGasFlow() throws IOException, DeviceException;

    /**
     * Sets whether the heater should be operated automatically or manually
     *
     * @param auto Automatic?
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public abstract void useAutoHeater(boolean auto) throws IOException, DeviceException;

    /**
     * Returns whether the heater is currently operating automatically or manually
     *
     * @return Automatic?
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public abstract boolean isHeaterAuto() throws IOException, DeviceException;

    /**
     * Sets whether the gas flow should be controlled automatically or manually
     *
     * @param auto Automatic?
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public abstract void useAutoFlow(boolean auto) throws IOException, DeviceException;

    /**
     * Returns whether the gas flow is currently controlled automatically or manually
     *
     * @return Automatic?
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public abstract boolean isFlowAuto() throws IOException, DeviceException;

    /**
     * Sets whether the PID values are being automatically controlled
     *
     * @return Automatic?
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public abstract void useAutoPID(boolean auto) throws IOException, DeviceException;

    /**
     * Returns whether the PID values are being automatically controlled
     *
     * @return Automatic?
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public abstract boolean isPIDAuto() throws IOException, DeviceException;

    /**
     * Sets the proportional term co-efficient for PID control
     *
     * @param value Proportional co-efficient
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public abstract void setPValue(double value) throws IOException, DeviceException;

    /**
     * Sets the integral term co-efficient for PID control
     *
     * @param value Integral co-efficient
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public abstract void setIValue(double value) throws IOException, DeviceException;

    /**
     * Sets the derivative term co-efficient for PID control
     *
     * @param value Derivative co-efficient
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public abstract void setDValue(double value) throws IOException, DeviceException;

    /**
     * Returns the proportional term co-efficient used for PID control
     *
     * @return Proportional co-efficient
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public abstract double getPValue() throws IOException, DeviceException;

    /**
     * Returns the integral term co-efficient used for PID control
     *
     * @return Integral co-efficient
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public abstract double getIValue() throws IOException, DeviceException;

    /**
     * Returns the derivative term co-efficient used for PID control
     *
     * @return Derivative co-efficient
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public abstract double getDValue() throws IOException, DeviceException;

    /**
     * Sets the target temperature and waits for it to be stably reached.
     *
     * @param temperature Target temperature, in Kelvin
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public void setTargetAndWait(double temperature) throws IOException, DeviceException {
        setTargetTemperature(temperature);
        waitForStableTemperature(temperature);
    }

    /**
     * Halts the current thread until the temperature has stabilised to the specified value within the given percentage
     * margin for at least the given amount of time.
     *
     * @param temperature Target temperature, in Kelvin
     * @param pctMargin   Percentage margin
     * @param time        Amount of time to be considered stable, in milliseconds
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public void waitForStableTemperature(double temperature, double pctMargin, long time) throws IOException, DeviceException {

        Synch.waitForStableTarget(
                this::getTemperature,
                temperature,
                pctMargin,
                100,
                time
        );

    }

    /**
     * Halts the current thread until the temperature has stabilised to the specified value within 1% for at least 10 seconds.
     *
     * @param temperature Target temperature, in Kelvin
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public void waitForStableTemperature(double temperature) throws IOException, DeviceException {
        waitForStableTemperature(temperature, 1.0, 10000);
    }

    /**
     * Halts the current thread until the temperature has stabilised to the target value within 1% for at least 10 seconds.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public void waitForStableTemperature() throws IOException, DeviceException {
        waitForStableTemperature(getTargetTemperature());
    }

}
