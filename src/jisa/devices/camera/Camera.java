package jisa.devices.camera;

import jisa.devices.DeviceException;
import jisa.devices.Instrument;
import jisa.devices.ParameterList;
import jisa.devices.camera.frame.Frame;
import jisa.devices.camera.frame.FrameQueue;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * Standard interface for representing cameras.
 *
 * @param <F> The class used to represent each frame returned by this camera, must extend Frame.
 */
public interface Camera<F extends Frame> extends Instrument {

    /**
     * Returns the integration/exposure time being used by this camera.
     *
     * @return Integration/exposure time, in seconds.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    double getIntegrationTime() throws IOException, DeviceException;

    /**
     * Sets the integration/exposure time for this camera to use.
     *
     * @param time Integration/exposure time, in seconds.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    void setIntegrationTime(double time) throws IOException, DeviceException;

    /**
     * Acquires and returns a single frame from the camera.
     *
     * @return Single acquisition frame, represented as a Frame object
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    F getFrame() throws IOException, DeviceException, InterruptedException, TimeoutException;

    /**
     * Sets the maximum time to wait for acquisitions before giving up. A value of 0 indicates no timeout.
     *
     * @param timeout Timeout, in milliseconds
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    void setAcquisitionTimeOut(int timeout) throws IOException, DeviceException;

    /**
     * Returns the timeout currently being used for acquisitions. A value of 0 indicates no timeout.
     *
     * @return Timeout, in milliseconds.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    int getAcquisitionTimeOut() throws IOException, DeviceException;

    /**
     * Initiates continuous acquisition, causing the camera to continuously acquire frames until stopAcquisition() is called.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    void startAcquisition() throws IOException, DeviceException;

    /**
     * Stops continuous acquisition, if running.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    void stopAcquisition() throws IOException, DeviceException;

    /**
     * Returns whether the camera is currently continuously acquiring frames.
     *
     * @return Acquiring continuously?
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    boolean isAcquiring() throws IOException, DeviceException;

    /**
     * Acquires a series of frames from the camera, returning them all as a List of Frame objects.
     *
     * @param count Number of frames to acquire
     *
     * @return List of acquisitions, as Frame objects
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    List<F> getFrameSeries(int count) throws IOException, DeviceException, InterruptedException, TimeoutException;

    /**
     * Adds a listener to this camera which is called, synchronously, every time a new frame is acquired.
     * Any frames acquired while this listener is still running from a previous frame will be skipped.
     * The Frame object passed to this listener will be recycled each time, thus a copy() should be made
     * if you intend to store it.
     *
     * @param listener Listener to add
     *
     * @return Reference to added listener
     */
    Listener<F> addFrameListener(Listener<F> listener);

    /**
     * Removes the given frame listener from the camera, stopping it from being called each time a new frame is acquired.
     *
     * @param listener Listener to remove
     */
    void removeFrameListener(Listener<F> listener);

    /**
     * Opens a new (blocking) queue into which copies of each newly acquired frame will be placed.
     * This is to allow for asynchronous, lossless processing of frame data.
     *
     * @return Queue of frames
     */
    FrameQueue<F> openFrameQueue();

    /**
     * Closes the given queue, preventing the camera from adding any new frames to it.
     *
     * @param queue Queue to close
     */
    void closeFrameQueue(FrameQueue<F> queue);

