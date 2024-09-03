package jisa.devices.meter;

import jisa.devices.DeviceException;
import jisa.devices.ParameterList;

import java.io.IOException;

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


    default ParameterList getBaseParameters(Class<?> target) {

        ParameterList parameters = TMeter.super.getBaseParameters(target);

        parameters.addChoice("Sensor Type", Type.UNKNOWN, this::setSensorType, Type.values());

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
