package jisa.devices.translator.feature;

import jisa.devices.DeviceException;
import jisa.devices.ParameterList;
import jisa.devices.features.Feature;

import java.io.IOException;

public interface DriftCorrection extends Feature {

    static void addParameters(DriftCorrection inst, Class<?> target, ParameterList params) {
        params.addValue("Drift Correction", inst::isDriftCorrectionEnabled, false, inst::setDriftCorrectionEnabled);
    }

    void setDriftCorrectionEnabled(boolean enabled) throws IOException, DeviceException;

    boolean isDriftCorrectionEnabled() throws IOException, DeviceException;

}