    /**
     * Returns the width (in pixels) of images captured by this camera, for its current configuration.
     *
     * @return Image width, in pixels.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    int getFrameWidth() throws IOException, DeviceException;

    /**
     * Sets the width (in pixels) to use when taking images.
     *
     * @param width Width, in pixels.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    void setFrameWidth(int width) throws IOException, DeviceException;

    /**
     * Returns number of physical pixel columns (i.e., before binning) used on the sensor to capture each frame.
     *
     * @return Width, in physical pixel columns.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    int getPhysicalFrameWidth() throws IOException, DeviceException;

    /**
     * Returns the height (in pixels) of images captured by this camera, for its current configuration.
     *
     * @return Image height, in pixels.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    int getFrameHeight() throws IOException, DeviceException;

    /**
     * Sets the height (in pixels) to use when taking images.
     *
     * @param height Height, in pixels.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    void setFrameHeight(int height) throws IOException, DeviceException;

    /**
     * Returns number of physical pixel rows (i.e., before binning) used on the sensor to capture each frame.
     *
     * @return Height, in physical pixel rows.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    int getPhysicalFrameHeight() throws IOException, DeviceException;

    /**
     * Returns which physical pixel column on the sensor is the left-most column used for acquiring frames.
     *
     * @return X-Offset of image, in physical pixels.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    int getFrameOffsetX() throws IOException, DeviceException;

    /**
     * Sets which physical pixel column on the sensor is the left-most column used for acquiring frames.
     *
     * @param offsetX X-Offset of image, in physical pixels.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    void setFrameOffsetX(int offsetX) throws IOException, DeviceException;

    /**
     * Sets whether the offset set using setFrameOffsetX() is ignored and the frame instead automatically
     * centred in x on the sensor.
     *
     * @param centredX Centre in the x direction?
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    void setFrameCentredX(boolean centredX) throws IOException, DeviceException;

    /**
     * Returns whether the offset set using setFrameOffsetX() is ignored and the frame instead automatically
     * centred in x on the sensor.
     *
     * @return Centre in the x direction?
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    boolean isFrameCentredX() throws IOException, DeviceException;

    /**
     * Returns the y co-ordinate of the top-most pixel used for capturing images.
     *
     * @return Y-Offset of image, in pixels.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    int getFrameOffsetY() throws IOException, DeviceException;

    /**
     * Sets the y co-ordinate of the top-most pixel used for capturing images.
     *
     * @param offsetY Y-Offset for images, in pixels.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    void setFrameOffsetY(int offsetY) throws IOException, DeviceException;

    /**
     * Sets whether the offset set using setFrameOffsetY() is ignored and the frame instead automatically
     * centred in y on the sensor.
     *
     * @param centredY Centre in the x direction?
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    void setFrameCentredY(boolean centredY) throws IOException, DeviceException;

    /**
     * Returns whether the offset set using setFrameOffsetY() is ignored and the frame instead automatically
     * centred in y on the sensor.
     *
     * @return Centre in the y direction?
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    boolean isFrameCentredY() throws IOException, DeviceException;

    /**
     * Returns the total number of pixels in images captured by this camera, for its current configuration.
     *
     * @return Total number of pixels.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    int getFrameSize() throws IOException, DeviceException;

    /**
     * Returns the total number of physical pixels (i.e., before binning) used to capture each frame.
     *
     * @return Total number of physical pixels per frame.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    int getPhysicalFrameSize() throws IOException, DeviceException;

    /**
     * Returns the total number of physical pixel columns on the sensor.
     *
     * @return Number of physical pixel columns.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    int getSensorWidth() throws IOException, DeviceException;

    /**
     * Returns the total number of physical pixel rows on the sensor.
     *
     * @return Number of physical pixel rows.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    int getSensorHeight() throws IOException, DeviceException;

    /**
     * Returns how many "real" pixels are being summed in the x direction per returned pixel.
     *
     * @return Number of "real" pixels binned in x direction per returned pixel.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    int getBinningX() throws IOException, DeviceException;

    /**
     * Sets how many "real" pixels are being summed in the x direction per returned pixel.
     *
     * @param x Number of "real" pixels binned in x direction per returned pixel.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    void setBinningX(int x) throws IOException, DeviceException;

    /**
     * Returns how many "real" pixels are being summed in the y direction per returned pixel.
     *
     * @return Number of "real" pixels binned in y direction per returned pixel.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    int getBinningY() throws IOException, DeviceException;

    /**
     * Sets how many "real" pixels are being summed in the y direction per returned pixel.
     *
     * @param y Number of "real" pixels binned in y direction per returned pixel.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    void setBinningY(int y) throws IOException, DeviceException;

    /**
     * Sets both the x and y binning simultaneously. Has the same end result as calling setBinningX(x) and setBinningY(y)
     * sequentially.
     *
     * @param x Binning in x
     * @param y Binning in y
     *
     * @throws IOException
     * @throws DeviceException
     */
    void setBinning(int x, int y) throws IOException, DeviceException;

    static void addParameters(Camera inst, Class<?> target, ParameterList parameters) {

        parameters.addValue("Integration Time [s]", inst::getIntegrationTime, 20e-3, inst::setIntegrationTime);
        parameters.addValue("X Binning", inst::getBinningX, 1, inst::setBinningX);
        parameters.addValue("Y Binning", inst::getBinningY, 1, inst::setBinningY);
        parameters.addValue("Frame Width", inst::getFrameWidth, 1024, inst::setFrameWidth);
        parameters.addValue("Frame Height", inst::getFrameHeight, 1024, inst::setFrameHeight);

        try {

            parameters.addAuto("Frame X Offset", inst.isFrameCentredX(), inst.getFrameOffsetX(), offset -> inst.setFrameCentredX(true), offset -> {
                inst.setFrameCentredX(false);
                inst.setFrameOffsetX(offset);
            });

        } catch (Throwable e) {

            parameters.addAuto("Frame X Offset", true, 0, offset -> inst.setFrameCentredX(true), offset -> {
                inst.setFrameCentredX(false);
                inst.setFrameOffsetX(offset);
            });

        }

        try {

            parameters.addAuto("Frame Y Offset", inst.isFrameCentredY(), inst.getFrameOffsetY(), offset -> inst.setFrameCentredY(true), offset -> {
                inst.setFrameCentredY(false);
                inst.setFrameOffsetY(offset);
            });

        } catch (Throwable e) {

            parameters.addAuto("Frame Y Offset", true, 0, offset -> inst.setFrameCentredY(true), offset -> {
                inst.setFrameCentredY(false);
                inst.setFrameOffsetY(offset);
            });

        }

    }

    interface Listener<F extends Frame> {
        void newFrame(F frame);
    }

}
