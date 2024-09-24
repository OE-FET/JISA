package jisa.devices.camera;

import jisa.Util;
import jisa.addresses.Address;
import jisa.devices.DeviceException;
import jisa.devices.camera.frame.FrameQueue;
import jisa.devices.camera.frame.Mono16BitFrame;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeoutException;

public class FakeCamera implements Camera<Mono16BitFrame> {

    private int     width           = 1024;
    private int     height          = 1024;
    private int     integrationTime = 5;
    private int     timeout;
    private boolean running         = false;
    private Thread  acquireThread;

    private final Random                          random          = new Random();
    private final ListenerManager<Mono16BitFrame> listenerManager = new ListenerManager<>();

    public FakeCamera() {
    }

    public FakeCamera(Address address) {
    }

    protected void generate(short[] data) {

        for (int i = 0; i < data.length; i++) {
            data[i] = (short) random.nextInt(Short.MAX_VALUE);
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
    public Mono16BitFrame getFrame() throws IOException, DeviceException, InterruptedException, TimeoutException {

        if (isAcquiring()) {

            FrameQueue<Mono16BitFrame> frameQueue = openFrameQueue();
            Mono16BitFrame             frame      = frameQueue.nextFrame(timeout);
            closeFrameQueue(frameQueue);

            return frame;

        }

        short[] data = new short[width * height];
        generate(data);

        Thread.sleep(integrationTime);

        return new Mono16BitFrame(data, width, height);

    }

    @Override
    public void setAcquisitionTimeOut(int timeout) throws IOException, DeviceException {
        this.timeout = timeout;
    }

    @Override
    public int getAcquisitionTimeOut() throws IOException, DeviceException {
        return timeout;
    }

    @Override
    public void startAcquisition() throws IOException, DeviceException {

        acquireThread = new Thread(() -> {

            short[]        data  = new short[width * height];
            Mono16BitFrame frame = new Mono16BitFrame(data, width, height);

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
    public List<Mono16BitFrame> getFrameSeries(int count) throws IOException, DeviceException, InterruptedException, TimeoutException {

        if (isAcquiring()) {

            List<Mono16BitFrame>       frames     = new ArrayList<>(count);
            FrameQueue<Mono16BitFrame> frameQueue = openFrameQueue();

            for (int i = 0; i < count; i++) {
                frames.add(frameQueue.nextFrame(timeout));
            }

            return frames;

        }

        List<Mono16BitFrame> series = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            series.add(getFrame());
        }

        return series;

    }

    @Override
    public Listener<Mono16BitFrame> addFrameListener(Listener<Mono16BitFrame> listener) {
        listenerManager.addListener(listener);
        return listener;
    }

    @Override
    public void removeFrameListener(Listener<Mono16BitFrame> listener) {
        listenerManager.removeListener(listener);
    }

    @Override
    public FrameQueue<Mono16BitFrame> openFrameQueue() {

        FrameQueue<Mono16BitFrame> queue = new FrameQueue<>();
        listenerManager.addQueue(queue);
        return queue;

    }

    @Override
    public void closeFrameQueue(FrameQueue<Mono16BitFrame> queue) {
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
}
