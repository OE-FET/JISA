package JISA.Devices;

import JISA.Addresses.InstrumentAddress;

import java.io.IOException;

public abstract class MSTController extends TController {

    public MSTController(InstrumentAddress address) throws IOException {
        super(address);
    }

    public abstract double getTemperature(int sensor) throws IOException, DeviceException;

    @Override
    public double getTemperature() throws IOException, DeviceException {
        return getTemperature(0);
    }

}
