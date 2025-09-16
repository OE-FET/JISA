package jisa.devices.camera;

import com.sun.jna.NativeLong;
import com.sun.jna.ptr.NativeLongByReference;
import jisa.Util;
import jisa.addresses.Address;
import jisa.devices.DeviceException;
import jisa.devices.camera.feature.MultiTrack;
import jisa.devices.camera.frame.FrameQueue;
import jisa.devices.camera.frame.U16Frame;
import jisa.devices.camera.nat.ATMCD32D;
import jisa.devices.features.TemperatureControlled;
import jisa.visa.NativeDevice;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static jisa.devices.camera.nat.ATMCD32D.DRV_SUCCESS;

public class Andor2 extends NativeDevice implements Camera<U16Frame>, TemperatureControlled, MultiTrack {

    private final ATMCD32D                  sdk;
    private final int                       index;
    private final NativeLong                pointer;
    private final ListenerManager<U16Frame> listenerManager = new ListenerManager<>();

    private int timeout = 10000;

    private static void handle(int result, String method) throws DeviceException {

        if (result != DRV_SUCCESS) {
            throw new DeviceException("Error encountered in method call \"%s\": %d", method, result);
        }

    }

    public Andor2(int index) throws DeviceException {

        super("Andor SDK2 Camera");

        this.sdk   = findLibrary(ATMCD32D.class, "atmcd23d");
        this.index = index;

        IntBuffer intBuffer = IntBuffer.allocate(1);

        handle(sdk.GetNumberDevices(intBuffer), "GetNumberDevices");

        int number = intBuffer.get(0);

        if (number < 1) {
            throw new DeviceException("No connected devices found!");
        } else if (index >= number || index < 0) {
            throw new DeviceException("Invalid device index!");
        } else {

            handle(sdk.SelectDevice(number), "SelectDevice");

            NativeLongByReference ref = new NativeLongByReference();

            handle(sdk.GetCameraPointer(new NativeLong(index, true), ref), "GetCameraPointer");

            pointer = ref.getValue();

        }

    }

    @Override
    public void setTemperatureControlEnabled(boolean enabled) throws IOException, DeviceException {

    }

    @Override
    public boolean isTemperatureControlEnabled() throws IOException, DeviceException {
        return false;
    }

    @Override
    public void setTemperatureControlTarget(double targetTemperature) throws IOException, DeviceException {

    }

    @Override
    public double getTemperatureControlTarget() throws IOException, DeviceException {
        return 0;
    }

    @Override
    public double getControlledTemperature() throws IOException, DeviceException {
        return 0;
    }

    @Override
    public boolean isTemperatureControlStable() throws IOException, DeviceException {
        return false;
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

    public interface CameraAction {
        void run(ATMCD32D sdk) throws IOException, DeviceException;
    }

    public interface CameraActionInterruptable {
        void run(ATMCD32D sdk) throws IOException, DeviceException, InterruptedException, TimeoutException;
    }

    protected void withCameraSelected(CameraAction toRun) throws IOException, DeviceException {

        synchronized (sdk) {
            handle(sdk.SelectDevice(index), "SelectDevice");
            toRun.run(sdk);
        }

    }

    protected void withCameraSelectedTO(CameraActionInterruptable toRun) throws IOException, DeviceException, InterruptedException, TimeoutException {

        synchronized (sdk) {
            handle(sdk.SelectDevice(index), "SelectDevice");
            toRun.run(sdk);
        }

    }

    @Override
    public double getIntegrationTime() throws IOException, DeviceException {

        FloatBuffer exposure   = FloatBuffer.allocate(1);
        FloatBuffer accumulate = FloatBuffer.allocate(1);
        FloatBuffer kinetic    = FloatBuffer.allocate(1);

        withCameraSelected(sdk -> {

            handle(
                sdk.GetAcquisitionTimings(exposure, accumulate, kinetic),
                "GetAcquisitionTimings"
            );

        });

        return exposure.get(0);

    }

    @Override
    public void setIntegrationTime(double time) throws IOException, DeviceException {

        if (!Util.isBetween(time, Float.MIN_VALUE, Float.MAX_VALUE)) {
            throw new DeviceException("Integration time out of range.");
        }

        withCameraSelected(sdk -> {

            handle(sdk.SetExposureTime((float) time), "SetExposureTime");

        });

    }

    @Override
    public synchronized U16Frame getFrame() throws IOException, DeviceException, InterruptedException, TimeoutException {

        // If we're already continuously acquiring, then just wait for the next frame
        if (isAcquiring()) {

            FrameQueue<U16Frame> queue = openFrameQueue(1);

            try {

                return queue.nextFrame(getAcquisitionTimeout());

            } finally {

                queue.close();
                queue.clear();

            }

        }

        withCameraSelected(sdk -> {

            int result;

            result = sdk.SetAcquisitionMode(ATMCD32D.AC_ACQMODE_SINGLE);

            if (result != DRV_SUCCESS) {
                throw new DeviceException("Error setting acquisition mode to single: %d", result);
            }

            result = sdk.StartAcquisition();

            if (result != DRV_SUCCESS) {
                throw new DeviceException("Error starting acquisition: %d", result);
            }

        });

        int result = sdk.WaitForAcquisitionByPointerTimeOut(pointer, timeout);

        if (result != DRV_SUCCESS) {

            if (result == ATMCD32D.DRV_NO_NEW_DATA) {
                throw new InterruptedException("Acquisition of image was interrupted/cancelled.");
            } else {
                throw new DeviceException("Error waiting for image acquisition: %d", result);
            }

        }

        return null;

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
    public double getAcquisitionFPS() throws IOException, DeviceException {
        return 0;
    }

    @Override
    public void startAcquisition() throws IOException, DeviceException {

    }

    @Override
    public void stopAcquisition() throws IOException, DeviceException {

    }

    @Override
    public boolean isAcquiring() throws IOException, DeviceException {
        return false;
    }

    @Override
    public List<U16Frame> getFrameSeries(int count) throws IOException, DeviceException, InterruptedException, TimeoutException {
        return List.of();
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
    public FrameQueue<U16Frame> openFrameQueue(int capacity) {

        FrameQueue<U16Frame> queue = new FrameQueue<>(this, capacity);
        listenerManager.addQueue(queue);

        return queue;

    }

    @Override
    public void closeFrameQueue(FrameQueue<U16Frame> queue) {
        listenerManager.removeQueue(queue);
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
