package jisa.devices.interfaces;

import jisa.devices.DeviceException;

import java.io.IOException;
import java.util.List;

public interface MultiSensor<T extends Sensor<T>> {

    int getNumSensors();

    String getSensorName(int sensorNumber);

    List<T> getSensors();

    T getSensor(int sensorNumber) throws IOException, DeviceException;

    Class<T> getSensorType();

}
