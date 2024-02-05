package jisa.devices.interfaces;

import jisa.devices.DeviceException;

import java.io.IOException;

/**
 * Interface for representing instruments that have a second-harmonic line filter.
 */
public interface LineFilter2X {

    /**
     * Enables or disables the second-harmonic line filter on the input for this instrument (i.e., to filter out the second harmonic of the power-line frequency).
     *
     * @param enabled Should it be enabled?
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatability error
     */
    void set2xLineFilterEnabled(boolean enabled) throws IOException, DeviceException;

    /**
     * Returns whether the second-harmonic line filter is enabled on the input for this instrument (i.e., to filter out the second harmonic of the power-line frequency).
     *
     * @return Is it enabled
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatability error
     */
    boolean is2xLineFilterEnabled() throws IOException, DeviceException;

}
