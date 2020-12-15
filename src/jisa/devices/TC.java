package jisa.devices;

import jisa.control.Synch;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Abstract class to define the standard functionality of temperature controllers
 */
public interface TC extends PID, TMeter {

    @Override
    default String getInputName() throws IOException, DeviceException {
        return "Temperature";
    }

    @Override
    default String getInputUnits() throws IOException, DeviceException {
        return "K";
    }

    @Override
    default double getSetPoint() throws IOException, DeviceException {
        return getTargetTemperature();
    }

    @Override
    default void setSetPoint(double value) throws IOException, DeviceException {
        setTargetTemperature(value);
    }

    @Override
    default double getInputValue() throws IOException, DeviceException {
        return getTemperature();
    }

    @Override
    default double getOutputValue() throws IOException, DeviceException {
        return getHeaterPower();
    }

    @Override
    default void setOutputValue(double value) throws IOException, DeviceException {
        setHeaterPower(value);
    }

    @Override
    default double getOutputRange() throws IOException, DeviceException {
        return getHeaterRange();
    }

    @Override
    default void setOutputRange(double range) throws IOException, DeviceException {
        setHeaterRange(range);
    }

    @Override
    default void useAutoOutput() throws IOException, DeviceException {
        useAutoHeater();
    }

    @Override
    default boolean isUsingAutoOutput() throws IOException, DeviceException {
        return isUsingAutoHeater();
    }

    /**
     * Returns the temperature measured by the controller.
     *
     * @return Temperature, in Kelvin
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    double getTemperature() throws IOException, DeviceException;

    /**
     * Returns the target temperature set on the controller.
     *
     * @return Target temperature, in Kelvin
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    double getTargetTemperature() throws IOException, DeviceException;

    /**
     * Sets the target temperature of the temperature controller.
     *
     * @param temperature Target temperature, in Kelvin
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    void setTargetTemperature(double temperature) throws IOException, DeviceException;

    default void setTemperature(double temperature) throws IOException, DeviceException {
        setTargetTemperature(temperature);
    }

    /**
     * Returns the heater output power percentage.
     *
     * @return Heater power, percentage of max
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    double getHeaterPower() throws IOException, DeviceException;

    /**
     * Returns the gas flow in whatever units the controller uses
     *
     * @return Gas flow (arbitrary units)
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    double getFlow() throws IOException, DeviceException;

    /**
     * Sets the heater to be operated automatically
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    void useAutoHeater() throws IOException, DeviceException;

    /**
     * Sets the heater to be operated manually with the specified power output percentage
     *
     * @param powerPCT Output power (percentage of max)
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    void setHeaterPower(double powerPCT) throws IOException, DeviceException;

    /**
     * Returns whether the heater is currently operating automatically or manually
     *
     * @return Automatic?
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    boolean isUsingAutoHeater() throws IOException, DeviceException;

    /**
     * Sets the gas flow to be controlled automatically
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    void useAutoFlow() throws IOException, DeviceException;

    /**
     * Sets the gas flow to be controlled manually with the specified output
     *
     * @param outputPCT Output
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    void setFlow(double outputPCT) throws IOException, DeviceException;

    /**
     * Returns whether the gas flow is currently controlled automatically or manually
     *
     * @return Automatic?
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    boolean isUsingAutoFlow() throws IOException, DeviceException;

    double getHeaterRange() throws IOException, DeviceException;

    void setHeaterRange(double rangePCT) throws IOException, DeviceException;

    default TMeter asThermometer() {
        return this;
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
    default void waitForStableTemperature(double temperature, double pctMargin, long time) throws IOException, DeviceException, InterruptedException {

        Synch.waitForStableTarget(
                this::getTemperature,
                temperature,
                pctMargin,
                100,
                time
        );

    }

}
