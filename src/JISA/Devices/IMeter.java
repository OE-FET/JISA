package JISA.Devices;

import JISA.Devices.SMU.AMode;

import java.io.IOException;

public interface IMeter extends Instrument {


    /**
     * Takes a current measurement and returns the value.
     *
     * @return Current measurement value, in Amps
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    double getCurrent() throws IOException, DeviceException;

    /**
     * Sets the integration time for each measurement.
     *
     * @param time Integration time, in seconds.
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void setIntegrationTime(double time) throws IOException, DeviceException;

    /**
     *
     * @return
     * @throws IOException
     * @throws DeviceException
     */
    double getIntegrationTime() throws IOException, DeviceException;

    void setCurrentRange(double range) throws IOException, DeviceException;

    double getCurrentRange() throws IOException, DeviceException;

    void useAutoCurrentRange() throws IOException, DeviceException;

    boolean isCurrentRangeAuto() throws IOException, DeviceException;

    void setAverageMode(AMode mode) throws IOException, DeviceException;

    void setAverageCount(int count) throws IOException, DeviceException;

    AMode getAverageMode() throws IOException, DeviceException;

    int getAverageCount() throws IOException, DeviceException;

    void turnOn() throws IOException, DeviceException;

    void turnOff() throws IOException, DeviceException;

    boolean isOn() throws IOException, DeviceException;

}
