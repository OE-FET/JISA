package jisa.devices;

import java.io.IOException;
import java.util.List;

public interface MultiSensor<T> {

    int getNumSensors();

    String getSensorName(int sensorNumber);

    List<T> getSensors();

    T getSensor(int sensorNumber) throws IOException, DeviceException;

    Class<T> getSensorType();

}
