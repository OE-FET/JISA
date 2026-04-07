package jisa.devices.spectrometer;

import jisa.Util;
import jisa.addresses.Address;
import jisa.devices.DeviceException;
import jisa.devices.ParameterList;
import jisa.devices.camera.Camera;
import jisa.devices.camera.frame.Frame;
import jisa.devices.camera.frame.FrameThread;
import jisa.devices.spectrometer.spectrum.Spectrum;
import jisa.devices.spectrometer.spectrum.SpectrumQueue;
import jisa.maths.Range;
import jisa.maths.fits.Fitting;
import jisa.maths.fits.LinearFit;
import jisa.maths.fits.PolyFit;
import jisa.maths.functions.Function;
import kotlin.ranges.IntRange;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CameraSpectrometer<C extends Camera<F>, F extends Frame<? extends Number, ? extends F>, S extends Spectrograph> implements Spectrometer {

    private final C                                                    camera;
    private final S                                                    spectrograph;
    private final Map<Listener, Camera.Listener>                       listeners            = new HashMap<>();
    private final Map<SpectrumQueue, FrameThread>                      threads              = new HashMap<>();
    private final Map<AcquisitionListener, Camera.AcquisitionListener> acquisitionListeners = new HashMap<>();
    private       Converter<F>                                         converter;
    private       Converter<F>                                         converterCopy;

    public CameraSpectrometer(C camera, S spectrograph) throws IOException, DeviceException {

        this.camera       = camera;
        this.spectrograph = spectrograph;

        setConverterFullVerticalBinning();

    }

    @Override
    public void addInstrumentParameters(Class<?> target, ParameterList parameters) {

        parameters.addAll(camera.getAllParameters());

        if (spectrograph != null) {
            parameters.addAll(spectrograph.getAllParameters());
        }

    }

    public void setConverter(Converter<F> converter) {

        this.converter = f -> {

            synchronized (this) {

                Spectrum s = converter.convert(f);

                s.getAttributes().clear();
                s.getAttributes().putAll(f.getAttributes());

                if (spectrograph != null) {
                    s.getAttributes().putAll(spectrograph.getAllParametersAsMap());
                }

                s.setTimestamp(f.getTimestamp());

                return s;

            }

        };

        this.converterCopy = f -> {

            synchronized (this) {

                Spectrum s = converter.convert(f);

                s.getAttributes().clear();
                s.getAttributes().putAll(f.getAttributes());

                if (spectrograph != null) {
                    s.getAttributes().putAll(spectrograph.getAllParametersAsMap());
                }

                s.setTimestamp(f.getTimestamp());

                return s.copy();

            }

        };

    }

    public void setConverterFullVerticalBinning() {

        AtomicReference<double[]> wavelengths = new AtomicReference<>(new double[0]);
        AtomicReference<double[]> counts      = new AtomicReference<>(new double[0]);
        AtomicReference<Spectrum> spectrum    = new AtomicReference<>(null);

        setConverter(frame -> {

            int count  = frame.getWidth();
            int height = frame.getHeight();

            if (counts.get().length != count) {
                wavelengths.set(Range.linear(0, count - 1).doubleArray());
                counts.set(new double[count]);
                spectrum.set(new Spectrum(wavelengths.get(), counts.get()));
            }

            double[] cts = counts.get();

            for (int x = 0; x < count; x++) {

                cts[x] = 0.0;

                for (int y = 0; y < height; y++) {
                    cts[x] += frame.get(x, y).doubleValue();
                }

            }

            return spectrum.get();

        });

    }

    public void setConverterFullVerticalBinning(Map<Integer, Double> peaks, int fitOrder) {

        if (peaks.size() < Math.max(2, fitOrder)) {
            setConverterFullVerticalBinning();
            return;
        }

        PolyFit fit = Fitting.polyFit(
            peaks.keySet().stream().map(Number::doubleValue).collect(Collectors.toList()),
            peaks.values(),
            fitOrder
        );

        if (fit == null) {
            setConverterFullVerticalBinning();
            return;
        }

        Function                  fitFunc     = fit.getFunction();
        AtomicReference<double[]> wavelengths = new AtomicReference<>(new double[0]);
        AtomicReference<double[]> counts      = new AtomicReference<>(new double[0]);
        AtomicReference<Spectrum> spectrum    = new AtomicReference<>(null);

        setConverter(frame -> {

            int count  = frame.getWidth();
            int height = frame.getHeight();

            if (counts.get().length != count) {
                wavelengths.set(Range.linear(0, count - 1).stream().mapToDouble(fitFunc::value).toArray());
                counts.set(new double[count]);
                spectrum.set(new Spectrum(wavelengths.get(), counts.get()));
            }

            double[] cts = counts.get();

            for (int x = 0; x < count; x++) {

                cts[x] = 0.0;

                for (int y = 0; y < height; y++) {
                    cts[x] += frame.get(x, y).doubleValue();
                }

            }

            return spectrum.get();

        });

    }

    public void setConverter(int startX, int startY, int endX, int endY, int binning, Map<Integer, Double> wavelengths) throws DeviceException {

        if (wavelengths.size() < 2) {
            throw new DeviceException("Need at least two wavelength positions to calibrate spectra.");
        }

        int                order   = wavelengths.size() - 1;
        List<Double>       indices = wavelengths.keySet().stream().map(Number::doubleValue).collect(Collectors.toList());
        Collection<Double> wls     = wavelengths.values();
        PolyFit            wlFit   = Fitting.polyFit(indices, wls, order);

        if (wlFit == null) {
            throw new DeviceException("Cannot fit function to provided wavelength data");
        }

        Function wlFunc = wlFit.getFunction();

        final double theta      = Math.atan2(endY - startY, endX - startX);
        final double orthogonal = theta + Math.PI / 2.0;
        final int    steps      = Math.max(Math.abs(endY - startY), Math.abs(endX - startX)) + 1;
        final double step       = steps > 1 ? Math.sqrt(Math.pow(endY - startY, 2) + Math.pow(endX - startX, 2)) / (steps - 1) : 0.0;

        final int    topLeftX    = (int) Math.round(startX + binning * Math.cos(orthogonal));
        final int    topLeftY    = (int) Math.round(startY + binning * Math.sin(orthogonal));
        final int    bottomLeftX = (int) Math.round(startX - binning * Math.cos(orthogonal));
        final int    bottomLeftY = (int) Math.round(startY - binning * Math.sin(orthogonal));
        final int    binSteps    = Math.max(Math.abs(topLeftX - bottomLeftX), Math.abs(topLeftY - bottomLeftY)) + 1;
        final double binStep     = binSteps > 1 ? Math.sqrt(Math.pow(topLeftX - bottomLeftX, 2) + Math.pow(topLeftY - bottomLeftY, 2)) / (binSteps - 1) : 0.0;

        final int[][][] pixels = new int[steps][binSteps][2];

        for (int i = 0; i < steps; i++) {

            for (int j = 0; j < binSteps; j++) {

                double rp = i * step;
                double ro = j * binStep;

                pixels[i][j][0] = (int) Math.round(bottomLeftX + (rp * Math.cos(theta)) + (ro * Math.cos(orthogonal)));
                pixels[i][j][1] = (int) Math.round(bottomLeftY + (rp * Math.sin(theta)) + (ro * Math.sin(orthogonal)));

            }

        }

        final double[] wl     = IntStream.range(0, steps).mapToDouble(wlFunc::value).toArray();
        final double[] counts = new double[wl.length];
        final Spectrum buffer = new Spectrum(wl, counts);

        setConverter(frame -> {

            for (int i = 0; i < steps; i++) {

                double value = 0.0;

                for (int j = 0; j < binSteps; j++) {
                    value += frame.get(pixels[i][j][0], pixels[i][j][1]).doubleValue();
                }

                counts[i] = value;

            }

            return buffer;

        });

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
    public AcquisitionListener addAcquisitionListener(AcquisitionListener listener) {
        acquisitionListeners.put(listener, camera.addAcquisitionListener(listener::changed));
        return listener;
    }

    @Override
    public void removeAcquisitionListener(AcquisitionListener listener) {
        camera.removeAcquisitionListener(acquisitionListeners.get(listener));
        acquisitionListeners.remove(listener);
    }

    @Override
    public String getIDN() throws IOException, DeviceException {
        return String.format("%s + %s", camera.getIDN(), spectrograph != null ? spectrograph.getName() : "No Spectrograph");
    }

    @Override
    public String getName() {
        return String.format("%s + %s", camera.getName(), spectrograph != null ? spectrograph.getName() : "No Spectrograph");
    }

    @Override
    public void close() throws IOException, DeviceException {

        camera.close();

        if (spectrograph != null) {
            spectrograph.close();
        }

    }

    @Override
    public Address getAddress() {
        return camera.getAddress();
    }

    @Override
    public double getSlitWidth() throws IOException, DeviceException {
        return spectrograph != null ? spectrograph.getSlitWidth() : 0.0;
    }

    @Override
    public double getGratingDensity() throws IOException, DeviceException {
        return spectrograph != null ? spectrograph.getGratingDensity() : 0.0;
    }

    public interface Converter<F extends Frame> {
        Spectrum convert(F frame);
    }

    @Override
    public Spectrum getSpectrum() throws IOException, DeviceException, InterruptedException, TimeoutException {
        return converterCopy.convert(camera.getFrame()).copy();
    }

    @Override
    public List<Spectrum> getSpectrumSeries(int count) throws IOException, DeviceException, InterruptedException, TimeoutException {
        return camera.getFrameSeries(count).stream().map(f -> converterCopy.convert(f).copy()).collect(Collectors.toList());
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
        FrameThread<F> thread        = camera.startFrameThread(f -> spectrumQueue.offer(converterCopy.convert(f)));

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
