package jisa.devices.meter;

import jisa.devices.DeviceException;
import jisa.devices.Instrument;
import jisa.devices.relay.Switch;

import java.io.IOException;

public interface Meter extends Instrument, Switch {

    static String getDescription() {
        return "General Meter";
    }

    double getValue() throws IOException, DeviceException;

    void setIntegrationTime(double intTime) throws IOException, DeviceException;

    double getIntegrationTime() throws IOException, DeviceException;

    String getMeasuredQuantity() throws IOException, DeviceException;

    String getMeasuredUnits() throws IOException, DeviceException;

}
