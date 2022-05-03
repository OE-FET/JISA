package jisa.devices.interfaces;

import jisa.Util;
import jisa.addresses.Address;
import jisa.devices.DeviceException;
import jisa.enums.*;
import jisa.experiment.IVPoint;
import jisa.experiment.MCIVPoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class defining the standard interface for controller Multiple-Channel SMUs.
 */
public interface MCSMU extends SMU, MultiChannel<SMU> {

    public static String getDescription() {
        return "Multi-Channel Source Measure Unit";
    }

    String getChannelName(int channel);

    /**
     * Returns the voltage of the specified channel
     *
     * @param channel Channel number
     *
     * @return Voltage, in Volts
     *
     * @throws DeviceException Upon device compatibility error
     * @throws IOException     Upon communications error
     */
    double getVoltage(int channel) throws DeviceException, IOException;


    /**
     * Returns the voltage of the default channel
     *
     * @return Voltage, in Volts
     *
     * @throws DeviceException Upon device compatibility error
     * @throws IOException     Upon communications error
     */
    default double getVoltage() throws DeviceException, IOException {
        return getVoltage(0);
    }

    /**
     * Sets the default channel to source the given voltage (when turned on)
     *
     * @param voltage Voltage to source, in Volts
     *
     * @throws DeviceException Upon device compatibility error
     * @throws IOException     Upon communications error
     */
    default void setVoltage(double voltage) throws DeviceException, IOException {
        for (int cn = 0; cn < getNumChannels(); cn++) {
            setVoltage(cn, voltage);
        }
    }

    /**
     * Returns the current of the specified channel
     *
     * @param channel Channel number
     *
     * @return Current, in Amps
     *
     * @throws DeviceException Upon device compatibility error
     * @throws IOException     Upon communications error
     */
    double getCurrent(int channel) throws DeviceException, IOException;

    /**
     * Returns the current of the default channel
     *
     * @return Current, in Amps
     *
     * @throws DeviceException Upon device compatibility error
     * @throws IOException     Upon communications error
     */
    default double getCurrent() throws DeviceException, IOException {
        return getCurrent(0);
    }

    /**
     * Sets the default channel to source the given current (when turned on)
     *
     * @param current Current to source, in Amps
     *
     * @throws DeviceException Upon device compatibility error
     * @throws IOException     Upon communications error
     */
    default void setCurrent(double current) throws DeviceException, IOException {
        for (int cn = 0; cn < getNumChannels(); cn++) {
            setCurrent(cn, current);
        }
    }

    /**
     * Sets the specified channel to source the given voltage (when turned on)
     *
     * @param channel Channel number
     * @param voltage Voltage to source, in Volts
     *
     * @throws DeviceException Upon device compatibility error
     * @throws IOException     Upon communications error
     */
    void setVoltage(int channel, double voltage) throws DeviceException, IOException;

    /**
     * Sets the specified channel to source the given current (when turned on)
     *
     * @param channel Channel number
     * @param current Current to source, in Amps
     *
     * @throws DeviceException Upon device compatibility error
     * @throws IOException     Upon communications error
     */
    void setCurrent(int channel, double current) throws DeviceException, IOException;

    /**
     * Enables output on the specified channel
     *
     * @param channel Channel number
     *
     * @throws DeviceException Upon device compatibility error
     * @throws IOException     Upon communications error
     */
    void turnOn(int channel) throws DeviceException, IOException;

    /**
     * Enables output on all channels
     *
     * @throws DeviceException Upon device compatibility error
     * @throws IOException     Upon communications error
     */
    default void turnOn() throws DeviceException, IOException {
        for (int cn = 0; cn < getNumChannels(); cn++) {
            turnOn(cn);
        }
    }

    /**
     * Disables output on the specified channel
     *
     * @param channel Channel number
     *
     * @throws DeviceException Upon device compatibility error
     * @throws IOException     Upon communications error
     */
    void turnOff(int channel) throws DeviceException, IOException;

    /**
     * Disables output on all channels
     *
     * @throws DeviceException Upon device compatibility error
     * @throws IOException     Upon communications error
     */
    default void turnOff() throws DeviceException, IOException {
        for (int cn = 0; cn < getNumChannels(); cn++) {
            turnOff(cn);
        }
    }

    /**
     * Returns whether the specified channel currently has its output enabled
     *
     * @param channel Channel number
     *
     * @return Is it enabled?
     *
     * @throws DeviceException Upon device compatibility error
     * @throws IOException     Upon communications error
     */
    boolean isOn(int channel) throws DeviceException, IOException;

    /**
     * Returns whether the default channel currently has its output enabled
     *
     * @return Is it enabled?
     *
     * @throws DeviceException Upon device compatibility error
     * @throws IOException     Upon communications error
     */
    default boolean isOn() throws DeviceException, IOException {
        return isOn(0);
    }

