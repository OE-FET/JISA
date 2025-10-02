package jisa.devices.camera;

import jisa.devices.DeviceException;
import jisa.devices.camera.frame.Frame;
import jisa.devices.camera.frame.FrameQueue;
import jisa.visa.NativeDevice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

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
    protected abstract F createEmptyFrame();

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

    @Override
    public synchronized void startAcquisition() throws IOException, DeviceException {

        if (isAcquiring()) {
            return;
        }

        setupAcquisition(0);

        acquisitionThread = new Thread(() -> {

            final F frameBuffer = createEmptyFrame();

            count         = 0;
            lastCount     = 0;
            lastTimestamp = System.nanoTime();

            while (acquiring) {

                try {
                    acquisitionLoop(frameBuffer);
                } catch (IOException | DeviceException e) {
                    System.err.println(e.getMessage());
                } catch (InterruptedException | TimeoutException e) {
                    continue;
                }

                manager.trigger(frameBuffer);
                count++;

            }

        });

        acquiring = true;
        acquisitionThread.start();

    }

    @Override
    public synchronized void stopAcquisition() throws IOException, DeviceException {

        if (!isAcquiring()) {
            return;
        }

        acquiring = false;
        acquisitionThread.interrupt();

        try {
            acquisitionThread.join();
        } catch (InterruptedException e) {
            System.err.println("Interrupted waiting for acquisition thread to end gracefully.");
        }

        count         = 0;
        lastCount     = 0;
        lastTimestamp = 0;

    }

    @Override
    public double getAcquisitionFPS() throws IOException, DeviceException {

        if (isAcquiring()) {

            long   time = System.nanoTime();
            long   cnt  = count;
            double rate = (cnt - lastCount) / (time - lastTimestamp);

            lastCount     = cnt;
            lastTimestamp = time;

            return rate;

        } else {
            return 0.0;
        }

    }

    @Override
    public synchronized boolean isAcquiring() throws IOException, DeviceException {
        return acquiring;
    }

    @Override
    public List<F> getFrameSeries(int count) throws IOException, DeviceException, InterruptedException, TimeoutException {

        List<F> frames  = new ArrayList<>(count);
        int     timeout = getAcquisitionTimeout();

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

        setupAcquisition(count);

        try {

            for (int i = 0; i < count; i++) {
                F frame = createEmptyFrame();
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

        if (isAcquiring()) {

            FrameQueue<F> queue = openFrameQueue(1);

            try {
                return queue.nextFrame(getAcquisitionTimeout());
            } finally {
                queue.close();
                queue.clear();
            }

        }

        F frame = createEmptyFrame();

        try {
            setupAcquisition(1);
            acquisitionLoop(frame);
        } finally {
            cleanupAcquisition();
        }

        return frame;

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
