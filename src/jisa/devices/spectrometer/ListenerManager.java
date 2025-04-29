package jisa.devices.spectrometer;

import jisa.devices.spectrometer.spectrum.Spectrum;
import jisa.devices.spectrometer.spectrum.SpectrumQueue;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

public class ListenerManager {

    private final List<Runner>        listeners = new LinkedList<>();
    private final List<SpectrumQueue> queues    = new LinkedList<>();
    private final Executor            executor  = Executors.newCachedThreadPool();

    public void addListener(Spectrometer.Listener listener) {

        synchronized (listeners) {
            listeners.add(new Runner(listener));
        }

    }

    public void removeListener(Spectrometer.Listener listener) {

        synchronized (listeners) {
            List<Runner> runners = listeners.stream().filter(f -> f.listener == listener).collect(Collectors.toList());
            listeners.removeAll(runners);
        }

    }

    public void addQueue(SpectrumQueue queue) {

        synchronized (queues) {
            queues.add(queue);
        }

    }

    public void removeQueue(SpectrumQueue queue) {

        synchronized (queues) {
            queues.remove(queue);
        }

    }

    public void trigger(Spectrum spectrum) {

        synchronized (queues) {

            for (SpectrumQueue queue : queues) {
                queue.offer(spectrum.copy());
            }

        }

        synchronized (listeners) {

            for (Runner runner : listeners) {
                runner.submitSpectrum(spectrum);
            }

        }

    }

    public void clearListenerBuffers() {

        synchronized (listeners) {
            listeners.forEach(Runner::clearBuffer);
        }

    }

    public class Runner {

        private final Spectrometer.Listener listener;
        private final Semaphore             semaphore = new Semaphore(1);

        private Spectrum buffer = null;

        public Runner(Spectrometer.Listener listener) {
            this.listener = listener;
        }

        public void clearBuffer() {
            this.buffer = null;
        }

        public void submitSpectrum(Spectrum spectrum) {

            if (semaphore.tryAcquire()) {

                if (buffer == null || spectrum.size() != buffer.size()) {
                    buffer = spectrum.copy();
                } else {
                    buffer.copyFrom(spectrum);
                }

                executor.execute(() -> {

                    try {
                        listener.newSpectrum(buffer);
                    } finally {
                        semaphore.release();
                    }

                });

            }

        }

    }

}
