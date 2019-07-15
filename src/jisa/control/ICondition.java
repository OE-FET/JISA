package jisa.control;

import jisa.devices.DeviceException;

import java.io.IOException;

public interface ICondition {

    boolean isMet(int i) throws IOException, DeviceException;

}
