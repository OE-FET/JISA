package jisa.devices.interfaces;

import jisa.devices.DeviceException;

import java.io.IOException;

public interface XYTranslationStage extends Instrument {
    static String getDescription() {
        return "XY Translation Stage";
    }

    /**
     * Returns the x component of Position, in m.
     *
     * @return x-Position, in m
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    double getXPosition() throws IOException, DeviceException;

    /**
     * Sets the x-position to the desired value.
     *
     * @param xposition x-Position, in m
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void setXPosition(double xposition) throws IOException, DeviceException, InterruptedException;


    /**
     * Returns the y component of Position, in m.
     *
     * @return y-Position, in m
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    double getYPosition() throws IOException, DeviceException;

    /**
     * Sets the y-position to the desired value.
     *
     * @param yposition y-Position, in m
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void setYPosition(double yposition) throws IOException, DeviceException, InterruptedException;

    /**
     * Sets the x- and y-position to the desired value.
     *
     * @param xposition x-Position, in m
     * @param yposition y-Position, in m
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void setXYPosition(double xposition,double yposition) throws IOException, DeviceException, InterruptedException;

    /**
     * Sets the speed to the desired value in TODO.
     *
     * @param speed in TODO
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void setXYSpeed(double speed) throws IOException, DeviceException;

    /**
     * Returns the speed to the desired value in TODO.
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    double getXYSpeed() throws IOException, DeviceException;

    /**
     * Sets the rotation angle theta to the desired value.
     *
     * @param theta rotation angle, in degrees
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void setRotation(double theta) throws IOException, DeviceException, InterruptedException;

    /**
     * Returns rotation angle theta in degrees
     *
     * @return rotation angle, in degrees
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    double getRotation() throws IOException, DeviceException;


}
