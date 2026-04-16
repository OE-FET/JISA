package jisa.devices.spectrometer.feature;

import jisa.devices.DeviceException;
import jisa.devices.ParameterList;
import jisa.devices.features.Feature;

import java.io.IOException;

public interface Shuttered extends Feature {

    static void addParameters(Shuttered inst, Class<?> target, ParameterList parameters) {
        parameters.addChoice("Input", "Shutter Mode", inst::getShutterMode, Mode.OPEN, inst::setShutterMode, Mode.values());
    }

    void setShutterMode(Mode mode) throws IOException, DeviceException;

    Mode getShutterMode() throws IOException, DeviceException;

    enum Mode {
        OPEN,
        CLOSED,
        EXTERNAL;
    }

}
