package jisa.devices.translator.feature;

import jisa.devices.DeviceException;
import jisa.devices.ParameterList;
import jisa.devices.features.Feature;

import java.io.IOException;

public interface Backlash extends Feature {

    static void addParameters(Backlash instrument, Class<?> target, ParameterList parameters) {

        parameters.addOptional(
            "Backlash",
            instrument::isBacklashEnabled,
            false,
            instrument::getBacklashSteps,
            0,
            v -> instrument.setBacklashEnabled(false),
            v -> {
                instrument.setBacklashEnabled(true);
                instrument.setBacklashSteps(v);
            });

    }

    void setBacklashEnabled(boolean enabled) throws IOException, DeviceException;

    boolean isBacklashEnabled() throws IOException, DeviceException;

    void setBacklashSteps(int steps) throws IOException, DeviceException;

    int getBacklashSteps() throws IOException, DeviceException;

}
