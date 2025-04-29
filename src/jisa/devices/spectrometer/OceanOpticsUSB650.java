package jisa.devices.spectrometer;

import com.oceanoptics.omnidriver.spectrometer.SpectrometerChannel;
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
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class OceanOpticsUSB650 extends NativeDevice implements Spectrometer<OceanOpticsUSB650.Channel> {

    private final USB650        usb650;
    private final List<Channel> channels;
    private       boolean       acquiring         = false;
    private       Thread        acquisitionThread = null;

    public OceanOpticsUSB650() throws IOException {

        super("Ocean Optics RedTide USB650 Spectrometer");

        usb650   = new USB650();
        channels = Arrays.stream(usb650.getChannels()).map(Channel::new).collect(Collectors.toList());

    }

    public OceanOpticsUSB650(int index) throws IOException {

        super("Ocean Optics RedTide USB650 Spectrometer");

        usb650   = new USB650(index);
        channels = Arrays.stream(usb650.getChannels()).map(Channel::new).collect(Collectors.toList());

    }

    public OceanOpticsUSB650(IDAddress address) throws IOException, DeviceException {

        super("Ocean Optics RedTide Spectrometer");

        try {
            usb650   = new USB650(Integer.parseInt(address.getID()));
            channels = Arrays.stream(usb650.getChannels()).map(Channel::new).collect(Collectors.toList());
        } catch (NumberFormatException e) {
            throw new DeviceException("ID in address object must be an integer.");
        }

    }

    @Override
    public List<Channel> getChannels() {
        return channels;
    }

    @Override
    public double getIntegrationTime() throws IOException, DeviceException {
        return (double) usb650.getActualIntegrationTime() / 1e6;
    }

    @Override
    public void setIntegrationTime(double time) throws IOException, DeviceException {
        usb650.setIntegrationTime((int) (time * 1e6));
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
        return false;
    }

    @Override
    public double getSlitWidth() {
        return 0;
    }

    @Override
    public double getGratingDensity() {
        return 0;
    }

    @Override
    public String getIDN() throws IOException, DeviceException {
        return "Ocean Optics RedTide USB650 Spectrometer";
    }

    @Override
    public String getName() {
        return "Ocean Optics RedTide USB650 Spectrometer";
    }

    @Override
    public void close() throws IOException, DeviceException {
        usb650.close();
    }

    @Override
    public Address getAddress() {
        return new IDAddress(String.format("%d", usb650.getDeviceIndex()));
    }

    public class Channel implements Spectrometer.Channel, SubInstrument<OceanOpticsUSB650> {

        private final SpectrometerChannel                         channel;
        private final ListenerManager                             manager  = new ListenerManager();
        private       com.oceanoptics.omnidriver.spectra.Spectrum buffer   = null;
        private       Spectrum                                    spectrum = null;

        public Channel(SpectrometerChannel channel) {
            this.channel = channel;
        }

        @Override
        public Spectrum getSpectrum() throws IOException {
            return new Spectrum(channel.getAllWavelengths(), channel.getSpectrum().getSpectrum());
        }

        @Override
        public List<Spectrum> getSpectrumSeries(int count) throws IOException, DeviceException, InterruptedException, TimeoutException {

            List<Spectrum> spectrumSeries = new ArrayList<>(count);

            for (int i = 0; i < count; i++) {
                spectrumSeries.add(getSpectrum());
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
        public OceanOpticsUSB650 getParentInstrument() {
            return OceanOpticsUSB650.this;
        }

        @Override
        public String getName() {
            return usb650.getName();
        }

    }

}
