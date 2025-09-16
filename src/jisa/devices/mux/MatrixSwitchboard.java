package jisa.devices.mux;

import jisa.devices.DeviceException;

import java.io.IOException;

public interface MatrixSwitchboard {

    void connect(int row, int col) throws IOException, DeviceException;

    void disconnect(int row, int col) throws IOException, DeviceException;

    boolean isConnected(int row, int col) throws IOException, DeviceException;

}
