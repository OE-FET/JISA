package JISA.Control;

import JISA.Devices.DeviceException;

import java.io.IOException;

public interface DoubleReturn {

    public double getValue() throws IOException, DeviceException;

}
