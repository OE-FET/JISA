package jisa.devices.spectrometer;

import jisa.addresses.Address;
import jisa.devices.DeviceException;
import jisa.devices.camera.Camera;
import jisa.devices.camera.frame.Frame;
import jisa.devices.spectrometer.spectrum.Spectrum;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class CameraSpectrometer<F extends Frame, C extends Camera<F>, S extends Spectrograph> implements Spectrometer {

    private final C               camera;
    private final S               spectrograph;
    private       double[]        wavelengths;
    private       List<Converter> converter = new LinkedList<>();

    public CameraSpectrometer(C camera, S spectrograph) {
        this.camera       = camera;
        this.spectrograph = spectrograph;
    }

    public void addChannel(Converter converter) {
        this.converter.add(converter);
    }

    public C getCamera() {
        return camera;
    }

    public S getSpectrograph() {
        return spectrograph;
    }

    @Override
    public List getChannels() {
        return List.of();
    }

    @Override
    public double getIntegrationTime() throws IOException, DeviceException {
        return 0;
    }

    @Override
    public void setIntegrationTime(double time) throws IOException, DeviceException {

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

}
