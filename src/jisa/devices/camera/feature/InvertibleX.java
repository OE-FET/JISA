package jisa.devices.camera.feature;

import jisa.devices.DeviceException;
import jisa.devices.ParameterList;
import jisa.devices.features.Feature;

import java.io.IOException;

public interface InvertibleX extends Feature {

    static void addParameters(InvertibleX inst, Class<?> target, ParameterList parameters) {

        parameters.addValue("Frame Inversion", "Invert X", inst::isInvertedX, false, inst::setInvertedX);

    }

    /**
     * Returns whether this camera is inverting its x-axis or not.
     *
     * @return Inverted?
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device / compatibility error
     */
    boolean isInvertedX() throws IOException, DeviceException;

    /**
     * Sets whether this camera should invert its x-axis or not.
     *
     * @param flippedX Invert?
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device / compatibility error
     */
    void setInvertedX(boolean flippedX) throws IOException, DeviceException;

}
