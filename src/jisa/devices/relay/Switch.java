package jisa.devices.relay;

import jisa.devices.DeviceException;
import jisa.devices.Instrument;

import java.io.IOException;

public interface Switch extends Instrument {

    static String getDescription() {
        return "Switch";
    }

    void turnOn() throws IOException, DeviceException;

    void turnOff() throws IOException, DeviceException;

    default void setOn(boolean on) throws IOException, DeviceException {

        if (on) {
            turnOn();
        } else {
            turnOff();
        }

    }

    boolean isOn() throws IOException, DeviceException;

}
