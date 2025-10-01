package jisa.devices.spectrometer.feature;

import jisa.devices.DeviceException;
import jisa.devices.ParameterList;
import jisa.devices.features.Feature;

import java.io.IOException;

public interface Fan extends Feature {

    static void addParameters(Fan inst, Class<?> target, ParameterList params) {
        params.addValue("Fan Enabled", inst::isFanEnabled, false, inst::setFanEnabled);
    }

    void setFanEnabled(boolean enabled) throws IOException, DeviceException;

    boolean isFanEnabled() throws IOException, DeviceException;

}
