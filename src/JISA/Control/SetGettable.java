package JISA.Control;

import JISA.Devices.DeviceException;

import java.io.IOException;

public interface SetGettable<T> {

    void set(T value) throws IOException, DeviceException;

    T get() throws IOException, DeviceException;

}
