package jisa.control;

import jisa.devices.DeviceException;

import java.io.IOException;

public interface Returnable<T> {

    T get() throws IOException, DeviceException;

}
