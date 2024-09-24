package jisa.devices.power;

import jisa.control.Synch;
import jisa.devices.DeviceException;
import jisa.devices.ParameterList;
import jisa.devices.source.IVSource;

import java.io.IOException;

public interface DCPower extends IVSource {

    static String getDescription() {
        return "DC Power Supply";
    }

    static void addParameters(DCPower inst, Class target, ParameterList parameters) {
        parameters.addValue("Voltage Limit [V]", inst::getVoltageLimit, 10.0, inst::setVoltageLimit);
        parameters.addOptional("Set Voltage [V]", false, 10.0, v -> {}, inst::setVoltage);
        parameters.addOptional("Set Current [A]", false, 1.0, i -> {}, inst::setCurrent);
    }

    /**
     * Enables the output of the power supply
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon device compatibility error
     */
    void turnOn() throws IOException, DeviceException;

    /**
     * Disables the output of the power supply
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon device compatibility error
     */
    void turnOff() throws IOException, DeviceException;

    /**
     * Enables or disables output of the power supply
     *
     * @param on Output enabled?
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon device compatibility error
     */
    default void setOn(boolean on) throws IOException, DeviceException {

        if (on) {
            turnOn();
        } else {
            turnOff();
        }

    }

    /**
     * Returns whether the output of the supply is enabled or not
     *
     * @return Is it enabled?
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon device compatibility error
     */
    boolean isOn() throws IOException, DeviceException;

    /**
     * Sets the voltage output level of the supply
     *
     * @param voltage Voltage, in Volts
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon device compatibility error
     */
    void setVoltage(double voltage) throws IOException, DeviceException;

    /**
     * Sets the current output level of the supply
     *
     * @param current Current, in Amps
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon device compatibility error
     */
    void setCurrent(double current) throws IOException, DeviceException;

    /**
     * Returns the voltage output of the supply
     *
     * @return Output voltage, in Volts
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon device compatibility error
     */
    double getVoltage() throws IOException, DeviceException;

    /**
     * Returns the current output of the supply
     *
     * @return Output current, in Amps
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon device compatibility error
     */
    double getCurrent() throws IOException, DeviceException;

    /**
     * Sets the voltage limit, also known as over-voltage protection. Setting a value of zero will disable this limit.
     *
     * @param limit Voltage limit, in Volts
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon device compatibility error
     */
    void setVoltageLimit(double limit) throws IOException, DeviceException;

    /**
     * Returns the currently set voltage limit (OVP). Zero means no limit is set.
     *
     * @return Voltage limit, in Volts
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon device compatibility error
     */
    double getVoltageLimit() throws IOException, DeviceException;

    /**
     * Sets the current protection limit
     *
     * @param current limit [A]
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon device compatibility error
     */
    void setCurrentLimit(double current) throws IOException, DeviceException;

    /**
     * Wait for the voltage output of the supply to be stable within the given percentage margin and time-frame
     *
     * @param pctError Margin of error, percentage
     * @param time     Minimum time for voltage to be considered stable, milliseconds
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon device compatibility error
     */
    default void waitForStableVoltage(double pctError, long time) throws IOException, DeviceException, InterruptedException {


        Synch.waitForParamStable(
                this::getVoltage,
                pctError,
                100,
                time
        );


    }

    /**
     * Wait for the current output of the supply to be stable within the given percentage margin and time-frame
     *
     * @param pctError Margin of error, percentage
     * @param time     Minimum time for voltage to be considered stable, milliseconds
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon device compatibility error
     */
    default void waitForStableCurrent(double pctError, long time) throws IOException, DeviceException, InterruptedException {

        Synch.waitForParamStable(
                this::getCurrent,
                pctError,
                100,
                time
        );

    }

}
