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
        this(camera, (i,f) -> listener.newFrame(f), closer);
    }

    public FrameThread(Camera<F> camera, Camera.Streamer<F> listener) {
        this(camera, listener, () -> { });
    }

    private void run() {

        while (queue.isAlive()) {

            if (Thread.interrupted()) {
                break;
            }

            try {

                F frame = queue.nextFrame();
                listener.newFrame(count, frame);
                count++;

            } catch (InterruptedException e) {
                break;
            } catch (Throwable e) {
                exceptions.add(e);
            }

        }

        Util.runRegardless(closer);

    }

    public boolean isRunning() {
        return thread.isAlive();
    }

    public synchronized void stop() {

        queue.close();

        try {
            thread.join();
        } catch (InterruptedException ignored) { }

    }

    public synchronized void forceStop() {

        thread.interrupt();
        queue.clear();
        queue.close();
        queue.clear();

        try {
            thread.join();
        } catch (InterruptedException ignored) { }

    }

    public synchronized long getFrameCount() {
        return count;
    }

    public boolean hasExceptions() {
        return !exceptions.isEmpty();
    }

    public List<Throwable> getExceptions() {
        return List.copyOf(exceptions);
    }


}
