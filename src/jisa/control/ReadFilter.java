package jisa.control;

import jisa.devices.DeviceException;

import java.io.IOException;

public interface ReadFilter {

    double getValue() throws IOException, DeviceException;

    void setCount(int count);

    int getCount();

    void clear();

    void setUp() throws IOException, DeviceException;

    interface Setupable {
        void run(int count) throws IOException, DeviceException;
    }
}
