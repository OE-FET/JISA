package jisa.devices.spectrometer;

import jisa.devices.DeviceException;
import jisa.devices.spectrometer.spectrum.Spectrum;
import jisa.devices.spectrometer.spectrum.SpectrumQueue;
import jisa.visa.NativeDevice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

public abstract class ManagedSpectrometer extends NativeDevice implements Spectrometer {

    private final ListenerManager listenerManager = new ListenerManager();
    
    private boolean acquiring         = false;
    private Thread  acquisitionThread = null;

    public ManagedSpectrometer(String name) {
        super(name);
    }

    protected abstract void setupAcquisition(int limit) throws IOException, DeviceException;

    protected abstract double[] createSpectrumBuffer() throws IOException, DeviceException;

    protected abstract double[] getWavelengths() throws IOException, DeviceException;

    protected abstract void acquisitionLoop(double[] buffer) throws IOException, DeviceException, InterruptedException, TimeoutException;

    protected abstract void cancelAcquisition();

    protected abstract void cleanupAcquisition();

    @Override
    public synchronized void startAcquisition() throws IOException, DeviceException {

        if (isAcquiring()) {
            return;
        }

        final double[] buffer      = createSpectrumBuffer();
        final double[] wavelengths = getWavelengths();
        final Spectrum spectrum    = new Spectrum(wavelengths, buffer);

        acquisitionThread = new Thread(() -> {

            while (acquiring) {

                try {
                    acquisitionLoop(buffer);
                } catch (IOException | DeviceException e) {
                    System.err.println(e.getMessage());
                    continue;
                } catch (InterruptedException | TimeoutException e) {
                    continue;
                }

                spectrum.setTimestamp(System.nanoTime());
                listenerManager.trigger(spectrum);

            }

        });

        acquiring = true;
        acquisitionThread.start();

    }

    @Override
    public synchronized void stopAcquisition() throws IOException, DeviceException {

        // If we're already stopped, then ignore this call
        if (!isAcquiring()) {
            return;
        }

        // Set loop flag to false, so it should break at the next iteration
        acquiring = false;

        try {

            // In case we are waiting on anything preventing the loop from progressing
            acquisitionThread.interrupt();
            cancelAcquisition();

            // Cleanup after acquisition has finished (i.e., freeing memory, disabling streaming etc)
            cleanupAcquisition();

            // Await for the acquisition thread to die gracefully (one can only hope for such a death)
            acquisitionThread.join(10000);

        } catch (InterruptedException e) {

            System.err.println("Interrupted while waiting for acquisition thread to end gracefully.");
            acquisitionThread.stop(); // If it failed to exit gracefully within 10 seconds, commit a war crime

        } catch (Throwable e) {

            // If all else fails, commit more war crimes, but then complain about it
            acquisitionThread.stop();
            throw e;

        } finally {

        }

    }

    @Override
    public synchronized boolean isAcquiring() throws IOException, DeviceException {
        return acquiring;
    }

    @Override
    public Spectrum getSpectrum() throws IOException, DeviceException, InterruptedException, TimeoutException {

        if (isAcquiring()) {

            SpectrumQueue queue = openSpectrumQueue(1);

            try {
                return queue.nextSpectrum(getAcquisitionTimeout());
            } finally {
                queue.close();
                queue.clear();
            }

        }

        try {

            setupAcquisition(1);

            double[] buffer      = createSpectrumBuffer();
            double[] wavelengths = getWavelengths();

            acquisitionLoop(buffer);

            return new Spectrum(wavelengths, buffer);

        } finally {
            cleanupAcquisition();
        }

    }

    @Override
    public List<Spectrum> getSpectrumSeries(int count) throws IOException, DeviceException, InterruptedException, TimeoutException {

        // Create list to hold our frame series
        List<Spectrum> spectra = new ArrayList<>(count);
        int            timeout = getAcquisitionTimeout();

        // If we're already continuously acquiring, open a frame queue and wait for the next n frames that would
        // have been acquired anyway
        if (isAcquiring()) {

            SpectrumQueue queue = openSpectrumQueue(count);

            try {

                for (int i = 0; i < count; i++) {
                    spectra.add(queue.nextSpectrum(timeout));
                }

            } finally {
                queue.close();
                queue.clear();
            }

        }

        // Otherwise, run the acquisition loop n times to get the frames
        try {

            double[] wavelengths = getWavelengths();
            setupAcquisition(count);

            for (int i = 0; i < count; i++) {
                double[] buffer = createSpectrumBuffer();
                acquisitionLoop(buffer);
                spectra.add(new Spectrum(wavelengths, buffer));
            }

        } finally {
            cleanupAcquisition();
        }

        return spectra;
    }

    @Override
    public synchronized Listener addSpectrumListener(Listener listener) {
        listenerManager.addListener(listener);
        return listener;
    }

    @Override
    public synchronized void removeSpectrumListener(Listener listener) {
        listenerManager.removeListener(listener);
    }

    @Override
    public synchronized SpectrumQueue openSpectrumQueue(int limit) {
        SpectrumQueue queue = new SpectrumQueue(this, limit);
        listenerManager.addQueue(queue);
        return queue;
    }

    @Override
    public synchronized void closeSpectrumQueue(SpectrumQueue queue) {
        listenerManager.removeQueue(queue);
        queue.close();
    }

}
