package jisa.devices.camera;

import jisa.devices.DeviceException;
import jisa.devices.camera.frame.Frame;
import jisa.devices.camera.frame.FrameQueue;
import jisa.visa.NativeDevice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * Abstract class for NativeDevice cameras (i.e., those that need to use a system library/so/dylib/dll) that provides
 * a standard framework for handling frame acquisition and queue/listener management.
 *
 * @param <F> The Frame class returned by this camera
 */
public abstract class ManagedCamera<F extends Frame<?, F>> extends NativeDevice implements Camera<F> {

    protected final ListenerManager<F> manager = new ListenerManager<>();

    protected boolean acquiring         = false;
    protected Thread  acquisitionThread = null;
    protected long    count             = 0;
    protected long    lastCount         = 0;
    protected long    lastTimestamp     = 0;

    public ManagedCamera(String name) {
        super(name);
    }

    /**
     * This method is called to initiate acqisition, with the limit argument indicating whether a finite number of
     * frames are to be taken or if continuous acquisition is to occur (limit = 0).
     *
     * @param limit The number of frames to be acquired (0 means unlimited).
     *
     * @throws IOException
     * @throws DeviceException
     */
    protected abstract void setupAcquisition(int limit) throws IOException, DeviceException;

    /**
     * This method should return a frame object to be used as the framebuffer for the camera --- temporarily holding
     * the data of each latest frame as it comes in.
     *
     * @return Created frame object
     */
    protected abstract F createFrameBuffer();

    /**
     * This is the method within the acquisition loop. It should retrieve one frame from the camera, do whatever
     * processing is necessary, then write the data into the provided Frame object ("frameBuffer"). Each time this method
     * completes, the frameBuffer object is offered to the internal listener and queue manager, offering the frame to all
     * attached frame listeners and queues.
     *
     * @param frameBuffer The frame buffer to dump frame data into.
     *
     * @throws IOException          Upon communications error.
     * @throws DeviceException      Upon device/compatibility error.
     * @throws InterruptedException Upon the current thread being interruped while awaiting a new frame.
     * @throws TimeoutException     Upon the wait for a new frame timing out.
     */
    protected abstract void acquisitionLoop(F frameBuffer) throws IOException, DeviceException, InterruptedException, TimeoutException;

    /**
     * This method is always called after acquisition has concluded.
     *
     * @throws IOException     Upon communications error.
     * @throws DeviceException Upon device/compatibility error.
     */
    protected abstract void cleanupAcquisition() throws IOException, DeviceException;

    /**
     * This method is called alongside interrupting the acquisition thread when stopAcquisition() is called. This method
     * should call whatever cancel/interrupt method the camera API itself provides (i.e., whatever method will cause a
     * "wait for frame" method to return early).
     * <p>
     * If no such method exists, leave this blank.
     * <p>
     * If it doesn't work, it should just ignore it, so that the built-in contingencies of the stopAcquisition() method
     * can be employed.
     */
    protected abstract void cancelAcquisition();

    @Override
    public synchronized void startAcquisition() throws IOException, DeviceException {

        // If we're already running, don't spawn another thread!
        if (isAcquiring()) {
            return;
        }

        // Set the camera up for unlimited acquisition
        setupAcquisition(0);

        // Create a thread that will continously acquire until we set acquiring = false
        acquisitionThread = new Thread(() -> {

            final F frameBuffer = createFrameBuffer();

            count         = 0;
            lastCount     = 0;
            lastTimestamp = System.nanoTime();

            // Keep going until acquiring = false
            while (acquiring) {

                try {
                    acquisitionLoop(frameBuffer);
                } catch (IOException | DeviceException e) {
                    System.err.println(e.getMessage());
                    continue;
                } catch (InterruptedException | TimeoutException e) {
                    continue;
                }

                // Every time we get a new frame, send it to the listener manager to distribute
                // amongst all FrameQueues and FrameListeners and handle the threading thereof
                manager.trigger(frameBuffer);

                // Another full loop, another frame
                count++;

            }

        });

        // Set the acquiring flag to true and launch the thread
        acquiring = true;
        acquisitionThread.start();

    }

