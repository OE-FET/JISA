package jisa.devices;

import java.io.IOException;
import java.util.List;

public interface MultiOutput<T> {

    int getNumOutputs();

    List<T> getOutputs() throws DeviceException, IOException;

    T getOutput(int outputNumber) throws IOException, DeviceException;

}
