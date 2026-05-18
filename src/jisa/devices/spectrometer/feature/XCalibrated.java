package jisa.devices.spectrometer.feature;

import jisa.devices.DeviceException;
import jisa.devices.features.Feature;

import java.io.IOException;

public interface XCalibrated extends Feature {

    double[] getWavelengths(int sensorColumnCount, double columnWidth, int startColumn, int width) throws IOException, DeviceException;

}
