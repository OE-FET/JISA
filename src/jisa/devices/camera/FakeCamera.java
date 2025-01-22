package jisa.devices.camera;

import jisa.Util;
import jisa.addresses.Address;
import jisa.devices.DeviceException;
import jisa.devices.camera.features.MultiTrack;
import jisa.devices.camera.frame.FrameQueue;
import jisa.devices.camera.frame.U16Frame;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeoutException;

public class FakeCamera implements Camera<U16Frame>, MultiTrack {

    private int     width           = 1024;
    private int     height          = 1024;
    private int     integrationTime = 5;
    private int     timeout;
    private boolean running         = false;
    private Thread  acquireThread;

    private final Random                    random          = new Random();
    private final ListenerManager<U16Frame> listenerManager = new ListenerManager<>();

    public FakeCamera() {
    }

    public FakeCamera(Address address) {
    }

    protected void generate(short[] data) {

        for (int i = 0; i < data.length; i++) {
            data[i] = (short) (0.3 * random.nextInt(Short.MAX_VALUE) + 0.7 * Short.MAX_VALUE * ((double) i / data.length));
        }

    }

    @Override
    public double getIntegrationTime() throws IOException, DeviceException {
        return integrationTime / 1e3;
    }

    @Override
    public void setIntegrationTime(double time) throws IOException, DeviceException {
        integrationTime = (int) (1e3 * time);
    }

    @Override
    public U16Frame getFrame() throws IOException, DeviceException, InterruptedException, TimeoutException {

        if (isAcquiring()) {

            FrameQueue<U16Frame> frameQueue = openFrameQueue();
            U16Frame             frame      = frameQueue.nextFrame(timeout);

            frameQueue.close();

            return frame;

        }

        short[] data = new short[width * height];
        generate(data);

        Thread.sleep(integrationTime);

        return new U16Frame(data, width, height);

    }

    @Override
    public void setAcquisitionTimeout(int timeout) throws IOException, DeviceException {
        this.timeout = timeout;
    }

    @Override
    public int getAcquisitionTimeout() throws IOException, DeviceException {
        return timeout;
    }

    @Override
    public void startAcquisition() throws IOException, DeviceException {

        acquireThread = new Thread(() -> {

            short[]  data  = new short[width * height];
            U16Frame frame = new U16Frame(data, width, height);

            while (running) {

                Util.sleep(integrationTime);
                generate(data);
                frame.setTimestamp(System.nanoTime());
                listenerManager.trigger(frame);

            }

        });

        running = true;

        acquireThread.start();

    }

    @Override
    public void stopAcquisition() throws IOException, DeviceException {

        running = false;
        acquireThread.interrupt();

        try {
            acquireThread.join();
        } catch (InterruptedException ignored) { }

    }

    @Override
    public boolean isAcquiring() throws IOException, DeviceException {
        return running;
    }

    @Override
    public List<U16Frame> getFrameSeries(int count) throws IOException, DeviceException, InterruptedException, TimeoutException {

        if (isAcquiring()) {

            List<U16Frame>       frames     = new ArrayList<>(count);
            FrameQueue<U16Frame> frameQueue = openFrameQueue();

            for (int i = 0; i < count; i++) {
                frames.add(frameQueue.nextFrame(timeout));
            }

            frameQueue.close();

            return frames;

        }

        List<U16Frame> series = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            series.add(getFrame());
        }

        return series;

    }

    @Override
    public Listener<U16Frame> addFrameListener(Listener<U16Frame> listener) {
        listenerManager.addListener(listener);
        return listener;
    }

    @Override
    public void removeFrameListener(Listener<U16Frame> listener) {
        listenerManager.removeListener(listener);
    }

    @Override
    public FrameQueue<U16Frame> openFrameQueue() {

        FrameQueue<U16Frame> queue = new FrameQueue<>(this);
        listenerManager.addQueue(queue);
        return queue;

    }

    @Override
    public void closeFrameQueue(FrameQueue<U16Frame> queue) {
        listenerManager.removeQueue(queue);
        queue.close();
    }

    @Override
    public int getFrameWidth() throws IOException, DeviceException {
        return width;
    }

    @Override
    public void setFrameWidth(int width) throws IOException, DeviceException {
        this.width = width;
    }

    @Override
    public int getPhysicalFrameWidth() throws IOException, DeviceException {
        return width;
    }

    @Override
    public int getFrameHeight() throws IOException, DeviceException {
        return height;
    }

    @Override
    public void setFrameHeight(int height) throws IOException, DeviceException {
        this.height = height;
    }

    @Override
    public int getPhysicalFrameHeight() throws IOException, DeviceException {
        return height;
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
        return width;
    }

    @Override
    public int getSensorHeight() throws IOException, DeviceException {
        return height;
    }

    @Override
    public int getBinningX() throws IOException, DeviceException {
        return 1;
    }

    @Override
    public void setBinningX(int x) throws IOException, DeviceException {

    }

    @Override
    public int getBinningY() throws IOException, DeviceException {
        return 1;
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
    public int getFrameBinning() throws IOException, DeviceException {
        return 1;
    }

    @Override
    public void setFrameBinning(int binning) throws IOException, DeviceException {

    }

    @Override
    public String getIDN() throws IOException, DeviceException {
        return "Fake Camera";
    }

    @Override
    public String getName() {
        return "Fake Camera";
    }

    @Override
    public void close() throws IOException, DeviceException {

    }

    @Override
    public Address getAddress() {
        return null;
    }

    @Override
    public void setMultiTrackEnabled(boolean enabled) throws IOException, DeviceException {

    }

    @Override
    public boolean isMultiTrackEnabled() throws IOException, DeviceException {
        return false;
    }

    @Override
    public void setMultiTracks(Collection<Track> tracks) throws IOException, DeviceException {

    }

    @Override
    public List<Track> getMultiTracks() throws IOException, DeviceException {
        return List.of();
    }
}
