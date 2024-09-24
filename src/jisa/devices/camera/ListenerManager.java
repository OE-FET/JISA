package jisa.devices.camera;

import jisa.devices.camera.frame.Frame;
import jisa.devices.camera.frame.FrameQueue;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class ListenerManager<F extends Frame<?, F>> {

    private final List<Runner<F>>     listeners = new LinkedList<>();
    private final List<FrameQueue<F>> queues    = new LinkedList<>();
    private final Executor            executor  = Executors.newCachedThreadPool();

    public void addListener(Camera.Listener<F> listener) {

        synchronized (listeners) {
            listeners.add(new Runner<>(listener));
        }

    }

    public void removeListener(Camera.Listener<F> listener) {

        synchronized (listeners) {
            listeners.stream().filter(f -> f.listener == listener).findAny().ifPresent(listeners::remove);
        }

    }

    public void addQueue(FrameQueue<F> queue) {

        synchronized (queues) {
            queues.add(queue);
        }

    }

    public void removeQueue(FrameQueue<F> queue) {

        synchronized (queues) {
            queues.remove(queue);
        }

    }

    public void trigger(F frame) {

        synchronized (queues) {

            for (FrameQueue<F> queue : queues) {
                queue.offer(frame.copy());
            }

        }

        synchronized (listeners) {

            for (Runner<F> runner : listeners) {
                executor.execute(() -> runner.submitFrame(frame));
            }

        }

    }

    public static class Runner<F extends Frame<?, F>> {

        private final Camera.Listener<F> listener;
        private final Semaphore          semaphore = new Semaphore(1);

        private F buffer = null;

        public Runner(Camera.Listener<F> listener) {
            this.listener = listener;
        }

        public void clearBuffer() {
            this.buffer = null;
        }

        public void submitFrame(F frame) {

            if (semaphore.tryAcquire()) {

                if (buffer == null) {
                    buffer = frame.copy();
                } else {
                    buffer.copyFrom(frame);
                    buffer.setTimestamp(frame.getTimestamp());
                }

                listener.newFrame(buffer);

                semaphore.release();

            }

        }

    }

}
