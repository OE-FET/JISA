package jisa.devices.camera.frame;

import jisa.devices.camera.Camera;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class FrameQueue<F extends Frame> extends LinkedBlockingQueue<F> {

    private       boolean      open    = true;
    private final Camera<F>    camera;
    private final List<Thread> threads = Collections.synchronizedList(new ArrayList<>(1));

    public FrameQueue(Camera<F> camera) {
        this(camera, Integer.MAX_VALUE);
    }

    public FrameQueue(Camera<F> camera, int capacity) {
        super(capacity);
        this.camera = camera;
    }

    /**
     * Takes the frame at the head of the queue and returns it, removing it from the queue. If no frame is available, this method
     * will block (i.e., wait) until one is available to return. If no frame ever becomes available, this method will never return.
     *
     * @return The next frame in the queue.
     *
     * @throws InterruptedException Upon the thread being interrupted while waiting for a frame.
     */
    public F nextFrame() throws InterruptedException {

        threads.add(Thread.currentThread());
        F frame = take();
        threads.remove(Thread.currentThread());

        return frame;

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

        threads.add(Thread.currentThread());
        F frame = poll(timeout, TimeUnit.MILLISECONDS);
        threads.remove(Thread.currentThread());

        if (frame == null) {
            throw new TimeoutException("Timed out waiting for frame.");
        }

        return frame;

    }

    public void close() {

        if (open) {

            open = false;
            camera.closeFrameQueue(this);

            if (isEmpty()) {
                threads.forEach(Thread::interrupt);
            }

        }

    }

    public boolean isOpen() {
        return open;
    }

    public boolean hasFrames() {
        return !isEmpty();
    }

    public boolean isAlive() {
        return isOpen() || hasFrames();
    }

}
