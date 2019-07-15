package jisa.devices;

import jisa.control.Synch;
import jisa.enums.Coupling;
import jisa.enums.Shield;

import java.io.IOException;
import java.util.List;

/**
 * Abstract class to define the standard functionality of lock-in amplifiers
 */
public interface LockIn extends Instrument {

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
     * Instructs the lock-in to use synchronous filtering (removes higher harmonics of reference frequency from signal).
     *
     * @param flag Should this feature be enabled?
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    void useSyncFiltering(boolean flag) throws IOException, DeviceException;

    /**
     * Returns whether the lock-in is currently using synchronous filtering.
     *
     * @return Enabled?
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    boolean isUsingSyncFiltering() throws IOException, DeviceException;

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

    Shield getShielding() throws IOException, DeviceException;

    void setShielding(Shield mode) throws IOException, DeviceException;

    List<Integer> getLineFilterHarmonics() throws IOException, DeviceException;

    void setLineFilterHarmonics(int... harmonics) throws IOException, DeviceException;

    void setOffsetExpansion(double offset, double expand) throws IOException, DeviceException;

    double getOffset() throws IOException, DeviceException;

    void setOffset(double offset) throws IOException, DeviceException;

    double getExpansion() throws IOException, DeviceException;

    void setExpansion(double expand) throws IOException, DeviceException;

    void autoOffset() throws IOException, DeviceException;

    void autoRange() throws IOException, DeviceException;

    TrigMode getExternalTriggerMode() throws IOException, DeviceException;

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
     */
    default void waitForStableLock(double pctMargin, long duration) throws IOException, DeviceException, InterruptedException {

        Synch.waitForParamStable(
                this::getLockedAmplitude,
                pctMargin,
                100,
                duration
        );

    }

    void setInput(Input source) throws IOException, DeviceException;

    Input getInput() throws IOException, DeviceException;

    /**
     * Halts the current thread (ie pauses the program) until the lock-in has a stable lock
     * (ie the locked-on amplitude has not varied by more than 0.1% in 5 seconds).
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    default void waitForStableLock() throws IOException, DeviceException, InterruptedException {
        waitForStableLock(0.1, 5000);
    }

    enum Input {
        VOLTAGE_SINGLE,
        VOLTAGE_DIFFERENCE,
        CURRENT_LOW_IMPEDANCE,
        CURRENT_HIGH_IMPEDANCE
    }

    /**
     * Enumeration of reference modes
     */
    enum RefMode {
        INTERNAL,
        EXTERNAL
    }

    enum TrigMode {
        SINE,
        POS_TTL,
        NEG_TTL
    }

}
