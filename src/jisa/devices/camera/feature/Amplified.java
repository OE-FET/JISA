package jisa.devices.camera.feature;

import jisa.devices.DeviceException;
import jisa.devices.ParameterList;
import jisa.devices.features.Feature;

import java.io.IOException;

public interface Amplified extends Feature {

    /**
     * Sets the gain to use when amplifying the readout of frames from this camera.
     * If only discrete options are available, the closest value is select.
     *
     * @param gain The gain to use, in dB.
     *
     * @throws DeviceException Upon device incompatibility error.
     * @throws IOException     Upon communications error.
     */
    void setGain(double gain) throws DeviceException, IOException;

    /**
     * Returns the gain used when amplifying the readout of frames from this camera.
     *
     * @returns The gain being used, in dB.
     *
     * @throws DeviceException Upon device incompatibility error.
     * @throws IOException     Upon communications error.
     */
    double getGain() throws DeviceException, IOException;

    static void addParameters(Amplified inst, Class<?> target, ParameterList params) {

        params.addValue("Gain", inst::getGain, 1.0, inst::setGain);

    }

}
