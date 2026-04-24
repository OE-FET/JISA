package jisa.devices.camera;

import jisa.devices.DeviceException;
import jisa.devices.Instrument;
import jisa.devices.ParameterList;
import jisa.devices.camera.frame.Frame;
import jisa.devices.camera.frame.FrameQueue;
import jisa.devices.camera.frame.FrameThread;
import jisa.devices.camera.imagemodes.*;
import jisa.gui.FrameAcceptor;
import jisa.gui.HeatMap;
import jisa.gui.ImageDisplay;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * Standard interface for representing cameras.
 *
 * @param <F> The class used to represent each frame returned by this camera, must extend Frame.
 */
public interface Camera<F extends Frame> extends Instrument, FullImage, ROI {

    String IMAGE_STREAM_HEADER = "JISA IMAGE STREAM: width (int, 4 bytes), height (int, 4 bytes), bytes per pixel (int, 4 bytes), timestamp (long, 8 bytes), image data (byte array, w*h*bpp bytes)";

    static void addParameters(Camera<?> inst, Class<?> target, ParameterList parameters) {

        parameters.addChoice("Image Mode", inst::getImageMode, ImageMode.FULL_IMAGE, inst::setImageMode, inst.getImageModes().toArray(ImageMode[]::new));

        parameters.addValue("Integration Time [s]", inst::getIntegrationTime, 20e-3, inst::setIntegrationTime);
        parameters.addValue("Acquisition Timeout [ms]", inst::getAcquisitionTimeout, 1000, inst::setAcquisitionTimeout);

        parameters.addValue("Binning", "X Binning", inst::getBinningX, 1, inst::setBinningX);
        parameters.addValue("Binning", "Y Binning", inst::getBinningY, 1, inst::setBinningY);

    }

    @Override
    default boolean beforeApplyParameters() {

        try {

            if (isAcquiring()) {
                stopAcquisition();
                return true;
            } else {
                return false;
            }

        } catch (Throwable e) {
            return false;
        }

    }

    default void afterApplyParameters(boolean result) {

        try {

            if (result) {
                startAcquisition();
            }

        } catch (Throwable ignored) {
        }

    }

    /**
     * Returns the integration/exposure time being used by this camera.
     *
     * @return Integration/exposure time, in seconds.
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    double getIntegrationTime() throws IOException, DeviceException;

    /**
     * Sets the integration/exposure time for this camera to use.
     *
     * @param time Integration/exposure time, in seconds.
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    void setIntegrationTime(double time) throws IOException, DeviceException;

    /**
     * Acquires and returns a single frame from the camera.
     *
     * @return Single acquisition frame, represented as a Frame object
     * @throws IOException          Upon communications error
     * @throws DeviceException      Upon device compatibility error
     * @throws InterruptedException If the thread is interrupted while waiting for frame from camera
     * @throws TimeoutException     If the acquisition timeout expires before the camera returns a frame
     */
    F getFrame() throws IOException, DeviceException, InterruptedException, TimeoutException;

    /**
     * Sets the maximum time to wait for acquisitions before giving up. A value of 0 indicates no timeout.
     *
     * @param timeout Timeout, in millisefconds
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    void setAcquisitionTimeout(int timeout) throws IOException, DeviceException;

    /**
     * Returns the timeout currently being used for acquisitions. A value of 0 indicates no timeout.
     *
     * @return Timeout, in milliseconds.
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    int getAcquisitionTimeout() throws IOException, DeviceException;

    /**
     * Returns the current rate at which the camera is acquiring frames (if it is continuously acquiring, zero otherwise).
     *
     * @return Acquisition frames per second.
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    double getAcquisitionRate() throws IOException, DeviceException;

    /**
     * Returns the current rate at which the camera is processing frames (if it is continuously acquiring, zero otherwise).
     * Unless this has been overridden, this will just return the same value as getAcquisitionFPS() by default.
     *
     * @return Processing frames per second.
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    default double getProcessingRate() throws IOException, DeviceException {
        return getAcquisitionRate();
    }

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
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    boolean isAcquiring() throws IOException, DeviceException;

    /**
     * Attaches a listener to the camera that is called anytime it starts or stops continuous acquisition.
     * The listener should accept two arguments: an integer representing the number of acquisitions to be taken
     * or that were taken during the acquisition (0 means continuous), and a boolean representing whether the
     * acquisition has started or ended.
     *
     * @param listener The listener to attach.
     * @return Reference to the listener that was attached.
     */
    AcquisitionListener addAcquisitionListener(AcquisitionListener listener);

