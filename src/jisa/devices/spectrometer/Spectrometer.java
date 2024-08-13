package jisa.devices.spectrometer;

import jisa.Util;
import jisa.devices.DeviceException;
import jisa.devices.Instrument;
import jisa.devices.MultiInstrument;
import jisa.devices.spectrometer.spectrum.Spectrum;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public interface Spectrometer<C extends Spectrometer.Channel> extends Instrument, MultiInstrument {

    /**
     * Returns a list of all channels this spectrometer has.
     *
     * @return Spectrometer channels.
     */
    List<C> getChannels();

    /**
     * Returns the nth channel of this spectrometer.
     *
     * @param index n.
     *
     * @return Spectrometer channel n.
     */
    default C getChannel(int index) {
        return getChannels().get(index);
    }

    default List<? extends Instrument> getSubInstruments() {
        return getChannels();
    }

    /**
     * Tells all channels to take a spectrum and returns a map of channel to spectrum.
     *
     * @return Map of spectra
     */
    default Map<C, Spectrum> getSpectra() {

        Map<C, Spectrum> spectra = new LinkedHashMap<>();

        for (C channel : getChannels()) {
            Util.runRegardless(() -> spectra.put(channel, channel.getSpectrum()));
        }

        return spectra;

    }

    /**
     * Returns the integration time currently being used by the spectrometer, in seconds.
     *
     * @return Integration time, in seconds.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    double getIntegrationTime() throws IOException, DeviceException;

    /**
     * Sets the integration time, in seconds, for the spectrometer to use.
     *
     * @param time Integration time, in seconds.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    void setIntegrationTime(double time) throws IOException, DeviceException;

    interface Channel extends Instrument {

        /**
         * Acquires and returns a single spectrum.
         *
         * @return Resulting spectrum, as a Spectrum object.
         *
         * @throws IOException     Upon communications error
         * @throws DeviceException Upon device compatibility error
         */
        Spectrum getSpectrum() throws IOException, DeviceException;

        /**
         * Acquires and returns a set number of spectra, and returns them in a list.
         *
         * @param count Number of spectra to take.
         *
         * @return List of acquired spectra, as Spectrum objects.
         *
         * @throws IOException     Upon communications error
         * @throws DeviceException Upon device compatibility error
         */
        List<Spectrum> getSpectrumSeries(int count) throws IOException, DeviceException;

        /**
         * Adds a listener to this spectrometer channel that is called for each new spectrum that is acquired. Any spectra
         * that are acquired while this listener is still running from a previous spectrum will be skipped by this listener.
         * The spectrum object passed to this listener is liable to be recycled for future callbacks, so use copy() if your
         * intention is to store it, or consider opening a queue with openSpectrumQueue() instead.
         *
         * @param listener Listener for new spectra.
         *
         * @return Reference to listener.
         */
        Listener addSpectrumListener(Listener listener);

        /**
         * Removes the given listener from this channel, stopping it from being called when new spectra are taken.
         *
         * @param listener Listener to remove.
         */
        void removeSpectrumListener(Listener listener);

        /**
         * Opens a (blocking) queue into which copies of newly acquired spectra will be placed. This is to allow for asynchronous, lossless processing of spectral data.
         *
         * @return Queue of spectra.
         */
        SpectrumQueue openSpectrumQueue();

        /**
         * Closes the given queue, preventing the spectrometer channel from adding any new frames to it.
         *
         * @param queue Queue to close
         */
        void closeSpectrumQueue(SpectrumQueue queue);

        /**
         * Instructs the spectrometer channel to start acquiring continuously (if it isn't already).
         *
         * @throws IOException     Upon communications error
         * @throws DeviceException Upon device compatibility error
         */
        void startAcquisition() throws IOException, DeviceException;

        /**
         * Instructs the spectrometer channel to stop acquiring continuously (if it hasn't already).
         *
         * @throws IOException     Upon communications error
         * @throws DeviceException Upon device compatibility error
         */
        void stopAcquisition() throws IOException, DeviceException;

    }

    interface Listener {
        void newSpectrum(Spectrum spectrum);
    }

    class SpectrumQueue extends LinkedBlockingQueue<Spectrum> {

        /**
         * Takes the spectrum at the head of the queue and returns it, removing it from the queue. If no spectrum is available, this method
         * will block (i.e., wait) until one is available to return. If no spectrum ever becomes available, this method will never return.
         *
         * @return The next frame in the queue.
         *
         * @throws InterruptedException Upon the thread being interrupted while waiting for a frame.
         */
        public Spectrum takeSpectrum() throws InterruptedException {
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
        public Spectrum takeSpectrum(long timeout) throws InterruptedException, TimeoutException {

            Spectrum frame = poll(timeout, TimeUnit.MILLISECONDS);

            if (frame == null) {
                throw new TimeoutException();
            }

            return frame;

        }

    }

}
