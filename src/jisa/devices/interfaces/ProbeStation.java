package jisa.devices.interfaces;

import jisa.devices.DeviceException;

import java.io.IOException;

/**
 * Interface for defining the standard functionality of a probe station.
 */


public interface ProbeStation extends Instrument {

    public static String getDescription() {
        return "Probe Station";
    }

    /**
     * Write z-axis fine lift
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void WritezFineLift(double fineheight) throws IOException, DeviceException;


    /**
     * Write z-axis gross lift
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void WritezGrossLift(double grossheight) throws IOException, DeviceException;


    /**
     * Moves chuck to the gross UP position.
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void ChuckGrossUp() throws IOException, DeviceException;


    /**
     * Moves chuck to the gross DOWN position.
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void ChuckGrossDown() throws IOException, DeviceException;

    /**
     * Moves chuck to the fine UP position.
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void ChuckFineUp() throws IOException, DeviceException;

    /**
     * Moves chuck to the fine DOWN position.
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void ChuckFineDown() throws IOException, DeviceException;


    /**
     * Returns the x component of Position, in um.
     *
     * @return x-Position, in um
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    double getXposition() throws IOException, DeviceException;

    /**
     * Returns the y component of Position, in um.
     *
     * @return y-Position, in um
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    double getYposition() throws IOException, DeviceException;

    /**
     * Sets the x-position to the desired value.
     *
     * @param xposition x-Position, in um
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void setXposition(double xposition) throws IOException, DeviceException;


    /**
     * Sets the y-position to the desired value.
     *
     * @param yposition y-Position, in um
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void setYposition(double yposition) throws IOException, DeviceException;

    /**
     * Returns the angle
     *
     * @return rotation angle, in millidegrees
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    double getAngle() throws IOException, DeviceException;

    /**
     * Sets the angle theta to the desired value.
     *
     * @param theta rotation angle, in millidegrees
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void setAngle(double theta) throws IOException, DeviceException;


    /**
     * Returns Pegasus model name, followed by a semi-colon,followed by a list of options separated by commas
     *
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    String getModel() throws IOException, DeviceException;

}

