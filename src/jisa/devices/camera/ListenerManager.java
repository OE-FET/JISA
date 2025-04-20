package jisa.devices.camera;

import jisa.devices.camera.frame.Frame;
import jisa.devices.camera.frame.FrameQueue;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

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
            List<Runner<F>> runners = listeners.stream().filter(f -> f.listener == listener).collect(Collectors.toList());
            listeners.removeAll(runners);
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
                runner.submitFrame(frame);
            }

        }

    }

    public void clearListenerBuffers() {

        synchronized (listeners) {
            listeners.forEach(Runner::clearBuffer);
        }

    }

    public class Runner<I extends Frame<?, I>> {

        private final Camera.Listener<I> listener;
        private final Semaphore          semaphore = new Semaphore(1);

        private I buffer = null;

        public Runner(Camera.Listener<I> listener) {
            this.listener = listener;
        }

        public void clearBuffer() {
            this.buffer = null;
        }

        public void submitFrame(I frame) {

            if (semaphore.tryAcquire()) {

                if (buffer == null || frame.size() != buffer.size()) {
                    buffer = frame.copy();
                } else {
                    buffer.copyFrom(frame);
                    buffer.setTimestamp(frame.getTimestamp());
                }

                executor.execute(() -> {

                    try {
                        listener.newFrame(buffer);
                    } finally {
                        semaphore.release();
                    }

                });

            }

        }

    }

}
