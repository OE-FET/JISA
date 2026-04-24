package jisa.devices.spectrometer;

import com.oceanoptics.omnidriver.spectrometer.SpectrometerChannel;
import com.oceanoptics.omnidriver.spectrometer.USBSpectrometer;
import jisa.addresses.Address;
import jisa.addresses.IDAddress;
import jisa.devices.DeviceException;
import jisa.devices.spectrometer.spectrum.Spectrum;
import jisa.devices.spectrometer.spectrum.SpectrumQueue;
import jisa.visa.NativeDevice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class OceanOpticsLegacy<T extends USBSpectrometer, S extends OceanOpticsLegacy<T, S>> extends NativeDevice implements Spectrometer {

    private final T                                           usb;
    private final SpectrometerChannel                         channel;
    private final ListenerManager                             manager              = new ListenerManager();
    private final List<AcquisitionListener>                   acquisitionListeners = new LinkedList<>();
    private final long[]                                      stats                = new long[3];
    private       double                                      fps                  = 0.0;
    private       boolean                                     acquiring            = false;
    private       Thread                                      acquisitionThread    = null;
    private       com.oceanoptics.omnidriver.spectra.Spectrum buffer               = null;
    private       Spectrum                                    spectrum             = null;

    public OceanOpticsLegacy(String name, Class<T> type) throws IOException, DeviceException {

        super(name);

        try {
            usb = type.getConstructor().newInstance();
        } catch (Exception e) {
            throw new DeviceException(e.getMessage());
        }

        if (usb.getChannels() == null) {
            throw new IOException(String.format("Connection to %s failed.", usb.getName()));
        }

        channel  = usb.channels[0];
        buffer   = channel.getUnfilledSpectrum();
        spectrum = new Spectrum(channel.getAllWavelengths(), new double[channel.getNumberOfPixels()]);

    }

    public OceanOpticsLegacy(String name, Class<T> type, int index) throws IOException, DeviceException {

        super(name);

        try {
            usb = type.getConstructor(Integer.TYPE).newInstance(index);
        } catch (Exception e) {
            throw new DeviceException(e.getMessage());
        }

        if (usb.getChannels() == null) {
            throw new IOException(String.format("Connection to %s failed.", usb.getName()));
        }

        channel = usb.channels[0];

    }

    public OceanOpticsLegacy(String name, Class<T> type, IDAddress address) throws DeviceException, IOException {

        super(name);

        try {
            usb = type.getConstructor(Integer.TYPE).newInstance(Integer.parseInt(address.getID()));
        } catch (Exception e) {
            throw new DeviceException(e.getMessage());
        }

        if (usb.getChannels() == null) {
            throw new IOException(String.format("Connection to %s failed.", usb.getName()));
        }

        channel = usb.channels[0];

    }

    @Override
    public double getIntegrationTime() throws IOException, DeviceException {
        return (double) usb.getActualIntegrationTime() / 1e6;
    }

    @Override
    public void setIntegrationTime(double time) throws IOException, DeviceException {
        usb.setIntegrationTime((int) (time * 1e6));
    }

    @Override
    public int getAcquisitionTimeout() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void setAcquisitionTimeout(int timeout) {

    }

    private void acquisition() {

        try {
            buffer = channel.getUnfilledSpectrum();
        } catch (Exception e) {
            stopAcquisition();
            return;
        }

        while (acquiring) {

            try {
                channel.getSpectrum(buffer);
                spectrum.copyFrom(buffer.getSpectrum());
                manager.trigger(spectrum);
            } catch (Exception e) {
                System.err.println("Error acquiring spectrum: " + e.getMessage());
            }

            synchronized (stats) {
                stats[0]++;
            }

            if (Thread.interrupted() || !acquiring) {
                break;
            }

        }

    }

    @Override
    public synchronized void startAcquisition() throws IOException, DeviceException {

        if (acquiring) {
            return;
        }

        acquisitionThread = new Thread(this::acquisition);
        acquiring         = true;
        acquisitionThread.start();

        acquisitionListeners.forEach(l -> l.changed(0, true));

    }

    @Override
    public synchronized void stopAcquisition() {

        if (!acquiring) {
            return;
        }

        acquiring = false;
        acquisitionThread.interrupt();

        try {
            acquisitionThread.join();
        } catch (InterruptedException ignored) { }

        synchronized (stats) {
            stats[0] = 0;
            stats[1] = 0;
            stats[2] = System.nanoTime();
            fps      = 0.0;
        }

        acquisitionListeners.forEach(l -> l.changed(0, false));

    }

    @Override
    public boolean isAcquiring() {
        return acquiring;
    }

    @Override
    public AcquisitionListener addAcquisitionListener(AcquisitionListener listener) {
        acquisitionListeners.add(listener);
        return listener;
    }

    @Override
    public void removeAcquisitionListener(AcquisitionListener listener) {
        acquisitionListeners.remove(listener);
    }

    @Override
    public double getAcquisitionRate() throws IOException, DeviceException {

        if ((stats[0] != stats[1]) && ((System.nanoTime() - stats[2]) >= 2L * getIntegrationTime() * 1e9)) {

            synchronized (stats) {

                long frames  = stats[0];
                long dFrames = frames - stats[1];
                long time    = System.nanoTime();
                long dTime   = time - stats[2];

                stats[1] = frames;
                stats[2] = time;

                fps = 1e9 * dFrames / dTime;

            }

        }

        return fps;

    }

    @Override
    public String getIDN() throws IOException, DeviceException {
        return getName();
    }

    @Override
    public String getName() {
        return String.format("Ocean Optics %s Spectrometer", usb.getName());
    }

    @Override
    public void close() throws IOException, DeviceException {
        usb.close();
    }

    @Override
    public Address getAddress() {
        return new IDAddress(String.format("%d", usb.getDeviceIndex()));
    }

    @Override
    public Spectrum getSpectrum() throws IOException, InterruptedException {

        if (isAcquiring()) {

            SpectrumQueue queue = openSpectrumQueue(1);

            try {
                return queue.nextSpectrum();
            } finally {
                queue.close();
                queue.clear();
            }

        } else {

            acquisitionListeners.forEach(l -> l.changed(1, true));

            try {
                return new Spectrum(channel.getAllWavelengths(), channel.getSpectrum().getSpectrum(), System.nanoTime(), getAllParametersAsMap());
            } finally {
                acquisitionListeners.forEach(l -> l.changed(1, false));
            }
        }

    }

    @Override
    public List<Spectrum> getSpectrumSeries(int count) throws IOException, InterruptedException {

        List<Spectrum> spectrumSeries = new ArrayList<>(count);

        if (isAcquiring()) {

            SpectrumQueue queue = openSpectrumQueue(count);

            try {

                for (int i = 0; i < count; i++) {
                    spectrumSeries.add(queue.nextSpectrum());
                }

            } finally {
                queue.close();
                queue.clear();
            }

        } else {

            acquisitionListeners.forEach(l -> l.changed(count, true));

            try {

                for (int i = 0; i < count; i++) {
                    spectrumSeries.add(new Spectrum(channel.getAllWavelengths(), channel.getSpectrum().getSpectrum(), System.nanoTime(), getAllParametersAsMap()));
                }

            } finally {
                acquisitionListeners.forEach(l -> l.changed(count, false));
            }

        }

        return spectrumSeries;

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

        if (queue.isOpen()) {
            queue.close();
        }

    }

    @Override
    public List<Component> getComponents() {
        return List.of();
    }


    public static class USB650 extends OceanOpticsLegacy<com.oceanoptics.omnidriver.spectrometer.usb650.USB650, USB650> {

        public static final Class<com.oceanoptics.omnidriver.spectrometer.usb650.USB650> CLASS = com.oceanoptics.omnidriver.spectrometer.usb650.USB650.class;

        public USB650() throws IOException, DeviceException {
            super("Ocean Optics USB650 Spectrometer", CLASS);
        }

        public USB650(int index) throws IOException, DeviceException {
            super("Ocean Optics USB650 Spectrometer", CLASS, index);
        }

        public USB650(IDAddress address) throws DeviceException, IOException {
            super("Ocean Optics USB650 Spectrometer", CLASS, address);
        }

    }

    public static class USB2000 extends OceanOpticsLegacy<com.oceanoptics.omnidriver.spectrometer.usb2000.USB2000, USB2000> {

        public static final Class<com.oceanoptics.omnidriver.spectrometer.usb2000.USB2000> CLASS = com.oceanoptics.omnidriver.spectrometer.usb2000.USB2000.class;

        public USB2000() throws IOException, DeviceException {
            super("Ocean Optics USB2000 Spectrometer", CLASS);
        }

        public USB2000(int index) throws IOException, DeviceException {
            super("Ocean Optics USB2000 Spectrometer", CLASS, index);
        }

        public USB2000(IDAddress address) throws DeviceException, IOException {
            super("Ocean Optics USB2000 Spectrometer", CLASS, address);
        }

    }

    public static class USB2000Plus extends OceanOpticsLegacy<com.oceanoptics.omnidriver.spectrometer.usb2000plus.USB2000Plus, USB2000Plus> {

        public static final Class<com.oceanoptics.omnidriver.spectrometer.usb2000plus.USB2000Plus> CLASS = com.oceanoptics.omnidriver.spectrometer.usb2000plus.USB2000Plus.class;

        public USB2000Plus() throws IOException, DeviceException {
            super("Ocean Optics USB2000Plus Spectrometer", CLASS);
        }

        public USB2000Plus(int index) throws IOException, DeviceException {
            super("Ocean Optics USB2000Plus Spectrometer", CLASS, index);
        }

        public USB2000Plus(IDAddress address) throws DeviceException, IOException {
            super("Ocean Optics USB2000Plus Spectrometer", CLASS, address);
        }

    }

}
