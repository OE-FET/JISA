package JISA.Devices;

import java.io.IOException;

public interface VSource extends Instrument {

    void setVoltage(double voltage) throws IOException, DeviceException;

    double getCurrent() throws IOException, DeviceException;

    void turnOn() throws IOException, DeviceException;

    void turnOff() throws IOException, DeviceException;

    boolean isOn() throws IOException, DeviceException;

}
