package jisa.devices;

public interface Sensor<T> {

    String getSensorName();

    Class<T> getSensorType();

}
