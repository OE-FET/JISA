package jisa.devices.interfaces;

import jisa.Util;
import jisa.addresses.Address;
import jisa.devices.DeviceException;

import java.io.IOException;

public interface MSTCouple extends MSTMeter, TCouple {

    void setSensorType(int sensor, Type type) throws IOException, DeviceException;

    Type getSensorType(int sensor) throws IOException, DeviceException;

    default void setSensorType(Type type) throws IOException, DeviceException {

        for (int i = 0; i < getNumSensors(); i++) {
            setSensorType(i, type);
        }

    }

    default Type getSensorType() throws IOException, DeviceException {
        return getSensorType(0);
    }

    /**
     * Returns the specified sensor as its own thermometer object.
     *
     * @param sensor The sensor to return
     *
     * @return Sensor as thermometer
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    default TCouple getSensor(int sensor) {

        if (!Util.isBetween(sensor, 0, getNumSensors() - 1)) {
            return null;
        }

        return new TCouple() {

            @Override
            public void setSensorType(Type type) throws IOException, DeviceException {
                MSTCouple.this.setSensorType(sensor, type);
            }

            @Override
            public Type getSensorType() throws IOException, DeviceException {
                return MSTCouple.this.getSensorType(sensor);
            }

            @Override
            public String getName() {
                return MSTCouple.this.getName(sensor);
            }

            @Override
            public double getTemperature() throws IOException, DeviceException {
                return MSTCouple.this.getTemperature(sensor);
            }

            @Override
            public void setTemperatureRange(double range) throws IOException, DeviceException {
                MSTCouple.this.setTemperatureRange(sensor, range);
            }

            @Override
            public double getTemperatureRange() throws IOException, DeviceException {
                return MSTCouple.this.getTemperatureRange(sensor);
            }

            @Override
            public String getIDN() throws IOException, DeviceException {
                return MSTCouple.this.getIDN();
            }

            @Override
            public void close() throws IOException, DeviceException {
                MSTCouple.this.close();
            }

            @Override
            public Address getAddress() {
                return MSTCouple.this.getAddress();
            }

        };

    }

}
