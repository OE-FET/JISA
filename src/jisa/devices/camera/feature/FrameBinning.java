package jisa.devices.camera.feature;

import jisa.devices.DeviceException;
import jisa.devices.ParameterList;
import jisa.devices.features.Feature;

import java.io.IOException;

public interface FrameBinning extends Feature {

    /**
     * Returns the number of sequential frames captured by the camera to bin into each returned frame.
     *
     * @return Temporal binning count
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    int getFrameBinning() throws IOException, DeviceException;

    /**
     * Sets the number of sequential frames captured by the camera to bin into each returned frame.
     *
     * @param binning Temporal binning count
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    void setFrameBinning(int binning) throws IOException, DeviceException;

    static void addParameters(FrameBinning inst, Class<?> target, ParameterList parameters) {
        parameters.addValue("Frame Binning", inst::getFrameBinning, 1, inst::setFrameBinning);
    }

}
