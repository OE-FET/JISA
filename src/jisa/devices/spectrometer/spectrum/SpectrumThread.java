package jisa.devices.spectrometer.spectrum;

import jisa.Util;
import jisa.control.SRunnable;
import jisa.devices.spectrometer.Spectrometer;

import java.util.LinkedList;
import java.util.List;

public class SpectrumThread {

    private final Spectrometer.Channel       channel;
    private final Spectrometer.CountStreamer streamer;
    private final SRunnable                  closer;
    private final SpectrumQueue              queue;
    private final Thread                     thread;
    private final List<Throwable>            exceptions;

    private long count = 0;

    public SpectrumThread(Spectrometer.Channel channel, Spectrometer.CountStreamer streamer, SRunnable closer) {

        this.channel    = channel;
        this.streamer   = streamer;
        this.closer     = closer;
        this.queue      = channel.openSpectrumQueue();
        this.thread     = new Thread(this::run);
        this.exceptions = new LinkedList<>();

        this.thread.start();

    }

    public SpectrumThread(Spectrometer.Channel channel, Spectrometer.Streamer streamer, SRunnable closer) {
        this(channel, (i, s) -> streamer.newSpectrum(s), closer);
    }

    public SpectrumThread(Spectrometer.Channel channel, Spectrometer.CountStreamer streamer) {
        this(channel, streamer, () -> { });
    }

    public SpectrumThread(Spectrometer.Channel channel, Spectrometer.Streamer streamer) {
        this(channel, streamer, () -> { });
    }


    private void run() {

        while (queue.isAlive()) {

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

    public synchronized long getSpectrumCount() {
        return count;
    }

    public boolean hasExceptions() {
        return !exceptions.isEmpty();
    }

    public List<Throwable> getExceptions() {
        return List.copyOf(exceptions);
    }

    public SpectrumQueue getSpectrumQueue() {
        return queue;
    }

}
