package jisa.devices.meter;

import jisa.control.Synch;
import jisa.devices.DeviceException;
import jisa.devices.Instrument;
import jisa.devices.ParameterList;
import jisa.devices.features.LineFilter;
import jisa.devices.features.LineFilter2X;
import jisa.enums.AMode;
import jisa.enums.TType;
import jisa.enums.Terminals;

import java.io.IOException;
import java.util.List;

public interface VMeter extends Meter, Instrument {

    static String getDescription() {
        return "Voltmeter";
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
    double getIntegrationTime() throws IOException, DeviceException;

    /**
     * Sets the integration time for each measurement.
     *
     * @param time Integration time, in seconds.
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void setIntegrationTime(double time) throws IOException, DeviceException;

    /**
     * Returns the measurement range being used for voltage measurements. A range of n indicates -n to +n.
     *
     * @return Range being used, in Volts
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
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
    void setVoltageRange(double range) throws IOException, DeviceException;

    /**
     * Tells the voltmeter to use auto-ranging for voltage measurements.
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void useAutoVoltageRange() throws IOException, DeviceException;

    /**
     * Returns whether the voltmeter is using auto-ranging for voltage measurements.
     *
     * @return Is it auto-ranging?
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    boolean isAutoRangingVoltage() throws IOException, DeviceException;

    /**
     * Returns the averaging mode being used for measurements by the voltmeter.
     *
     * @return Averaging mode
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    AMode getAverageMode() throws IOException, DeviceException;

    /**
     * Sets the averaging mode used for taking each measurement.
     *
     * @param mode Averaging mode to use
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void setAverageMode(AMode mode) throws IOException, DeviceException;

    /**
     * Returns the number of measurements used for averaging by the voltmeter.
     *
     * @return Number of measurements
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    int getAverageCount() throws IOException, DeviceException;

    /**
     * Sets the number of measurements to use for averaging.
     *
     * @param count Number of measurements
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
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
    boolean isOn() throws IOException, DeviceException;

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
    Terminals getTerminals() throws DeviceException, IOException;

    /**
     * Sets which set of terminals should be used on the voltmeter.
     *
     * @param terminals Which type of terminals to use
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void setTerminals(Terminals terminals) throws DeviceException, IOException;

    default void waitForStableVoltage(double pctMargin, int duration) throws IOException, DeviceException, InterruptedException {
        Synch.waitForParamStable(this::getVoltage, pctMargin, (int) (getIntegrationTime() * 4000.0), duration);
    }

    default List<Parameter<?>> getBaseParameters(Class<?> target) {


        ParameterList params = new ParameterList();

        params.addChoice("Terminals", Terminals.FRONT, this::setTerminals, Terminals.values());
        params.addAuto("Voltage Range [V]", true, 100.0, q -> useAutoVoltageRange(), this::setVoltageRange);
        params.addValue("Integration Time [s]", 20e-3, this::setIntegrationTime);

        if (this instanceof LineFilter) {
            params.addValue("Line Filter", true, ((LineFilter) this)::setLineFilterEnabled);
        }

        if (this instanceof LineFilter2X) {
            params.addValue("2x Line Filter", true, ((LineFilter2X) this)::set2xLineFilterEnabled);
        }

        return params;

    }

}
