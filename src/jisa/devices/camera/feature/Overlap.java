package jisa.devices.camera.feature;

import jisa.devices.DeviceException;
import jisa.devices.ParameterList;
import jisa.devices.features.Feature;

import java.io.IOException;

public interface Overlap extends Feature {

    /**
     * Returns whether this camera is overlapping exposure and readout periods to increase framerate.
     *
     * @return Is overlapping?
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    boolean isOverlapEnabled() throws IOException, DeviceException;

    /**
     * Sets whether this camera should overlap its exposure and readout periods to increase framerate.
     *
     * @param overlapEnabled Enabled overlap?
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    void setOverlapEnabled(boolean overlapEnabled) throws IOException, DeviceException;

    static void addParameters(Overlap inst, Class<?> target, ParameterList parameters)  {
        parameters.addValue("Overlapping Readout", inst::isOverlapEnabled, false, inst::setOverlapEnabled);
    }

}
