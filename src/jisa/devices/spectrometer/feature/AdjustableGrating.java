package jisa.devices.spectrometer.feature;

import jisa.devices.DeviceException;
import jisa.devices.features.Feature;

import java.io.IOException;

public interface AdjustableGrating extends Feature {

    /**
     * Returns the grating line density currently being used by the spectrograph.
     *
     * @return Line density, in lines per metre.
     *
     * @throws IOException     Upon communications error.
     * @throws DeviceException Upon instrument compatibility error.
     */
    double getGratingDensity() throws IOException, DeviceException;

    /**
     * Changes the grating line density being used by the spectrograph.
     * If only discrete options are available, the closest value will be chosen.
     *
     * @param density The line density to use, in lines per metre.
     *
     * @throws IOException     Upon communications error.
     * @throws DeviceException Upon instrument compatibility error.
     */
    void setGratingDensity(double density) throws IOException, DeviceException;

}
