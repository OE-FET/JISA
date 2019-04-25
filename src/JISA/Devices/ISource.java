package JISA.Devices;

import java.io.IOException;

public interface ISource extends Instrument {

    void setCurrent(double current) throws IOException, DeviceException;

    double getCurrent() throws IOException, DeviceException;

    void turnOn() throws IOException, DeviceException;

    void turnOff() throws IOException, DeviceException;

    boolean isOn() throws IOException, DeviceException;

}
