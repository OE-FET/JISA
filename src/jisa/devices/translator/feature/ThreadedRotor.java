package jisa.devices.translator.feature;

import jisa.devices.DeviceException;
import jisa.devices.ParameterList;
import jisa.devices.features.Feature;

import java.io.IOException;

public interface ThreadedRotor extends Feature {

    static void addParameters(ThreadedRotor inst, Class<?> target, ParameterList params) {
        params.addValue("Distance per Revolution", inst::getDistancePerRevolution, 200e-6, inst::setDistancePerRevolution);
    }

    void setDistancePerRevolution(double distancePerRevolution) throws IOException, DeviceException;

    double getDistancePerRevolution() throws IOException, DeviceException;

}
