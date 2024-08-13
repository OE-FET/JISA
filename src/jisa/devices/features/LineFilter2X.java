package jisa.devices.features;

import jisa.devices.DeviceException;
import jisa.devices.ParameterList;

import java.io.IOException;

/**
 * Interface for representing instruments that have a second-harmonic line filter.
 */
public interface LineFilter2X extends Feature {

    static void addParameters(LineFilter2X instrument, ParameterList parameters) {

        try {
            parameters.addValue("2x Line Filter", instrument.is2xLineFilterEnabled(), instrument::set2xLineFilterEnabled);
        } catch (Exception e) {
            parameters.addValue("2x Line Filter", false, instrument::set2xLineFilterEnabled);
        }

    }

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
