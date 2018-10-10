package JISA.Control;

import JISA.Devices.DeviceException;

import java.io.IOException;

public interface DoubleReturn {

    double getValue() throws IOException, DeviceException;

}
