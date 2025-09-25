package jisa.devices.spectrometer.feature;

import jisa.devices.DeviceException;

import java.io.IOException;

public interface Shutter {

    void openShutter() throws IOException, DeviceException;

    void closeShutter() throws IOException, DeviceException;

    default void setShutterOpen(boolean open) throws IOException, DeviceException {

        if (open) {
            openShutter();
        } else {
            closeShutter();
        }

    }

    boolean isShutterOpen() throws IOException, DeviceException;

}
