package JISA.Devices;

import JISA.Addresses.InstrumentAddress;

import java.io.IOException;

/**
 * Extension of the LockIn class for lock-in amplifiers with dual-phase capabilities
 */
public abstract class DPLockIn extends LockIn {

    public DPLockIn(InstrumentAddress address) throws IOException {
        super(address);
    }

    /**
     * Returns the amplitude of the component of the signal in-phase with the reference signal
     *
     * @return Amplitude, in volts
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    public abstract double getLockedX() throws IOException, DeviceException;

    /**
     * Returns the amplitude of the component of the signal 90 degrees out of phase with the reference signal
     *
     * @return Amplitude, in volts
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    public abstract double getLockedY() throws IOException, DeviceException;

    /**
     * Returns of the phase of the locked-on signal (relative to the reference)
     *
     * @return Phase, in degrees
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon compatibility error
     */
    public abstract double getLockedPhase() throws IOException, DeviceException;

}
