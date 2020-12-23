package jisa.devices;

import jisa.control.Synch;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public interface DCPower extends IVSource {

    @Override
    default List<Parameter<?>> getConfigurationParameters(Class<?> target) {

        List<Parameter<?>> parameters = new LinkedList<>();

        parameters.add(new Parameter<>("Voltage Limit [V]", 10.0, this::setVoltageLimit));
        parameters.add(new Parameter<>("Set Voltage [V]", new OptionalQuantity<>(false, 10.0), o -> {if (o.isUsed()) setVoltage(o.getValue());}));
        parameters.add(new Parameter<>("Set Current [A]", new OptionalQuantity<>(false, 10.0), o -> {if (o.isUsed()) setCurrent(o.getValue());}));

        return parameters;

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
