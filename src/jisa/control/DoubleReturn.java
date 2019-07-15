package jisa.control;

import jisa.devices.DeviceException;

import java.io.IOException;

public interface DoubleReturn {

    double getValue() throws IOException, DeviceException;

}
