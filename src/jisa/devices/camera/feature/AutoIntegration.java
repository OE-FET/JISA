package jisa.devices.camera.feature;

import jisa.devices.DeviceException;
import jisa.devices.ParameterList;
import jisa.devices.camera.Camera;
import jisa.devices.features.Feature;

import java.io.IOException;

public interface AutoIntegration extends Feature {

    static void addParameters(AutoIntegration inst, Class<?> target, ParameterList parameters) {

        parameters.addAuto("Integration Time [s]", inst::isAutoIntegrationEnabled, false, ((Camera) inst)::getIntegrationTime, 0.0, v -> inst.setAutoIntegrationEnabled(true), v -> {
            inst.setAutoIntegrationEnabled(false);
            ((Camera) inst).setIntegrationTime(v);
        });

    }

    /**
     * Returns whether the camera is automatically adjusting its integration time.
     *
     * @return Auto adjusting?
     *
     * @throws IOException     Upon communications error.
     * @throws DeviceException Upon device/compatibility error.
     */
    boolean isAutoIntegrationEnabled() throws IOException, DeviceException;

    /**
     * Sets whether the camera should automatically adjust its integration time.
     *
     * @param enabled Auto adjusting?
     *
     * @throws IOException     Upon communications error.
     * @throws DeviceException Upon device/compatibility error.
     */
    void setAutoIntegrationEnabled(boolean enabled) throws IOException, DeviceException;

}
