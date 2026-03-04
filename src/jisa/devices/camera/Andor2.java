package jisa.devices.camera;

import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.ptr.NativeLongByReference;
import jisa.Util;
import jisa.addresses.Address;
import jisa.devices.DeviceException;
import jisa.devices.camera.feature.MultiTrack;
import jisa.devices.camera.feature.SensorCrop;
import jisa.devices.camera.frame.U16Frame;
import jisa.devices.camera.nat.ATMCD32D;
import jisa.devices.features.TemperatureControlled;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static jisa.devices.camera.nat.ATMCD32D.*;

public class Andor2 extends ManagedCamera<U16Frame> implements TemperatureControlled, MultiTrack, SensorCrop {

    private final ATMCD32D                  sdk;
    private final int                       index;
    private final NativeLong                handle;
    private final ListenerManager<U16Frame> listenerManager = new ListenerManager<>();
    private final int                       maxWidth;
    private final int                       maxHeight;
    private final List<Track>               multiTracks     = new LinkedList<>();

    private int timeout = 10000;
    private int target  = 290;

    private int         width        = 500;
    private int         height       = 500;
    private int         startX       = 0;
    private int         startY       = 0;
    private int         xBin         = 1;
    private int         yBin         = 1;
    private boolean     centredX     = false;
    private boolean     centredY     = false;
    private boolean     multiTrack   = false;
    private boolean     isolatedCrop = false;
    private int         cropX        = 1;
    private int         cropY        = 1;
    private Memory      imageMemory  = null;
    private ShortBuffer imageBuffer  = null;
    private int         frameSize    = 0;

    private static void handle(int result, String method) throws DeviceException {

        if (result != DRV_SUCCESS) {
            throw new DeviceException("Error encountered in method call \"%s\": %d", method, result);
        }

    }

    public Andor2(int index) throws DeviceException {

        super("Andor SDK2 Camera");

        this.sdk   = findLibrary(ATMCD32D.class, "atmcd23d");
        this.index = index;

        synchronized (sdk) {

            IntBuffer intBuffer = IntBuffer.allocate(1);

            handle(sdk.GetNumberDevices(intBuffer), "GetNumberDevices");

            int number = intBuffer.get(0);

            if (number < 1) {
                throw new DeviceException("No connected devices found!");
            } else if (index >= number || index < 0) {
                throw new DeviceException("Invalid device index!");
            } else {

                NativeLongByReference ref = new NativeLongByReference();

                handle(sdk.GetCameraHandle(new NativeLong(index, true), ref), "GetCameraHandle");
                handle = ref.getValue();

                handle(sdk.SetCurrentCamera(handle), "SetCurrentCamera");
                handle(sdk.Initialize(""), "Initialize");

                IntBuffer xBuffer = IntBuffer.allocate(1);
                IntBuffer yBuffer = IntBuffer.allocate(1);
                handle(sdk.GetDetector(xBuffer, yBuffer), "GetDetector");

                width     = xBuffer.get(0);
                height    = yBuffer.get(0);
                maxWidth  = width;
                maxHeight = height;

                handle(sdk.SetReadMode(4), "SetReadMode(IMAGE)");
                handle(sdk.SetImage(xBin, yBin, 1, width, 1, height), "SetImage");

            }

        }

    }

    @Override
    public void setTemperatureControlEnabled(boolean enabled) throws IOException, DeviceException {

        withCameraSelected(sdk -> {

            if (enabled) {
                handle(sdk.CoolerON(), "CoolerON");
            } else {
                handle(sdk.CoolerOFF(), "CoolerOFF");
            }

        });

    }

    @Override
    public boolean isTemperatureControlEnabled() throws IOException, DeviceException {

        IntBuffer buffer = IntBuffer.allocate(1);

        withCameraSelected(sdk -> {
            handle(sdk.IsCoolerOn(buffer), "IsCoolerOn");
        });

        return buffer.get(0) == 1;

    }

    @Override
    public synchronized void setTemperatureControlTarget(double targetTemperature) throws IOException, DeviceException {

        withCameraSelected(sdk -> {
            handle(sdk.SetTemperature((int) Math.round(targetTemperature - 273.15)), "SetTemperature");
        });

        target = (int) Math.round(targetTemperature);

    }

    @Override
    public synchronized double getTemperatureControlTarget() throws IOException, DeviceException {
        return target;
    }

    @Override
    public double getControlledTemperature() throws IOException, DeviceException {

        FloatBuffer buffer = FloatBuffer.allocate(1);

        withCameraSelected(sdk -> {
            sdk.GetTemperatureF(buffer); // This returns many different codes, not just DRV_SUCCESS
        });

        return buffer.get(0) + 273.15;

    }

    @Override
    public boolean isTemperatureControlStable() throws IOException, DeviceException {

        int[]     resBuffer = new int[1];
        IntBuffer intBuffer = IntBuffer.allocate(1);

        withCameraSelected(sdk -> {
            resBuffer[0] = sdk.GetTemperature(intBuffer);
        });

        return resBuffer[0] == DRV_TEMP_STABILIZED;

    }

