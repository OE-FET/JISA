package jisa.devices.camera.feature;

import jisa.devices.DeviceException;
import jisa.devices.ParameterList;
import jisa.devices.features.Feature;

import java.io.IOException;

public interface Shuttered extends Feature {

    static void addParameters(Shuttered inst, Class<?> target, ParameterList parameters) {

        parameters.addAuto("Shutter Open", inst::isShutterAuto, false, inst::isShutterOpen, false,
            v -> inst.setShutterAuto(true),
            v -> {
                inst.setShutterAuto(false);
                inst.setShutterOpen(v);
            }
        );

    }

    /**
     * Sets whether the shutter (either on this camera or controlled by it) is open or closed.
     *
     * @param open Should it be open?
     *
     * @throws IOException     Upon communications error.
     * @throws DeviceException Upon error on device/compatibility error.
     */
    void setShutterOpen(boolean open) throws IOException, DeviceException;

    /**
     * Returns whether the shutter (either on this camera or controlled by it) is open or closed.
     *
     * @return Is it open?
     *
     * @throws IOException     Upon communications error.
     * @throws DeviceException Upon error on device/compatibility error.
     */
    boolean isShutterOpen() throws IOException, DeviceException;

    /**
     * Sets whether the camera should automatically control the shutter.
     *
     * @param auto Automatic shutter?
     *
     * @throws IOException     Upon communications error.
     * @throws DeviceException Upon error on device/compatibility error.
     */
    void setShutterAuto(boolean auto) throws IOException, DeviceException;

    /**
     * Returns whether the camera is automatically controlling the shutter.
     *
     * @return Automatic shutter?
     *
     * @throws IOException     Upon communications error.
     * @throws DeviceException Upon error on device/compatibility error.
     */
    boolean isShutterAuto() throws IOException, DeviceException;

}