    /**
     * Sets the source mode of the specified channel
     *
     * @param channel Channel number
     * @param source  VOLTAGE or CURRENT
     *
     * @throws DeviceException Upon device compatibility error
     * @throws IOException     Upon communications error
     */
    void setSource(int channel, Source source) throws DeviceException, IOException;

    /**
     * Returns the source mode of the specified channel
     *
     * @param channel Channel number
     *
     * @return Source mode (VOLTAGE or CURRENT)
     *
     * @throws DeviceException Upon device compatibility error
     * @throws IOException     Upon communications error
     */
    Source getSource(int channel) throws DeviceException, IOException;

    /**
     * Returns the source mode of the default channel
     *
     * @return Source mode (VOLTAGE or CURRENT)
     *
     * @throws DeviceException Upon device compatibility error
     * @throws IOException     Upon communications error
     */
    default Source getSource() throws DeviceException, IOException {
        return getSource(0);
    }

    /**
     * Sets the source mode of all channels
     *
     * @param source VOLTAGE or CURRENT
     *
     * @throws DeviceException Upon device compatibility error
     * @throws IOException     Upon communications error
     */
    default void setSource(Source source) throws DeviceException, IOException {
        for (int cn = 0; cn < getNumChannels(); cn++) {
            setSource(cn, source);
        }
    }

    /**
     * Sets the level of whichever quantity is being sourced on the specified channel
     *
     * @param channel Channel number
     * @param level   Volts or Amps
     *
     * @throws DeviceException Upon device compatibility error
     * @throws IOException     Upon communications error
     */
    void setBias(int channel, double level) throws DeviceException, IOException;

    /**
     * Sets the level of whichever quantity is being sourced on all channels
     *
     * @param level Volts or Amps
     *
     * @throws DeviceException Upon device compatibility error
     * @throws IOException     Upon communications error
     */
    default void setSourceValue(double level) throws DeviceException, IOException {
        for (int cn = 0; cn < getNumChannels(); cn++) {
            setBias(cn, level);
        }
    }

    /**
     * Returns the value of whichever quantity is being sourced on the specified channel
     *
     * @param channel Channel number
     *
     * @return Voltage or Current, in Volts or Amps
     *
     * @throws DeviceException Upon device compatibility error
     * @throws IOException     Upon communications error
     */
    double getSourceValue(int channel) throws DeviceException, IOException;

    /**
     * Returns the value of whichever quantity is being sourced on the default channel
     *
     * @return Voltage or Current, in Volts or Amps
     *
     * @throws DeviceException Upon device compatibility error
     * @throws IOException     Upon communications error
     */
    default double getSourceValue() throws DeviceException, IOException {
        return getSourceValue(0);
    }

    /**
     * Returns the value of whichever quantity is being measured on the specified channel
     *
     * @param channel Channel number
     *
     * @return Voltage or Current, in Volts or Amps
     *
     * @throws DeviceException Upon device compatibility error
     * @throws IOException     Upon communications error
     */
    double getMeasureValue(int channel) throws DeviceException, IOException;

    /**
     * Returns the value of whichever quantity is being measured on the default channel
     *
     * @return Voltage or Current, in Volts or Amps
     *
     * @throws DeviceException Upon device compatibility error
     * @throws IOException     Upon communications error
     */
    default double getMeasureValue() throws DeviceException, IOException {
        return getMeasureValue(0);
    }

    /**
     * Returns the number of channels this SMU has.
     *
     * @return Number of channels
     */
    int getNumChannels();

