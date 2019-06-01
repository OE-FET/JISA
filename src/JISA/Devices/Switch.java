package JISA.Devices;

import java.io.IOException;

public interface Switch extends Instrument {

    void turnOn() throws IOException, DeviceException;

    void turnOff() throws IOException, DeviceException;

    boolean isOn() throws IOException, DeviceException;

}
