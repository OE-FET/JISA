package jisa.devices.interfaces;

import jisa.devices.DeviceException;

import java.io.IOException;

/**
 * Interface for representing instruments that have a line filter.
 */
public interface LineFilter {

    /**
     * Enables or disables the line filter on the input for this instrument (i.e., to filter out the power-line frequency).
     *
     * @param enabled Should it be enabled?
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatability error
     */
    void setLineFilterEnabled(boolean enabled) throws IOException, DeviceException;

    /**
     * Returns whether the line filter is enabled on the input for this instrument (i.e., to filter out the power-line frequency).
     *
     * @return Is it enabled
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatability error
     */
    boolean isLineFilterEnabled() throws IOException, DeviceException;

}
