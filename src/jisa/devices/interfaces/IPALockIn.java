package jisa.devices.interfaces;

import jisa.devices.DeviceException;

import java.io.IOException;

public interface IPALockIn extends LockIn {

    static String getDescription() {
        return "Lock-In Amplifier with Current Pre-Amp";
    }

    void setCurrentInput(boolean flag) throws IOException, DeviceException;

    boolean isCurrentInput() throws IOException, DeviceException;

    void setCurrentGain(double voltsPerAmp) throws IOException, DeviceException;

    double getCurrentGain() throws IOException, DeviceException;

}
