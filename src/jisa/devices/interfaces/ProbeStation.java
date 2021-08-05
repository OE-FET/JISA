package jisa.devices.interfaces;

import jisa.devices.DeviceException;

import java.io.IOException;

/**
 * Interface for defining the standard functionality of a probe station.
 */


public interface ProbeStation extends XYZTranslationStage {

    public static String getDescription() {
        return "Probe Station";
    }

    /**
     * Write z axis locking distance
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void setLockDistance(double dist) throws IOException, DeviceException;

    /**
     * Returns the z axis locking distance
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    double getLockDistance() throws IOException, DeviceException;
    
    /**
     * Write: 1 = locked position (up). 2 = not locked (down)
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void setLocked(boolean locked) throws IOException, DeviceException;

    /**
     * Returns if in locked position (1) or not (0)
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    boolean isLocked() throws IOException, DeviceException;

    /**
     * Write z-axis fine lift
     *
     * @param axis : Movement axis (X,Y)
     * @param axis : Percentage of velocity: 0 for stop, negative for movement in negative direction
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void continMovement(String axis,double velocityPercentage) throws IOException, DeviceException;

    /**
     * Write z-axis gross lift
     *
     * @param position in m
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void setGrossUpDistance(double position) throws IOException, DeviceException;

    /**
     * Set Gross Up (1) or Gross Down (0)
     *
     * @param locked 1: up; 0: down
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void setGrossUp(boolean locked) throws IOException, DeviceException;

    /**
     * Returns if Gross Up or not
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    boolean isGrossLocked() throws IOException;

    double getGrossUpDistance() throws IOException, DeviceException;

    /**
     * Switches Light on (1) or off (2)
     *
     * @param lightOn 1: light on; 0: light off
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void setLightOn(boolean lightOn) throws IOException, DeviceException;

    /**
     * Returns if light is on or not
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    boolean getLightOn() throws IOException;



}

