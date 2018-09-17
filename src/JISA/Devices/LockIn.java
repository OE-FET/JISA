package JISA.Devices;

import JISA.Addresses.InstrumentAddress;
import JISA.Control.Synch;
import JISA.VISA.VISADevice;

import java.io.IOException;

/**
 * Abstract class to define the standard functionality of lock-in amplifiers
 */
public abstract class LockIn extends VISADevice {

    public LockIn(InstrumentAddress address) throws IOException {
        super(address);
    }

    /**
     * Sets whether the lock-in amplifier is to use an internal or external reference signal
     *
     * @param mode INTERNAL or EXTERNAL
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    public abstract void setRefMode(RefMode mode) throws IOException, DeviceException;

    /**
     * Sets the frequency of the internal oscillator of the amplifier (eg for internal reference)
     *
     * @param frequency Frequency, in Hz
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    public abstract void setOscFrequency(double frequency) throws IOException, DeviceException;

    /**
     * Sets the phase of the internal oscillator
     *
     * @param phase Phase, in degrees
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    public abstract void setOscPhase(double phase) throws IOException, DeviceException;

    /**
     * Sets the amplitude of the internal oscillator output.
     *
     * @param level Amplitude, in volts
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    public abstract void setOscAmplitude(double level) throws IOException, DeviceException;

    /**
     * Sets the time constant to use for locking onto a signal (or closest over-approximation for devices with discrete
     * settings)
     *
     * @param seconds Time constant, in seconds
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    public abstract void setTimeConstant(double seconds) throws IOException, DeviceException;

    /**
     * Returns the frequency of the reference signal
     *
     * @return Frequency, in Hz
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    public abstract double getFrequency() throws IOException, DeviceException;

    /**
     * Returns the phase of the reference signal
     *
     * @return Phase, in degrees
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    public abstract double getRefPhase() throws IOException, DeviceException;

    /**
     * Returns the amplitude of the reference signal
     *
     * @return Amplitude, in Volts
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    public abstract double getRefAmplitude() throws IOException, DeviceException;

    /**
     * Returns the amplitude of the signal component locked on to by the amplifier
     *
     * @return Amplitude, in Volts
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    public abstract double getLockedAmplitude() throws IOException, DeviceException;

    /**
     * Returns the set time constant, in seconds
     *
     * @return Time constant, in seconds
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    public abstract double getTimeConstant() throws IOException, DeviceException;


    public void waitForStableLock(double pctMargin, long duration) throws IOException, DeviceException {

        Synch.waitForParamStable(
                this::getRefAmplitude,
                pctMargin,
                100,
                duration
        );

    }

    public void waitForStableLock() throws IOException, DeviceException {
        waitForStableLock(0.1, 5000);
    }

    /**
     * Enumeration of reference modes
     */
    public enum RefMode {
        INTERNAL,
        EXTERNAL
    }

}
