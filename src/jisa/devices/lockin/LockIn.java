package jisa.devices.lockin;

import jisa.control.Sync;
import jisa.devices.DeviceException;
import jisa.devices.Instrument;
import jisa.devices.ParameterList;
import jisa.devices.meter.FMeter;

import java.io.IOException;

/**
 * Abstract class to define the standard functionality of lock-in amplifiers
 */
public interface LockIn extends Instrument, FMeter {

    static String getDescription() {
        return "Lock-In Amplifier";
    }

    static void addParameters(LockIn inst, Class target, ParameterList parameters) {

        parameters.addValue("Differential Input", inst::isDifferentialInputEnabled, false, inst::setDifferentialInputEnabled);
        parameters.addValue("Range [V]", inst::getVoltageRange, 1.0, inst::setVoltageRange);
        parameters.addValue("Integration Time [s]", inst::getIntegrationTime, 1e-3, inst::setIntegrationTime);
        parameters.addValue("AC Input Coupling", inst::isCouplingAC, true, inst::setCouplingAC);
        parameters.addValue("Ground Input Shielding", inst::isShieldGrounded, true, inst::setShieldGrounded);
        parameters.addValue("Low-Pass Filter Roll-Off [dB/oct]", inst::getLowPassRollOff, 24.0, inst::setLowPassRollOff);

    }

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
     * Returns the phase shift being applied between the reference and input
     * signals.
     *
     * @return Phase shift, in degrees
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    double getPhaseShift() throws IOException, DeviceException;

    /**
     * Sets the phase shift to be applied between the reference and input signals.
     *
     * @param offset Phase offset, in degrees
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    void setPhaseShift(double offset) throws IOException, DeviceException;

    /**
     * Tells the lock-in to set the phase shift to achieve a zero phase difference between reference and input.
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    void autoShiftPhase() throws IOException, DeviceException;

    /**
     * Returns the set time constant, in seconds
     *
     * @return Time constant, in seconds
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    double getIntegrationTime() throws IOException, DeviceException;

    /**
     * Sets the time constant to use for locking onto a signal (or closest over-approximation for devices with discrete
     * settings)
     *
     * @param seconds Time constant, in seconds
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    void setIntegrationTime(double seconds) throws IOException, DeviceException;

    /**
     * Returns the maximum range, in Volts, that the instrument is measuring.
     *
     * @return +/- Range, in Volts
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    double getVoltageRange() throws IOException, DeviceException;

    /**
     * Sets the sensitivity of the instrument based on the maximum range of values you desire to measure.
     *
     * @param range +/- Range, in Volts
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error (eg sensitivity greater than can be achieved by instrument)
     */
    void setVoltageRange(double range) throws IOException, DeviceException;

    /**
     * Returns the filter roll-off used by the lock-in.
     *
     * @return Roll-off in dB/oct
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    double getLowPassRollOff() throws IOException, DeviceException;

    /**
     * Sets the filter roll-off for the lock-in's input filter.
     *
     * @param dBperOct Roll-off in dB/oct
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    void setLowPassRollOff(double dBperOct) throws IOException, DeviceException;

    /**
     * Returns whether the lock-in is using AC input coupling or not.
     *
     * @return AC or not?
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    boolean isCouplingAC() throws IOException, DeviceException;

    /**
     * Sets the input coupling mode of the lock-in.
     *
     * @param ac AC = true, DC = false.
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    void setCouplingAC(boolean ac) throws IOException, DeviceException;

    /**
     * Returns the currently used shielding mode for input connections.
     *
     * @return true = grounded, false = floating
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    boolean isShieldGrounded() throws IOException, DeviceException;

    /**
     * Sets the shielding mode to use for input connections.
     *
     * @param mode true = grounded, false = floating
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
    boolean isDifferentialInputEnabled() throws IOException, DeviceException;

    /**
     * Sets whether the lock-in amplifier should use its differential input mode or not (i.e., A-B or just A).
     *
     * @param differential Differential mode enabled?
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    void setDifferentialInputEnabled(boolean differential) throws IOException, DeviceException;

    /**
     * Returns the offset being applied to the amplitude channel.
     *
     * @return Amplitude shift, in % of measurement range.
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    double getAmplitudeOffset() throws IOException, DeviceException;

    /**
     * Sets the offset to apply to the amplitude channel
     *
     * @param offset Amplitude shift, in % of measurement of range.
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    void setAmplitudeOffset(double offset) throws IOException, DeviceException;

    /**
     * Automatically offset the locked amplitude signal to zero, or as close as possible.
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    void autoOffsetAmplitude() throws IOException, DeviceException;

    /**
     * Instruct the lock-in to automatically determine the measurement range to use for the currently measured input.
     * This is done by picking the smallest range (multiplied by the supplied factor) within which the current value
     * fits within after any offsetting. Does not return until completed.
     *
     * @param factor Multiplicative factor used to modify the range (i.e. 0.5 would mean the value must fit within half of the range selected)
     *
     * @throws IOException          Upon communication error
     * @throws DeviceException      Upon compatibility error
     * @throws InterruptedException Upon interruption error
     */
    void autoRange(double factor) throws IOException, DeviceException, InterruptedException;

    default void autoRange() throws IOException, DeviceException, InterruptedException {
        autoRange(0.75);
    }

    void autoRangeOffset(double factor) throws IOException, DeviceException, InterruptedException;

    default void autoRangeOffset() throws IOException, DeviceException, InterruptedException {
        autoRangeOffset(0.75);
    }

    /**
     * Returns the triggering mode used for external referencing (SINE, POS_TTL, NEG_TTL).
     *
     * @return Triggering mode
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    Trigger getReferenceTriggerMode() throws IOException, DeviceException;


    /**
     * Sets the triggering mode used for external referencing (SINE, POS_TTL, NEG_TTL).
     *
     * @param mode Triggering mode
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    void setReferenceTriggerMode(Trigger mode) throws IOException, DeviceException;

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

        Sync.waitForParamStable(
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
     * Enumeration of reference triggering modes
     */
    enum Trigger {
        SINE,
        POS_TTL,
        NEG_TTL
    }

}
