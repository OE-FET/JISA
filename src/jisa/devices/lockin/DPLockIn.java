package jisa.devices.lockin;

import jisa.devices.DeviceException;

import java.io.IOException;

/**
 * Extension of the LockIn class for lock-in amplifiers with dual-phase capabilities
 */
public interface DPLockIn extends LockIn {

    static String getDescription() {
        return "Dual-Phase Lock-In Amplifier";
    }

    /**
     * Returns the amplitude of the component of the signal in-phase with the reference signal
     *
     * @return Amplitude, in volts
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    double getLockedX() throws IOException, DeviceException;

    /**
     * Returns the amplitude of the component of the signal 90 degrees out of phase with the reference signal
     *
     * @return Amplitude, in volts
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    double getLockedY() throws IOException, DeviceException;

    /**
     * Returns of the phase of the locked-on signal (relative to the reference)
     *
     * @return Phase, in degrees
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    double getLockedPhase() throws IOException, DeviceException;

    /**
     * Returns the offset being applied to the x component of the input signal.
     *
     * @return Offset on x, as % of measurement range.
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    double getXOffset() throws IOException, DeviceException;

    /**
     * Sets the offset to use on the x component of the input signal.
     *
     * @param offset Offset on x, as % of measurement range.
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    void setXOffset(double offset) throws IOException, DeviceException;

    /**
     * Tells the lock-in to set the x-offset to achieve a zero value of x.
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    void autoOffsetX() throws IOException, DeviceException;

    /**
     * Returns the offset being applied to the y component of the input signal.
     *
     * @return Offset on y, as % of measurement range.
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    double getYOffset() throws IOException, DeviceException;

    /**
     * Sets the offset to use on the y component of the input signal.
     *
     * @param offset Offset on y, as % of measurement range.
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    void setYOffset(double offset) throws IOException, DeviceException;

    /**
     * Tells the lock-in to set the x-offset to achieve a zero value of y.
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    void autoOffsetY() throws IOException, DeviceException;

}
