package jisa.devices.spectrometer;

import jisa.addresses.Address;
import jisa.devices.DeviceException;
import jisa.devices.spectrometer.spectrum.Spectrum;
import jisa.devices.spectrometer.spectrum.SpectrumQueue;
import jisa.maths.Range;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeoutException;

public class FakeSpectrometer implements Spectrometer {

    public static String getDescription() {
        return "Fake Spectrometer";
    }

    private long    delay             = 0;
    private Thread  acquisitionThread = null;
    private boolean acquiring         = false;

    public FakeSpectrometer(Address address) {

    }

    @Override
    public double getIntegrationTime() throws IOException, DeviceException {
        return delay * 1000;
    }

    @Override
    public void setIntegrationTime(double time) throws IOException, DeviceException {
        delay = (long) (time / 1000);
    }

    @Override
    public int getAcquisitionTimeout() throws IOException, DeviceException {
        return 0;
    }

    @Override
    public void setAcquisitionTimeout(int timeout) throws IOException, DeviceException {

    }

    @Override
    public synchronized void startAcquisition() throws IOException, DeviceException {

        if (isAcquiring()) {
            return;
        }

        acquisitionThread = new Thread(() -> {

            Spectrum specBuf = new Spectrum(wavelengths, buffer, System.nanoTime());

            try {

                while (acquiring) {

                    Thread.sleep(delay);
                    generate();
                    specBuf.setTimestamp(System.nanoTime());
                    manager.trigger(specBuf);

                }

            } catch (InterruptedException e) {
                System.err.println("Acquisition thread interrupted.");
            }

        });

        acquiring = true;
        acquisitionThread.start();

    }

    @Override
    public synchronized void stopAcquisition() throws IOException, DeviceException {

        if (!isAcquiring()) {
            return;
        }

        acquiring = false;
        acquisitionThread.interrupt();

        try {
            acquisitionThread.join();
        } catch (InterruptedException ignored) {

        }

    }

    @Override
    public boolean isAcquiring() throws IOException, DeviceException {
        return acquiring;
    }

    @Override
    public double getSlitWidth() throws IOException, DeviceException {
        return 0;
    }

    @Override
    public double getGratingDensity() throws IOException, DeviceException {
        return 0;
    }

    @Override
    public String getIDN() throws IOException, DeviceException {
        return "";
    }

    @Override
    public String getName() {
        return "Fake Spectrometer";
    }

    @Override
    public void close() throws IOException, DeviceException {

    }

    @Override
    public Address getAddress() {
        return null;
    }

    private final double[]        wavelengths = Range.linear(200, 800, 101).doubleArray();
    private final double[]        buffer      = new double[wavelengths.length];
    private final Random          random      = new Random();
    private final ListenerManager manager     = new ListenerManager();

    protected synchronized void generate() throws InterruptedException {

        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = random.nextDouble();
        }

    }

    @Override
    public Spectrum getSpectrum() throws IOException, DeviceException, InterruptedException, TimeoutException {

        if (isAcquiring()) {

            SpectrumQueue queue = openSpectrumQueue(1);

            try {
                return queue.nextSpectrum();
            } finally {
                queue.close();
                queue.clear();
            }

        } else {

            Thread.sleep(delay);
            generate();
            return new Spectrum(wavelengths, buffer.clone(), System.nanoTime());

        }

    }

    @Override
    public List<Spectrum> getSpectrumSeries(int count) throws IOException, DeviceException, InterruptedException, TimeoutException {

        List<Spectrum> spectra = new ArrayList<>(count);

        if (isAcquiring()) {

            SpectrumQueue queue = openSpectrumQueue(count);

            try {

                for (int i = 0; i < count; i++) {
                    spectra.add(queue.nextSpectrum());

                }

            } finally {
                queue.close();
                queue.clear();
            }

        } else {

            for (int i = 0; i < count; i++) {

                Thread.sleep(delay);
                generate();
                spectra.add(new Spectrum(wavelengths, buffer.clone(), System.nanoTime()));

            }

        }

        return spectra;

    }

    @Override
    public Listener addSpectrumListener(Listener listener) {
        manager.addListener(listener);
        return listener;
    }

    @Override
    public void removeSpectrumListener(Listener listener) {
        manager.removeListener(listener);
    }

    @Override
    public SpectrumQueue openSpectrumQueue(int limit) {
        SpectrumQueue queue = new SpectrumQueue(this, limit);
        manager.addQueue(queue);
        return queue;
    }

    @Override
    public void closeSpectrumQueue(SpectrumQueue queue) {
        manager.removeQueue(queue);
    }


}
