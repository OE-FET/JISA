package jisa.devices.camera.frame;

import jisa.Util;
import jisa.control.SRunnable;
import jisa.devices.camera.Camera;

import java.util.LinkedList;
import java.util.List;

public class FrameThread<F extends Frame> {

    private final Camera<F>               camera;
    private final Camera.CountStreamer<F> listener;
    private final FrameQueue<F>           queue;
    private final Thread                  thread;
    private final List<Throwable>         exceptions;
    private final SRunnable               closer;
    private       long                    count = 0;

    public FrameThread(Camera<F> camera, Camera.CountStreamer<F> listener, SRunnable closer) {

        this.camera     = camera;
        this.listener   = listener;
        this.queue      = this.camera.openFrameQueue();
        this.thread     = new Thread(this::run);
        this.exceptions = new LinkedList<>();
        this.closer     = closer;

        this.thread.start();

    }

    public FrameThread(Camera<F> camera, Camera.CountStreamer<F> listener) {
        this(camera, listener, () -> { });
    }

    public FrameThread(Camera<F> camera, Camera.Streamer<F> listener, SRunnable closer) {
        this(camera, (i, f) -> listener.newFrame(f), closer);
    }

    public FrameThread(Camera<F> camera, Camera.Streamer<F> listener) {
        this(camera, listener, () -> { });
    }

    private void run() {

        while (queue.isAlive()) {

            try {

                F frame = queue.nextFrame();
                listener.newFrame(count, frame);
                count++;

            } catch (InterruptedException e) {
                continue;
            } catch (Throwable e) {
                exceptions.add(e);
            }

        }

        Util.runRegardless(closer);

    }

    public boolean isRunning() {
        return thread.isAlive();
    }

    /**
     * Closes the queue and waits for the thread to finish processing any frames left in the queue (i.e., any backlog), up until the specified timeout, before shutting
     * down and returning.
     *
     * @param timeout The maximum amount of time to wait for the thread to finish gracefully before timing out (in milliseconds).
     */
    public synchronized void stop(int timeout) {

        queue.close();

        try {
            thread.join(timeout);
        } catch (InterruptedException ignored) { }

    }

    /**
     * Closes the queue and waits for the thread to finish processing any frames left in the queue (i.e., any backlog) before shutting
     * down and returning.
     */
    public synchronized void stop() {
        stop(0);
    }

    /**
     * Attempts to make the thread shut down now --- clearing out any remaining backlog of frames without processing them.
     * If it is not able to make this happen gracefully within 10 seconds, it will forcibly shut down the thread.
     */
    public synchronized void stopNow() {

        // Close and clear out whatever's left
        queue.close();
        queue.clear();

        // If the thread is waiting on something, make it give up
        thread.interrupt();

        try {
            thread.join(10000); // Give it 10 seconds to shutdown gracefully
        } catch (InterruptedException ignored) {
            thread.stop();      // The time for grace is over
        }

    }

    /**
     * Returns the number of frames that have been processed by the thread so far.
     *
     * @return Number of frames processed so far.
     */
    public synchronized long getFrameCount() {
        return count;
    }

    public boolean hasExceptions() {
        return !exceptions.isEmpty();
    }

    public List<Throwable> getExceptions() {
        return List.copyOf(exceptions);
    }

    public FrameQueue<F> getFrameQueue() {
        return queue;
    }


}
