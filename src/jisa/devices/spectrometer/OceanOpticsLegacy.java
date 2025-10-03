package jisa.devices.spectrometer;

import com.oceanoptics.omnidriver.spectrometer.SpectrometerChannel;
import com.oceanoptics.omnidriver.spectrometer.USBSpectrometer;
import com.oceanoptics.omnidriver.spectrometer.usb2000plus.USB2000Plus;
import jisa.addresses.Address;
import jisa.addresses.IDAddress;
import jisa.devices.DeviceException;
import jisa.devices.spectrometer.spectrum.Spectrum;
import jisa.devices.spectrometer.spectrum.SpectrumQueue;
import jisa.visa.NativeDevice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OceanOpticsLegacy<T extends USBSpectrometer, S extends OceanOpticsLegacy<T, S>> extends NativeDevice implements Spectrometer {

    private final T                                           usb;
    private final SpectrometerChannel                         channel;
    private final ListenerManager                             manager           = new ListenerManager();
    private       boolean                                     acquiring         = false;
    private       Thread                                      acquisitionThread = null;
    private       com.oceanoptics.omnidriver.spectra.Spectrum buffer            = null;
    private       Spectrum                                    spectrum          = null;

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

    }

    @Override
    public boolean isAcquiring() {
        return acquiring;
    }

    @Override
    public double getSlitWidth() {
        return 25e-6;
    }

    @Override
    public double getGratingDensity() {
        return 0;
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
            return new Spectrum(channel.getAllWavelengths(), channel.getSpectrum().getSpectrum());
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

            for (int i = 0; i < count; i++) {
                spectrumSeries.add(getSpectrum());
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
