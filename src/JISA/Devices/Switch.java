package JISA.Devices;

import java.io.IOException;

public interface Switch extends Instrument {

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
