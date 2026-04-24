package jisa.devices.camera;

import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.Platform;
import com.sun.jna.ptr.NativeLongByReference;
import jisa.Util;
import jisa.addresses.Address;
import jisa.addresses.IDAddress;
import jisa.devices.DeviceException;
import jisa.devices.camera.feature.KineticSeries;
import jisa.devices.camera.frame.FrameQueue;
import jisa.devices.camera.frame.FrameReader;
import jisa.devices.camera.frame.U16Frame;
import jisa.devices.camera.imagemodes.FullVerticalBinning;
import jisa.devices.camera.imagemodes.MultiTrack;
import jisa.devices.camera.imagemodes.SingleTrack;
import jisa.devices.camera.imagemodes.TrackSequence;
import jisa.devices.camera.nat.ATMCDxxD;
import jisa.devices.features.TemperatureControlled;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static jisa.devices.camera.nat.ATMCDxxD.*;

public class Andor2 extends ManagedCamera<U16Frame> implements TemperatureControlled, SingleTrack, FullVerticalBinning, TrackSequence, MultiTrack, KineticSeries<U16Frame> {

    public static String getDescription() {
        return "Andor CCD Camera (Andor SDK2)";
    }

    private final ATMCDxxD   sdk;
    private final int        index;
    private final NativeLong handle;
    private final int        maxWidth;
    private final int        maxHeight;

    private final ListenerManager<U16Frame> listenerManager = new ListenerManager<>();
    private final List<Track>               multiTracks     = new LinkedList<>();

    private int timeout = 10000;
    private int target  = 290;

    private ImageMode   imageMode           = ImageMode.FULL_IMAGE;
    private int         width               = 500;
    private int         height              = 500;
    private int         startX              = 0;
    private int         startY              = 0;
    private int         xBin                = 1;
    private int         yBin                = 1;
    private boolean     centredX            = false;
    private boolean     centredY            = false;
    private int         singleTrackStart    = 1;
    private int         singleTrackHeight   = 1;
    private int         trackSequenceCount  = 1;
    private int         trackSequenceHeight = 1;
    private int         trackSequenceOffset = 1;
    private ShortBuffer imageBuffer         = null;

    public static FrameReader<U16Frame> openFrameReader(String path) throws IOException {

        U16Frame[] buffer  = new U16Frame[1];
        short[][]  dBuffer = new short[1][];

        return new FrameReader<>(path, (width, height, bpp, timestamp, data) -> {

            if (buffer[0] == null || buffer[0].getWidth() != width || buffer[0].getHeight() != height) {
                dBuffer[0] = new short[width * height];
                buffer[0]  = new U16Frame(dBuffer[0], width, height);
            }

            ByteBuffer.wrap(data).asShortBuffer().rewind().get(dBuffer[0]);
            buffer[0].setTimestamp(timestamp);

            return buffer[0];

        });

    }

    private static void handle(int result, String method) throws DeviceException {

        if (result != DRV_SUCCESS) {
            throw new DeviceException("Error encountered in method call \"%s\": %d", method, result);
        }

    }

    public Andor2(int index) throws DeviceException {
        this((Object) index);
    }

    public Andor2(Address address) throws DeviceException {
        this((Object) address);
    }

