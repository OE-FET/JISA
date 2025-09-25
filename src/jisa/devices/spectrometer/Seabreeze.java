package jisa.devices.spectrometer;

import jisa.Util;
import jisa.addresses.Address;
import jisa.addresses.IDAddress;
import jisa.devices.DeviceException;
import jisa.devices.features.TemperatureControlled;
import jisa.devices.spectrometer.feature.Shutter;
import jisa.devices.spectrometer.nat.SeabreezeLibrary;
import jisa.devices.spectrometer.spectrum.Spectrum;
import jisa.devices.spectrometer.spectrum.SpectrumQueue;
import jisa.visa.NativeDevice;

import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class Seabreeze extends NativeDevice implements Spectrometer, TemperatureControlled, Shutter {

    private final SeabreezeLibrary lib;
    private final int              index;
    private final ListenerManager  manager = new ListenerManager();

    private long    intTime           = 20000;
    private boolean acquiring         = false;
    private Thread  acquisitionThread = null;
    private boolean tec               = false;
    private double  tecTarget         = 273.15;
    private int     timeout           = 10000;
    private boolean shutter           = false;

    public Seabreeze(int index) throws DeviceException {

        super("Ocean Optics Seabreeze Spectrometer");

        lib = findLibrary(SeabreezeLibrary.class, "seabreeze");

        IntBuffer error = IntBuffer.allocate(1);

        lib.seabreeze_open_spectrometer(index, error);
        parseError(error);

        this.index = index;

        lib.seabreeze_set_integration_time_microsec(index, error.rewind(), intTime);
        parseError(error);

    }

    public Seabreeze(Address address) throws DeviceException {
        this(Integer.parseInt(((IDAddress) address).getID()));
    }

    protected void parseError(IntBuffer buffer) throws DeviceException {

        int code = buffer.get(0);

        if (code != 0) {

            CharBuffer charBuffer = CharBuffer.allocate(1024);
            lib.seabreeze_get_error_string(code, charBuffer, 1024);
            throw new DeviceException("Error encountered with Seabreeze device: %s (%d)", buffer.toString(), 1024);

        }

    }

    @Override
    public double getIntegrationTime() throws IOException, DeviceException {
        return ((double) intTime / 1e6);
    }

    @Override
    public void setIntegrationTime(double time) throws IOException, DeviceException {

        long micro = (long) (time * 1e6);

        IntBuffer error = IntBuffer.allocate(1);
        lib.seabreeze_set_integration_time_microsec(index, error, micro);

        parseError(error);

        intTime = micro;

    }

    @Override
    public int getAcquisitionTimeout() throws IOException, DeviceException {
        return timeout;
    }

    @Override
    public void setAcquisitionTimeout(int timeout) throws IOException, DeviceException {
        this.timeout = timeout;
    }

    @Override
    public synchronized void startAcquisition() throws IOException, DeviceException {

        if (isAcquiring()) {
            return;
        }

        acquisitionThread = new Thread(() -> {

            IntBuffer    error    = IntBuffer.allocate(1);
            int          capacity = lib.seabreeze_get_formatted_spectrum_length(index, error);
            DoubleBuffer wlBuffer = DoubleBuffer.allocate(capacity);
            DoubleBuffer ctBuffer = DoubleBuffer.allocate(capacity);

            lib.seabreeze_get_wavelengths(index, error, wlBuffer, capacity);

            Spectrum buffer = new Spectrum(wlBuffer.array(), ctBuffer.array());

            while (acquiring) {

                lib.seabreeze_get_formatted_spectrum(index, error.rewind(), ctBuffer.rewind(), capacity);
                buffer.setTimestamp(System.nanoTime());
                manager.trigger(buffer);

                if (Thread.interrupted()) {
                    break;
                }

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
        } catch (InterruptedException ignored) { }

    }

    @Override
    public synchronized boolean isAcquiring() throws IOException, DeviceException {
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
        return "Ocean Optics Seabreeze Driver Spectrometer";
    }

    @Override
    public String getName() {
        return "Ocean Optics Seabreeze Driver Spectrometer";
    }

    @Override
    public void close() throws IOException, DeviceException {
        IntBuffer error = IntBuffer.allocate(1);
        lib.seabreeze_close_spectrometer(index, error);
        parseError(error);
    }

    @Override
    public Address getAddress() {
        return new IDAddress(String.format("%d", index));
    }

    @Override
    public synchronized void setTemperatureControlEnabled(boolean enabled) throws IOException, DeviceException {
        IntBuffer error = IntBuffer.allocate(1);
        lib.seabreeze_set_tec_enable(index, error, (byte) (enabled ? 1 : 0));
        parseError(error);
        tec = enabled;
    }

    @Override
    public synchronized boolean isTemperatureControlEnabled() throws IOException, DeviceException {
        return tec;
    }

    @Override
    public synchronized void setTemperatureControlTarget(double targetTemperature) throws IOException, DeviceException {
        IntBuffer error = IntBuffer.allocate(1);
        lib.seabreeze_set_tec_temperature(index, error, targetTemperature - 273.15);
        parseError(error);
        tecTarget = targetTemperature;
    }

    @Override
    public synchronized double getTemperatureControlTarget() throws IOException, DeviceException {
        return tecTarget;
    }

    @Override
    public double getControlledTemperature() throws IOException, DeviceException {

        IntBuffer error = IntBuffer.allocate(1);
        double    value = lib.seabreeze_read_tec_temperature(index, error);

        parseError(error);

        return value + 273.15;

    }

    @Override
    public boolean isTemperatureControlStable() throws IOException, DeviceException {
        return Util.isBetween(getControlledTemperature(), tecTarget - 1, tecTarget + 1);
    }

    @Override
    public Spectrum getSpectrum() throws IOException, DeviceException, InterruptedException, TimeoutException {

        // If we're already continuously acquiring, then just wait for the next frame to come in
        if (isAcquiring()) {

            SpectrumQueue queue = openSpectrumQueue(1);

            try {
                return queue.nextSpectrum(timeout);
            } finally {
                queue.close();
                queue.clear();
            }

        }

        IntBuffer    error       = IntBuffer.allocate(1);
        int          capacity    = lib.seabreeze_get_formatted_spectrum_length(index, error.rewind());
        DoubleBuffer buffer      = DoubleBuffer.allocate(capacity);
        DoubleBuffer wavelengths = DoubleBuffer.allocate(capacity);

        lib.seabreeze_get_formatted_spectrum(index, error.rewind(), buffer, capacity);
        parseError(error);

        lib.seabreeze_get_wavelengths(index, error.rewind(), wavelengths, capacity);
        parseError(error);

        return new Spectrum(wavelengths.array(), buffer.array(), System.nanoTime());

    }

    @Override
    public List<Spectrum> getSpectrumSeries(int count) throws IOException, DeviceException, InterruptedException, TimeoutException {

        List<Spectrum> series = new ArrayList<>(count);

        if (isAcquiring()) {

            SpectrumQueue queue = openSpectrumQueue(count);

            try {

                for (int i = 0; i < count; i++) {
                    series.add(queue.nextSpectrum(timeout));
                }

                return series;

            } finally {
                queue.close();
                queue.clear();
            }

        }

        IntBuffer error    = IntBuffer.allocate(1);
        int       capacity = lib.seabreeze_get_formatted_spectrum_length(index, error.rewind());
        parseError(error);

        DoubleBuffer buffer      = DoubleBuffer.allocate(capacity);
        DoubleBuffer wavelengths = DoubleBuffer.allocate(capacity);

        lib.seabreeze_get_wavelengths(index, error.rewind(), wavelengths, capacity);
        parseError(error);

        double[] wl = wavelengths.array();

        for (int i = 0; i < count; i++) {

            lib.seabreeze_get_formatted_spectrum(index, error.rewind(), buffer.rewind().clear(), capacity);
            parseError(error);
            series.add(new Spectrum(wl.clone(), buffer.array().clone(), System.nanoTime()));

        }

        return series;

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

    @Override
    public synchronized void openShutter() throws IOException, DeviceException {

        IntBuffer error = IntBuffer.allocate(1);
        lib.seabreeze_set_shutter_open(index, error, (byte) 1);

        parseError(error);

        shutter = true;

    }

    @Override
    public synchronized void closeShutter() throws IOException, DeviceException {

        IntBuffer error = IntBuffer.allocate(1);
        lib.seabreeze_set_shutter_open(index, error, (byte) 0);

        parseError(error);

        shutter = false;

    }

    @Override
    public synchronized boolean isShutterOpen() throws IOException, DeviceException {
        return shutter;
    }
}
