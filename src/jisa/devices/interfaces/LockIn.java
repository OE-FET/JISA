package jisa.devices.interfaces;

import jisa.control.Synch;
import jisa.devices.DeviceException;
import jisa.enums.*;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Abstract class to define the standard functionality of lock-in amplifiers
 */
public interface LockIn extends Instrument, FMeter {

    public static String getDescription() {
        return "Lock-In Amplifier";
    }

    @Override
    default List<Parameter<?>> getConfigurationParameters(Class<?> target) {

        List<Parameter<?>> parameters = new LinkedList<>();

        parameters.add(new Parameter<>("Reference", RefMode.EXTERNAL, this::setRefMode, RefMode.values()));
        parameters.add(new Parameter<>("Input", Input.DIFF, this::setInput, Input.values()));
        parameters.add(new Parameter<>("Range", 1.0, this::setRange));
        parameters.add(new Parameter<>("Time Constant", 1.0, this::setTimeConstant));
        parameters.add(new Parameter<>("Coupling", Coupling.AC, this::setCoupling, Coupling.values()));
        parameters.add(new Parameter<>("Shielding", Shield.GROUND, this::setShielding, Shield.values()));
        parameters.add(new Parameter<>("Filter Roll-Off", 24.0, this::setFilterRollOff));
        parameters.add(new Parameter<>("Sync Filter", true, this::setSyncFilterEnabled));
        parameters.add(new Parameter<>("Line Filter", true, b -> setLineFilterHarmonics(b ? new int[]{1, 2} : new int[0])));

        return parameters;

    }

    /**
     * Returns whether the lock-in amplifier is using an internal or external reference signal.
     *
     * @return INTERNAL or EXTERNAL
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    RefMode getRefMode() throws IOException, DeviceException;

    /**
     * Sets whether the lock-in amplifier is to use an internal or external reference signal
     *
     * @param mode INTERNAL or EXTERNAL
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    void setRefMode(RefMode mode) throws IOException, DeviceException;

    /**
     * Sets the frequency of the internal oscillator of the amplifier (eg for internal reference)
     *
     * @param frequency Frequency, in Hz
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    void setOscFrequency(double frequency) throws IOException, DeviceException;

    /**
     * Sets the phase of the internal oscillator
     *
     * @param phase Phase, in degrees
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    void setOscPhase(double phase) throws IOException, DeviceException;

    /**
     * Sets the amplitude of the internal oscillator output.
     *
     * @param level Amplitude, in volts
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    void setOscAmplitude(double level) throws IOException, DeviceException;

    /**
     * Returns the frequency of the reference signal
     *
     * @return Frequency, in Hz
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    double getFrequency() throws IOException, DeviceException;

    /**
     * Returns the phase of the reference signal
     *
     * @return Phase, in degrees
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    double getRefPhase() throws IOException, DeviceException;

    /**
     * Returns the amplitude of the reference signal
     *
     * @return Amplitude, in Volts
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    double getRefAmplitude() throws IOException, DeviceException;

    /**
     * Returns the amplitude of the signal component locked on to by the amplifier
     *
     * @return Amplitude, in Volts
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    double getLockedAmplitude() throws IOException, DeviceException;

    /**
     * Returns the set time constant, in seconds
     *
     * @return Time constant, in seconds
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    double getTimeConstant() throws IOException, DeviceException;

    /**
     * Sets the time constant to use for locking onto a signal (or closest over-approximation for devices with discrete
     * settings)
     *
     * @param seconds Time constant, in seconds
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    void setTimeConstant(double seconds) throws IOException, DeviceException;

    /**
     * Returns the maximum range, in Volts, that the instrument is measuring.
     *
     * @return +/- Range, in Volts
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    double getRange() throws IOException, DeviceException;

    /**
     * Sets the sensitivity of the instrument based on the maximum range of values you desire to measure.
     *
     * @param range +/- Range, in Volts
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error (eg sensitivity greater than can be achieved by instrument)
     */
    void setRange(double range) throws IOException, DeviceException;

