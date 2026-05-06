package jisa.devices.camera.feature;

import jisa.devices.DeviceException;
import jisa.devices.ParameterList;
import jisa.devices.features.Feature;

import java.io.IOException;

public interface InvertibleY extends Feature {

    static void addParameters(InvertibleY inst, Class<?> target, ParameterList parameters) {

        parameters.addValue("Frame Inversion", "Invert Y", inst::isInvertedY, false, inst::setInvertedY);

    }

    /**
     * Returns whether this camera is inverting its y-axis or not.
     *
     * @return Inverted?
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device / compatibility error
     */
    boolean isInvertedY() throws IOException, DeviceException;

    /**
     * Sets whether this camera should invert its y-axis or not.
     *
     * @param flippedY Invert?
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device / compatibility error
     */
    void setInvertedY(boolean flippedY) throws IOException, DeviceException;

}