    /**
     * Sets whether the SMU should apply source using FORCE probes and measure using separate SENSE probes or whether is should
     * do both with the FORCE probes on the specified channel.
     *
     * @param channel    Channel number
     * @param fourProbes Should it use all four probes?
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void setFourProbeEnabled(int channel, boolean fourProbes) throws DeviceException, IOException;

    /**
     * Sets whether the SMU should apply source using FORCE probes and measure using separate SENSE probes or whether is should
     * do both with the FORCE probes on all channels.
     *
     * @param fourProbes Should it use all four probes?
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    default void setFourProbeEnabled(boolean fourProbes) throws DeviceException, IOException {

        for (int cn = 0; cn < getNumChannels(); cn++) {
            setFourProbeEnabled(cn, fourProbes);
        }

    }

    /**
     * Returns whether the device is currently configured to use all four probes on the specified channel.
     *
     * @param channel Channel number
     *
     * @return Are all probes to be used?
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    boolean isFourProbeEnabled(int channel) throws DeviceException, IOException;

    /**
     * Returns whether the device is currently configured to use all four probes on the default channel.
     *
     * @return Are all probes to be used?
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    default boolean isFourProbeEnabled() throws DeviceException, IOException {
        return isFourProbeEnabled(0);
    }

    /**
     * Sets which type of averaging the SMU should use when making a measurement on the specified channel.
     *
     * @param channel Channel number
     * @param mode    Averaging mode
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void setAverageMode(int channel, AMode mode) throws DeviceException, IOException;

    /**
     * Sets how many measurements to use when averaging on the specified channel.
     *
     * @param channel Channel number
     * @param count   Averaging count
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void setAverageCount(int channel, int count) throws DeviceException, IOException;

    /**
     * Sets both the averaging mode and count together for the specified channel.
     *
     * @param channel Channel number
     * @param mode    Averaging mode
     * @param count   Averaging count
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    default void setAveraging(int channel, AMode mode, int count) throws DeviceException, IOException {
        setAverageMode(channel, mode);
        setAverageCount(channel, count);
    }

    /**
     * Sets both the averaging mode and count together for all channels.
     *
     * @param mode  Averaging mode
     * @param count Averaging count
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    default void setAveraging(AMode mode, int count) throws DeviceException, IOException {
        for (int cn = 0; cn < getNumChannels(); cn++) {
            setAveraging(cn, mode, count);
        }
    }

    /**
     * Returns how many measurements the SMU is using to perform its averaging on the specified channel.
     *
     * @param channel Channel number
     *
     * @return Averaging count
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    int getAverageCount(int channel) throws DeviceException, IOException;

    /**
     * Returns how many measurements the SMU is using to perform its averaging on the default channel.
     *
     * @return Averaging count
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    default int getAverageCount() throws DeviceException, IOException {
        return getAverageCount(0);
    }

    /**
     * Sets how many measurements to use when averaging on all channels.
     *
     * @param count Averaging count
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    default void setAverageCount(int count) throws DeviceException, IOException {
        for (int cn = 0; cn < getNumChannels(); cn++) {
            setAverageCount(cn, count);
        }
    }

    /**
     * Returns which averaging mode the SMU is currently using for measurements on the specified channel.
     *
     * @param channel Channel number
     *
     * @return Averaging mode
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    AMode getAverageMode(int channel) throws DeviceException, IOException;

    /**
     * Returns which averaging mode the SMU is currently using for measurements on the default channel.
     *
     * @return Averaging mode
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    default AMode getAverageMode() throws DeviceException, IOException {
        return getAverageMode(0);
    }

    /**
     * Sets which type of averaging the SMU should use when making a measurement on all channels.
     *
     * @param mode Averaging mode
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    default void setAverageMode(AMode mode) throws DeviceException, IOException {
        for (int cn = 0; cn < getNumChannels(); cn++) {
            setAverageMode(cn, mode);
        }
    }

    /**
     * Sets the range (and thus precision) to use for the sourced quantity on the given channel.
     *
     * @param channel Channel number
     * @param value   Range value, in Volts or Amps
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void setSourceRange(int channel, double value) throws DeviceException, IOException;

    /**
     * Returns the range being used for the sourced quantity on the specified channel.
     *
     * @param channel Channel number
     *
     * @return Range value, in Volts or Amps
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    double getSourceRange(int channel) throws DeviceException, IOException;

    /**
     * Returns the range being used for the sourced quantity on the default channel.
     *
     * @return Range value, in Volts or Amps
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    default double getSourceRange() throws DeviceException, IOException {
        return getSourceRange(0);
    }

    /**
     * Sets the range (and thus precision) to use for the sourced quantity on all channels.
     *
     * @param value Range value, in Volts or Amps
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    default void setSourceRange(double value) throws DeviceException, IOException {
        for (int cn = 0; cn < getNumChannels(); cn++) {
            setSourceRange(cn, value);
        }
    }

    /**
     * Tells the SMU to use auto-ranging for the sourced quantity on the specified channel.
     *
     * @param channel Channel number
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void useAutoSourceRange(int channel) throws DeviceException, IOException;

    /**
     * Tells the SMU to use auto-ranging for the sourced quantity on all channels.
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    default void useAutoSourceRange() throws DeviceException, IOException {
        for (int cn = 0; cn < getNumChannels(); cn++) {
            useAutoSourceRange(cn);
        }
    }

    /**
     * Returns whether auto-ranging is being used for the source quantity on the specified channel.
     *
     * @param channel Channel number
     *
     * @return Auto-ranging in use?
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    boolean isAutoRangingSource(int channel) throws DeviceException, IOException;

    /**
     * Returns whether auto-ranging is being used for the source quantity on the default channel.
     *
     * @return Auto-ranging in use?
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    default boolean isAutoRangingSource() throws DeviceException, IOException {
        return isAutoRangingSource(0);
    }

    /**
     * Sets the range (and thus precision) to use for the measured quantity on the given channel.
     *
     * @param channel Channel number
     * @param value   Range value, in Volts or Amps
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void setMeasureRange(int channel, double value) throws DeviceException, IOException;

    /**
     * Returns the range being used for the measured quantity on the specified channel.
     *
     * @param channel Channel number
     *
     * @return Range value, in Volts or Amps
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    double getMeasureRange(int channel) throws DeviceException, IOException;

    /**
     * Returns the range being used for the measured quantity on the default channel.
     *
     * @return Range value, in Volts or Amps
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    default double getMeasureRange() throws DeviceException, IOException {
        return getMeasureRange(0);
    }

    /**
     * Sets the range (and thus precision) to use for the measured quantity on all channels.
     *
     * @param value Range value, in Volts or Amps
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    default void setMeasureRange(double value) throws DeviceException, IOException {
        for (int cn = 0; cn < getNumChannels(); cn++) {
            setMeasureRange(cn, value);
        }
    }

    /**
     * Tells the SMU to use auto-ranging for the measured quantity on the specified channel.
     *
     * @param channel Channel number
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void useAutoMeasureRange(int channel) throws DeviceException, IOException;

    /**
     * Tells the SMU to use auto-ranging for the measured quantity on all channels.
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    default void useAutoMeasureRange() throws DeviceException, IOException {
        for (int cn = 0; cn < getNumChannels(); cn++) {
            useAutoMeasureRange(cn);
        }
    }

    /**
     * Returns whether auto-ranging is being used for the measured quantity on the specified channel.
     *
     * @param channel Channel number
     *
     * @return Auto-ranging in use?
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    boolean isAutoRangingMeasure(int channel) throws DeviceException, IOException;

    /**
     * Returns whether auto-ranging is being used for the measured quantity on the default channel.
     *
     * @return Auto-ranging in use?
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    default boolean isAutoRangingMeasure() throws DeviceException, IOException {
        return isAutoRangingMeasure(0);
    }

    /**
     * Sets the range (and thus precision) to use for voltage values on the specified channel.
     *
     * @param channel Channel number
     * @param value   Range, in Volts
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void setVoltageRange(int channel, double value) throws DeviceException, IOException;

    /**
     * Returns the range being used for voltage values on the specified channel.
     *
     * @param channel Channel number
     *
     * @return Range, in Volts
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    double getVoltageRange(int channel) throws DeviceException, IOException;

    /**
     * Returns the range being used for voltage values on the default channel.
     *
     * @return Range, in Volts
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    default double getVoltageRange() throws DeviceException, IOException {
        return getVoltageRange(0);
    }

    /**
     * Sets the range (and thus precision) to use for voltage values on all channels.
     *
     * @param value Range, in Volts
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    default void setVoltageRange(double value) throws DeviceException, IOException {
        for (int cn = 0; cn < getNumChannels(); cn++) {
            setVoltageRange(cn, value);
        }
    }

    /**
     * Tells the SMU to use auto-ranging for voltage values on the specified channel.
     *
     * @param channel Channel number
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void useAutoVoltageRange(int channel) throws DeviceException, IOException;

    /**
     * Tells the SMU to use auto-ranging for voltage values on all channels.
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    default void useAutoVoltageRange() throws DeviceException, IOException {
        for (int cn = 0; cn < getNumChannels(); cn++) {
            useAutoVoltageRange(cn);
        }
    }

    /**
     * Returns whether auto-ranging is being used for voltage values on the specified channel.
     *
     * @param channel Channel number
     *
     * @return Auto-ranging in use?
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    boolean isAutoRangingVoltage(int channel) throws DeviceException, IOException;

    /**
     * Returns whether auto-ranging is being used for voltage values on the default channel.
     *
     * @return Auto-ranging in use?
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    default boolean isAutoRangingVoltage() throws DeviceException, IOException {
        return isAutoRangingVoltage(0);
    }

    /**
     * Sets the range (and thus precision) to use for current values on the specified channel.
     *
     * @param channel Channel number
     * @param value   Range, in Amps
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void setCurrentRange(int channel, double value) throws DeviceException, IOException;

    /**
     * Returns the range being used for current values on the specified channel.
     *
     * @param channel Channel number
     *
     * @return Range, in Amps
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    double getCurrentRange(int channel) throws DeviceException, IOException;

    /**
     * Returns the range being used for voltage values on the default channel.
     *
     * @return Range, in Volts
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    default double getCurrentRange() throws DeviceException, IOException {
        return getCurrentRange(0);
    }

    /**
     * Sets the range (and thus precision) to use for current values on all channels.
     *
     * @param value Range, in Amps
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    default void setCurrentRange(double value) throws DeviceException, IOException {
        for (int cn = 0; cn < getNumChannels(); cn++) {
            setCurrentRange(cn, value);
        }
    }

    /**
     * Tells the SMU to use auto-ranging for current values on the specified channel.
     *
     * @param channel Channel number
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void useAutoCurrentRange(int channel) throws DeviceException, IOException;

    /**
     * Tells the SMU to use auto-ranging for voltage values on all channels.
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    default void useAutoCurrentRange() throws DeviceException, IOException {
        for (int cn = 0; cn < getNumChannels(); cn++) {
            useAutoCurrentRange(cn);
        }
    }

    /**
     * Returns whether auto-ranging is being used for current values on the specified channel.
     *
     * @param channel Channel number
     *
     * @return Auto-ranging in use?
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    boolean isAutoRangingCurrent(int channel) throws DeviceException, IOException;

    /**
     * Returns whether auto-ranging is being used for voltage values on the default channel.
     *
     * @return Auto-ranging in use?
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    default boolean isAutoRangingCurrent() throws DeviceException, IOException {
        return isAutoRangingCurrent(0);
    }

    /**
     * Sets the limit (compliance) on whichever quantity is not being sourced on the given channel.
     *
     * @param channel Channel number
     * @param value   Limit, in Volts or Amps
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void setOutputLimit(int channel, double value) throws DeviceException, IOException;

    /**
     * Returns the limit (compliance) on whichever quantity is not being sourced on the specified channel.
     *
     * @param channel Channel number
     *
     * @return Limit, in Volts or Amps
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    double getOutputLimit(int channel) throws DeviceException, IOException;

    /**
     * Returns the limit (compliance) on whichever quantity is not being sourced on the default channel.
     *
     * @return Limit, in Volts or Amps
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    default double getOutputLimit() throws DeviceException, IOException {
        return getOutputLimit(0);
    }

    /**
     * Sets the limit (compliance) on whichever quantity is not being sourced on all channels.
     *
     * @param value Limit, in Volts or Amps
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    default void setOutputLimit(double value) throws DeviceException, IOException {
        for (int cn = 0; cn < getNumChannels(); cn++) {
            setOutputLimit(cn, value);
        }
    }

    /**
     * Sets the limit (compliance) on voltage values when not being sourced on the specified channel.
     *
     * @param channel Channel number
     * @param value   Limit, in Volts
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void setVoltageLimit(int channel, double value) throws DeviceException, IOException;

    /**
     * Returns the limit (compliance) on voltage when not being sourced on the specified channel.
     *
     * @param channel Channel number
     *
     * @return Limit, in Volts
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    double getVoltageLimit(int channel) throws DeviceException, IOException;

    /**
     * Returns the limit (compliance) on voltage when not being sourced on the default channel.
     *
     * @return Limit, in Volts
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    default double getVoltageLimit() throws DeviceException, IOException {
        return getVoltageLimit(0);
    }

    /**
     * Sets the limit (compliance) on voltage when not being sourced on all channels.
     *
     * @param value Limit, in Volts
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    default void setVoltageLimit(double value) throws DeviceException, IOException {
        for (int cn = 0; cn < getNumChannels(); cn++) {
            setVoltageLimit(cn, value);
        }
    }

    /**
     * Sets the limit (compliance) on current when not being sourced on the specified channel.
     *
     * @param channel Channel number
     * @param value   Limit, in Amps
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void setCurrentLimit(int channel, double value) throws DeviceException, IOException;

    /**
     * Returns the limit (compliance) on current when not being sourced on the specified channel.
     *
     * @param channel Channel number
     *
     * @return Limit, in Amps
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    double getCurrentLimit(int channel) throws DeviceException, IOException;

    /**
     * Returns the limit (compliance) on current when not being sourced on the default channel.
     *
     * @return Limit, in Amps
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    default double getCurrentLimit() throws DeviceException, IOException {
        return getCurrentLimit(0);
    }

    /**
     * Sets the limit (compliance) on current when not being sourced on the all channels.
     *
     * @param value Limit, in Amps
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    default void setCurrentLimit(double value) throws DeviceException, IOException {
        for (int cn = 0; cn < getNumChannels(); cn++) {
            setCurrentLimit(cn, value);
        }
    }

    /**
     * Sets the integration time to use for measurements on the specified channel.
     *
     * @param channel Channel number
     * @param time    Integration time, in seconds
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void setIntegrationTime(int channel, double time) throws DeviceException, IOException;

    /**
     * Returns the integration time used for measurements on the specified channel.
     *
     * @param channel Channel number
     *
     * @return Integration time, in seconds
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    double getIntegrationTime(int channel) throws DeviceException, IOException;

    /**
     * Returns the integration time used for measurements on the default channel.
     *
     * @return Integration time, in seconds
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    default double getIntegrationTime() throws DeviceException, IOException {
        return getIntegrationTime(0);
    }

    /**
     * Sets the integration time to use for measurements on all channels.
     *
     * @param time Integration time, in seconds
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    default void setIntegrationTime(double time) throws DeviceException, IOException {
        for (int cn = 0; cn < getNumChannels(); cn++) {
            setIntegrationTime(cn, time);
        }
    }

    TType getTerminalType(int channel, Terminals terminals) throws DeviceException, IOException;

    default TType getTerminalType(Terminals terminals) throws DeviceException, IOException {
        return getTerminalType(0, terminals);
    }

    void setTerminals(int channel, Terminals terminals) throws DeviceException, IOException;

    void setProbeMode(int channel, Function funcType, boolean enableSense) throws DeviceException, IOException;

    Terminals getTerminals(int channel) throws DeviceException, IOException;

    default Terminals getTerminals() throws DeviceException, IOException {
        return getTerminals(0);
    }

    default void setTerminals(Terminals terminals) throws DeviceException, IOException {
        for (int cn = 0; cn < getNumChannels(); cn++) {
            setTerminals(cn, terminals);
        }
    }

    default void setProbeMode(Function funcType, boolean enableSense) throws DeviceException, IOException {
        for (int cn = 0; cn < getNumChannels(); cn++) {
            setProbeMode(cn, funcType, enableSense);
        }
    }

    void setOffMode(int channel, OffMode mode) throws DeviceException, IOException;

    OffMode getOffMode(int channel) throws DeviceException, IOException;

    default OffMode getOffMode() throws DeviceException, IOException {
        return getOffMode(0);
    }

    default void setOffMode(OffMode mode) throws DeviceException, IOException {
        for (int cn = 0; cn < getNumChannels(); cn++) {
            setOffMode(cn, mode);
        }
    }

    /**
     * Returns whether the voltmeter is using any line-frequency filtering
     *
     * @param channel Channel number
     *
     * @return Using line filter?
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    boolean isLineFilterEnabled(int channel) throws DeviceException, IOException;

    /**
     * Sets whether the voltmeter should use any line-frequency filtering (if available)
     *
     * @param channel Channel number
     * @param enabled Use line filter?
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void setLineFilterEnabled(int channel, boolean enabled) throws DeviceException, IOException;

    /**
     * Returns whether the voltmeter is using any line-frequency filtering
     *
     * @return Using line filter?
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    default boolean isLineFilterEnabled() throws DeviceException, IOException {
        return isLineFilterEnabled(0);
    }

    /**
     * Sets whether the voltmeter should use any line-frequency filtering (if available)
     *
     * @param enabled Use line filter?
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    default void setLineFilterEnabled(boolean enabled) throws DeviceException, IOException {

        for (int i = 0; i < getNumChannels(); i++) {
            setLineFilterEnabled(i, enabled);
        }

    }

    /**
     * Returns a combined voltage and current measurement from the specified channel.
     *
     * @param channel Channel number
     *
     * @return Voltage and current
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    default IVPoint getIVPoint(int channel) throws DeviceException, IOException {
        return new IVPoint(getVoltage(channel), getCurrent(channel));
    }

    /**
     * Returns a combined voltage and current measurement from the default channel.
     *
     * @return Voltage and current
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    default IVPoint getIVPoint() throws DeviceException, IOException {
        return getIVPoint(0);
    }

    /**
     * Returns combined voltage and current measurements for each channel.
     *
     * @return Voltages and currents
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    default MCIVPoint getMCIVPoint() throws DeviceException, IOException {
        MCIVPoint point = new MCIVPoint();

        for (int i = 0; i < getNumChannels(); i++) {
            point.addChannel(i, new IVPoint(getVoltage(i), getCurrent(i)));
        }

        return point;

    }

    /**
     * Sets both the voltage and current ranges to use on the specified channel.
     *
     * @param channel      Channel number
     * @param voltageRange Voltage range, in Volts
     * @param currentRange Current range, in Amps
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    default void setRanges(int channel, double voltageRange, double currentRange) throws DeviceException, IOException {
        setVoltageRange(channel, voltageRange);
        setCurrentRange(channel, currentRange);
    }

    /**
     * Sets both the voltage and current ranges to use on all channels.
     *
     * @param voltageRange Voltage range, in Volts
     * @param currentRange Current range, in Amps
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    default void setRanges(double voltageRange, double currentRange) throws DeviceException, IOException {
        for (int i = 0; i < getNumChannels(); i++) {
            setRanges(i, voltageRange, currentRange);
        }
    }

    /**
     * Tells the SMU to use auto-ranging for both voltage and current on the specified channel.
     *
     * @param channel Channel number
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    default void useAutoRanges(int channel) throws DeviceException, IOException {
        useAutoVoltageRange(channel);
        useAutoCurrentRange(channel);
    }

    /**
     * Tells the SMU to use auto-ranging for both voltage and current on all channels.
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    default void useAutoRanges() throws DeviceException, IOException {
        for (int i = 0; i < getNumChannels(); i++) {
            useAutoRanges(i);
        }
    }

    /**
     * Sets the limits for both voltage and current (when not being sourced) on the specified channel.
     *
     * @param channel      Channel number
     * @param voltageLimit Voltage limit, in Volts
     * @param currentLimit Current limit, in Amps
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    default void setLimits(int channel, double voltageLimit, double currentLimit) throws DeviceException, IOException {
        setVoltageLimit(channel, voltageLimit);
        setCurrentLimit(channel, currentLimit);
    }

    /**
     * Sets the limits for both voltage and current (when not being sourced) on all channels.
     *
     * @param voltageLimit Voltage limit, in Volts
     * @param currentLimit Current limit, in Amps
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    default void setLimits(double voltageLimit, double currentLimit) throws DeviceException, IOException {
        for (int i = 0; i < getNumChannels(); i++) {
            setLimits(i, voltageLimit, currentLimit);
        }
    }

    /**
     * Returns a virtual SMU object to control the specified channel of the MCSMU
     *
     * @param channel Channel number
     *
     * @return Virtual SMU
     *
     * @throws DeviceException If channel does not exist
     */
    default SMU getChannel(int channel) throws DeviceException {

        try {
            return new VirtualSMU(this, channel);
        } catch (Exception e) {
            return null;
        }

    }

