package jisa.devices.meter;

import jisa.control.Sync;
import jisa.devices.DeviceException;
import jisa.devices.ParameterList;
import jisa.enums.AMode;
import jisa.enums.TType;
import jisa.enums.Terminals;

import java.io.IOException;

public interface VMeter extends Meter {

    static String getDescription() {
        return "Voltmeter";
    }

    static void addParameters(VMeter inst, Class target, ParameterList params) {

        params.addChoice("Terminals", inst::getTerminals, Terminals.REAR, inst::setTerminals, Terminals.values());
        params.addAuto("Voltage Range [V]", true, 100.0, q -> inst.useAutoVoltageRange(), inst::setVoltageRange);
        params.addValue("Integration Time [s]", inst::getIntegrationTime, 20e-3, inst::setIntegrationTime);

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
     * Returns the measurement range currently being used by this voltmeter for voltage measurements. A range of n
     * indicates -n to +n.
     *
     * @return Voltage range, in Volts
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
        Sync.waitForParamStable(this::getVoltage, pctMargin, (int) (getIntegrationTime() * 4000.0), duration);
    }

}
