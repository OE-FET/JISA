package jisa.devices.camera;

import jisa.addresses.Address;
import jisa.addresses.IDAddress;
import jisa.devices.DeviceException;
import jisa.devices.camera.frame.RGBFrame;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static com.github.sarxos.webcam.Webcam.getWebcams;

public class Webcam extends ManagedCamera<RGBFrame> {

    private final com.github.sarxos.webcam.Webcam webcam;

    public Webcam(Address address) throws DeviceException {

        super("Webcam Driver");

        if (!(address instanceof IDAddress)) {
            throw new DeviceException("Address must be of type IDAddress, %s was given.", address.getClass().getSimpleName());
        }

        webcam = getWebcams().stream().filter(w -> w.getName().equalsIgnoreCase(((IDAddress) address).getID())).findFirst().orElseThrow(() -> new DeviceException("No webcam with name \"%s\" found", ((IDAddress) address).getID()));

    }

    @Override
    protected void setupAcquisition(int limit) throws IOException, DeviceException {

        if (limit > 0) {
            webcam.open(false);
        } else {
            webcam.open(true);
        }

    }

    @Override
    protected RGBFrame createFrameBuffer() {

        try {
            return new RGBFrame(new int[getFrameSize()], getFrameWidth(), getFrameHeight(), System.nanoTime());
        } catch (Exception e) {
            return new RGBFrame(new int[0], 0, 0, System.nanoTime());
        }

    }

    @Override
    protected void acquisitionLoop(RGBFrame frameBuffer) throws IOException, DeviceException, InterruptedException, TimeoutException {



    }

    @Override
    protected void cleanupAcquisition() throws IOException, DeviceException {

    }

    @Override
    protected void cancelAcquisition() {

    }

    @Override
    public double getIntegrationTime() throws IOException, DeviceException {
        return 0;
    }

    @Override
    public void setIntegrationTime(double time) throws IOException, DeviceException {

    }

    @Override
    public void setAcquisitionTimeout(int timeout) throws IOException, DeviceException {

    }

    @Override
    public int getAcquisitionTimeout() throws IOException, DeviceException {
        return 0;
    }

    @Override
    public int getFrameWidth() throws IOException, DeviceException {
        return 0;
    }

    @Override
    public void setImageWidth(int width) throws IOException, DeviceException {

    }

    @Override
    public int getImageWidth() throws IOException, DeviceException {
        return 0;
    }

    @Override
    public int getPhysicalFrameWidth() throws IOException, DeviceException {
        return 0;
    }

    @Override
    public int getFrameHeight() throws IOException, DeviceException {
        return 0;
    }

    @Override
    public void setImageHeight(int height) throws IOException, DeviceException {

    }

    @Override
    public int getImageHeight() throws IOException, DeviceException {
        return 0;
    }

    @Override
    public int getPhysicalFrameHeight() throws IOException, DeviceException {
        return 0;
    }

    @Override
    public int getImageOffsetX() throws IOException, DeviceException {
        return 0;
    }

    @Override
    public void setImageOffsetX(int offsetX) throws IOException, DeviceException {

    }

    @Override
    public void setImageCentredX(boolean centredX) throws IOException, DeviceException {

    }

    @Override
    public boolean isImageCentredX() throws IOException, DeviceException {
        return false;
    }

    @Override
    public int getImageOffsetY() throws IOException, DeviceException {
        return 0;
    }

    @Override
    public void setImageOffsetY(int offsetY) throws IOException, DeviceException {

    }

    @Override
    public void setImageCentredY(boolean centredY) throws IOException, DeviceException {

    }

    @Override
    public boolean isImageCentredY() throws IOException, DeviceException {
        return false;
    }

    @Override
    public int getFrameSize() throws IOException, DeviceException {
        return 0;
    }

    @Override
    public int getPhysicalFrameSize() throws IOException, DeviceException {
        return 0;
    }

    @Override
    public int getSensorWidth() throws IOException, DeviceException {
        return 0;
    }

    @Override
    public int getSensorHeight() throws IOException, DeviceException {
        return 0;
    }

    @Override
    public int getBinningX() throws IOException, DeviceException {
        return 0;
    }

    @Override
    public void setBinningX(int x) throws IOException, DeviceException {

    }

    @Override
    public int getBinningY() throws IOException, DeviceException {
        return 0;
    }

    @Override
    public void setBinningY(int y) throws IOException, DeviceException {

    }

    @Override
    public void setBinning(int x, int y) throws IOException, DeviceException {

    }

    @Override
    public ImageMode getImageMode() throws IOException, DeviceException {
        return ImageMode.REGION_OF_INTEREST;
    }

    @Override
    public void setImageMode(ImageMode mode) throws IOException, DeviceException {

        if (!getImageModes().contains(mode)) {
            throw new DeviceException("Invalid ImageMode \"%s\" for Webcam cameras.", mode);
        }

        if (mode == ImageMode.FULL_IMAGE) {
            setImageCentredX(false);
            setImageCentredY(false);
            setImageOffsetX(0);
            setImageOffsetY(0);
            setImageWidth(getSensorWidth());
            setImageHeight(getSensorHeight());
        }

    }

    @Override
    public String getIDN() throws IOException, DeviceException {
        return "";
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
