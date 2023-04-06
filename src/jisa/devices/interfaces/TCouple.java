package jisa.devices.interfaces;

import jisa.devices.DeviceException;

import java.io.IOException;
import java.util.List;

public interface TCouple extends TMeter {

    /**
     * Sets the type of thermal sensor being used for this thermometer. If instrument has no ability to set the sensor
     * type, then this method will have no effect.
     *
     * @param type Sensor type
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void setSensorType(Type type) throws IOException, DeviceException;

    /**
     * Returns the type of thermal sensor being used for this thermometer. If instrument has no ability to set the sensor
     * type, then this method will most likely return SensorType.UNKNOWN
     *
     * @return Sensor type
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    Type getSensorType() throws IOException, DeviceException;


    default List<Parameter<?>> getConfigurationParameters(Class<?> target) {

        List<Parameter<?>> parameters = TMeter.super.getConfigurationParameters(target);

        parameters.add(new Parameter<>("Sensor Type", Type.UNKNOWN, this::setSensorType, Type.values()));

        return parameters;

    }


    /**
     * Enumeration of standard thermocouple types
     */
    enum Type {

        UNKNOWN,
        B,
        E,
        J,
        K,
        N,
        R,
        S,
        T

    }
}
