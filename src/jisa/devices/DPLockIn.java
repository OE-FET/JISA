package jisa.devices;

import java.io.IOException;

/**
 * Extension of the LockIn class for lock-in amplifiers with dual-phase capabilities
 */
public interface DPLockIn extends LockIn {

    public static String getDescription() {
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

    void autoOffsetX() throws IOException, DeviceException;

    void autoOffsetY() throws IOException, DeviceException;

    void autoOffsetAmplitude() throws IOException, DeviceException;

    default void autoOffset() throws IOException, DeviceException {
        autoOffsetAmplitude();
        autoOffsetX();
        autoOffsetY();
    }

}
