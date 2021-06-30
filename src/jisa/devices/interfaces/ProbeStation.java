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










}

