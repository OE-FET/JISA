package jisa.devices.spectrometer.feature;

import jisa.devices.DeviceException;
import jisa.devices.features.Feature;

import java.io.IOException;

public interface AdjustableSlit extends Feature {

    /**
     * Returns the slit width currently being used at the entrance of the spectrograph.
     *
     * @return Slit width, in metres.
     *
     * @throws IOException     Upon communications error.
     * @throws DeviceException Upon instrument compatibility error.
     */
    double getSlitWidth() throws IOException, DeviceException;

    /**
     * Changes the width of the slit at the entrance of the spectrograph.
     * If only discrete options are available, this will choose the option that is closest
     * in numerical value.
     *
     * @param width Slit width to use, in metres.
     *
     * @throws IOException     Upon communications error.
     * @throws DeviceException Upon instrument compatibility error.
     */
    void setSlitWidth(double width) throws IOException, DeviceException;

}