    /**
     * Removes a listener that is currently attached to the camera, preventing it from being called anymore.
     *
     * @param listener The listener to remove.
     */
    void removeAcquisitionListener(AcquisitionListener listener);

    /**
     * Acquires a series of frames from the camera, returning them all as a List of Frame objects.
     *
     * @param count Number of frames to acquire
     * @return List of acquisitions, as Frame objects
     * @throws IOException          Upon communications error
     * @throws DeviceException      Upon device compatibility error
     * @throws InterruptedException If acquisition is interrupted
     * @throws TimeoutException     If the timeout is exceeded while waiting for a frame
     */
    List<F> getFrameSeries(int count) throws IOException, DeviceException, InterruptedException, TimeoutException;

    /**
     * Adds a listener to this camera which is called every time a new frame is acquired.
     * Any frames acquired while this listener is still running from a previous frame will be skipped.
     * The Frame object passed to this listener will be recycled each time, thus a copy() should be made
     * if you intend to store it.
     *
     * @param listener Listener to add
     * @return Reference to added listener
     */
    Listener<F> addFrameListener(Listener<F> listener);

    /**
     * Removes the given frame listener from the camera, stopping it from being called each time a new frame is acquired.
     *
     * @param listener Listener to remove
     */
    void removeFrameListener(Listener listener);

    /**
     * Opens a new (blocking) queue into which copies of each newly acquired frame will be placed.
     * This is to allow for asynchronous, lossless processing of frame data.
     *
     * @param capacity The maximum capacity of the queue (beyond which frames will be rejected).
     * @return Queue of frames
     */
    FrameQueue<F> openFrameQueue(int capacity);

    /**
     * Opens a new (blocking) queue into which copies of each newly acquired frame will be placed.
     * This is to allow for asynchronous, lossless processing of frame data.
     *
     * @return Queue of frames
     */
    default FrameQueue<F> openFrameQueue() {
        return openFrameQueue(Integer.MAX_VALUE);
    }

    /**
     * Closes the given queue, preventing the camera from adding any new frames to it.
     *
     * @param queue Queue to close
     */
    void closeFrameQueue(FrameQueue<F> queue);

    /**
     * Opens a new frame queue and launches a new thread to handle it, giving each item in the queue to the
     * provided lambda sequentially. In effect, this is like adding a frame listener, except it is lossless.
     *
     * @param listener Action to perform with each frame that comes through the queue.
     * @return FrameThread object representing the new thread and queue.
     */
    default FrameThread<F> startFrameThread(CountStreamer<F> listener) {
        return new FrameThread<>(this, listener);
    }

    /**
     * Opens a new frame queue and launches a new thread to handle it, giving each item in the queue to the
     * provided lambda sequentially. In effect, this is like adding a frame listener, except it is lossless.
     *
     * @param listener Action to perform with each frame that comes through the queue.
     * @return FrameThread object representing the new thread and queue.
     */
    default FrameThread<F> startFrameThread(Streamer<F> listener) {
        return new FrameThread<>(this, listener);
    }

    /**
     * Stream binary frame data losslessly to the given output stream.
     *
     * @param stream The output stream to stream to.
     * @return FrameThread object representing the worker thread running the stream.
     */
    default FrameThread<F> stream(DataOutputStream stream) {
        return startFrameThread(f -> f.writeToStream(stream));
    }

