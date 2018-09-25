package JISA.Devices;

import JISA.Addresses.InstrumentAddress;
import JISA.VISA.VISADevice;

import java.io.IOException;

public abstract class TController extends VISADevice {

    public TController(InstrumentAddress address) throws IOException {
        super(address);
    }

    public abstract void setTargetTemperature(double temperature) throws IOException, DeviceException;

    public abstract double getTemperature() throws IOException, DeviceException;

    public abstract double getTargetTemperature() throws IOException, DeviceException;

    public abstract double getHeaterPower() throws IOException, DeviceException;

    public abstract double getGasFlow() throws IOException, DeviceException;

}