    @Override
    public synchronized void stopAcquisition() throws IOException, DeviceException {

        // If we're already stopped, then ignore this call
        if (!isAcquiring()) {
            return;
        }

        // Set loop flag to false, so it should break at the next iteration
        acquiring = false;

        try {

            // In case we are waiting on anything preventing the loop from progressing
            acquisitionThread.interrupt();
            cancelAcquisition();

            // Cleanup after acquisition has finished (i.e., freeing memory, disabling streaming etc)
            cleanupAcquisition();

            // Await for the acquisition thread to die gracefully (one can only hope for such a death)
            acquisitionThread.join(10000);

        } catch (InterruptedException e) {

            System.err.println("Interrupted while waiting for acquisition thread to end gracefully.");
            acquisitionThread.stop(); // If it failed to exit gracefully within 10 seconds, commit a war crime

        } catch (Throwable e) {

            // If all else fails, commit more war crimes, but then complain about it
            acquisitionThread.stop();
            throw e;

        } finally {

            // Reset the FPS counters
            count         = 0;
            lastCount     = 0;
            lastTimestamp = 0;

        }

    }

    @Override
    public synchronized double getAcquisitionFPS() throws IOException, DeviceException {

        if (isAcquiring()) {

            long   time = System.nanoTime();
            long   cnt  = count;
            double rate = (cnt - lastCount) / (time - lastTimestamp);

            lastCount     = cnt;
            lastTimestamp = time;

            return rate;

        } else {

            // If we're not continously acquiring, then the framerate is 0, no point calculating it.
            return 0.0;

        }

    }

    @Override
    public synchronized boolean isAcquiring() throws IOException, DeviceException {
        return acquiring;
    }

    @Override
    public List<F> getFrameSeries(int count) throws IOException, DeviceException, InterruptedException, TimeoutException {

        // Create list to hold our frame series
        List<F> frames  = new ArrayList<>(count);
        int     timeout = getAcquisitionTimeout();

        // If we're already continuously acquiring, open a frame queue and wait for the next n frames that would
        // have been acquired anyway
        if (isAcquiring()) {

            FrameQueue<F> queue = openFrameQueue(count);

            try {

                for (int i = 0; i < count; i++) {
                    frames.add(queue.nextFrame(timeout));
                }

            } finally {
                queue.close();
                queue.clear();
            }

        }


        // Otherwise, run the acquisition loop n times to get the frames
        try {

            setupAcquisition(count);

            for (int i = 0; i < count; i++) {
                F frame = createFrameBuffer();
                acquisitionLoop(frame);
                frames.add(frame);
            }

        } finally {
            cleanupAcquisition();
        }

        return frames;

    }

    @Override
    public F getFrame() throws IOException, DeviceException, InterruptedException, TimeoutException {

        // If we are already continuously acquiring, open a frame queue and wait for the next frame to come in
        // (i.e., we'll give a copy of the next frame that would have been acquired anyway)
        if (isAcquiring()) {

            FrameQueue<F> queue = openFrameQueue(1);

            try {
                return queue.nextFrame(getAcquisitionTimeout());
            } finally {
                queue.close();
                queue.clear();
            }

        }

        // Otherwise, run the acquisition loop once to acquire a frame
        try {

            setupAcquisition(1);
            F frame = createFrameBuffer();
            acquisitionLoop(frame);

            return frame;

        } finally {
            cleanupAcquisition();
        }

    }

    @Override
    public synchronized Listener<F> addFrameListener(Listener<F> listener) {
        manager.addListener(listener);
        return listener;
    }

    @Override
    public synchronized void removeFrameListener(Listener<F> listener) {
        manager.removeListener(listener);
    }

    @Override
    public synchronized FrameQueue<F> openFrameQueue(int capacity) {
        FrameQueue<F> queue = new FrameQueue<>(this, capacity);
        manager.addQueue(queue);
        return queue;
    }

    @Override
    public synchronized void closeFrameQueue(FrameQueue<F> queue) {
        manager.removeQueue(queue);
    }

}