    /**
     * Stream binary frame data losslessly to the given file.
     *
     * @param path The file to stream to.
     * @return FrameThread object representing the worker thread running the stream.
     */
    default FrameThread<F> streamToFile(String path) throws IOException {

        DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(path)));
        dos.write(IMAGE_STREAM_HEADER.getBytes(StandardCharsets.US_ASCII));

        return new FrameThread<>(this, f -> f.writeToStream(dos), dos::close);

    }

    /**
     * Returns the width (in pixels) of images captured by this camera, for its current configuration.
     *
     * @return Image width, in pixels.
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    int getFrameWidth() throws IOException, DeviceException;

    /**
     * Returns number of physical pixel columns (i.e., before binning) used on the sensor to capture each frame.
     *
     * @return Width, in physical pixel columns.
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    int getPhysicalFrameWidth() throws IOException, DeviceException;

    /**
     * Returns the height (in pixels) of images captured by this camera, for its current configuration.
     *
     * @return Image height, in pixels.
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    int getFrameHeight() throws IOException, DeviceException;


    /**
     * Returns number of physical pixel rows (i.e., before binning) used on the sensor to capture each frame.
     *
     * @return Height, in physical pixel rows.
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    int getPhysicalFrameHeight() throws IOException, DeviceException;


    /**
     * Returns the total number of pixels in images captured by this camera, for its current configuration.
     *
     * @return Total number of pixels.
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    int getFrameSize() throws IOException, DeviceException;

    /**
     * Returns the total number of physical pixels (i.e., before binning) used to capture each frame.
     *
     * @return Total number of physical pixels per frame.
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    int getPhysicalFrameSize() throws IOException, DeviceException;

    /**
     * Returns the total number of physical pixel columns on the sensor.
     *
     * @return Number of physical pixel columns.
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    int getSensorWidth() throws IOException, DeviceException;

    /**
     * Returns the total number of physical pixel rows on the sensor.
     *
     * @return Number of physical pixel rows.
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    int getSensorHeight() throws IOException, DeviceException;

    /**
     * Returns how many "real" pixels are being summed in the x direction per returned pixel.
     *
     * @return Number of "real" pixels binned in x direction per returned pixel.
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    int getBinningX() throws IOException, DeviceException;

    /**
     * Sets how many "real" pixels are being summed in the x direction per returned pixel.
     *
     * @param x Number of "real" pixels binned in x direction per returned pixel.
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    void setBinningX(int x) throws IOException, DeviceException;

    /**
     * Returns how many "real" pixels are being summed in the y direction per returned pixel.
     *
     * @return Number of "real" pixels binned in y direction per returned pixel.
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    int getBinningY() throws IOException, DeviceException;

    /**
     * Sets how many "real" pixels are being summed in the y direction per returned pixel.
     *
     * @param y Number of "real" pixels binned in y direction per returned pixel.
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
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    void setBinning(int x, int y) throws IOException, DeviceException;

    default List<ImageMode> getImageModes() {

        Class<? extends Camera> thisClass = this.getClass();

        return Arrays.stream(ImageMode.values())
                .filter(im -> im.getInterface().isAssignableFrom(thisClass))
                .collect(Collectors.toList());

    }

    ImageMode getImageMode() throws IOException, DeviceException;

    void setImageMode(ImageMode mode) throws IOException, DeviceException;

    default Listener<F> sendFramesTo(FrameAcceptor drawer) {
        return addFrameListener(drawer::acceptFrame);
    }

    default Listener<F> sendFramesTo(ImageDisplay drawer) {
        return addFrameListener(drawer::drawFrame);
    }

    default Listener<F> sendFramesTo(HeatMap drawer) {
        return addFrameListener(drawer::drawFrame);
    }

    interface Listener<F extends Frame> {
        void newFrame(F frame);
    }

    interface Streamer<F extends Frame> {
        void newFrame(F frame) throws Exception;
    }

    interface CountStreamer<F extends Frame> {
        void newFrame(long count, F frame) throws Exception;
    }

    interface AcquisitionListener {
        void changed(int count, boolean acquiring);
    }

    enum ImageMode {

        FULL_IMAGE("Full Image", FullImage.class),
        ROI("Region of Interest", ROI.class),
        FULL_VERTICAL_BINNING("Full Vertical Binning", FullVerticalBinning.class),
        SINGLE_TRACK("Single-Track", SingleTrack.class),
        TRACK_SEQUENCE("Track Sequence", TrackSequence.class),
        MULTI_TRACK("Multi-Track", MultiTrack.class);

        private final static Map<Class<?>, ImageMode> map = new LinkedHashMap<>();

        static {

            for (ImageMode value : values()) {
                map.put(value.getInterface(), value);
            }

        }

        public static ImageMode lookup(Class<?> itfc) {
            return map.getOrDefault(itfc, null);
        }

        private final String                           name;
        private final Class<? extends CameraImageMode> mode;

        private ImageMode(String name, Class<? extends CameraImageMode> mode) {
            this.name = name;
            this.mode = mode;
        }

        public String getName() {
            return name;
        }

        public String toString() {
            return getName();
        }

        public Class<? extends CameraImageMode> getInterface() {
            return mode;
        }

    }

}