    @Override
    public void setMultiTrackEnabled(boolean enabled) throws IOException, DeviceException {

        this.multiTrack = enabled;

        if (enabled) {
            isolatedCrop = false;
        }

    }

    @Override
    public boolean isMultiTrackEnabled() throws IOException, DeviceException {
        return multiTrack;
    }

    @Override
    public void setMultiTracks(Collection<Track> tracks) throws IOException, DeviceException {
        multiTracks.clear();
        multiTracks.addAll(tracks);
    }

    @Override
    public List<Track> getMultiTracks() throws IOException, DeviceException {
        return List.copyOf(multiTracks);
    }

    @Override
    public void setSensorCropEnabled(boolean enabled) throws IOException, DeviceException {

        isolatedCrop = enabled;

        if (enabled) {
            multiTrack = false;
        }

    }

    @Override
    public boolean isSensorCropEnabled() throws IOException, DeviceException {
        return isolatedCrop;
    }

    public interface CameraAction {
        void run(ATMCD32D sdk) throws IOException, DeviceException;
    }

    public interface CameraActionInterruptable {
        void run(ATMCD32D sdk) throws IOException, DeviceException, InterruptedException, TimeoutException;
    }

    protected void withCameraSelected(CameraAction toRun) throws IOException, DeviceException {

        synchronized (sdk) {

            NativeLongByReference ref = new NativeLongByReference();
            handle(sdk.GetCurrentCamera(ref), "GetCurrentCamera");

            if (!ref.getValue().equals(handle)) {
                handle(sdk.SetCurrentCamera(handle), "SetCurrentCamera");
            }

            toRun.run(sdk);

        }

    }

