package JISA.Devices;

import JISA.Experiment.IVPoint;

import java.io.IOException;

public interface IVMeter extends VMeter, IMeter {

    default IVPoint getIVPoint() throws IOException, DeviceException {
        return new IVPoint(getVoltage(), getCurrent());
    }

    default void setRanges(double voltage, double current) throws IOException, DeviceException {
        setVoltageRange(voltage);
        setCurrentRange(current);
    }

    default void useAutoRanges() throws IOException, DeviceException {
        useAutoVoltageRange();
        useAutoCurrentRange();
    }

}
