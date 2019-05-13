package JISA.Devices;

import java.io.IOException;

public interface MSTMeter extends TMeter {

    double getTemperature(int sensor) throws IOException, DeviceException;

    int getNumSensors() throws IOException, DeviceException;

    default double getTemperature() throws IOException, DeviceException {
        return getTemperature(0);
    }

    default double[] getTemperatures() throws IOException, DeviceException {

        double[] values = new double[getNumSensors()];

        for (int i = 0; i < getNumSensors(); i++) {
            values[i] = getTemperature(i);
        }

        return values;

    }

    default TMeter getSensor(int sensor) throws IOException, DeviceException {

        return new TMeter() {
            @Override
            public double getTemperature() throws IOException, DeviceException {
                return MSTMeter.this.getTemperature(sensor);
            }

            @Override
            public String getIDN() throws IOException {
                return MSTMeter.this.getIDN();
            }

            @Override
            public void close() throws IOException, DeviceException {
                MSTMeter.this.close();
            }
        };

    }

}
