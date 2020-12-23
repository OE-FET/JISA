package jisa.devices;

import java.io.IOException;
import java.util.List;

public interface MultiOutput<T> {

    int getNumOutputs();

    List<T> getOutputs();

    T getOutput(int outputNumber) throws IOException, DeviceException;

    String getOutputName(int outputNumber);

    Class<T> getOutputType();

}
