package jisa.devices.spectrometer.spectrum;

import jisa.devices.spectrometer.Spectrometer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SpectrumQueue extends LinkedBlockingQueue<Spectrum> {

    private       boolean      open    = true;
    private final Spectrometer spectrometer;
    private final List<Thread> threads = Collections.synchronizedList(new ArrayList<>(1));

    public SpectrumQueue(Spectrometer spectrometer, int limit) {
        super(limit);
        this.spectrometer = spectrometer;
    }

    public SpectrumQueue(Spectrometer spectrometer) {
        this(spectrometer, Integer.MAX_VALUE);
    }

    /**
     * Takes the spectrum at the head of the queue and returns it, removing it from the queue. If no spectrum is available, this method
     * will block (i.e., wait) until one is available to return. If no spectrum ever becomes available, this method will never return.
     *
     * @return The next spectrum in the queue.
     *
     * @throws InterruptedException Upon the thread being interrupted while waiting for a spectrum.
     */
    public Spectrum nextSpectrum() throws InterruptedException {

        threads.add(Thread.currentThread());
        Spectrum spectrum = take();
        threads.remove(Thread.currentThread());

        return spectrum;

    }

    /**
     * Takes the spectrum at the head of the queue and returns it, removing it from the queue. If no spectrum is available, this method
     * will block (i.e., wait) until one is available to return, or the specified timeout expires.
     *
     * @param timeout Max time to wait, in milliseconds.
     *
     * @return The next spectrum in the queue.
     *
     * @throws InterruptedException Upon the thread being interrupted while waiting.
     * @throws TimeoutException     Upon timing out before a spectrum becomes available.
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

    /**
     * Closes this spectrum queue, so that it receives no further new frames.
     */
    public void close() {

        if (open) {

            open = false;
            spectrometer.closeSpectrumQueue(this);

            if (isEmpty()) {
                threads.forEach(Thread::interrupt);
            }

        }

    }

    /**
     * Returns whether this queue is currently open to receiving new frames.
     *
     * @return Is it open?
     */
    public boolean isOpen() {
        return open;
    }

    /**
     * Returns whether there are any spectra in the queue.
     *
     * @return Any spectra present?
     */
    public boolean hasSpectra() {
        return !isEmpty();
    }

    /**
     * Returns whether this queue is still open and/or has frames left to be taken. Essentially checks whether there
     * are frames to be taken, or if it's still possible that frame(s) may be added.
     *
     * @return Is it alive?
     */
    public boolean isAlive() {
        return isOpen() || hasSpectra();
    }

}
