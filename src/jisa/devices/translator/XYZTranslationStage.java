package jisa.devices.translator;

import jisa.devices.DeviceException;

import java.io.IOException;

public interface XYZTranslationStage extends XYTranslationStage {
    /**
     * Returns the z component of Position, in m.
     *
     * @return z-Position, in m
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    double getZPosition() throws IOException, DeviceException;

    /**
     * Sets the x-position to the desired value.
     *
     * @param zposition z-Position, in m
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void setZPosition(double zposition) throws IOException, DeviceException;

    /**
     * Sets the speed to the desired value in TODO.
     *
     * @param speed in TODO
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void setZSpeed(double speed) throws IOException, DeviceException;

    /**
     * Sets the speed to the desired value in TODO.
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    double getZSpeed() throws IOException, DeviceException;
}
