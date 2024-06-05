package jisa.devices.interfaces;

import jisa.devices.DeviceException;

import java.io.IOException;

public interface Translator extends Instrument {

    /**
     * Set the absolute position of this translation axis.
     *
     * @param position Position to move to, in metres.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatability error
     */
    void setPosition(double position) throws IOException, DeviceException;

    /**
     * Return the absolute position of this translation axis.
     *
     * @return Current position, in metres.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatability error
     */
    double getPosition() throws IOException, DeviceException;
    double getMinPosition() throws IOException, DeviceException;
    double getMaxPosition() throws IOException, DeviceException;

    /**
     * Sets the speed of this translation axis.
     *
     * @param speed Speed, in metres per second.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatability error
     */
    void setSpeed(double speed) throws IOException, DeviceException;

    /**
     * Returns the speed of this translation axis.
     *
     * @return Speed, in metres per second.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatability error
     */
    double getSpeed() throws IOException, DeviceException;
    double getMinSpeed() throws IOException, DeviceException;
    double getMaxSpeed() throws IOException, DeviceException;

    /**
     * Moves this translation axis by the specific amount.
     *
     * @param distance Relative distance, in metres.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatability error
     */
    void moveBy(double distance) throws IOException, DeviceException;

    /**
     * Returns this axis to its home position.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatability error
     */
    void moveToHome() throws IOException, DeviceException;

    /**
     * Sets whether this axis is locked or not. When locked, it shall not respond to movement commands.
     *
     * @param locked Locked?
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatability error
     */
    void setLocked(boolean locked) throws IOException, DeviceException;

    /**
     * Returns whether this axis ia locked or not. When locked, it shall not respond to movement commands.
     *
     * @return Locked?
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatability error
     */
    boolean isLocked() throws IOException, DeviceException;

    /**
     * Returns whether this translator is currently moving.
     *
     * @return Moving?
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatability error
     */
    boolean isMoving() throws IOException, DeviceException;

    /**
     * Stops any movement of this translator immediately.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatability error
     */
    void stop() throws IOException, DeviceException;

}
