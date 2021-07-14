package jisa.devices.interfaces;

public interface Sensor<T> {

    String getSensorName();

    Class<T> getSensorClass();

}
