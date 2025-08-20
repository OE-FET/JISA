package jisa.devices.spectrometer;

import jisa.Util;
import jisa.addresses.Address;
import jisa.addresses.USBAddress;
import jisa.devices.DeviceException;
import jisa.devices.spectrometer.spectrum.Spectrum;
import jisa.devices.spectrometer.spectrum.SpectrumQueue;
import jisa.visa.USBDevice;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class ThorLabsCCS extends USBDevice implements Spectrometer<ThorLabsCCS.Channel> {

    public ThorLabsCCS(USBAddress address) throws IOException, DeviceException {
        super(address.getVendorID(), address.getProductID(), address.getSerialNumber(), -1, address.getInterfaceNumber(), -1);
    }

    @Override
    public List<Channel> getChannels() {
        return List.of();
    }

    @Override
    public double getIntegrationTime() throws IOException, DeviceException {
        return 0;
    }

    @Override
    public void setIntegrationTime(double time) throws IOException, DeviceException {

        if (!Util.isBetween(time, 1e-5, 6e+1)) {
            throw new DeviceException("Integration time of %.02e s out of range for ThorLabs CCS Spectrometer (10 us <= t <= 60 s).");
        }

        long micro = (long) (time * 1e6);
        long max   = (long) (4095.0 / 1.165);

        long value     = micro;
        int  precision = 0;

        while (value > max) {
            value >>= 1;
            precision++;
        }

        long diff = 0;

    }

    @Override
    public int getAcquisitionTimeout() throws IOException, DeviceException {
        return 0;
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

    public class Channel implements Spectrometer.Channel<ThorLabsCCS> {

        @Override
        public Spectrum getSpectrum() throws IOException, DeviceException, InterruptedException, TimeoutException {
            return null;
        }

        @Override
        public List<Spectrum> getSpectrumSeries(int count) throws IOException, DeviceException, InterruptedException, TimeoutException {
            return List.of();
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
        public String getIDN() throws IOException, DeviceException {
            return "";
        }

        @Override
        public ThorLabsCCS getParentInstrument() {
            return ThorLabsCCS.this;
        }

        @Override
        public String getName() {
            return "";
        }

        @Override
        public void close() throws IOException, DeviceException {

        }

        @Override
        public Address getAddress() {
            return null;
        }

    }

}
