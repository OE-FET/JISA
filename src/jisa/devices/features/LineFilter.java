package jisa.devices.features;

import jisa.devices.DeviceException;
import jisa.devices.ParameterList;

import java.io.IOException;

/**
 * Interface for representing instruments that have a line filter.
 */
public interface LineFilter extends Feature {

    static void addParameters(LineFilter instrument, Class<?> target, ParameterList parameters) {

        try {
            parameters.addValue("Line Filter", instrument.isLineFilterEnabled(), instrument::setLineFilterEnabled);
        } catch (Exception e) {
            parameters.addValue("Line Filter", false, instrument::setLineFilterEnabled);
        }

    }

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
