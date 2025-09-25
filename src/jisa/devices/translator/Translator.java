package jisa.devices.translator;

import jisa.control.Sync;
import jisa.devices.DeviceException;
import jisa.devices.Instrument;
import jisa.devices.ParameterList;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public interface Translator extends Instrument {

    static void addParameters(Translator inst, Class<?> target, ParameterList params) {

        params.addValue("Max Speed [%]", inst::getMaxSpeed, 100.0, inst::setMaxSpeed);
        params.addValue("Locked", inst::isLocked, false, inst::setLocked);

    }

    /**
     * Set the absolute position of this translation axis.
     *
     * @param position Position to move to, in metres (linear) or degrees (rotational).
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatability error
     */
    void setPosition(double position) throws IOException, DeviceException;

    default void setPositionAndWait(double position) throws IOException, DeviceException, InterruptedException {
        setPosition(position);
        waitUntilStationary();
    }

    default void setPositionAndWait(double position, long timeout) throws IOException, DeviceException, InterruptedException, TimeoutException {
        setPosition(position);
        waitUntilStationary(timeout);
    }

    default void waitUntilStationary() throws IOException, DeviceException, InterruptedException {
        Sync.waitForCondition((i) -> !isMoving(), 250);
    }

    default void waitUntilStationary(long timeout) throws IOException, DeviceException, InterruptedException, TimeoutException {
        Sync.waitForCondition((i) -> !isMoving(), 250, timeout);
    }

    /**
     * Return the absolute position of this translator.
     *
     * @return Current position, in metres (linear) or degrees (rotational).
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatability error
     */
    double getPosition() throws IOException, DeviceException;

    /**
     * Returns the minimum value of position for this translator.
     *
     * @return Minimum position, in metres (linear) or degrees (rotational).
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatability error
     */
    double getMinPosition() throws IOException, DeviceException;

    /**
     * Returns the maximum value of position for this translator.
     *
     * @return Maximum position, in metres (linear) or degrees (rotational).
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatability error
     */
    double getMaxPosition() throws IOException, DeviceException;

    /**
     * Sets the speed of this translation axis.
     *
     * @param speed Speed, as a percentage of the max possible value.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatability error
     */
    void setMaxSpeed(double speed) throws IOException, DeviceException;

    /**
     * Returns the speed of this translation axis.
     *
     * @return Speed, as a percentage of the max possible value.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatability error
     */
    double getMaxSpeed() throws IOException, DeviceException;

    /**
     * Moves this translation axis by the specific amount.
     *
     * @param distance Relative distance, in metres (linear) or degrees (rotational).
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatability error
     */
    void moveBy(double distance) throws IOException, DeviceException;

    default void moveByAndWait(double distance) throws IOException, DeviceException, InterruptedException {
        moveBy(distance);
        waitUntilStationary();
    }

    default void moveByAndWait(double distance, long timeout) throws IOException, DeviceException, InterruptedException, TimeoutException {
        moveBy(distance);
        waitUntilStationary(timeout);
    }

    /**
     * Returns this axis to its home position.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatability error
     */
    void moveToHome() throws IOException, DeviceException;

    default void moveToHomeAndWait() throws IOException, DeviceException, InterruptedException {
        moveToHome();
        waitUntilStationary();
    }

    default void moveToHomeAndWait(long timeout) throws IOException, DeviceException, InterruptedException, TimeoutException {
        moveToHome();
        waitUntilStationary(timeout);
    }

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
     * Returns whether this axis is locked or not. When locked, it shall not respond to movement commands.
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

    interface Linear extends Translator {

        /**
         * Set the absolute position of this translation axis.
         *
         * @param metres Position to move to, in metres.
         *
         * @throws IOException     Upon communications error
         * @throws DeviceException Upon device compatability error
         */
        @Override
        void setPosition(double metres) throws IOException, DeviceException;

        /**
         * Returns the minimum value of position for this translation axis.
         *
         * @return Minimum position, in metres.
         *
         * @throws IOException     Upon communications error
         * @throws DeviceException Upon device compatability error
         */
        @Override
        double getMinPosition() throws IOException, DeviceException;

        /**
         * Returns the maximum value of position for this translation axis.
         *
         * @return Maximum position, in metres.
         *
         * @throws IOException     Upon communications error
         * @throws DeviceException Upon device compatability error
         */
        @Override
        double getMaxPosition() throws IOException, DeviceException;

        /**
         * Return the absolute position of this translation axis.
         *
         * @return Current position, in metres.
         *
         * @throws IOException     Upon communications error
         * @throws DeviceException Upon device compatability error
         */
        @Override
        double getPosition() throws IOException, DeviceException;


        /**
         * Moves this translation axis by the specific amount.
         *
         * @param metres Relative distance, in metres (linear).
         *
         * @throws IOException     Upon communications error
         * @throws DeviceException Upon device compatability error
         */
        @Override
        void moveBy(double metres) throws IOException, DeviceException;

    }

    interface Rotational extends Translator {

        /**
         * Set the absolute position of this rotation axis.
         *
         * @param degrees Position to move to, in degrees.
         *
         * @throws IOException     Upon communications error
         * @throws DeviceException Upon device compatability error
         */
        @Override
        void setPosition(double degrees) throws IOException, DeviceException;

        /**
         * Returns the minimum value of position for this rotational axis.
         *
         * @return Minimum position, in degrees.
         *
         * @throws IOException     Upon communications error
         * @throws DeviceException Upon device compatability error
         */
        @Override
        double getMinPosition() throws IOException, DeviceException;

        /**
         * Returns the maximum value of position for this rotational axis.
         *
         * @return Maximum position, in degrees.
         *
         * @throws IOException     Upon communications error
         * @throws DeviceException Upon device compatability error
         */
        @Override
        double getMaxPosition() throws IOException, DeviceException;

        /**
         * Return the absolute position of this rotation axis.
         *
         * @return Current position, in degrees.
         *
         * @throws IOException     Upon communications error
         * @throws DeviceException Upon device compatability error
         */
        @Override
        double getPosition() throws IOException, DeviceException;

        /**
         * Moves this rotation axis by the specific amount.
         *
         * @param degrees Relative distance, in degrees.
         *
         * @throws IOException     Upon communications error
         * @throws DeviceException Upon device compatability error
         */
        @Override
        void moveBy(double degrees) throws IOException, DeviceException;

    }

}
