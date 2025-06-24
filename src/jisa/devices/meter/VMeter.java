package jisa.devices.meter;

import jisa.control.Sync;
import jisa.devices.DeviceException;
import jisa.devices.Instrument;
import jisa.devices.ParameterList;
import jisa.enums.AMode;
import jisa.enums.TType;
import jisa.enums.Terminals;

import java.io.IOException;

public interface VMeter extends Meter, Instrument {

    static String getDescription() {
        return "Voltmeter";
    }

    static void addParameters(VMeter inst, Class target, ParameterList params) {

        params.addChoice("Terminals", inst::getTerminals, Terminals.REAR, inst::setTerminals, Terminals.values());
        params.addAuto("Voltage Range [V]", true, 100.0, q -> inst.useAutoVoltageRange(), inst::setVoltageRange);
        params.addValue("Integration Time [s]", inst::getIntegrationTime, 20e-3, inst::setIntegrationTime);

    }

    @Override
    default double getValue() throws IOException, DeviceException {
        return getVoltage();
    }

    @Override
    default String getMeasuredQuantity() throws IOException, DeviceException {
        return "Voltage";
    }

    @Override
    default String getMeasuredUnits() throws IOException, DeviceException {
        return "V";
    }

    /**
     * Takes a voltage measurement and returns the value.
     *
     * @return Voltage measurement value, in Volts
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    @QuantityGetter(name="Voltage", units="V")
    double getVoltage() throws IOException, DeviceException;

    /**
     * Takes a voltage measurement using a specified one-off integration time.
     *
     * @param integrationTime Integration time to use in seconds
     *
     * @return Voltage measurement value, in Volts
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    default double getVoltage(double integrationTime) throws DeviceException, IOException {

        synchronized (getLockObject()) {

            double prevTime = getIntegrationTime();
            setIntegrationTime(integrationTime);
            double voltage = getVoltage();
            setIntegrationTime(prevTime);
            return voltage;

        }

    }

    /**
     * Returns the integration time being used for measurements.
     *
     * @return Integration time, in seconds.
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    @ParameterGetter(name="Integration Time", units="s")
    double getIntegrationTime() throws IOException, DeviceException;

    /**
     * Sets the integration time for each measurement.
     *
     * @param time Integration time, in seconds.
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    @ParameterSetter(name="Integration Time", units="s")
    void setIntegrationTime(double time) throws IOException, DeviceException;

    /**
     * Returns the measurement range being used for voltage measurements. A range of n indicates -n to +n.
     *
     * @return Range being used, in Volts
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    @ParameterGetter(name="Voltage Range", units="V")
    double getVoltageRange() throws IOException, DeviceException;

    /**
     * Sets the measurement range to use for voltage measurements. If only discrete options are available, the smallest
     * range that contains the supplied value is used. A range of n indicates -n to +n.
     *
     * @param range Range to use, in Volts
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    @ParameterSetter(name="Voltage Range", units="V")
    void setVoltageRange(double range) throws IOException, DeviceException;

    /**
     * Tells the voltmeter to use auto-ranging for voltage measurements.
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    @AutoParameterSetter(name="Voltage Range")
    void useAutoVoltageRange() throws IOException, DeviceException;

    /**
     * Returns whether the voltmeter is using auto-ranging for voltage measurements.
     *
     * @return Is it auto-ranging?
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    @AutoParameterGetter(name="Voltage Range")
    boolean isAutoRangingVoltage() throws IOException, DeviceException;

    /**
     * Returns the averaging mode being used for measurements by the voltmeter.
     *
     * @return Averaging mode
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    @ParameterGetter(name="Averaging Mode")
    AMode getAverageMode() throws IOException, DeviceException;

    /**
     * Sets the averaging mode used for taking each measurement.
     *
     * @param mode Averaging mode to use
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    @ParameterSetter(name="Averaging Mode")
    void setAverageMode(AMode mode) throws IOException, DeviceException;

    /**
     * Returns the number of measurements used for averaging by the voltmeter.
     *
     * @return Number of measurements
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    @ParameterGetter(name="Averaging Count")
    int getAverageCount() throws IOException, DeviceException;

    /**
     * Sets the number of measurements to use for averaging.
     *
     * @param count Number of measurements
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    @ParameterSetter(name="Averaging Count")
    void setAverageCount(int count) throws IOException, DeviceException;

    /**
     * Turns on the voltmeter.
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void turnOn() throws IOException, DeviceException;

    /**
     * Turns off the voltmeter.
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void turnOff() throws IOException, DeviceException;

    /**
     * Returns whether the voltmeter is on or not.
     *
     * @return Is it on?
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    @QuantityGetter(name="Output Enabled")
    boolean isOn() throws IOException, DeviceException;

    @QuantitySetter(name="Output Enabled")
    default void setOn(boolean on) throws IOException, DeviceException {

        if (on) {
            turnOn();
        } else {
            turnOff();
        }

    }

    /**
     * Returns what type of connector is used for the given terminal.
     *
     * @param terminals Which terminal
     *
     * @return Terminal type (TRIAX, PHOENIX, BNC or BANANA)
     *
     * @throws DeviceException
     * @throws IOException
     */
    TType getTerminalType(Terminals terminals) throws DeviceException, IOException;

    /**
     * Returns the type of the set of terminals currently being used on the voltmeter.
     *
     * @return The type of terminals being used
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    @ParameterGetter(name="Terminals")
    Terminals getTerminals() throws DeviceException, IOException;

    /**
     * Sets which set of terminals should be used on the voltmeter.
     *
     * @param terminals Which type of terminals to use
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    @ParameterSetter(name="Terminals")
    void setTerminals(Terminals terminals) throws DeviceException, IOException;

    default void waitForStableVoltage(double pctMargin, int duration) throws IOException, DeviceException, InterruptedException {
        Sync.waitForParamStable(this::getVoltage, pctMargin, (int) (getIntegrationTime() * 4000.0), duration);
    }

}