    default List<SMU> getChannels() {

        List<SMU> list = new ArrayList<>();

        for (int i = 0; i < getNumChannels(); i++) {

            try {
                list.add(getChannel(i));
            } catch (DeviceException e) {
                e.printStackTrace();
            }

        }

        return list;

    }

    default void checkChannel(int channel) throws DeviceException {

        if (!Util.isBetween(channel, 0, getNumChannels() - 1)) {
            throw new DeviceException(
                "Invalid channel, %d, specified for %s SMU (valid range: 0 to %d)",
                channel,
                getClass().getSimpleName(),
                getNumChannels() - 1
            );
        }

    }

    default Class<SMU> getChannelClass() {
        return SMU.class;
    }

    /**
     * Class for controlling an MCSMU channel as if it were a separate SMU
     */
    class VirtualSMU implements SMU {

        private final int   channel;
        private final MCSMU smu;

        public VirtualSMU(MCSMU smu, int channel) {
            this.smu     = smu;
            this.channel = channel;
        }

        @Override
        public String getChannelName() {
            return smu.getChannelName(channel);
        }

        @Override
        public double getVoltage() throws DeviceException, IOException {
            return smu.getVoltage(channel);
        }

        @Override
        public void setVoltage(double voltage) throws DeviceException, IOException {
            smu.setVoltage(channel, voltage);
        }

