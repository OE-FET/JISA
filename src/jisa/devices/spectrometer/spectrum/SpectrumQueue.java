package jisa.devices.spectrometer.spectrum;

import jisa.devices.spectrometer.Spectrometer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SpectrumQueue extends LinkedBlockingQueue<Spectrum> {

    private       boolean              open    = true;
    private final Spectrometer.Channel spectrometer;
    private final List<Thread>         threads = Collections.synchronizedList(new ArrayList<>(1));

    public SpectrumQueue(Spectrometer.Channel spectrometer, int limit) {
        super(limit);
        this.spectrometer = spectrometer;
    }

    public SpectrumQueue(Spectrometer.Channel spectrometer) {
        this(spectrometer, Integer.MAX_VALUE);
    }

    /**
     * Takes the frame at the head of the queue and returns it, removing it from the queue. If no frame is available, this method
     * will block (i.e., wait) until one is available to return. If no frame ever becomes available, this method will never return.
     *
     * @return The next frame in the queue.
     *
     * @throws InterruptedException Upon the thread being interrupted while waiting for a frame.
     */
    public Spectrum nextSpectrum() throws InterruptedException {

        threads.add(Thread.currentThread());
        Spectrum spectrum = take();
        threads.remove(Thread.currentThread());

        return spectrum;

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
    public Spectrum nextSpectrum(long timeout) throws InterruptedException, TimeoutException {

        threads.add(Thread.currentThread());
        Spectrum spectrum = poll(timeout, TimeUnit.MILLISECONDS);
        threads.remove(Thread.currentThread());

        if (spectrum == null) {
            throw new TimeoutException("Timed out waiting for spectrum.");
        }

        return spectrum;

    }

    public void close() {

        if (open) {

            open = false;
            spectrometer.closeSpectrumQueue(this);

            if (isEmpty()) {
                threads.forEach(Thread::interrupt);
            }

        }

    }

    public boolean isOpen() {
        return open;
    }

    public boolean hasSpectra() {
        return !isEmpty();
    }

    public boolean isAlive() {
        return isOpen() || hasSpectra();
    }

}