    protected Andor2(Object indexObject) throws DeviceException {

        super("Andor SDK2 Camera");

        List<String> extraPaths = new LinkedList<>();

        if (Platform.isWindows() && System.getenv("ProgramFiles") != null) {
            extraPaths.add(Util.joinPath(System.getenv("ProgramFiles"), "Andor SDK"));
        }

        if (Platform.is64Bit()) {
            this.sdk = findLibrary(ATMCDxxD.class, "atmcd64d", extraPaths);
        } else {
            this.sdk = findLibrary(ATMCDxxD.class, "atmcd32d", extraPaths);
        }

        if (indexObject instanceof Integer) {
            this.index = (Integer) indexObject;
        } else if (indexObject instanceof IDAddress) {
            this.index = Integer.parseInt(((IDAddress) indexObject).getID());
        } else {
            throw new DeviceException("Andor2 must have an integer index given as its address.");
        }

        synchronized (sdk) {

            try (Memory countBuffer = new Memory(Integer.BYTES)) {

                IntBuffer intBuffer = countBuffer.getByteBuffer(0, Integer.BYTES).asIntBuffer();

                handle(sdk.GetNumberDevices(intBuffer), "GetNumberDevices");

                int number = intBuffer.get(0);

                if (number < 1) {
                    throw new DeviceException("No connected devices found!");
                } else if (index >= number || index < 0) {
                    throw new DeviceException("Invalid device index!");
                }

            }

            NativeLongByReference ref = new NativeLongByReference();

            handle(sdk.GetCameraHandle(new NativeLong(index, true), ref), "GetCameraPointer");
            handle = ref.getValue();

            handle(sdk.SetCurrentCamera(handle), "SelectDevice");
            handle(sdk.Initialize(""), "Initialize");

            try (Memory sizeBuffer = new Memory(2 * Integer.BYTES)) {

                IntBuffer xBuffer = sizeBuffer.getByteBuffer(0, Integer.BYTES).asIntBuffer();
                IntBuffer yBuffer = sizeBuffer.getByteBuffer(Integer.BYTES, Integer.BYTES).asIntBuffer();

                handle(sdk.GetDetector(xBuffer, yBuffer), "GetDetector");

                width     = xBuffer.get(0);
                height    = yBuffer.get(0);
                maxWidth  = width;
                maxHeight = height;

            }

            handle(sdk.SetImage(xBin, yBin, 1, width, 1, height), "SetImage");

        }

    }

