package jisa.devices.spectrometer.spectrum;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SpectrumQueue extends LinkedBlockingQueue<Spectrum> {

    /**
     * Takes the spectrum at the head of the queue and returns it, removing it from the queue. If no spectrum is available, this method
     * will block (i.e., wait) until one is available to return. If no spectrum ever becomes available, this method will never return.
     *
     * @return The next frame in the queue.
     *
     * @throws InterruptedException Upon the thread being interrupted while waiting for a frame.
     */
    public Spectrum nextSpectrum() throws InterruptedException {
        return take();
    }

    /**
     * Takes the spectrum at the head of the queue and returns it, removing it from the queue. If no spectrum is available, this method
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

        Spectrum frame = poll(timeout, TimeUnit.MILLISECONDS);

        if (frame == null) {
            throw new TimeoutException();
        }

        return frame;

    }

}
