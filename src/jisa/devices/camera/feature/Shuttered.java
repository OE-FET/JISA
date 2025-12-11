package jisa.devices.camera.feature;

import jisa.devices.DeviceException;
import jisa.devices.ParameterList;
import jisa.devices.features.Feature;

import java.io.IOException;

public interface Shuttered extends Feature {

    static void addParameters(Shuttered inst, Class<?> target, ParameterList parameters)  {

        parameters.addAuto("Shutter Open", inst::isShutterAuto, false, inst::isShutterOpen, false, v -> inst.setShutterAuto(true), v -> {
            inst.setShutterAuto(false);
            inst.setShutterOpen(v);
        });

    }

    void setShutterOpen(boolean open) throws IOException, DeviceException;

    boolean isShutterOpen() throws IOException, DeviceException;

    void setShutterAuto(boolean auto) throws IOException, DeviceException;

    boolean isShutterAuto() throws IOException, DeviceException;

}
