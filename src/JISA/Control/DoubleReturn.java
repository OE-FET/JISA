package JISA.Control;

import JISA.DeviceException;

import java.io.IOException;

public interface DoubleReturn {

    public double getValue() throws IOException, DeviceException;

}