    protected void configureReadout() throws IOException, DeviceException {

        withCameraSelected(sdk -> {

            switch (imageMode) {

                case FULL_IMAGE:

                    handle(sdk.SetReadMode(4), "SetReadMode(IMAGE)");
                    handle(sdk.SetImage(xBin, yBin, 1, maxWidth, 1, maxHeight), "SetImage");

                    break;

                case ROI:

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

                    xEnd = xStart + width;
                    yEnd = yStart + height;

                    handle(sdk.SetReadMode(4), "SetReadMode(IMAGE)");
                    handle(sdk.SetImage(xBin, yBin, xStart + 1, xEnd, yStart + 1, yEnd), "SetImage");

                    break;


                case SINGLE_TRACK:

                    handle(sdk.SetReadMode(3), "SetReadMode(SINGLE-TRACK)");
                    handle(sdk.SetSingleTrack(singleTrackStart - singleTrackHeight / 2, singleTrackHeight), String.format("SetSingleTrack(%d, %d)", singleTrackStart - singleTrackHeight / 2, height));

                    break;

                case FULL_VERTICAL_BINNING:

                    handle(sdk.SetReadMode(0), "SetReadMode(FULL-VERTICAL-BINNING)");
                    break;

                case TRACK_SEQUENCE:

                    handle(sdk.SetReadMode(1), "SetReadMode(MULTI-TRACK [sequence])");
                    handle(
                            sdk.SetMultiTrack(trackSequenceCount, trackSequenceHeight, trackSequenceOffset, IntBuffer.allocate(1), IntBuffer.allocate(1)),
                            String.format("SetMultiTrack(%d, %d, %d)", trackSequenceCount, trackSequenceHeight, trackSequenceOffset)
                    );

                    break;


                case MULTI_TRACK:

                    handle(sdk.SetReadMode(3), "SetReadMode(RANDOM-TRACK [multitrack])");

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

                    handle(sdk.SetRandomTracks(count, areas), "SetRandomTracks");

            }

        });

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
    public synchronized void setTemperatureControlTarget(double targetTemperature) throws
            IOException, DeviceException {

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

        int[]     buffer    = new int[1];
        IntBuffer intBuffer = IntBuffer.allocate(1);

        withCameraSelected(sdk -> {
            buffer[0] = sdk.GetTemperature(intBuffer);
        });

        return buffer[0] == DRV_TEMP_STABILIZED;

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
    public void setSingleTrackStart(int track) throws IOException, DeviceException {
        singleTrackStart = track;
    }

    @Override
    public int getSingleTrackStart() throws IOException, DeviceException {
        return singleTrackStart;
    }

    @Override
    public void setSingleTrackHeight(int tracks) throws IOException, DeviceException {
        singleTrackHeight = tracks;
    }

    @Override
    public int getSingleTrackHeight() throws IOException, DeviceException {
        return singleTrackHeight;
    }

    @Override
    public void setTrackSequenceCount(int count) throws IOException, DeviceException {
        trackSequenceCount = count;
    }

    @Override
    public int getTrackSequenceCount() throws IOException, DeviceException {
        return trackSequenceCount;
    }

    @Override
    public void setTrackSequenceHeight(int height) throws IOException, DeviceException {
        trackSequenceHeight = height;
    }

    @Override
    public int getTrackSequenceHeight() throws IOException, DeviceException {
        return trackSequenceHeight;
    }

    @Override
    public void setTrackSequenceOffset(int offset) throws IOException, DeviceException {
        trackSequenceOffset = offset;
    }

    @Override
    public int getTrackSequenceOffset() throws IOException, DeviceException {
        return trackSequenceOffset;
    }

    @Override
    public FrameQueue<U16Frame> getKineticFrameSeries(int frameCount, int accPerFrame, double frameCycle, double accCycle) throws IOException, DeviceException, TimeoutException, InterruptedException {

        if (isAcquiring()) {
            stopAcquisition();
        }

        withCameraSelected(sdk -> {

            handle(sdk.SetAcquisitionMode(3), "SetAcquisitionMode(KINETICS)");
            handle(sdk.SetNumberAccumulations(accPerFrame), "SetNumberAccumulations");
            handle(sdk.SetAccumulationCycleTime((float) accCycle), "SetAccumulationCycleTime");
            handle(sdk.SetNumberKinetics(frameCount), "SetNumberKinetics");
            handle(sdk.SetKineticCycleTime((float) frameCycle), "SetKineticCycleTime");
            handle(sdk.StartAcquisition(), "StartAcquisition");

        });

        imageBuffer = ShortBuffer.allocate(getFrameSize());

        FrameQueue<U16Frame> frameQueue  = new FrameQueue<>(this, frameCount);
        U16Frame             frameBuffer = createFrameBuffer();

        Thread thread = new Thread(() -> {

            try {

                try {

                    frameBuffer.getAttributes().putAll(getAllParametersAsMap());

                    for (int i = 0; i < frameCount; i++) {

                        acquisitionLoop(frameBuffer);
                        frameQueue.offer(frameBuffer.copy());

                    }

                } finally {
                    frameQueue.close();
                    cleanupAcquisition();
                }

            } catch (Throwable e) {
                e.printStackTrace();
            }

        });

        thread.start();

        return frameQueue;

    }

    public interface CameraAction {
        void run(ATMCDxxD sdk) throws IOException, DeviceException;
    }

    public interface CameraActionInterruptable {
        void run(ATMCDxxD sdk) throws IOException, DeviceException, InterruptedException, TimeoutException;
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

        configureReadout();

        imageBuffer = ShortBuffer.allocate(getFrameSize());

        withCameraSelected(sdk -> {

            int result;

            if (limit == 1) {
                handle(sdk.SetAcquisitionMode(1), "SetAcquisitionMode(SINGLE)");
            } else {
                handle(sdk.SetAcquisitionMode(5), "SetAcquisitionMode(RUN-UNTIL-ABORT)");
            }

            handle(sdk.StartAcquisition(), "StartAcquisition");

        });

    }

    @Override
    protected U16Frame createFrameBuffer() {
        return new U16Frame(new short[getFrameSize()], getFrameWidth(), getFrameHeight());
    }

    @Override
    protected void acquisitionLoop(U16Frame frameBuffer) throws IOException, DeviceException, InterruptedException, TimeoutException {

        int result = sdk.WaitForAcquisitionByHandleTimeOut(handle, timeout);

        if (result != DRV_SUCCESS) {

            if (result == DRV_NO_NEW_DATA) {
                throw new InterruptedException("Acquisition of image was interrupted/cancelled.");
            } else {
                throw new DeviceException("Error waiting for image acquisition: %d", result);
            }

        }

        withCameraSelected(sdk -> sdk.GetMostRecentImage16(imageBuffer.clear().rewind(), new NativeLong(imageBuffer.capacity(), true)));

        imageBuffer.rewind().get(frameBuffer.array());
        frameBuffer.setTimestamp(System.nanoTime());

    }

    @Override
    protected void cleanupAcquisition() throws IOException, DeviceException {

        withCameraSelected(sdk -> {
            handle(sdk.AbortAcquisition(), "AbortAcquisition");
        });

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
    public int getFrameWidth() {

        switch (imageMode) {

            case FULL_IMAGE:
                return maxWidth / xBin;

            case ROI:
            case FULL_VERTICAL_BINNING:
            case SINGLE_TRACK:
            case TRACK_SEQUENCE:
            case MULTI_TRACK:
                return width;

        }

        return width;

    }

    @Override
    public void setImageWidth(int width) throws IOException, DeviceException {
        this.width = width;
    }

    @Override
    public int getImageWidth() throws IOException, DeviceException {
        return width;
    }

    @Override
    public int getPhysicalFrameWidth() throws IOException, DeviceException {
        return getFrameWidth() * xBin;
    }

    @Override
    public int getFrameHeight() {

        switch (imageMode) {

            case FULL_IMAGE:
                return maxHeight / yBin;

            case ROI:
                return height;

            case FULL_VERTICAL_BINNING:
            case SINGLE_TRACK:
                return 1;

            case TRACK_SEQUENCE:
                return trackSequenceCount * trackSequenceHeight;

            case MULTI_TRACK:

                int count = 0;

                for (Track track : multiTracks) {
                    count += track.isBinned() ? 1 : (track.getEndRow() - track.getStartRow() + 1);
                }

                return count;

        }

        return height;

    }

    @Override
    public void setImageHeight(int height) throws IOException, DeviceException {
        this.height = height;
    }

    @Override
    public int getImageHeight() throws IOException, DeviceException {
        return height;
    }

    @Override
    public int getPhysicalFrameHeight() throws IOException, DeviceException {
        return getFrameHeight() * yBin;
    }

    @Override
    public int getImageOffsetX() throws IOException, DeviceException {
        return startX;
    }

    @Override
    public void setImageOffsetX(int offsetX) throws IOException, DeviceException {
        this.startX = offsetX;
    }

    @Override
    public void setImageCentredX(boolean centredX) throws IOException, DeviceException {
        this.centredX = centredX;
    }

    @Override
    public boolean isImageCentredX() throws IOException, DeviceException {
        return centredX;
    }

    @Override
    public int getImageOffsetY() throws IOException, DeviceException {
        return startY;
    }

    @Override
    public void setImageOffsetY(int offsetY) throws IOException, DeviceException {
        startY = offsetY;
    }

    @Override
    public void setImageCentredY(boolean centredY) throws IOException, DeviceException {
        this.centredY = centredY;
    }

    @Override
    public boolean isImageCentredY() throws IOException, DeviceException {
        return centredY;
    }

    @Override
    public int getFrameSize() {
        return getFrameWidth() * getFrameHeight();
    }

    @Override
    public int getPhysicalFrameSize() throws IOException, DeviceException {
        return getFrameWidth() * xBin * getFrameHeight() * yBin;
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
    public synchronized ImageMode getImageMode() throws IOException, DeviceException {
        return imageMode;
    }

    @Override
    public synchronized void setImageMode(ImageMode mode) throws IOException, DeviceException {
        this.imageMode = mode;
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