        @Override
        public double getCurrent() throws DeviceException, IOException {
            return smu.getCurrent(channel);
        }

        @Override
        public void setCurrent(double current) throws DeviceException, IOException {
            smu.setCurrent(channel, current);
        }

        @Override
        public void turnOn() throws DeviceException, IOException {
            smu.turnOn(channel);
        }

        @Override
        public void turnOff() throws DeviceException, IOException {
            smu.turnOff(channel);
        }

        @Override
        public boolean isOn() throws DeviceException, IOException {
            return smu.isOn(channel);
        }

        @Override
        public Source getSource() throws DeviceException, IOException {
            return smu.getSource(channel);
        }

        @Override
        public void setSource(Source source) throws DeviceException, IOException {
            smu.setSource(channel, source);
        }

        @Override
        public void setSourceValue(double level) throws DeviceException, IOException {
            smu.setBias(channel, level);
        }

        @Override
        public double getSourceValue() throws DeviceException, IOException {
            return smu.getSourceValue(channel);
        }

        @Override
        public double getMeasureValue() throws DeviceException, IOException {
            return smu.getMeasureValue(channel);
        }

        @Override
        public void setFourProbeEnabled(boolean fourProbes) throws DeviceException, IOException {
            smu.setFourProbeEnabled(channel, fourProbes);
        }

