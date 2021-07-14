package jisa.devices.interfaces;

import jisa.devices.DeviceException;

import java.io.IOException;
import java.util.List;

public interface MultiOutput<T extends Output<T>> {

    int getNumOutputs();

    List<T> getOutputs();

    T getOutput(int outputNumber) throws IOException, DeviceException;

    String getOutputName(int outputNumber);

    Class<T> getOutputClass();

}
