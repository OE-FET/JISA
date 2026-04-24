package jisa.devices.spectrometer;

import jisa.addresses.Address;
import jisa.devices.DeviceException;
import jisa.devices.ParameterList;
import jisa.devices.camera.Camera;
import jisa.devices.camera.frame.Frame;
import jisa.devices.camera.frame.FrameThread;
import jisa.devices.spectrometer.feature.Shuttered;
import jisa.devices.spectrometer.spectrum.Spectrum;
import jisa.devices.spectrometer.spectrum.SpectrumQueue;
import jisa.maths.Range;
import jisa.maths.fits.Fitting;
import jisa.maths.fits.PolyFit;
import jisa.maths.functions.Function;
import jisa.results.Column;
import jisa.results.ResultList;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CameraSpectrometer<C extends Camera<F>, F extends Frame<? extends Number, ? extends F, ?>, S extends Spectrograph> implements Spectrometer {

    private final C            camera;
    private final S            spectrograph;
    private       Converter<F> converter;
    private       Converter<F> converterCopy;

    private final Map<SpectrumQueue, FrameThread>                      threads              = new HashMap<>();
    private final Map<AcquisitionListener, Camera.AcquisitionListener> acquisitionListeners = new HashMap<>();
    private final ListenerManager                                      listenerManager      = new ListenerManager();
    private final Map<String, Object>                                  frameAttributes      = new LinkedHashMap<>();
    private final AcquisitionListener                                  shutterConnect;
    private       boolean                                              attributesChanged    = true;
    private       boolean                                              shutterConnected     = false;

    public CameraSpectrometer(C camera, S spectrograph) {

        this.camera       = camera;
        this.spectrograph = spectrograph;

        setConverterFullVerticalBinning();

        camera.addFrameListener(frame -> listenerManager.trigger(converter.convert(frame)));
        camera.addAcquisitionListener((count, acquiring) -> {
            if (acquiring) {
                updateAttributes();
            }
        });

        if (spectrograph instanceof Shuttered) {

            shutterConnect = (c, a) -> {

                try {
                    ((Shuttered) spectrograph).setShutterMode(a ? Shuttered.Mode.OPEN : Shuttered.Mode.CLOSED);
                } catch (Throwable e) {
                    e.printStackTrace();
                }

            };

        } else {
            shutterConnect = (c, a) -> {
            };
        }

    }

    public CameraSpectrometer(C camera) {
        this(camera, null);
    }

    public void updateAttributes() {

        frameAttributes.clear();
        frameAttributes.putAll(camera.getAllParametersAsMap());

        if (spectrograph != null) {

            try {
                frameAttributes.putAll(spectrograph.getAllParametersAsMap());
            } catch (Throwable ignored) {
            }

        }

        attributesChanged = true;

    }

    public void setSoftwareShutterControlEnabled(boolean enabled) {

        if (enabled) {
            addAcquisitionListener(shutterConnect);
        } else {
            removeAcquisitionListener(shutterConnect);
        }

        shutterConnected = enabled;

    }

    public boolean isSoftwareShutterControlEnabled() {
        return shutterConnected;
    }

    @Override
    public void addInstrumentParameters(Class<?> target, ParameterList parameters) {

        parameters.addAll(camera.getAllParameters());

        if (spectrograph != null) {
            parameters.addAll(spectrograph.getAllParameters());
        }

        Column<Integer> IX     = Column.ofIntegers("Channel Index");
        Column<Double>  WL     = Column.ofDoubles("Wavelength", "m");
        ResultList      values = new ResultList(IX, WL);

        AtomicReference<ResultList> wavelengths  = new AtomicReference<>(values);
        AtomicReference<Integer>    fittingOrder = new AtomicReference<>(1);

        parameters.addValue(
                "Frame Conversion",
                "Wavelengths",
                wavelengths::get,
                values,
                wl -> {

                    wavelengths.set(wl);

                    setConverterFullVerticalBinning(
                            wavelengths.get().stream().collect(Collectors.toMap(r -> r.get(IX), r -> r.get(WL))),
                            fittingOrder.get()
                    );

                }

        );

        parameters.addValue(
                "Frame Conversion",
                "Fitting Order",
                fittingOrder::get,
                1,
                fo -> {

                    fittingOrder.set(fo);

                    setConverterFullVerticalBinning(
                            wavelengths.get().stream().collect(Collectors.toMap(r -> r.get(IX), r -> r.get(WL))),
                            fittingOrder.get()
                    );

                }

        );

        if (spectrograph instanceof Shuttered) {
            parameters.addValue("Workarounds", "Software Shutter Control", this::isSoftwareShutterControlEnabled, false, this::setSoftwareShutterControlEnabled);
        }

    }

    public void setConverter(Converter<F> converter) {

        this.converter = f -> {

            synchronized (this) {

                Spectrum s = converter.convert(f);

                if (s.getAttributes().isEmpty() || attributesChanged) {
                    s.getAttributes().putAll(frameAttributes);
                    attributesChanged = false;
                }

                s.setTimestamp(f.getTimestamp());

                return s;

            }

        };

        this.converterCopy = f -> {

            synchronized (this) {

                Spectrum s = converter.convert(f);

                if (s.getAttributes().isEmpty() || attributesChanged) {
                    s.getAttributes().putAll(frameAttributes);
                    attributesChanged = false;
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

    public void setConverterFullVerticalBinning(Map<? extends Number, ? extends Number> peaks, int fitOrder) {

        if (peaks.size() < Math.max(2, fitOrder)) {
            setConverterFullVerticalBinning();
            return;
        }

        PolyFit fit = Fitting.polyFit(
                peaks.keySet().stream().map(Number::doubleValue).collect(Collectors.toList()),
                peaks.values().stream().map(Number::doubleValue).collect(Collectors.toList()),
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

    public void setConverter(int startX, int startY, int endX, int endY, int binning, Map<? extends Number, ? extends Number> wavelengths) throws DeviceException {
        setConverter(startX, startY, endX, endY, binning, wavelengths, wavelengths.size() - 1);
    }

    public void setConverter(int startX, int startY, int endX, int endY, int binning, Map<? extends Number, ? extends Number> wavelengths, int order) throws DeviceException {

        if (wavelengths.size() < 2) {
            throw new DeviceException("Need at least two wavelength positions to calibrate spectra.");
        }

        PolyFit wlFit = Fitting.polyFit(
                wavelengths.keySet().stream().map(Number::doubleValue).collect(Collectors.toList()),
                wavelengths.values().stream().map(Number::doubleValue).collect(Collectors.toList()),
                order
        );

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
    public double getAcquisitionRate() throws IOException, DeviceException {
        return camera.getAcquisitionRate();
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
    public List<Component> getComponents() {

        if (spectrograph != null) {
            return spectrograph.getComponents();
        } else {
            return Collections.emptyList();
        }

    }

    public interface Converter<F extends Frame> {
        Spectrum convert(F frame);
    }

    @Override
    public Spectrum getSpectrum() throws IOException, DeviceException, InterruptedException, TimeoutException {
        updateAttributes();
        return converterCopy.convert(camera.getFrame());
    }

    @Override
    public List<Spectrum> getSpectrumSeries(int count) throws IOException, DeviceException, InterruptedException, TimeoutException {
        updateAttributes();
        return camera.getFrameSeries(count).stream().map(f -> converterCopy.convert(f).copy()).collect(Collectors.toList());
    }

    @Override
    public Listener addSpectrumListener(Listener listener) {
        listenerManager.addListener(listener);
        return listener;
    }

    @Override
    public void removeSpectrumListener(Listener listener) {
        listenerManager.removeListener(listener);
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
