package jisa.devices.camera;

import jisa.addresses.Address;
import jisa.addresses.IDAddress;
import jisa.devices.DeviceException;
import jisa.devices.interfaces.Camera;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Webcam implements Camera {

    private final com.github.sarxos.webcam.Webcam webcam;
    private final List<Mode>                      modes;

    public Webcam() throws IOException, DeviceException {
        this("");
    }

    public Webcam(String name) throws IOException, DeviceException {
        this(new IDAddress(name));
    }

    public Webcam(Address address) throws IOException, DeviceException {

        IDAddress id = address.toIDAddress();

        if (id == null) {
            throw new DeviceException("Webcam driver requires the name of the webcam to be given as an IDAddress object.");
        }

        if (id.getID().trim().isBlank()) {

            webcam = com.github.sarxos.webcam.Webcam.getDefault();

        } else {

            webcam = com.github.sarxos.webcam.Webcam.getWebcams()
                                                    .stream()
                                                    .filter(w -> w.getName().toLowerCase().trim().equals(id.getID().toLowerCase().trim()))
                                                    .findFirst()
                                                    .orElse(null);

        }

        if (webcam == null) {
            throw new IOException("No webcam found with that identifier.");
        }

        webcam.setViewSize(Arrays.stream(webcam.getViewSizes()).max(Comparator.comparingInt(v -> v.width * v.height)).orElse(webcam.getViewSize()));
        modes = Arrays.stream(webcam.getViewSizes()).map(v -> new Camera.Mode(v.width, v.height)).collect(Collectors.toUnmodifiableList());

    }

    @Override
    public void turnOn() throws IOException, DeviceException {
        webcam.open();
    }

    @Override
    public void turnOff() throws IOException, DeviceException {
        webcam.close();
    }

    @Override
    public boolean isOn() throws IOException, DeviceException {
        return webcam.isOpen();
    }

    @Override
    public Mode getMode() throws IOException, DeviceException {

        Dimension dimension = webcam.getViewSize();

        return modes.stream()
                    .filter(m -> m.getXResolution() == dimension.width && m.getYResolution() == dimension.height)
                    .findFirst()
                    .orElse(null);

    }

    @Override
    public void setMode(Mode mode) throws IOException, DeviceException {

        webcam.setViewSize(Arrays.stream(webcam.getViewSizes()).filter(dimension -> mode.getXResolution() == dimension.width && mode.getYResolution() == dimension.height)
                                 .findFirst()
                                 .orElse(webcam.getViewSize()));

    }

    @Override
    public double getFrameRate() {
        return webcam.getFPS();
    }

    @Override
    public BufferedImage getBufferedImage() {
        return webcam.getImage();
    }

    @Override
    public List<Mode> getModes() throws IOException, DeviceException {
        return modes;
    }

    @Override
    public String getIDN() throws IOException, DeviceException {
        return "Webcam: " + webcam.getName();
    }

    @Override
    public void close() throws IOException, DeviceException {
        turnOff();
    }

    @Override
    public Address getAddress() {
        return new IDAddress(webcam.getName());
    }

}
