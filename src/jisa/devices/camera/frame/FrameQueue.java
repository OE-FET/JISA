package jisa.devices.camera.frame;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class FrameQueue<F extends Frame> extends LinkedBlockingQueue<F> {

    private boolean open = true;

    /**
     * Takes the frame at the head of the queue and returns it, removing it from the queue. If no frame is available, this method
     * will block (i.e., wait) until one is available to return. If no frame ever becomes available, this method will never return.
     *
     * @return The next frame in the queue.
     *
     * @throws InterruptedException Upon the thread being interrupted while waiting for a frame.
     */
    public F nextFrame() throws InterruptedException {
        return take();
    }

    /**
     * Takes the frame at the head of the queue and returns it, removing it from the queue. If no frame is available, this method
     * will block (i.e., wait) until one is available to return, or the specified timeout expires.
     *
     * @param timeout Max time to wait, in milliseconds.
     *
     * @return The next frame in the queue.
     *
     * @throws InterruptedException Upon the thread being interrupted while waiting.
     * @throws TimeoutException     Upon timing out before a frame becomes available.
     */
    public F nextFrame(long timeout) throws InterruptedException, TimeoutException {

        F frame = poll(timeout, TimeUnit.MILLISECONDS);

        if (frame == null) {
            throw new TimeoutException("Timed out waiting for frame.");
        }

        return frame;

    }

    public void close() {
        open = false;
    }

    public boolean isOpen() {
        return open;
    }

}
