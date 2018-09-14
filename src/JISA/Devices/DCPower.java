package JISA.Devices;

import JISA.Addresses.InstrumentAddress;
import JISA.Control.Synch;
import JISA.VISA.VISADevice;

import java.io.IOException;

public abstract class DCPower extends VISADevice {

    public DCPower(InstrumentAddress address) throws IOException {
        super(address);
    }

    public abstract void turnOn() throws IOException, DeviceException;

    public abstract void turnOff() throws IOException, DeviceException;

    public abstract void setVoltage(double voltage) throws IOException, DeviceException;

    public abstract void setCurrent(double current) throws IOException, DeviceException;

    public abstract double getVoltage() throws IOException, DeviceException;

    public abstract double getCurrent() throws IOException, DeviceException;

    public void waitForStableVoltage(double pctError, long time) throws IOException, DeviceException {


        Synch.waitForParamStable(
                this::getVoltage,
                pctError,
                100,
                time
        );


    }

    public void waitForStableCurrent(double pctError, long time) throws IOException, DeviceException {

        Synch.waitForParamStable(
                this::getCurrent,
                pctError,
                100,
                time
        );


    }

}