        @Override
        public boolean isFourProbeEnabled() throws DeviceException, IOException {
            return smu.isFourProbeEnabled(channel);
        }

        @Override
        public AMode getAverageMode() throws DeviceException, IOException {
            return smu.getAverageMode(channel);
        }

        @Override
        public void setAverageMode(AMode mode) throws DeviceException, IOException {
            smu.setAverageMode(channel, mode);
        }

        @Override
        public int getAverageCount() throws DeviceException, IOException {
            return smu.getAverageCount(channel);
        }

        @Override
        public void setAverageCount(int count) throws DeviceException, IOException {
            smu.setAverageCount(channel, count);
        }

        @Override
        public double getSourceRange() throws DeviceException, IOException {
            return smu.getSourceRange(channel);
        }

        @Override
        public void setSourceRange(double value) throws DeviceException, IOException {
            smu.setSourceRange(channel, value);
        }

        @Override
        public void useAutoSourceRange() throws DeviceException, IOException {
            smu.useAutoSourceRange(channel);
        }

        @Override
        public boolean isAutoRangingSource() throws DeviceException, IOException {
            return smu.isAutoRangingSource(channel);
        }

        @Override
        public double getMeasureRange() throws DeviceException, IOException {
            return smu.getMeasureRange(channel);
        }

