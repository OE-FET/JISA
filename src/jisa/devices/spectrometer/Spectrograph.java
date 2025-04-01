package jisa.devices.spectrometer;

import jisa.devices.DeviceException;
import jisa.devices.Instrument;

import java.io.IOException;

public interface Spectrograph extends Instrument {

    /**
     * Returns the slit width being used at the entrance of the spectrograph.
     *
     * @return Slit width, in metres.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon instrument compatibility error
     */
    double getSlitWidth() throws IOException, DeviceException;

    /**
     * Returns the line density in the grating of the spectrograph.
     *
     * @return Line density, in lines per metre.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon instrument compatibility error
     */
    double getGratingDensity() throws IOException, DeviceException;

}
