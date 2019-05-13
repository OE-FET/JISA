package JISA.Devices;

import JISA.Addresses.Address;
import JISA.Control.Synch;
import JISA.VISA.Driver;
import JISA.VISA.VISADevice;
import org.python.antlr.op.Add;

import java.io.IOException;

public abstract class DCPower extends VISADevice {

    public DCPower(Address address, Class<? extends Driver> prefDriver) throws IOException {
        super(address, prefDriver);
    }

    public DCPower(Address address) throws IOException {
        this(address, null);
    }

    /**
     * Enables the output of the power supply
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon device compatibility error
     */
    public abstract void turnOn() throws IOException, DeviceException;

    /**
     * Disables the output of the power supply
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon device compatibility error
     */
    public abstract void turnOff() throws IOException, DeviceException;

    /**
     * Returns whether the output of the supply is enabled or not
     *
     * @return Is it enabled?
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon device compatibility error
     */
    public abstract boolean isOn() throws IOException, DeviceException;

    /**
     * Sets the voltage output level of the supply
     *
     * @param voltage Voltage, in Volts
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon device compatibility error
     */
    public abstract void setVoltage(double voltage) throws IOException, DeviceException;

    /**
     * Sets the current output level of the supply
     *
     * @param current Current, in Amps
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon device compatibility error
     */
    public abstract void setCurrent(double current) throws IOException, DeviceException;

    /**
     * Returns the voltage output of the supply
     *
     * @return Output voltage, in Volts
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon device compatibility error
     */
    public abstract double getVoltage() throws IOException, DeviceException;

    /**
     * Returns the current output of the supply
     *
     * @return Output current, in Amps
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon device compatibility error
     */
    public abstract double getCurrent() throws IOException, DeviceException;

    /**
     * Wait for the voltage output of the supply to be stable within the given percentage margin and time-frame
     *
     * @param pctError Margin of error, percentage
     * @param time     Minimum time for voltage to be considered stable, milliseconds
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon device compatibility error
     */
    public void waitForStableVoltage(double pctError, long time) throws IOException, DeviceException, InterruptedException {


        Synch.waitForParamStable(
                this::getVoltage,
                pctError,
                100,
                time
        );


    }

    /**
     * Wait for the voltage output of the supply to be stable within 0.1% for at least 5 seconds
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon device compatibility error
     */
    public void waitForStableVoltage() throws IOException, DeviceException, InterruptedException {
        waitForStableVoltage(0.1, 5000);
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
    public void waitForStableCurrent(double pctError, long time) throws IOException, DeviceException, InterruptedException {

        Synch.waitForParamStable(
                this::getCurrent,
                pctError,
                100,
                time
        );

    }

    /**
     * Wait for the current output of the supply to be stable within 0.1% for at least 5 seconds
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon device compatibility error
     */
    public void waitForStableCurrent() throws IOException, DeviceException, InterruptedException {
        waitForStableCurrent(0.1, 5000);
    }

    /**
     * Sets the voltage of the supply and waits for the actual output to reach the set value, within the given error percentage
     *
     * @param voltage  Voltage to set, in Volts
     * @param pctError Percentage error margin
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon device compatibility error
     */
    public void setVoltageAndWait(double voltage, double pctError) throws IOException, DeviceException {

        setVoltage(voltage);

        if (!isOn()) {
            turnOn();
        }

        Synch.waitForParamWithinError(
                this::getVoltage,
                voltage,
                pctError,
                100
        );

    }

    /**
     * Sets the voltage of the supply and waits for the actual output to be within 0.1% of the set value
     *
     * @param voltage Voltage to set, in Volts
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon device compatibility error
     */
    public void setVoltageAndWait(double voltage) throws IOException, DeviceException {
        setVoltageAndWait(voltage, 0.1);
    }

    /**
     * Sets the current of the supply and waits for the actual output to reach the set value, within the given error percentage
     *
     * @param current  Current to set, in Amps
     * @param pctError Percentage error margin
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon device compatibility error
     */
    public void setCurrentAndWait(double current, double pctError) throws IOException, DeviceException {

        setCurrent(current);

        if (!isOn()) {
            turnOn();
        }

        Synch.waitForParamWithinError(
                this::getCurrent,
                current,
                pctError,
                100
        );

    }

    /**
     * Sets the current of the supply and waits for the actual output to be within 0.1% of the set value
     *
     * @param current Current to set, in Amps
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon device compatibility error
     */
    public void setCurrentAndWait(double current) throws IOException, DeviceException {
        setCurrentAndWait(current, 0.1);
    }

}