        @Override
        public void setMeasureRange(double value) throws DeviceException, IOException {
            smu.setMeasureRange(channel, value);
        }

        @Override
        public void useAutoMeasureRange() throws DeviceException, IOException {
            smu.useAutoMeasureRange(channel);
        }

        @Override
        public boolean isAutoRangingMeasure() throws DeviceException, IOException {
            return smu.isAutoRangingMeasure(channel);
        }

        @Override
        public double getVoltageRange() throws DeviceException, IOException {
            return smu.getVoltageRange(channel);
        }

        @Override
        public void setVoltageRange(double value) throws DeviceException, IOException {
            smu.setVoltageRange(channel, value);
        }

        @Override
        public void useAutoVoltageRange() throws DeviceException, IOException {
            smu.useAutoVoltageRange(channel);
        }

        @Override
        public boolean isAutoRangingVoltage() throws DeviceException, IOException {
            return smu.isAutoRangingVoltage(channel);
        }

        @Override
        public double getCurrentRange() throws DeviceException, IOException {
            return smu.getCurrentRange(channel);
        }

        @Override
        public void setCurrentRange(double value) throws DeviceException, IOException {
            smu.setCurrentRange(channel, value);
        }

        @Override
        public void useAutoCurrentRange() throws DeviceException, IOException {
            smu.useAutoCurrentRange(channel);
        }

