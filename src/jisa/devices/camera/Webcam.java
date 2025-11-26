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
    public void setFrameWidth(int width) throws IOException, DeviceException {

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
    public void setFrameHeight(int height) throws IOException, DeviceException {

    }

    @Override
    public int getPhysicalFrameHeight() throws IOException, DeviceException {
        return 0;
    }

    @Override
    public int getFrameOffsetX() throws IOException, DeviceException {
        return 0;
    }

    @Override
    public void setFrameOffsetX(int offsetX) throws IOException, DeviceException {

    }

    @Override
    public void setFrameCentredX(boolean centredX) throws IOException, DeviceException {

    }

    @Override
    public boolean isFrameCentredX() throws IOException, DeviceException {
        return false;
    }

    @Override
    public int getFrameOffsetY() throws IOException, DeviceException {
        return 0;
    }

    @Override
    public void setFrameOffsetY(int offsetY) throws IOException, DeviceException {

    }

    @Override
    public void setFrameCentredY(boolean centredY) throws IOException, DeviceException {

    }

    @Override
    public boolean isFrameCentredY() throws IOException, DeviceException {
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
    public boolean isTimestampEnabled() throws IOException, DeviceException {
        return false;
    }

    @Override
    public void setTimestampEnabled(boolean timestamping) throws IOException, DeviceException {

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
