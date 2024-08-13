package jisa.devices.camera.frame;

import jisa.devices.DeviceException;

import java.io.IOException;

public interface LiveFrame<D, F extends Frame<D>> extends Frame<D> {

    /**
     * Adds a listener to this live frame which gets called every time a new frame is acquired.
     *
     * @param listener Listener to add.
     *
     * @return Reference to the added listener.
     */
    Listener<F> addListener(Listener<F> listener);

    /**
     * Removes the given listener from this live frame, so that it is no-longer called when new frames are acquired.
     *
     * @param listener The listener to remove.
     */
    void removeListener(Listener<F> listener);

    /**
     * Starts continuous acquisition from the camera to this live frame.
     *
     * @throws DeviceException Upon device compatibility error.
     * @throws IOException     Upon communications error.
     */
    void start() throws DeviceException, IOException;

    /**
     * Starts continuous acquisition from the camera to this live frame.
     *
     * @throws DeviceException Upon device compatibility error.
     * @throws IOException     Upon communications error.
     */
    void stop() throws DeviceException, IOException;

    /**
     * Takes a single acquisition and updates this live frame with it.
     *
     * @throws DeviceException Upon device compatibility error.
     * @throws IOException     Upon communications error.
     */
    void step() throws DeviceException, IOException;

    /**
     * Returns the timeout currently set to be used when acquiring frames for this live frame.
     *
     * @return Timeout, in milliseconds.
     */
    int getTimeout();

    /**
     * Sets the timeout to use when acquiring frames from this live frame.
     *
     * @param timeout Timeout to use, in milliseconds.
     */
    void setTimeout(int timeout);

    interface Listener<F extends Frame<?>> {
        void update(F newFrame);
    }

}
