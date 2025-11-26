package jisa.devices.spectrometer;

import jisa.addresses.Address;
import jisa.devices.DeviceException;
import jisa.devices.camera.Camera;
import jisa.devices.camera.frame.Frame;
import jisa.devices.camera.frame.FrameThread;
import jisa.devices.spectrometer.spectrum.Spectrum;
import jisa.devices.spectrometer.spectrum.SpectrumQueue;
import jisa.maths.Range;
import jisa.maths.fits.Fitting;
import jisa.maths.fits.LinearFit;
import jisa.maths.functions.Function;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class CameraSpectrometer<C extends Camera<F>, F extends Frame<? extends Number, ? extends F>, S extends Spectrograph> implements Spectrometer {

    private final C                               camera;
    private final S                               spectrograph;
    private final Map<Listener, Camera.Listener>  listeners = new HashMap<>();
    private final Map<SpectrumQueue, FrameThread> threads   = new HashMap<>();
    private       Converter<F>                    converter;

    public CameraSpectrometer(C camera, S spectrograph) throws IOException, DeviceException {
        this.camera       = camera;
        this.spectrograph = spectrograph;
        setConverter(0, getCamera().getFrameHeight()/2, camera.getFrameWidth() - 1, getCamera().getFrameHeight()/2, 200.0, 800.0);
    }

    public void setConverter(Converter<F> converter) {
        this.converter = converter;
    }

    public void setConverter(int startX, int startY, int endX, int endY, double startWL, double endWL) throws DeviceException {

        int       count       = endX - startX + 1;
        double[]  wavelengths = Range.linear(startWL, endWL, count).doubleArray();
        double[]  counts      = new double[count];
        LinearFit fit         = Fitting.linearFit(List.of((double) startX, (double) endX), List.of((double) startY, (double) endY));
        Spectrum  buffer      = new Spectrum(wavelengths, counts);

        if (fit == null) {
            throw new DeviceException("Cannot fit line to specified points.");
        }

        Function fitFunc = fit.getFunction();

        converter = frame -> {

            for (int x = startX; x <= endX; x++) {
                counts[x - startX] = frame.get(x, (int) fitFunc.value(x)).doubleValue();
            }

            return buffer;

        };

    }

    public C getCamera() {
        return camera;
    }

    public S getSpectrograph() {
        return spectrograph;
    }

    @Override
    public double getIntegrationTime() throws IOException, DeviceException {
        return camera.getIntegrationTime();
    }

    @Override
    public void setIntegrationTime(double time) throws IOException, DeviceException {
        camera.setIntegrationTime(time);
    }

    @Override
    public int getAcquisitionTimeout() throws IOException, DeviceException {
        return camera.getAcquisitionTimeout();
    }

    @Override
    public void setAcquisitionTimeout(int timeout) throws IOException, DeviceException {
        camera.setAcquisitionTimeout(timeout);
    }

    @Override
    public void startAcquisition() throws IOException, DeviceException {
        camera.startAcquisition();
    }

    @Override
    public void stopAcquisition() throws IOException, DeviceException {
        camera.stopAcquisition();
    }

    @Override
    public boolean isAcquiring() throws IOException, DeviceException {
        return camera.isAcquiring();
    }

    @Override
    public String getIDN() throws IOException, DeviceException {
        return String.format("%s + %s", camera.getIDN(), spectrograph.getIDN());
    }

    @Override
    public String getName() {
        return String.format("%s + %s", camera.getName(), spectrograph.getName());
    }

    @Override
    public void close() throws IOException, DeviceException {
        camera.close();
        spectrograph.close();
    }

    @Override
    public Address getAddress() {
        return camera.getAddress();
    }

    @Override
    public double getSlitWidth() throws IOException, DeviceException {
        return spectrograph.getSlitWidth();
    }

    @Override
    public double getGratingDensity() throws IOException, DeviceException {
        return spectrograph.getGratingDensity();
    }

    public interface Converter<F extends Frame> {
        Spectrum convert(F frame);
    }

    @Override
    public Spectrum getSpectrum() throws IOException, DeviceException, InterruptedException, TimeoutException {
        return converter.convert(camera.getFrame());
    }

    @Override
    public List<Spectrum> getSpectrumSeries(int count) throws IOException, DeviceException, InterruptedException, TimeoutException {
        return camera.getFrameSeries(count).stream().map(converter::convert).collect(Collectors.toList());
    }

    @Override
    public Listener addSpectrumListener(Listener listener) {

        Camera.Listener<F> cameraListener = camera.addFrameListener(f -> listener.newSpectrum(converter.convert(f)));
        listeners.put(listener, cameraListener);
        return listener;

    }

    @Override
    public void removeSpectrumListener(Listener listener) {

        if (listeners.containsKey(listener)) {
            camera.removeFrameListener(listeners.get(listener));
            listeners.remove(listener);
        }

    }

    @Override
    public SpectrumQueue openSpectrumQueue(int limit) {

        SpectrumQueue  spectrumQueue = new SpectrumQueue(this, limit);
        FrameThread<F> thread        = camera.startFrameThread(f -> spectrumQueue.offer(converter.convert(f)));

        threads.put(spectrumQueue, thread);

        return spectrumQueue;

    }

    @Override
    public void closeSpectrumQueue(SpectrumQueue queue) {

        if (threads.containsKey(queue)) {
            threads.get(queue).stop();
            threads.remove(queue);
        }

    }

}