        @Override
        public boolean isAutoRangingCurrent() throws DeviceException, IOException {
            return smu.isAutoRangingCurrent(channel);
        }

        @Override
        public double getOutputLimit() throws DeviceException, IOException {
            return smu.getOutputLimit(channel);
        }

        @Override
        public void setOutputLimit(double value) throws DeviceException, IOException {
            smu.setOutputLimit(channel, value);
        }

        @Override
        public double getVoltageLimit() throws DeviceException, IOException {
            return smu.getVoltageLimit(channel);
        }

        @Override
        public void setVoltageLimit(double voltage) throws DeviceException, IOException {
            smu.setVoltageLimit(channel, voltage);
        }

        @Override
        public double getCurrentLimit() throws DeviceException, IOException {
            return smu.getCurrentLimit(channel);
        }

        @Override
        public void setCurrentLimit(double current) throws DeviceException, IOException {
            smu.setCurrentLimit(channel, current);
        }

        @Override
        public double getIntegrationTime() throws DeviceException, IOException {
            return smu.getIntegrationTime(channel);
        }

        @Override
        public void setIntegrationTime(double time) throws DeviceException, IOException {
            smu.setIntegrationTime(channel, time);
        }

        public TType getTerminalType(Terminals terminals) throws DeviceException, IOException {
            return smu.getTerminalType(channel, terminals);
        }

        @Override
        public Terminals getTerminals() throws DeviceException, IOException {
            return smu.getTerminals(channel);
        }

        @Override
        public void setTerminals(Terminals terminals) throws DeviceException, IOException {
            smu.setTerminals(channel, terminals);
        }

        @Override
        public void setProbeMode(Function funcType, boolean enableSense) throws DeviceException, IOException{
            smu.setProbeMode(channel, funcType, enableSense);
        }

        @Override
        public OffMode getOffMode() throws DeviceException, IOException {
            return smu.getOffMode(channel);
        }

        @Override
        public void setOffMode(OffMode mode) throws DeviceException, IOException {
            smu.setOffMode(channel, mode);
        }

        @Override
        public boolean isLineFilterEnabled() throws DeviceException, IOException {
            return smu.isLineFilterEnabled(channel);
        }

        @Override
        public void setLineFilterEnabled(boolean enabled) throws DeviceException, IOException {
            smu.setLineFilterEnabled(channel, enabled);
        }

        @Override
        public String getIDN() throws IOException, DeviceException {
            return smu.getIDN();
        }

        @Override
        public void close() throws IOException, DeviceException {
            smu.close();
        }

        @Override
        public Address getAddress() {
            return smu.getAddress();
        }

        public Object getLockObject() {
            return smu;
        }

        @Override
        public double getSetCurrent() throws DeviceException, IOException
        {
            throw new DeviceException("Not implemented.");
        }

        @Override
        public double getSetVoltage() throws DeviceException, IOException
        {
            throw new DeviceException("Not implemented.");
        }
    }

}
