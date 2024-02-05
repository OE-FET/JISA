package jisa.devices.interfaces;

import jisa.devices.DeviceException;

import java.io.IOException;

/**
 * Interface for representing lock-in amplifiers that can accept current input via an internal current pre-amplifier.
 */
public interface IPALockIn extends LockIn {

    static String getDescription() {
        return "Lock-In Amplifier with Current Pre-Amp";
    }

    /**
     * Sets whether the lock-in amplifier should use its current pre-amplifier to accept current input.
     *
     * @param flag Preamp enabled?
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatability error
     */
    void setCurrentInputEnabled(boolean flag) throws IOException, DeviceException;

    /**
     * Returns whether the lock-in amplifier is using its current pre-amplifier to accept current input.
     *
     * @return Preamp enabled?
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatability error
     */
    boolean isCurrentInputEnabled() throws IOException, DeviceException;

    /**
     * Sets the current-to-voltage gain used by the lock-in amplifier's current pre-amplifier.
     *
     * @param voltsPerAmp Gain, in volts per amp.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatability error
     */
    void setCurrentInputGain(double voltsPerAmp) throws IOException, DeviceException;

    /**
     * Returns the current-to-voltage gain used by the lock-in amplifier's current pre-amplifier.
     *
     * @return Gain, in volts per amp.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatability error
     */
    double getCurrentInputGain() throws IOException, DeviceException;

}
