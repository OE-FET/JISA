package jisa.devices;

import java.io.IOException;
import java.util.List;

public interface MultiSensor<T> {

    int getNumSensors() throws DeviceException, IOException;

    List<T> getSensors() throws DeviceException, IOException;

    T getSensor(int sensorNumber) throws IOException, DeviceException;

}