    protected void withCameraSelectedTO(CameraActionInterruptable toRun) throws IOException, DeviceException, InterruptedException, TimeoutException {

        synchronized (sdk) {
            handle(sdk.SetCurrentCamera(handle), "SetCurrentCamera");
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

        withCameraSelected(sdk -> handle(sdk.SetExposureTime((float) time), "SetExposureTime"));

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
    protected void setupAcquisition(int limit) throws IOException, DeviceException {

        withCameraSelected(sdk -> {

            // Disable isolated crop mode to start with
            sdk.SetIsolatedCropMode(0, height, width, xBin, yBin);

            // We need to decide which read mode to use.
            if (multiTrack) {

                // If we only have one track defined, then we can use one of the single-track modes
                if (multiTracks.size() == 1 && (multiTracks.get(0).isBinned() || multiTracks.get(0).getStartRow() == multiTracks.get(0).getEndRow())) {

                    Track track = multiTracks.get(0);

                    // For one track that spans the entire sensor, use full vertical binning
                    if (track.getStartRow() == 1 && track.getEndRow() == maxHeight) {

                        handle(sdk.SetReadMode(0), "SetReadMode(FULL-VERTICAL-BINNING)");
                        handle(sdk.SetFVBHBin(xBin), "SetFVBHBin");

                        frameSize = maxWidth / xBin;

                    } else {

                        int height = track.getEndRow() - track.getStartRow() + 1;
                        int centre = (track.getStartRow() + track.getEndRow()) / 2;

                        handle(sdk.SetReadMode(3), "SetReadMode(SINGLE-TRACK)");
                        handle(sdk.SetSingleTrack(centre, height), String.format("SetSingleTrack(%d, %d)", centre, height));
                        handle(sdk.SetSingleTrackHBin(xBin), "SetSingleTrackHBin");

                        frameSize = maxWidth / xBin;

                    }

                } else {

                    int count = 0;

                    for (Track track : multiTracks) {

                        if (track.isBinned()) {
                            count++;
                        } else {
                            count += track.getEndRow() - track.getStartRow() + 1;
                        }

                    }

                    IntBuffer areas = IntBuffer.allocate(count * 2);

                    for (Track track : multiTracks) {

                        if (track.isBinned()) {

                            areas.put(track.getStartRow());
                            areas.put(track.getEndRow());

                        } else {

                            for (int i = track.getStartRow(); i <= track.getEndRow(); i++) {
                                areas.put(i);
                                areas.put(i);
                            }

                        }

                    }

                    handle(sdk.SetReadMode(2), "SetReadMode(RANDOM-TRACK)");
                    handle(sdk.SetRandomTracks(count, areas), "SetRandomTracks");
                    handle(sdk.SetCustomTrackHBin(xBin), "SetCustomTrackHBin");

                    frameSize = count * (maxWidth / xBin);

                }

            } else if (isolatedCrop) {

                if (yBin == height) {

                    handle(sdk.SetReadMode(0), "SetReadMode(FULL-VERTICAL-BINNING)");
                    handle(sdk.SetIsolatedCropMode(1, height, width, xBin, 1), "SetIsolatedCropMode");

                    frameSize = width;

                } else {

                    handle(sdk.SetReadMode(4), "SetReadMode(IMAGE)");
                    handle(sdk.SetIsolatedCropMode(1, height, width, xBin, yBin), "SetIsolatedCropMode");
                    handle(sdk.SetImage(1, 1, 1, width, 1, height), "SetImage");

                    frameSize = width * height;

                }


            } else {

                int xStart;
                int xEnd;
                int yStart;
                int yEnd;

                if (centredX) {
                    xStart = (maxWidth - width) / 2;
                } else {
                    xStart = startX;
                }

                if (centredY) {
                    yStart = (maxHeight - height) / 2;
                } else {
                    yStart = startY;
                }

                xEnd = xStart + width - 1;
                yEnd = yStart + height - 1;

                handle(sdk.SetReadMode(4), "SetReadMode(IMAGE)");
                handle(sdk.SetImage(xBin, yBin, xStart, xEnd, yStart, yEnd), "SetImage");

                frameSize = width * height;

            }

            imageMemory = new Memory(frameSize * Short.BYTES);
            imageBuffer = imageMemory.getByteBuffer(0, frameSize * Short.BYTES).asShortBuffer();

            if (limit == 1) {
                handle(sdk.SetAcquisitionMode(AC_ACQMODE_SINGLE), "SetAcquisitionMode(SINGLE)");
            } else {
                handle(sdk.SetAcquisitionMode(AC_ACQMODE_VIDEO), "SetAcquisitionMode(VIDEO)");
            }

            handle(sdk.StartAcquisition(), "StartAcquisition");
            handle(sdk.SendSoftwareTrigger(), "SendSoftwareTrigger");

        });

    }

    @Override
    protected U16Frame createFrameBuffer() {
        return new U16Frame(new short[frameSize], width, height);
    }

    @Override
    protected void acquisitionLoop(U16Frame frameBuffer) throws IOException, DeviceException, InterruptedException, TimeoutException {

        int  result    = sdk.WaitForAcquisitionByHandleTimeOut(handle, timeout);
        long timestamp = System.nanoTime();

        if (result != DRV_SUCCESS) {

            if (result == DRV_NO_NEW_DATA) {
                throw new InterruptedException("Acquisition of image was interrupted/cancelled.");
            } else {
                throw new DeviceException("Error waiting for image acquisition: %d", result);
            }

        }

        withCameraSelected(sdk -> {
            sdk.GetMostRecentImage16(imageBuffer.clear().rewind(), new NativeLong(imageBuffer.capacity(), true));
        });

        imageBuffer.get(frameBuffer.array());
        frameBuffer.setTimestamp(timestamp);

    }

    @Override
    protected void cleanupAcquisition() throws IOException, DeviceException {

        withCameraSelected(sdk -> {
            handle(sdk.AbortAcquisition(), "AbortAcquisition");
        });

        imageBuffer = null;
        imageMemory.close();

    }

    @Override
    protected void cancelAcquisition() {

        try {
            withCameraSelected(sdk -> sdk.CancelWait());
        } catch (Exception e) {
            e.printStackTrace();
        }

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
        return width * xBin;
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
        return height * yBin;
    }

    @Override
    public int getFrameOffsetX() throws IOException, DeviceException {
        return startX;
    }

    @Override
    public void setFrameOffsetX(int offsetX) throws IOException, DeviceException {
        this.startX = offsetX;
    }

    @Override
    public void setFrameCentredX(boolean centredX) throws IOException, DeviceException {
        this.centredX = centredX;
    }

    @Override
    public boolean isFrameCentredX() throws IOException, DeviceException {
        return centredX;
    }

    @Override
    public int getFrameOffsetY() throws IOException, DeviceException {
        return startY;
    }

    @Override
    public void setFrameOffsetY(int offsetY) throws IOException, DeviceException {
        startY = offsetY;
    }

    @Override
    public void setFrameCentredY(boolean centredY) throws IOException, DeviceException {
        this.centredY = centredY;
    }

    @Override
    public boolean isFrameCentredY() throws IOException, DeviceException {
        return centredY;
    }

    @Override
    public int getFrameSize() throws IOException, DeviceException {
        return width * height;
    }

    @Override
    public int getPhysicalFrameSize() throws IOException, DeviceException {
        return width * xBin * height * yBin;
    }

    @Override
    public int getSensorWidth() throws IOException, DeviceException {
        return maxWidth;
    }

    @Override
    public int getSensorHeight() throws IOException, DeviceException {
        return maxHeight;
    }

    @Override
    public int getBinningX() throws IOException, DeviceException {
        return xBin;
    }

    @Override
    public void setBinningX(int x) throws IOException, DeviceException {
        xBin = x;
    }

    @Override
    public int getBinningY() throws IOException, DeviceException {
        return yBin;
    }

    @Override
    public void setBinningY(int y) throws IOException, DeviceException {
        yBin = y;
    }

    @Override
    public void setBinning(int x, int y) throws IOException, DeviceException {
        xBin = x;
        yBin = y;
    }

    @Override
    public String getIDN() throws IOException, DeviceException {
        return "";
    }

    @Override
    public String getName() {
        return "Andor SDK 2 Camera";
    }

    @Override
    public void close() throws IOException, DeviceException {

    }

    @Override
    public Address getAddress() {
        return null;
    }
}
