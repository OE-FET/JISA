package JISA.Control;

import JISA.Devices.DeviceException;

import java.io.IOException;

public interface ICondition {

    boolean isMet(int i) throws IOException, DeviceException;

}
