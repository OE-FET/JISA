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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class OceanOpticsUSB650 extends NativeDevice implements Spectrometer<OceanOpticsUSB650.Channel> {

    private final USB650        usb650;
    private final List<Channel> channels;

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
        int microseconds = (int) (time * 1e6);
        usb650.setIntegrationTime(microseconds);
    }

    @Override
    public int getAcquisitionTimeout() throws IOException, DeviceException {
        return Integer.MAX_VALUE;
    }

    @Override
    public void setAcquisitionTimeout(int timeout) throws IOException, DeviceException {

    }

    @Override
    public void startAcquisition() throws IOException, DeviceException {

    }

    @Override
    public void stopAcquisition() throws IOException, DeviceException {

    }

    @Override
    public boolean isAcquiring() throws IOException, DeviceException {
        return false;
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

        private final SpectrometerChannel channel;

        public Channel(SpectrometerChannel channel) {
            this.channel = channel;
        }

        @Override
        public Spectrum getSpectrum() throws IOException, DeviceException, InterruptedException, TimeoutException {
            return new Spectrum(channel.getAllWavelengths(), DoubleStream.of(channel.getSpectrum().getSpectrum()).mapToLong(i -> (long) i).toArray());
        }

        @Override
        public List<Spectrum> getSpectrumSeries(int count) throws IOException, DeviceException, InterruptedException, TimeoutException {
            return null;
        }

        @Override
        public Listener addSpectrumListener(Listener listener) {
            return null;
        }

        @Override
        public void removeSpectrumListener(Listener listener) {

        }

        @Override
        public SpectrumQueue openSpectrumQueue(int limit) {
            return null;
        }

        @Override
        public void closeSpectrumQueue(SpectrumQueue queue) {

        }

        @Override
        public OceanOpticsUSB650 getParentInstrument() {
            return OceanOpticsUSB650.this;
        }

        @Override
        public String getName() {
            return "";
        }

    }

}
