package jisa.devices.camera.feature;

import jisa.devices.DeviceException;
import jisa.devices.ParameterList;
import jisa.devices.features.Feature;

import java.io.IOException;

public interface SensorCrop extends Feature {

    static void addParameters(SensorCrop inst, Class<?> target, ParameterList parameters) {

        parameters.addValue("Sensor Crop", inst::isSensorCropEnabled, false, inst::setSensorCropEnabled);

    }

    void setSensorCropEnabled(boolean enabled) throws IOException, DeviceException;

    boolean isSensorCropEnabled() throws IOException, DeviceException;

}