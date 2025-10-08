package jisa.devices.spectrometer.spectrum;

import jisa.Util;
import jisa.control.SRunnable;
import jisa.devices.spectrometer.Spectrometer;

import java.util.LinkedList;
import java.util.List;

public class SpectrumThread {

    private final Spectrometer               spectrometer;
    private final Spectrometer.CountStreamer streamer;
    private final SRunnable                  closer;
    private final SpectrumQueue              queue;
    private final Thread                     thread;
    private final List<Throwable>            exceptions;

    private long count = 0;

    public SpectrumThread(Spectrometer channel, Spectrometer.CountStreamer streamer, SRunnable closer) {

        this.spectrometer = channel;
        this.streamer     = streamer;
        this.closer       = closer;
        this.queue        = channel.openSpectrumQueue();
        this.thread       = new Thread(this::run);
        this.exceptions   = new LinkedList<>();

        this.thread.start();

    }

    public SpectrumThread(Spectrometer channel, Spectrometer.Streamer streamer, SRunnable closer) {
        this(channel, (i, s) -> streamer.newSpectrum(s), closer);
    }

    public SpectrumThread(Spectrometer channel, Spectrometer.CountStreamer streamer) {
        this(channel, streamer, () -> { });
    }

    public SpectrumThread(Spectrometer channel, Spectrometer.Streamer streamer) {
        this(channel, streamer, () -> { });
    }

    private void run() {

        try {

            while (queue.isAlive()) {

                if (Thread.interrupted()) {
                    break;
                }

                try {

                    Spectrum frame = queue.nextSpectrum();
                    streamer.newSpectrum(count, frame);
                    count++;

                } catch (InterruptedException e) {
                    continue;
                } catch (Throwable e) {
                    exceptions.add(e);
                }

            }

        } finally {
            Util.runRegardless(closer);
        }

    }

    /**
     * Returns whether the thread is currently running or not.
     *
     * @return Is it running?
     */
    public boolean isRunning() {
        return thread.isAlive();
    }

    /**
     * Closes the queue and waits for the thread to finish processing any spectra left in the queue (i.e., any backlog) before shutting
     * down and returning.
     */
    public synchronized void stop(int timeout) {

        queue.close();

        try {
            thread.join(timeout);
        } catch (InterruptedException ignored) { }

    }

    /**
     * Closes the queue and waits for the thread to finish processing any spectra left in the queue (i.e., any backlog) before shutting
     * down and returning.
     */
    public synchronized void stop() {
        stop(0);
    }

    /**
     * Attempts to make the thread shut down now --- clearing out any remaining backlog of spectra without processing them.
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
     * Returns the number of spectra that this thread has processed so far.
     *
     * @return Number of spectra processed.
     */
    public synchronized long getSpectrumCount() {
        return count;
    }

    /**
     * Returns whether any exceptions (errors) have occured while processing spectra so far.
     *
     * @return Any exceptions?
     */
    public boolean hasExceptions() {
        return !exceptions.isEmpty();
    }

    /**
     * Returns a list of all exceptions (errors) that have occured while processing spectra so far.
     *
     * @return List of exceptions.
     */
    public List<Throwable> getExceptions() {
        return List.copyOf(exceptions);
    }

    /**
     * Returns the spectrum queue that this thread is using to retrieve spectra from the spectrometer.
     *
     * @return Spectrum queue.
     */
    public SpectrumQueue getSpectrumQueue() {
        return queue;
    }

}