    /**
     * Returns whether the lock-in is currently using synchronous filtering.
     *
     * @return Enabled?
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    boolean isSyncFilterEnabled() throws IOException, DeviceException;

    /**
     * Instructs the lock-in to use synchronous filtering (removes higher harmonics of reference frequency from signal).
     *
     * @param flag Should this feature be enabled?
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    void setSyncFilterEnabled(boolean flag) throws IOException, DeviceException;

    /**
     * Returns the filter roll-off used by the lock-in.
     *
     * @return Roll-off in dB/oct
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    double getFilterRollOff() throws IOException, DeviceException;

    /**
     * Sets the filter roll-off for the lock-in's input filter.
     *
     * @param dBperOct Roll-off in dB/oct
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    void setFilterRollOff(double dBperOct) throws IOException, DeviceException;

    /**
     * Returns the input coupling mode of the lock-in.
     *
     * @return AC or DC
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    Coupling getCoupling() throws IOException, DeviceException;

    /**
     * Sets the input coupling mode of the lock-in.
     *
     * @param mode AC or DC?
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    void setCoupling(Coupling mode) throws IOException, DeviceException;

    /**
     * Returns the currently used shielding mode for input connections.
     *
     * @return Shield.FLOAT or Shield.GROUND
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    Shield getShielding() throws IOException, DeviceException;

    /**
     * Sets the shielding mode to use for input connections.
     *
     * @param mode Shield.FLOAT or Shield.GROUND
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    void setShielding(Shield mode) throws IOException, DeviceException;

    /**
     * Returns which input is currently being used by the lock-in.
     *
     * @return Input: A, B or DIFF (A - B)
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    Input getInput() throws IOException, DeviceException;

    /**
     * Sets which input the lock-in should use.
     *
     * @param source Input: A, B or DIFF (A - B)
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    void setInput(Input source) throws IOException, DeviceException;

    /**
     * Returns which quantity is being used for measurement (voltage or current).
     *
     * @return Source.VOLTAGE or Source.CURRENT
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    Source getSource() throws IOException, DeviceException;

    /**
     * Sets which source quantity to be using for measurement (voltage or current).
     *
     * @param source Source.VOLTAGE or Source.CURRENT
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    void setSource(Source source) throws IOException, DeviceException;

    /**
     * Returns whether HIGH or LOW impedance mode is currently in use.
     *
     * @return Impedance.HIGH or Impedance.LOW
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    Impedance getImpedanceMode() throws IOException, DeviceException;

    /**
     * Sets whether to use HIGH or LOW impedance mode for input signals.
     *
     * @param mode Impedance.HIGH or Impedance.LOW
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    void setImpedanceMode(Impedance mode) throws IOException, DeviceException;

    /**
     * Returns a list of all harmonics of the powerline frequency being filtered by the lock-in.
     *
     * @return List of harmonics (list of integers)
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    List<Integer> getLineFilterHarmonics() throws IOException, DeviceException;

    /**
     * Attempts to set the lock-in to filter all the specified harmonics of the powerline frequency. Any unavailable
     * harmonics will be ignored.
     *
     * @param harmonics Harmonics to filter, as integers (ie 1, 4, 5 for 1st 4th and 5th)
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    void setLineFilterHarmonics(int... harmonics) throws IOException, DeviceException;

    /**
     * Returns the offset currently being used by the lock-in.
     *
     * @return Offset as % of measurement range
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    double getOffset() throws IOException, DeviceException;

    /**
     * Sets the offset for input measurements.
     *
     * @param offset Offset as % of measurement range
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    void setOffset(double offset) throws IOException, DeviceException;

    /**
     * Returns the signal expansion factor being used
     *
     * @return Expansion factor
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    double getExpansion() throws IOException, DeviceException;

    /**
     * Sets the signal expansion factor to use. Will choose closest discrete option if not continuous.
     *
     * @param expand Expansion factor
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    void setExpansion(double expand) throws IOException, DeviceException;

    /**
     * Instruct the lock-in to automatically determine the offset to use.
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    void autoOffset() throws IOException, DeviceException;

    /**
     * Instruct the lock-in to automatically determine the measurement range to use for the currently measured input.
     * This is done by picking the smallest range (multiplied by the supplied factor) within which the current value
     * fits within after any offsetting. Does not return until completed.
     *
     * @param factor Multiplicative factor used to modify the range (i.e. 0.5 would mean the value must fit within half of the range selected)
     *
     * @param factor
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     * @throws InterruptedException Upon interruption error
     */
    void autoRange(double factor) throws IOException, DeviceException, InterruptedException;

    default void autoRange() throws IOException, DeviceException, InterruptedException {
        autoRange(1.0);
    }

    /**
     * Returns the triggering mode used for external referencing (SINE, POS_TTL, NEG_TTL).
     *
     * @return Triggering mode
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    TrigMode getExternalTriggerMode() throws IOException, DeviceException;


    /**
     * Sets the triggering mode used for external referencing (SINE, POS_TTL, NEG_TTL).
     *
     * @param mode Triggering mode
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    void setExternalTriggerMode(TrigMode mode) throws IOException, DeviceException;

    /**
     * Halts the current thread (ie pauses the program) until the lock-in has a stable lock
     * (ie the locked-on amplitude has remained within the given percentage margin for at least
     * the specified number of milliseconds).
     *
     * @param pctMargin Percentage margin within which to consider amplitude constant
     * @param duration  Minimum duration to be considered stable, in milliseconds
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     * @throws InterruptedException Upon interruption error
     */
    default void waitForStableLock(double pctMargin, long duration) throws IOException, DeviceException, InterruptedException {

        Synch.waitForParamStable(
            this::getLockedAmplitude,
            pctMargin,
            100,
            duration
        );

    }

    /**
     * Halts the current thread (ie pauses the program) until the lock-in has a stable lock
     * (ie the locked-on amplitude has not varied by more than 0.1% in 5 seconds).
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     * @throws InterruptedException Upon interruption error
     */
    default void waitForStableLock() throws IOException, DeviceException, InterruptedException {
        waitForStableLock(0.1, 5000);
    }

    /**
     * Enumeration of reference modes
     */
    enum RefMode {
        INTERNAL,
        EXTERNAL
    }

    /**
     * Enumeration of reference triggering modes
     */
    enum TrigMode {
        SINE,
        POS_TTL,
        NEG_TTL
    }

}
