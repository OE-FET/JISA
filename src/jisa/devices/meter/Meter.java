package jisa.devices.meter;

import jisa.devices.DeviceException;
import jisa.devices.Instrument;

import java.io.IOException;

public interface Meter extends Instrument {

    static String getDescription() {
        return "General Meter";
    }

    void setIntegrationTime(double intTime) throws IOException, DeviceException;

    double getIntegrationTime() throws IOException, DeviceException;

}
