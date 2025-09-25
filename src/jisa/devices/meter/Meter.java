package jisa.devices.meter;

import jisa.devices.DeviceException;
import jisa.devices.relay.Switch;

import java.io.IOException;

public interface Meter extends Switch {

    static String getDescription() {
        return "General Meter";
    }

    void setIntegrationTime(double intTime) throws IOException, DeviceException;

    double getIntegrationTime() throws IOException, DeviceException;

}
