package jisa.devices.spectrometer;

import com.oceanoptics.omnidriver.spectrometer.SpectrometerChannel;
import com.oceanoptics.omnidriver.spectrometer.USBSpectrometer;
import com.oceanoptics.omnidriver.spectrometer.usb2000.USB2000;
import com.oceanoptics.omnidriver.spectrometer.usb650.USB650;
import jisa.addresses.Address;
import jisa.addresses.IDAddress;
import jisa.devices.DeviceException;
import jisa.devices.SubInstrument;
import jisa.devices.spectrometer.spectrum.Spectrum;
import jisa.devices.spectrometer.spectrum.SpectrumQueue;
import jisa.visa.NativeDevice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class OceanOpticsUSB<T extends USBSpectrometer, S extends OceanOpticsUSB<T, S>> extends NativeDevice implements Spectrometer<OceanOpticsUSB<T, S>.Channel> {

    private final T             usb;
    private final List<Channel> channels;
    private       boolean       acquiring         = false;
    private       Thread        acquisitionThread = null;

    public OceanOpticsUSB(String name, Class<T> type) throws IOException, DeviceException {

        super(name);

        try {
            usb = type.getConstructor().newInstance();
        } catch (Exception e) {
            throw new DeviceException(e.getMessage());
        }

        if (usb.getChannels() == null) {
            throw new IOException(String.format("Connection to %s failed.", usb.getName()));
        }

        channels = Arrays.stream(usb.getChannels()).map(Channel::new).collect(Collectors.toList());

    }

    public OceanOpticsUSB(String name, Class<T> type, int index) throws IOException, DeviceException {

        super(name);

        try {
            usb = type.getConstructor(Integer.TYPE).newInstance(index);
        } catch (Exception e) {
            throw new DeviceException(e.getMessage());
        }

        if (usb.getChannels() == null) {
            throw new IOException(String.format("Connection to %s failed.", usb.getName()));
        }

        channels = Arrays.stream(usb.getChannels()).map(Channel::new).collect(Collectors.toList());

    }

    public OceanOpticsUSB(String name, Class<T> type, IDAddress address) throws DeviceException, IOException {

        super(name);

        try {
            usb = type.getConstructor(Integer.TYPE).newInstance(Integer.parseInt(address.getID()));
        } catch (Exception e) {
            throw new DeviceException(e.getMessage());
        }

        if (usb.getChannels() == null) {
            throw new IOException(String.format("Connection to %s failed.", usb.getName()));
        }

        channels = Arrays.stream(usb.getChannels()).map(Channel::new).collect(Collectors.toUnmodifiableList());

    }

    @Override
    public List<Channel> getChannels() {
        return channels;
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

        for (Channel channel : channels) {

            try {
                channel.buffer = channel.channel.getSpectrum();
            } catch (Exception e) {
                stopAcquisition();
                return;
            }

        }

        loop:
        while (acquiring) {

            for (Channel channel : channels) {

                try {
                    channel.channel.getSpectrum(channel.buffer);
                    channel.spectrum.copyFrom(channel.buffer.getSpectrum());
                    channel.manager.trigger(channel.spectrum);
                } catch (Exception e) {
                    System.err.println("Error acquiring spectrum: " + e.getMessage());
                }

                if (Thread.interrupted() || !acquiring) {
                    break loop;
                }

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

    public class Channel implements Spectrometer.Channel, SubInstrument<OceanOpticsUSB> {

        private final SpectrometerChannel                         channel;
        private final ListenerManager                             manager  = new ListenerManager();
        private       com.oceanoptics.omnidriver.spectra.Spectrum buffer   = null;
        private       Spectrum                                    spectrum = null;

        public Channel(SpectrometerChannel channel) {
            this.channel = channel;
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

        @Override
        public S getParentInstrument() {
            return (S) OceanOpticsUSB.this;
        }

        @Override
        public String getName() {
            return usb.getName();
        }

    }

    public static class Model650 extends OceanOpticsUSB<USB650, Model650> {

        public static final Class<USB650> CLASS = USB650.class;

        public Model650() throws IOException, DeviceException {
            super("Ocean Optics USB650 Spectrometer", CLASS);
        }

        public Model650(int index) throws IOException, DeviceException {
            super("Ocean Optics USB650 Spectrometer", CLASS, index);
        }

        public Model650(IDAddress address) throws DeviceException, IOException {
            super("Ocean Optics USB650 Spectrometer", CLASS, address);
        }
    }

    public static class Model2000 extends OceanOpticsUSB<USB2000, Model2000> {

        public static final Class<USB2000> CLASS = USB2000.class;

        public Model2000() throws IOException, DeviceException {
            super("Ocean Optics USB2000 Spectrometer", CLASS);
        }

        public Model2000(int index) throws IOException, DeviceException {
            super("Ocean Optics USB2000 Spectrometer", CLASS, index);
        }

        public Model2000(IDAddress address) throws DeviceException, IOException {
            super("Ocean Optics USB2000 Spectrometer", CLASS, address);
        }

    }
}
