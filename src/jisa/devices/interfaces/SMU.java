package jisa.devices.interfaces;

import jisa.Util;
import jisa.devices.DeviceException;
import jisa.enums.*;
import jisa.experiment.IVPoint;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public interface SMU extends IVMeter, IVSource, Channel<SMU> {

    public static String getDescription() {
        return "Source Measure Unit";
    }

    String getChannelName();

    default Class<SMU> getChannelClass() {
        return SMU.class;
    }

    /**
     * Returns the voltage either being applied or measured by the SMU.
     *
     * @return Voltage value
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    double getVoltage() throws DeviceException, IOException;

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
     * Sets the voltage value to be applied by the SMU (switching to voltage source mode if not already)
     *
     * @param voltage Value to set
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void setVoltage(double voltage) throws DeviceException, IOException;

    /**
     * Returns the current either being injected or measured by the SMU.
     *
     * @return Current value
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    double getCurrent() throws DeviceException, IOException;

    /**
     * Takes a current measurement using a specified one-off integration time.
     *
     * @param integrationTime Integration time to use in seconds
     *
     * @return Current measurement value, in Amps
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    default double getCurrent(double integrationTime) throws DeviceException, IOException {

        synchronized (getLockObject()) {

            double prevTime = getIntegrationTime();
            setIntegrationTime(integrationTime);
            double current = getCurrent();
            setIntegrationTime(prevTime);
            return current;

        }

    }

    /**
     * Sets the current value to be applied by the SMU (switching to current source mode if not already)
     *
     * @param current Value to set
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void setCurrent(double current) throws DeviceException, IOException;

    /**
     * Pulses the voltage output of the SMU, returning the measured current value after a set amount of time before returning voltage to base.
     * By default controlled and timed by computer but should be overridden in SMUs that support internal timing mechanisms.
     * <p>
     * 1. Turn off voltage output
     * 2. Wait for offTime seconds
     * 3. V set to puleVoltage
     * 4. Wait for measureDelay seconds
     * 5. I measured
     * 6. V set to baseVoltage
     * 7. Turn off voltage output
     * 8. Return previously measured current value
     *
     * @param pulseVoltage Voltage to pulse
     * @param offTime      Time to wait at base voltage before pulse
     * @param measureDelay Time to wait at pulse voltage before current measurement
     *
     * @return Current measurement taken during pulse
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    default double pulseVoltage(double pulseVoltage, double offTime, double measureDelay) throws DeviceException, IOException {

        int base    = (int) (offTime * 1000);
        int measure = (int) (measureDelay * 1000);

        turnOff();
        setVoltage(pulseVoltage);
        Util.sleep(base);
        turnOn();
        Util.sleep(measure);
        double current = getCurrent();
        turnOff();
        return current;

    }

    /**
     * Turns the output of the SMU on
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void turnOn() throws DeviceException, IOException;

    /**
     * Turns the output of the SMU off
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void turnOff() throws DeviceException, IOException;

    /**
     * Checks whether the output of the SMU is currently enabled
     *
     * @return Is the output on?
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    boolean isOn() throws DeviceException, IOException;

    /**
     * Returns the current source mode of the SMU (VOLTAGE OR CURRENT)
     *
     * @return Source mode
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    Source getSource() throws DeviceException, IOException;

    /**
     * Sets the source mode of the SMU (VOLTAGE or CURRENT)
     *
     * @param source Source mode to set
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void setSource(Source source) throws DeviceException, IOException;

    /**
     * Sets the value for whichever parameter is currently being sourced
     *
     * @param level The level to set
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void setSourceValue(double level) throws DeviceException, IOException;

    /**
     * Returns the value of whichever parameter is set as source currently
     *
     * @return Value of source
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    double getSourceValue() throws DeviceException, IOException;

    /**
     * Returns the value of whichever parameter is set as measure currently
     *
     * @return Value of measure
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    double getMeasureValue() throws DeviceException, IOException;

    /**
     * Returns whether the device is currently configured to use all four probes.
     *
     * @return Are all probes to be used?
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    boolean isFourProbeEnabled() throws DeviceException, IOException;

    /**
     * Sets whether the SMU should apply source using FORCE probes and measure using separate SENSE probes or whether is should
     * do both with the FORCE probes.
     *
     * @param fourProbes Should it use all four probes?
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void setFourProbeEnabled(boolean fourProbes) throws DeviceException, IOException;

    /**
     * Sets both the averaging mode and count together.
     *
     * @param mode  Averaging mode
     * @param count Averaging count
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    default void setAveraging(AMode mode, int count) throws DeviceException, IOException {
        setAverageMode(mode);
        setAverageCount(count);
    }

    /**
     * Returns the averaging mode of the SMU.
     *
     * @return Mode being used
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    AMode getAverageMode() throws DeviceException, IOException;

    /**
     * Sets the averaging mode of the SMU.
     *
     * @param mode Mode to use
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void setAverageMode(AMode mode) throws DeviceException, IOException;

    /**
     * Returns the number of measurements used for averaging by the SMU.
     *
     * @return Number of measurements
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    int getAverageCount() throws DeviceException, IOException;

    /**
     * Sets how many measurements the SMU should average over.
     *
     * @param count Number of measurements
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void setAverageCount(int count) throws DeviceException, IOException;

    /**
     * Returns the range of allowed values for the quantity being sourced by the SMU.
     * A value of n indicates a range of -n to +n.
     *
     * @return Range value, in Volts or Amps
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    double getSourceRange() throws DeviceException, IOException;

    /**
     * Sets the range of allowed values for the quantity being sourced by the SMU.
     * A value of n indicates a range of -n to +n.
     *
     * @param value Range value, in Volts or Amps
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void setSourceRange(double value) throws DeviceException, IOException;

    /**
     * Sets the SMU to automatically determine the source range to use.
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void useAutoSourceRange() throws DeviceException, IOException;

    /**
     * Returns whether the SMU is set to automatically determine the source range to use.
     *
     * @return Automatic?
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    boolean isAutoRangingSource() throws DeviceException, IOException;

    /**
     * Returns the range of allowed values for the quantity being measured by the SMU.
     *
     * @return Range value, in Volts or Amps
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    double getMeasureRange() throws DeviceException, IOException;

    /**
     * Sets the range of allowed values for the quantity being measured by the SMU.
     *
     * @param value Range value, in Volts or Amps
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void setMeasureRange(double value) throws DeviceException, IOException;

    /**
     * Tells the SMU to automatically determine the range to use for the measured quantity.
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void useAutoMeasureRange() throws DeviceException, IOException;

    /**
     * Returns whether the SMU is currently determining its measure range automatically.
     *
     * @return Is it automatic?
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    boolean isAutoRangingMeasure() throws DeviceException, IOException;

    /**
     * Returns the range of allowed values for voltages being sourced or measured by the SMU.
     * A value of n indicates a range of -n to +n.
     *
     * @return Range value, in Volts
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    double getVoltageRange() throws DeviceException, IOException;

    /**
     * Sets the range of allowed values for voltages being sourced or measured by the SMU.
     * A value of n indicates a range of -n to +n.
     *
     * @param value Range value, in Volts
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void setVoltageRange(double value) throws DeviceException, IOException;

    /**
     * Tells the SMU to automatically determine the range it uses for voltages.
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void useAutoVoltageRange() throws DeviceException, IOException;

    /**
     * Returns whether the SMU is automatically determining the range to use for voltages.
     *
     * @return Is it automatic?
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    boolean isAutoRangingVoltage() throws DeviceException, IOException;

    /**
     * Returns the range of allowed values for currents being sourced or measured by the SMU.
     *
     * @return Range value, inAmps
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    double getCurrentRange() throws DeviceException, IOException;

    /**
     * Sets the range of allowed values for currents being sourced or measured by the SMU.
     *
     * @param value Range value, in Amps
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void setCurrentRange(double value) throws DeviceException, IOException;

    /**
     * Tells the SMU to automatically determine the range to use for current values.
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void useAutoCurrentRange() throws DeviceException, IOException;

    /**
     * Returns whether the SMU is currently determining its current range automatically.
     *
     * @return Is it automatic?
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    boolean isAutoRangingCurrent() throws DeviceException, IOException;

    /**
     * Returns the limit for the measured quantity (compliance value).
     *
     * @return The limit, in Volts or Amps
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    double getOutputLimit() throws DeviceException, IOException;

    /**
     * Sets the limit for the measured quantity (ie compliance value).
     *
     * @param value The limit to use, in Volts or Amps
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void setOutputLimit(double value) throws DeviceException, IOException;

    /**
     * Returns the limit on voltages output when sourcing current (compliance value).
     *
     * @return The limit, in Volts
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    double getVoltageLimit() throws DeviceException, IOException;

    /**
     * Sets the limit for voltages output when sourcing current (compliance value).
     *
     * @param voltage The limit, in Volts
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void setVoltageLimit(double voltage) throws DeviceException, IOException;

    /**
     * Returns the limit for currents output when sourcing voltage (compliance value).
     *
     * @return The limit, in Amps
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    double getCurrentLimit() throws DeviceException, IOException;

    /**
     * Sets the limit for currents output when sourcing voltage (compliance value).
     *
     * @param current The limit, in Amps
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void setCurrentLimit(double current) throws DeviceException, IOException;

    /**
     * Returns the integration time used for each individual measurement.
     *
     * @return Integration time, in seconds
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    double getIntegrationTime() throws DeviceException, IOException;

    /**
     * Sets the integration time for each individual measurement, or closest over-estimate possible.
     *
     * @param time Integration time, in seconds
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void setIntegrationTime(double time) throws DeviceException, IOException;

    /**
     * Returns what type of connector is used for the given terminal.
     *
     * @param terminals Which terminal
     *
     * @return Terminal type (TRIAX, PHOENIX, BNC or BANANA)
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    TType getTerminalType(Terminals terminals) throws DeviceException, IOException;

    /**
     * Returns the type of the set of terminals currently being used on the SMU.
     *
     * @return The type of terminals being used
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    Terminals getTerminals() throws DeviceException, IOException;

    /**
     * Sets which set of terminals should be used on the SMU.
     *
     * @param terminals Which type of terminals to use
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void setTerminals(Terminals terminals) throws DeviceException, IOException;

    /**
     * Set the instrument for 4-wire or 2-wire sense with appropriate functions
     *
     * @param funcType set the function type to Current, Voltage, Resistance
     * @param enableSense True to turn ON output, false to turn OFF output
     *                    The sense lines are automatically reconnected when the output is turned ON
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void setProbeMode(Function funcType, boolean enableSense) throws DeviceException, IOException;

    /**
     * Returns the mode used by the SMU channel when turned off.
     *
     * @return Mode being used
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    OffMode getOffMode() throws DeviceException, IOException;

    /**
     * Sets which mode the SMU channel should use when turned off.
     *
     * @param mode Mode to use
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void setOffMode(OffMode mode) throws DeviceException, IOException;

    /**
     * Returns whether the voltmeter is using any line-frequency filtering
     *
     * @return Using line filter?
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    boolean isLineFilterEnabled() throws DeviceException, IOException;

    /**
     * Sets whether the voltmeter should use any line-frequency filtering (if available)
     *
     * @param enabled Use line filter?
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void setLineFilterEnabled(boolean enabled) throws DeviceException, IOException;

    /**
     * Returns a combined voltage and current measurement.
     *
     * @return Voltage and Current
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    default IVPoint getIVPoint() throws DeviceException, IOException {
        return new IVPoint(getVoltage(), getCurrent());
    }

    /**
     * Configures the SMU to act as a voltmeter, returning a VMeter representation of itself.
     *
     * @return This SMU, as a VMeter
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    default VMeter asVoltmeter() throws IOException, DeviceException {

        setSource(Source.CURRENT);
        setCurrent(0.0);
        return this;

    }

    /**
     * Configures the SMU to act as an ammeter, returning an IMeter representation of itself.
     *
     * @return This SMU, as an IMeter
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    default IMeter asAmmeter() throws IOException, DeviceException {

        setSource(Source.VOLTAGE);
        setVoltage(0.0);
        return this;

    }

    /**
     * Sets the value ranges for both voltage and current.
     *
     * @param voltageRange Range to use for voltage
     * @param currentRange Range to use for current
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    default void setRanges(double voltageRange, double currentRange) throws DeviceException, IOException {
        setVoltageRange(voltageRange);
        setCurrentRange(currentRange);
    }

    /**
     * Tells the SMU to use auto-ranging for both current and voltage.
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    default void useAutoRanges() throws DeviceException, IOException {
        useAutoVoltageRange();
        useAutoCurrentRange();
    }

    /**
     * Sets the limits (compliance values) for both voltage and current.
     *
     * @param voltageLimit Limit for voltage when not being sourced
     * @param currentLimit Limit for current when not being sourced
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    default void setLimits(double voltageLimit, double currentLimit) throws DeviceException, IOException {
        setVoltageLimit(voltageLimit);
        setCurrentLimit(currentLimit);
    }

    @Override
    default List<Parameter<?>> getConfigurationParameters(Class<?> target) {

        List<Parameter<?>> parameters = new LinkedList<>();

        parameters.add(new Parameter<>("Terminals", Terminals.FRONT, this::setTerminals, Terminals.values()));
        parameters.add(new Parameter<>("Voltage Limit [V]", 200.0, this::setVoltageLimit));
        parameters.add(new Parameter<>("Current Limit [A]", 100e-3, this::setCurrentLimit));
        parameters.add(new Parameter<>("Voltage Range [V]", new AutoQuantity<>(true, 0.0), q -> { if (q.isAuto()) this.useAutoVoltageRange(); else this.setVoltageRange(q.getValue()); }));
        parameters.add(new Parameter<>("Current Range [A]", new AutoQuantity<>(true, 0.0), q -> { if (q.isAuto()) this.useAutoCurrentRange(); else this.setCurrentRange(q.getValue()); }));
        parameters.add(new Parameter<>("Integration Time [s]", 20e-3, this::setIntegrationTime));
        parameters.add(new Parameter<>("Four Point Probe", false, this::setFourProbeEnabled));

        if (target.equals(IMeter.class)) {
            parameters.add(new Parameter<>("Set Voltage [V]", new OptionalQuantity<>(false, 0.0), q -> { if (q.isUsed()) this.setVoltage(q.getValue()); }));
        }

        if (target.equals(VMeter.class)) {
            parameters.add(new Parameter<>("Set Current [A]", new OptionalQuantity<>(false, 0.0), q -> { if (q.isUsed()) this.setCurrent(q.getValue()); }));
        }

        return parameters;

    }

    enum OffMode {
        NORMAL,
        ZERO,
        HIGH_IMPEDANCE,
        GUARD
    }

}
