package jisa.devices.interfaces;

import jisa.control.Synch;
import jisa.devices.DeviceException;
import jisa.devices.PList;
import jisa.enums.Coupling;

import java.io.IOException;

/**
 * Abstract class to define the standard functionality of lock-in amplifiers
 */
public interface LockIn extends Instrument, FMeter {

    static String getDescription() {
        return "Lock-In Amplifier";
    }

    @Override
    default PList getConfigurationParameters(Class<?> target) {

        PList params = new PList();

        params.addChoice("Reference", RefMode.EXTERNAL, this::setRefMode, RefMode.values());
        params.addValue("Differential Input", false, this::setDifferentialInput);
        params.addValue("Range [V]", 1.0, this::setRange);
        params.addValue("Time Constant", 1.0, this::setTimeConstant);
        params.addChoice("Input Coupling", Coupling.AC, this::setCoupling, Coupling.values());
        params.addValue("Ground Input Shielding", false, this::setShieldGrounded);
        params.addValue("Filter Roll-Off [dB/oct]", 24.0, this::setFilterRollOff);
        params.addValue("Sync Filter", true, this::setSyncFilterEnabled);

        if (this instanceof IPALockIn) {
            params.addOptional("Current Input Gain [V/A]", false, 1e6,
                q -> ((IPALockIn) this).setCurrentInputEnabled(false),
                q -> {
                    ((IPALockIn) this).setCurrentInputEnabled(true);
                    ((IPALockIn) this).setCurrentInputGain(q);
                });
        }

        if (this instanceof LineFilter) {
            params.addValue("Line Filter", true, ((LineFilter) this)::setLineFilterEnabled);
        }

        if (this instanceof LineFilter2X) {
            params.addValue("2x Line Filter", true, ((LineFilter2X) this)::set2xLineFilterEnabled);
        }

        return params;

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
    boolean isShieldGrounded() throws IOException, DeviceException;

    /**
     * Sets the shielding mode to use for input connections.
     *
     * @param mode Shield.FLOAT or Shield.GROUND
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    void setShieldGrounded(boolean mode) throws IOException, DeviceException;

    /**
     * Returns whether the lock-in amplifier is using its differential input mode or not (i.e., A-B or just A).
     *
     * @return Differential mode enabled?
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    boolean isDifferentialInput() throws IOException, DeviceException;

    /**
     * Sets whether the lock-in amplifier should use its differential input mode or not (i.e., A-B or just A).
     *
     * @param differential Differential mode enabled?
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    void setDifferentialInput(boolean differential) throws IOException, DeviceException;

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
     * @param factor
     *
     * @throws IOException          Upon communication error
     * @throws DeviceException      Upon compatibility error
     * @throws InterruptedException Upon interruption error
     */
    void autoRange(double factor, double integrationTime, long waitTime) throws IOException, DeviceException, InterruptedException;

    default void autoRange() throws IOException, DeviceException, InterruptedException {
        autoRange(1.0, 100e-3, 10000);
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
     * @throws IOException          Upon communication error
     * @throws DeviceException      Upon compatibility error
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
     * @throws IOException          Upon communication error
     * @throws DeviceException      Upon compatibility error
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
