package jisa.devices.camera;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.PointerByReference;
import jisa.Util;
import jisa.addresses.Address;
import jisa.addresses.IDAddress;
import jisa.control.RTask;
import jisa.devices.DeviceException;
import jisa.devices.ParameterList;
import jisa.devices.camera.feature.*;
import jisa.devices.camera.frame.FrameQueue;
import jisa.devices.camera.frame.FrameReader;
import jisa.devices.camera.frame.U16Frame;
import jisa.devices.camera.nat.ATCoreLibrary;
import jisa.devices.camera.nat.ATUtilityLibrary;
import jisa.devices.features.TemperatureControlled;
import jisa.gui.HeatMap;
import jisa.visa.NativeDevice;

import java.io.IOException;
import java.nio.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class Andor3 extends NativeDevice implements Camera<U16Frame>, FrameBinning, CMOS, Overlap, MultiTrack, TemperatureControlled, Shuttered {

    public static String getDescription() {
        return "Andor sCMOS Camera (Andor SDK3)";
    }

    public static FrameReader<U16Frame> openFrameReader(String path) throws IOException {

        Frame[]   buffer  = new Frame[1];
        short[][] dBuffer = new short[1][];

        return new FrameReader<>(path, (width, height, bpp, timestamp, data) -> {

            if (buffer[0] == null || buffer[0].getWidth() != width || buffer[0].getHeight() != height) {
                dBuffer[0] = new short[width * height];
                buffer[0]  = new Frame(dBuffer[0], width, height);
            }

            ByteBuffer.wrap(data).asShortBuffer().rewind().get(dBuffer[0]);
            buffer[0].setTimestamp(timestamp);

            return buffer[0];

        });

    }

    public final static int  AT_ERR_STRINGNOTAVAILABLE      = 18;
    public final static int  AT_ERR_NULL_COUNT_VAR          = 30;
    public final static int  AT_HANDLE_SYSTEM               = 1;
    public final static int  AT_ERR_INDEXNOTIMPLEMENTED     = 8;
    public final static int  AT_ERR_NULL_READABLE_VAR       = 23;
    public final static int  AT_CALLBACK_SUCCESS            = 0;
    public final static int  AT_ERR_NULL_WRITABLE_VAR       = 25;
    public final static int  AT_ERR_NULL_QUEUE_PTR          = 34;
    public final static int  AT_ERR_NOTWRITABLE             = 5;
    public final static int  AT_ERR_HARDWARE_OVERFLOW       = 100;
    public final static int  AT_ERR_BUFFERFULL              = 14;
    public final static int  AT_ERR_COMM                    = 17;
    public final static int  AT_ERR_NOTINITIALISED          = 1;
    public final static int  AT_ERR_NULL_READONLY_VAR       = 24;
    public final static int  AT_ERR_NULL_MINVALUE           = 26;
    public final static int  AT_ERR_NULL_MAXSTRINGLENGTH    = 32;
    public final static int  AT_ERR_NOTREADABLE             = 4;
    public final static int  AT_ERR_NULL_WAIT_PTR           = 35;
    public final static int  AT_ERR_NULL_EVCALLBACK         = 33;
    public final static long AT_INFINITE                    = 0xFFFFFFFFL;
    public final static int  AT_ERR_OUTOFRANGE              = 6;
    public final static int  AT_ERR_STRINGNOTIMPLEMENTED    = 19;
    public final static int  AT_ERR_READONLY                = 3;
    public final static int  AT_ERR_EXCEEDEDMAXSTRINGLENGTH = 9;
    public final static int  AT_TRUE                        = 1;
    public final static int  AT_ERR_INDEXNOTAVAILABLE       = 7;
    public final static int  AT_ERR_CONNECTION              = 10;
    public final static int  AT_FALSE                       = 0;
    public final static int  AT_HANDLE_UNINITIALISED        = -1;
    public final static int  AT_ERR_NULL_PTRSIZE            = 36;
    public final static int  AT_ERR_INVALIDALIGNMENT        = 16;
    public final static int  AT_ERR_INVALIDHANDLE           = 12;
    public final static int  AT_ERR_NULL_STRING             = 29;
    public final static int  AT_ERR_NULL_MAXVALUE           = 27;
    public final static int  AT_ERR_NOTIMPLEMENTED          = 2;
    public final static int  AT_ERR_NULL_IMPLEMENTED_VAR    = 22;
    public final static int  AT_ERR_INVALIDSIZE             = 15;
    public final static int  AT_ERR_NULL_HANDLE             = 21;
    public final static int  AT_ERR_DEVICEINUSE             = 38;
    public final static int  AT_ERR_NULL_VALUE              = 28;
    public final static int  AT_ERR_NOMEMORY                = 37;
    public final static int  AT_ERR_NODATA                  = 11;
    public final static int  AT_ERR_TIMEDOUT                = 13;
    public final static int  AT_ERR_DEVICENOTFOUND          = 39;
    public final static int  AT_SUCCESS                     = 0;
    public final static int  AT_ERR_NULL_FEATURE            = 20;
    public final static int  AT_ERR_NULL_ISAVAILABLE_VAR    = 31;

    public final static Map<Integer, String> ERROR_NAMES =
        Util.map(AT_ERR_STRINGNOTAVAILABLE, "String Not Available")
            .map(AT_ERR_NULL_COUNT_VAR, "Null Count Variable")
            .map(AT_ERR_INDEXNOTIMPLEMENTED, "Index Not Implemented")
            .map(AT_ERR_NULL_READABLE_VAR, "Null Readable Variable")
            .map(AT_ERR_NULL_WRITABLE_VAR, "Null Writable Variable")
            .map(AT_ERR_NULL_QUEUE_PTR, "Null Queue Pointer")
            .map(AT_ERR_NOTWRITABLE, "Not Writable")
            .map(AT_ERR_HARDWARE_OVERFLOW, "Hardware Overflow")
            .map(AT_ERR_BUFFERFULL, "Buffer Full")
            .map(AT_ERR_COMM, "Communications Error")
            .map(AT_ERR_NOTINITIALISED, "Not Initialised")
            .map(AT_ERR_NULL_READONLY_VAR, "Read-Only Variable is Null")
            .map(AT_ERR_NULL_MINVALUE, "Minimum Value is Null")
            .map(AT_ERR_NULL_MAXSTRINGLENGTH, "Maximum String Length is Null")
            .map(AT_ERR_NOTREADABLE, "Not Readable")
            .map(AT_ERR_NULL_WAIT_PTR, "Wait Pointer is Null")
            .map(AT_ERR_NULL_EVCALLBACK, "Event Callback is Null")
            .map(AT_ERR_OUTOFRANGE, "Out of Range")
            .map(AT_ERR_STRINGNOTIMPLEMENTED, "String Not Implemented")
            .map(AT_ERR_READONLY, "Read Only")
            .map(AT_ERR_EXCEEDEDMAXSTRINGLENGTH, "Maximum String Length Exceeded")
            .map(AT_ERR_INDEXNOTAVAILABLE, "Index Not Available")
            .map(AT_ERR_CONNECTION, "Connection Error")
            .map(AT_ERR_NULL_PTRSIZE, "Pointer Size is Null")
            .map(AT_ERR_INVALIDALIGNMENT, "Invalid Alignment")
            .map(AT_ERR_INVALIDHANDLE, "Invalid Handle")
            .map(AT_ERR_NULL_STRING, "String is Null")
            .map(AT_ERR_NULL_MAXVALUE, "Maximum Value is Null")
            .map(AT_ERR_NOTIMPLEMENTED, "Not Implemented")
            .map(AT_ERR_NULL_IMPLEMENTED_VAR, "isImplemented Variable is Null")
            .map(AT_ERR_INVALIDSIZE, "Invalid Size")
            .map(AT_ERR_NULL_HANDLE, "Handle is Null")
            .map(AT_ERR_DEVICEINUSE, "Device in Use")
            .map(AT_ERR_NULL_VALUE, "Value is Null")
            .map(AT_ERR_NOMEMORY, "No Memory")
            .map(AT_ERR_NODATA, "No Data")
            .map(AT_ERR_TIMEDOUT, "Timed Out")
            .map(AT_ERR_DEVICENOTFOUND, "Device Not Found")
            .map(AT_ERR_NULL_FEATURE, "Feature is Null")
            .map(AT_ERR_NULL_ISAVAILABLE_VAR, "isAvailable Variable is Null");

    public final static List<Integer> IO_ERRORS = List.of(
        AT_ERR_COMM,
        AT_ERR_CONNECTION,
        AT_ERR_DEVICEINUSE,
        AT_ERR_TIMEDOUT
    );

    private final ATCoreLibrary    core;
    private final ATUtilityLibrary util;
    private final int              handle;

    private final ListenerManager<U16Frame> listenerManager = new ListenerManager<>();

    private BlockingQueue<byte[]> queued            = new LinkedBlockingQueue<>();
    private boolean               backlog           = true;
    private Frame                 frameBuffer       = null;
    private boolean               centreX           = false;
    private int                   timeout           = 0;
    private boolean               acquiring         = false;
    private Thread                acquisitionThread = null;
    private Thread                processingThread  = null;
    private double                acquireFPS        = 0;
    private double                processFPS        = 0;

    @Override
    public void addInstrumentParameters(Class<?> target, ParameterList parameters) {

        parameters.addValue("Fast AOI Readout Mode", this::isFastAOIFrameRateEnabled, false, this::setFastAOIFrameRateEnabled);
        parameters.addValue("Internal Backlog Enabled", this::isInternalBacklogEnabled, false, this::setInternalBacklogEnabled);

    }

    public Andor3(Address address) throws DeviceException, IOException {
        this((Object) address);
    }

    public Andor3(int cameraIndex) throws DeviceException, IOException {
        this((Object) cameraIndex);
    }

    private Andor3(Object idObject) throws DeviceException, IOException {

        super("Andor 3 Camera");

        // Load or retrieve necessary system libraries
        core = findLibrary(ATCoreLibrary.class, "atcore");
        util = findLibrary(ATUtilityLibrary.class, "atutility");

        // Extract camera index from provided object
        int cameraIndex = extractIndex(idObject);

        // Open the connection and store the returned handle
        IntBuffer intBuffer = IntBuffer.allocate(1);
        handleResult("Open", "N/A", core.AT_Open(cameraIndex, intBuffer.clear()));

        handle = intBuffer.get(0);

        setEnum("TriggerMode", "Internal");
        setBoolean("RollingShutterGlobalClear", false);

    }

    private static int extractIndex(Object idObject) throws DeviceException {

        int cameraIndex;

        // Determine how the camera index is encoded in the address object
        if (idObject instanceof IDAddress) {

            try {
                cameraIndex = Integer.parseInt(((IDAddress) idObject).getID());
            } catch (NumberFormatException e) {
                throw new DeviceException("ID must be an integer");
            }

        } else if (idObject instanceof Integer) {
            cameraIndex = (Integer) idObject;
        } else if (idObject instanceof Address) {
            throw new DeviceException("Address for Andor SDK3 Camera must be an IDAddress.");
        } else {
            throw new DeviceException("Invalid ID Object.");
        }

        return cameraIndex;

    }

    protected void handleResult(String method, String feature, int result) throws IOException, DeviceException {

        if (result != AT_SUCCESS) {

            if (IO_ERRORS.contains(result)) {
                throw new IOException(String.format("Error communicating with Andor SDK 3 camera using %s: %d (%s)", method, result, ERROR_NAMES.getOrDefault(result, "UNKNOWN")));
            } else {
                throw new DeviceException("%s for feature \"%s\" failed: %d (%s)", method, feature, result, ERROR_NAMES.getOrDefault(result, "UNKNOWN"));
            }

        }

    }

    /**
     * Sets whether the frame buffer between the frame acquisition and processing threads can hold more than one frame or not.
     * <p>
     * <hr/>
     * <p><i>Some Andor3 cameras, under certain conditions, can provide raw frames faster than we can realistically process them into useable images (particularly on slower computers).
     * If this backlog is disabled, then frames acquired while a previous frame is still being processed are rejected by the processing thread (i.e., they are dropped), effectively clamping the acquisition rate to the processing rate. If it is
     * enabled, they are instead queued. This is often undesirable as it means by the time a frame reaches any FrameListeners or is added to a FrameQueue, it is significantly old,
     * leading to an immediate and ever-increasing latency to any live display/processing.</i></p>
     * <p><i>Really then, you only want to enable this if your intention is simply to stream as many frames as possible to disk without any on-line processing.</i></p>
     * <hr/>
     *
     * @param enabled Whether the enable the backlog or not. Default is false.     *
     */
    public void setInternalBacklogEnabled(boolean enabled) {

        if (enabled) {
            queued = new LinkedBlockingQueue<>();
        } else {
            queued = new LinkedBlockingQueue<>(1);
        }

        backlog = enabled;

    }

    public boolean isInternalBacklogEnabled() {
        return backlog;
    }

    protected synchronized String getString(String feature) throws DeviceException, IOException {

        CharBuffer charBuffer = CharBuffer.allocate(1024);
        handleResult("GetString", feature, core.AT_GetString(handle, new WString(feature), charBuffer.clear(), 1024));

        return charBuffer.rewind().toString().trim();

    }

    protected synchronized void setString(String feature, String value) throws DeviceException, IOException {

        handleResult("SetString", feature, core.AT_SetString(handle, new WString(feature), new WString(value)));

    }

    protected synchronized double getFloat(String feature) throws DeviceException, IOException {

        DoubleBuffer doubleBuffer = DoubleBuffer.allocate(1);
        handleResult("GetFloat", feature, core.AT_GetFloat(handle, new WString(feature), doubleBuffer.clear()));

        return doubleBuffer.get(0);

    }

    protected synchronized void setFloat(String feature, double value) throws DeviceException, IOException {

        handleResult("SetFloat", feature, core.AT_SetFloat(handle, new WString(feature), value));

    }

    protected synchronized long getLong(String feature) throws DeviceException, IOException {

        LongBuffer longBuffer = LongBuffer.allocate(1);
        handleResult("GetInt", feature, core.AT_GetInt(handle, new WString(feature), longBuffer.clear()));

        return longBuffer.get(0);

    }

    protected synchronized int getInt(String feature) throws DeviceException, IOException {

        return (int) getLong(feature);

    }

    protected synchronized void setLong(String feature, long value) throws DeviceException, IOException {

        handleResult("SetInt", feature, core.AT_SetInt(handle, new WString(feature), value));

    }

    protected synchronized void setInt(String feature, int value) throws DeviceException, IOException {

        setLong(feature, value);

    }

    protected synchronized boolean getBoolean(String feature) throws DeviceException, IOException {

        IntBuffer intBuffer = IntBuffer.allocate(1);
        handleResult("GetBool", feature, core.AT_GetBool(handle, new WString(feature), intBuffer.clear()));

        return intBuffer.get(0) == 1;

    }

    protected synchronized void setBoolean(String feature, boolean value) throws DeviceException, IOException {

        handleResult("SetBool", feature, core.AT_SetBool(handle, new WString(feature), value ? 1 : 0));

    }

    protected synchronized Enum getEnum(String feature) throws DeviceException, IOException {

        IntBuffer intBuffer = IntBuffer.allocate(1);

        WString wFeature = new WString(feature);
        handleResult("GetEnumIndex", feature, core.AT_GetEnumIndex(handle, wFeature, intBuffer.clear()));

        CharBuffer buffer = CharBuffer.allocate(1024);
        handleResult("GetEnumStringByIndex", feature, core.AT_GetEnumStringByIndex(handle, wFeature, intBuffer.get(0), buffer, 1024));

        return new Enum(intBuffer.get(0), buffer.rewind().toString().trim());

    }

    protected synchronized List<Enum> getEnumOptions(String feature) throws DeviceException, IOException {

        IntBuffer intBuffer = IntBuffer.allocate(1);
        WString   wFeature  = new WString(feature);

        handleResult("GetEnumCount", feature, core.AT_GetEnumCount(handle, wFeature, intBuffer.clear()));

        int        count = intBuffer.get(0);
        List<Enum> list  = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {

            CharBuffer charBuffer = CharBuffer.allocate(1024);
            handleResult("AT_GetEnumStringByIndex", feature, core.AT_GetEnumStringByIndex(handle, wFeature, i, charBuffer.clear(), 1024));
            String text = charBuffer.rewind().toString().trim();

            handleResult("IsEnumeratedIndexAvailable", feature, core.AT_IsEnumeratedIndexAvailable(handle, wFeature, i, intBuffer.clear()));
            boolean available = intBuffer.get(0) == 1;

            handleResult("IsEnumIndexImplemented", feature, core.AT_IsEnumIndexImplemented(handle, wFeature, i, intBuffer.clear()));
            boolean implemented = intBuffer.get(0) == 1;

            list.add(new Enum(i, text, available, implemented));

        }

        return list;

    }

    protected synchronized void setEnum(String feature, int index) throws DeviceException, IOException {

        handleResult("SetEnumIndex", feature, core.AT_SetEnumIndex(handle, new WString(feature), index));

    }

    protected synchronized void setEnum(String feature, Enum value) throws DeviceException, IOException {
        setEnum(feature, value.getIndex());
    }

    protected synchronized void setEnum(String feature, String text) throws DeviceException, IOException {

        List<Enum> available = getEnumOptions(feature);
        Enum       match     = available.stream().filter(e -> e.getText().trim().equalsIgnoreCase(text.trim())).findFirst().orElse(null);

        if (match == null) {
            throw new DeviceException("Invalid enum option \"%s\" for feature \"%s\" (Available: %s)", text, feature, available.stream().map(Enum::getText).collect(Collectors.joining(", ")));
        }

        setEnum(feature, match);

    }

    protected synchronized void command(String feature) throws DeviceException, IOException {
        handleResult("Command", feature, core.AT_Command(handle, new WString(feature)));
    }

    protected void flush() throws DeviceException, IOException {
        handleResult("Flush", "N/A", core.AT_Flush(handle));
    }

    protected synchronized void acquisitionStart() throws DeviceException, IOException {
        command("AcquisitionStart");
    }

    protected synchronized void acquisitionStop() throws DeviceException, IOException {
        command("AcquisitionStop");
    }

    protected void checkSymmetricalBinning() throws DeviceException, IOException {

        List<Enum> options = getEnumOptions("AOIBinning");

        List<Integer> values = options.stream()
                                      .map(e -> Integer.parseInt(e.getText().split("x")[0]))
                                      .collect(Collectors.toList());

        int x = getBinningX();
        int y = getBinningY();

        if (x == y && values.contains(x)) {
            setEnum("AOIBinning", options.get(values.indexOf(x)));
        }

    }

    @Override
    public double getIntegrationTime() throws IOException, DeviceException {
        return getFloat("ExposureTime");
    }

    @Override
    public void setIntegrationTime(double time) throws IOException, DeviceException {

        double maxRate = getFloat("MaxInterfaceTransferRate");
        double minTime = 1.0 / maxRate;

        setFloat("ExposureTime", time);

    }

    public boolean isFastAOIFrameRateEnabled() throws IOException, DeviceException {
        return getBoolean("FastAOIFrameRateEnable");
    }

    public void setFastAOIFrameRateEnabled(boolean enabled) throws IOException, DeviceException {
        setBoolean("FastAOIFrameRateEnable", enabled);
    }

    @Override
    public boolean isRollingElectronicShutterEnabled() throws IOException, DeviceException {
        return getEnum("ElectronicShutteringMode").getText().toLowerCase().contains("rolling");
    }

    @Override
    public void setRollingElectronicShutterEnabled(boolean enabled) throws IOException, DeviceException {
        setEnum("ElectronicShutteringMode", enabled ? "Rolling" : "Global");
    }

    @Override
    public int getFrameBinning() throws IOException, DeviceException {
        return getInt("AccumulateCount");
    }

    @Override
    public void setFrameBinning(int count) throws IOException, DeviceException {
        setInt("AccumulateCount", count);
    }

    @Override
    public U16Frame getFrame() throws DeviceException, IOException, InterruptedException, TimeoutException {

        // If we're already continuously acquiring, we can just open a FrameQueue and wait for the next frame from that.
        if (isAcquiring()) {

            FrameQueue<U16Frame> queue = openFrameQueue(1);

            try {

                return queue.nextFrame(timeout);

            } finally {

                queue.close();
                queue.clear();

            }

        }

        // Get important values that we will need
        final long    tmo        = (timeout == Integer.MAX_VALUE || timeout < 1) ? AT_INFINITE : timeout;
        final long    safeTMO    = Math.max((long) (2 * getIntegrationTime() * 1e3), 100);
        final int     bufferSize = getInt("ImageSizeBytes");
        final long    frequency  = getLong("TimestampClockFrequency");
        final long    nsPerTick  = (long) (1e9 / frequency);
        final int     width      = getInt("AOIWidth");
        final int     height     = getInt("AOIHeight");
        final int     stride     = getInt("AOIStride");
        final Enum    encoding   = getEnum("PixelEncoding");
        final WString mono16     = new WString("Mono16");
        final WString encText    = new WString(encoding.getText());
        final int     imageSize  = width * height;

        setEnum("CycleMode", "Fixed");
        setLong("FrameCount", 1);

        // Allocate memory for frame buffer
        try (Memory memory = new Memory(bufferSize)) {

            ByteBuffer frameBuffer = memory.getByteBuffer(0, bufferSize);

            flush();

            // Queue the buffer up to be populated by camera
            int result = core.AT_QueueBuffer(handle, frameBuffer.clear().rewind(), bufferSize);

            if (result != AT_SUCCESS) {
                throw new DeviceException("QueueBuffer failed: %d (%s)", result, ERROR_NAMES.getOrDefault(result, "UNKNOWN"));
            }


            PointerByReference reference = new PointerByReference();
            IntBuffer          intBuffer = IntBuffer.allocate(1);

            acquisitionStart();

            if (tmo == AT_INFINITE) {

                // Infinite timeout is a problem because we can't interrupt the WaitBuffer method, so let's do a
                // lööp of WaitBuffers with smallish timeouts between which we can check for interrupts
                do {

                    if (Thread.interrupted()) {

                        acquisitionStop();
                        flush();
                        throw new InterruptedException("Waiting for frame data interrupted.");

                    }

                    result = core.AT_WaitBuffer(handle, reference, intBuffer.clear(), safeTMO);

                } while (result == AT_ERR_TIMEDOUT); // Keep going until we get something other than a timeout

            } else {

                // Otherwise, just do it normally
                result = core.AT_WaitBuffer(handle, reference, intBuffer.clear(), tmo);

            }

            acquisitionStop();
            flush();

            if (result != AT_SUCCESS) {

                if (result == AT_ERR_TIMEDOUT) {
                    throw new TimeoutException("Timed out waiting for frame.");
                }

                throw new DeviceException("WaitBuffer failed: %d (%s)", result, ERROR_NAMES.getOrDefault(result, "UNKNOWN"));
            }

            long       returnedSize = Integer.toUnsignedLong(intBuffer.get(0));
            ByteBuffer returned     = reference.getValue().getByteBuffer(0, returnedSize);

            // Allocate memory to hold converted
            try (Memory convertedMemory = new Memory(imageSize * 2L)) {

                ByteBuffer converted = convertedMemory.getByteBuffer(0, imageSize * 2L);

                result = util.AT_ConvertBufferUsingMetadata(returned.rewind(), converted.clear().rewind(), returnedSize, mono16);

                if (result != AT_SUCCESS) {

                    result = util.AT_ConvertBuffer(returned.rewind(), converted.clear().rewind(), width, height, stride, encText, mono16);

                    if (result != AT_SUCCESS) {
                        throw new DeviceException("ConvertBuffer failed: %d (%s)", result, ERROR_NAMES.getOrDefault(result, "UNKNOWN"));
                    }

                }

                LongByReference timeStampBuffer = new LongByReference();

                result = util.AT_GetTimeStampFromMetadata(returned.rewind(), returnedSize, timeStampBuffer);

                Frame frame = new Frame(width, height);
                converted.asShortBuffer().get(frame.array(), 0, imageSize);

                if (result != AT_SUCCESS) {
                    frame.setTimestamp(nsPerTick * timeStampBuffer.getValue());
                }

                flush();

                return frame;

            }

        }

    }

    @Override
    public List<U16Frame> getFrameSeries(int count) throws DeviceException, IOException, InterruptedException, TimeoutException {

        // If we're already continuously acquiring, we can just open a FrameQueue and wait for the next n frames from that
        if (isAcquiring()) {

            List<U16Frame>       frames = new ArrayList<>(count);
            FrameQueue<U16Frame> queue  = openFrameQueue();

            try {

                for (int i = 0; i < count; i++) {
                    frames.add(queue.nextFrame(timeout));
                }

                return frames;

            } finally {

                queue.close();
                queue.clear();

            }

        }

        // Check for values that might indicate no timeout
        long tmo = (timeout == Integer.MAX_VALUE || timeout < 1) ? AT_INFINITE : timeout;
        long ftm = Math.max((long) (2 * getIntegrationTime() * 1e3), 100);

        long    bufferSize       = getLong("ImageSizeBytes");
        int     width            = getInt("AOIWidth");
        int     height           = getInt("AOIHeight");
        int     stride           = getInt("AOIStride");
        int     imageSize        = width * height;
        long    frequency        = getLong("TimestampClockFrequency");
        long    nsPerTick        = (long) (1e9 / frequency);
        Enum    encoding         = getEnum("PixelEncoding");
        WString mono16           = new WString("Mono16");
        WString encText          = new WString(encoding.getText());
        boolean timeStampEnabled = isTimestampEnabled();
        int     result;

        setEnum("CycleMode", "Fixed");
        setLong("FrameCount", count);

        flush();

        List<U16Frame> frames = new ArrayList<>(count);

        // Reserve block of memory for all frames
        try (Memory memory = new Memory(bufferSize * count)) {

            for (int i = 0; i < count; i++) {

                result = core.AT_QueueBuffer(handle, memory.getByteBuffer(i * bufferSize, bufferSize), (int) bufferSize);

                if (result != AT_SUCCESS) {
                    throw new DeviceException("QueueBuffer failed: %d (%s)", result, ERROR_NAMES.getOrDefault(result, "UNKNOWN"));
                }

            }

            acquisitionStart();

            Pointer[] pointers  = new Pointer[count];
            IntBuffer intBuffer = IntBuffer.allocate(1);

            for (int i = 0; i < count; i++) {

                PointerByReference pointer = new PointerByReference();

                if (tmo == AT_INFINITE) {

                    // Infinite timeout is a problem because we can't interrupt the WaitBuffer method, so let's do a
                    // lööp of WaitBuffers with smallish timeouts between which we can check for interrupts
                    do {

                        if (Thread.interrupted()) {

                            acquisitionStop();
                            flush();
                            throw new InterruptedException("Waiting for frame data interrupted.");

                        }

                        result = core.AT_WaitBuffer(handle, pointer, intBuffer.clear(), ftm);

                    } while (result == AT_ERR_TIMEDOUT);

                } else {

                    // Otherwise, just do it normally
                    result = core.AT_WaitBuffer(handle, pointer, intBuffer.clear(), tmo);

                }

                if (result != AT_SUCCESS) {

                    if (result == AT_ERR_TIMEDOUT) {
                        throw new TimeoutException("Timed out waiting for frame.");
                    }

                    throw new DeviceException("WaitBuffer failed: %d (%s)", result, ERROR_NAMES.getOrDefault(result, "UNKNOWN"));
                }

                pointers[i] = pointer.getValue();

                if (Thread.interrupted()) {

                    acquisitionStop();
                    flush();
                    throw new InterruptedException("Waiting for frame data interrupted.");

                }

            }

            acquisitionStop();
            flush();

            // Reserve block of memory for all converted frames
            try (Memory convertedMemory = new Memory(count * imageSize * 2L)) {

                for (int i = 0; i < count; i++) {

                    ByteBuffer returned  = pointers[i].getByteBuffer(0, bufferSize);
                    ByteBuffer converted = convertedMemory.getByteBuffer(i * imageSize * 2L, imageSize * 2L);

                    result = util.AT_ConvertBuffer(returned.rewind(), converted.clear().rewind(), width, height, stride, encText, mono16);

                    if (result != AT_SUCCESS) {
                        throw new DeviceException("ConvertBuffer failed: %d (%s)", result, ERROR_NAMES.getOrDefault(result, "UNKNOWN"));
                    }

                    short[] frameData = new short[imageSize];
                    converted.asShortBuffer().get(frameData, 0, imageSize);

                    Frame frame = new Frame(frameData, width, height);

                    if (timeStampEnabled) {

                        LongByReference timeStampBuffer = new LongByReference();

                        result = util.AT_GetTimeStampFromMetadata(returned.rewind(), bufferSize, timeStampBuffer);

                        if (result == AT_SUCCESS) {
                            frame.setTimestamp(nsPerTick * timeStampBuffer.getValue());
                        } else {
                            frame.setTimestamp(System.nanoTime());
                        }

                    } else {
                        frame.setTimestamp(System.nanoTime());
                    }

                    frames.add(frame);

                }

            }

        }

        return frames;

    }

    public double getAcquisitionFPS() {
        return acquireFPS;
    }

    public double getProcessingFPS() {
        return processFPS;
    }

    @Override
    public void setAcquisitionTimeout(int timeout) throws IOException, DeviceException {
        this.timeout = Math.max(0, timeout);
    }

    @Override
    public int getAcquisitionTimeout() throws IOException, DeviceException {
        return timeout;
    }

    /**
     * Main method of the continuous acquisition thread. Responsible for continuously queuing and extracting buffers
     * to and from the camera, then passing them on to the processing thread.
     *
     * @param bufferSize The size of each individual buffer to continuously queue (in bytes).
     */
    private void acquisition(int bufferSize) {

        final long[] stats = {0, 0, System.nanoTime()};

        RTask task = new RTask(1000, () -> {

            synchronized (stats) {

                long frames  = stats[0];
                long dFrames = frames - stats[1];
                long time    = System.nanoTime();
                long dTime   = time - stats[2];

                stats[1] = frames;
                stats[2] = time;

                this.acquireFPS = 1e9 * dFrames / dTime;

            }

        });

        task.start();

        try (Memory memory = new Memory(bufferSize)) {

            long tmo = (timeout == Integer.MAX_VALUE || timeout < 1) ? AT_INFINITE : timeout;
            long ftm = Math.max((long) (2 * getIntegrationTime() * 1e3), 100);

            int result;

            ByteBuffer         frameBuffer = memory.getByteBuffer(0, bufferSize);
            IntBuffer          intBuffer   = IntBuffer.allocate(1);
            PointerByReference reference   = new PointerByReference();

            listenerManager.clearListenerBuffers();

            acq:
            while (acquiring) {

                result = core.AT_QueueBuffer(handle, frameBuffer.clear(), bufferSize);

                if (result != AT_SUCCESS) {
                    System.err.printf("Error queueing buffer: %d (%s)%n", result, ERROR_NAMES.getOrDefault(result, "UNKNOWN"));
                    continue;
                }

                if (tmo == AT_INFINITE) {

                    // Infinite timeout is a problem because we can't interrupt the WaitBuffer method, so let's do a
                    // lööp of WaitBuffers with smallish timeouts between which we can check for interrupts
                    do {

                        if (Thread.interrupted()) {
                            break acq;
                        }

                        result = core.AT_WaitBuffer(handle, reference, intBuffer.clear(), ftm);

                    } while (result == AT_ERR_TIMEDOUT);

                } else {

                    // Otherwise, just do it normally
                    result = core.AT_WaitBuffer(handle, reference, intBuffer.clear(), tmo);

                }

                if (result != AT_SUCCESS) {

                    // If there's nothing queued, then we've reached the end.
                    if (result == AT_ERR_NODATA) {
                        break;
                    }

                    System.err.printf("Error waiting for buffer: %d (%s)%n", result, ERROR_NAMES.getOrDefault(result, "UNKNOWN"));

                    flush();
                    continue;

                }

                // Whatever we received, offer it to the queue for the processing thread
                queued.offer(reference.getValue().getByteArray(0, intBuffer.get(0)));

                synchronized (stats) {
                    stats[0]++;
                }

                if (Thread.interrupted()) {
                    break;
                }

            }

        } catch (Throwable t) {

            System.err.println("Andor3 Acquisition Thread Halted!");
            t.printStackTrace();

        } finally {

            acquireFPS = 0.0;
            task.stop();

        }

    }

    /**
     * Main method of the processing thread for continuous acquisition. This thread takes frames that have been
     * retrieved by the acquisition thread and performs the necessary data processing on them to turn them
     * into Frame objects before distributing them to all active listeners and queues.
     *
     * @param bufferSize The size of each frame buffer to expect, in bytes
     * @param width      The width of each image (after processing), in pixels
     * @param height     The height of each image (after processing), in pixels
     * @param stride     The stride of images returned by the acquisition thread (i.e., width including padding), in pixels
     * @param data       The data array of the internal Frame object holding the current frame
     * @param frequency  The frequency of the camera's internal clock
     * @param encoding   The encoding used by the camera for each frame
     */
    private void processing(int bufferSize, int width, int height, int stride, short[] data, long frequency, Enum encoding) {

        boolean timestampEnabled;
        try {
            timestampEnabled = isTimestampEnabled();
        } catch (Exception e) {
            timestampEnabled = false;
        }

        final long[] stats = {0, 0, System.nanoTime()};

        // Set up repeating task to calculate FPS each second on a separate thread
        RTask task = new RTask(1000, () -> {

            synchronized (stats) {

                long frames  = stats[0];
                long dFrames = frames - stats[1];
                long time    = System.nanoTime();
                long dTime   = time - stats[2];

                stats[1] = frames;
                stats[2] = time;

                this.processFPS = 1e9 * dFrames / dTime;

            }

        });

        task.start();

        try (Memory bufferMemory = new Memory(bufferSize)) {

            // Constants
            final long    nsPerTick = (long) (1e9 / frequency);
            final int     imageSize = width * height;
            final WString mono16    = new WString("Mono16");
            final WString encText   = new WString(encoding.getText());

            try (Memory convertedMemory = new Memory(imageSize * 2L)) {

                // Buffers for retrieving data
                final ByteBuffer      buffer    = bufferMemory.getByteBuffer(0, bufferSize);
                final ByteBuffer      converted = convertedMemory.getByteBuffer(0, imageSize * 2L);
                final LongByReference timeStamp = new LongByReference();

                int result;

                while (acquiring || !queued.isEmpty()) {

                    try {
                        buffer.clear().rewind().put(queued.take());
                    } catch (InterruptedException e) {
                        continue;
                    }

                    result = util.AT_ConvertBuffer(buffer.rewind(), converted.clear(), width, height, stride, encText, mono16);

                    if (result != AT_SUCCESS) {
                        System.err.printf("Error converting buffer: %d (%s)%n", result, ERROR_NAMES.getOrDefault(result, "UNKNOWN"));
                        continue;
                    }

                    converted.rewind().asShortBuffer().get(data, 0, imageSize);

                    if (timestampEnabled) {

                        result = util.AT_GetTimeStampFromMetadata(buffer.rewind(), bufferSize, timeStamp);

                        if (result == AT_SUCCESS) {
                            frameBuffer.setTimestamp(nsPerTick * timeStamp.getValue());
                        } else {
                            frameBuffer.setTimestamp(System.nanoTime());
                        }

                    } else {
                        frameBuffer.setTimestamp(System.nanoTime());
                    }

                    listenerManager.trigger(frameBuffer);

                    synchronized (stats) {
                        stats[0]++;
                    }

                }

            }

        } catch (Throwable t) {
            System.err.printf("Andor3 Processing Thread Halted: %s%n", t.getMessage());
        } finally {

            task.stop();
            processFPS = 0.0;

        }

    }

    @Override
    public synchronized void startAcquisition() throws IOException, DeviceException {

        if (isAcquiring()) {
            return;
        }

        // Get info about the frames the camera is going to spitting back to us
        final int     width      = getFrameWidth();
        final int     height     = getFrameHeight();
        final int     stride     = getInt("AOIStride");
        final int     size       = getFrameSize();
        final int     bufferSize = getInt("ImageSizeBytes");
        final short[] data       = new short[size];
        final long    frequency  = getLong("TimestampClockFrequency");
        final Enum    encoding   = getEnum("PixelEncoding");

        // Create a frame to hold the latest frame data
        frameBuffer = new Frame(data, width, height);

        // Clear the queue between the acquisition and processing threads
        queued.clear();

        // Set the camera up to acquire continuously and flush out its memory
        setEnum("CycleMode", "Continuous");
        flush();

        // Set up acquisition and processing threads
        acquisitionThread = new Thread(() -> acquisition(bufferSize));
        processingThread  = new Thread(() -> processing(bufferSize, width, height, stride, data, frequency, encoding));

        acquiring = true;

        // Set the camera rolling
        acquisitionStart();

        // Start the acquisition and processing threads
        acquisitionThread.start();
        processingThread.start();

    }

    @Override
    public synchronized void stopAcquisition() throws IOException, DeviceException {

        if (!isAcquiring()) {
            return;
        }

        // Break the while loop conditions in both threads
        acquiring = false;

        // Stop the camera rolling and flush out its memory
        acquisitionStop();
        flush();

        // Send interrupt signals to both threads to break any blocking methods
        acquisitionThread.interrupt();
        processingThread.interrupt();

        // Wait for both threads to die
        try {
            acquisitionThread.join();
            processingThread.join();
        } catch (InterruptedException ignored) { }

        // Cleanup
        acquisitionThread = null;
        processingThread  = null;
        flush();

    }

    @Override
    public synchronized boolean isAcquiring() throws IOException, DeviceException {
        return acquiring;
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
        queue.close();
    }

    @Override
    public int getFrameWidth() throws DeviceException, IOException {
        return getInt("AOIWidth");
    }

    @Override
    public int getFrameHeight() throws DeviceException, IOException {
        return getInt("AOIHeight");
    }

    @Override
    public int getFrameSize() throws DeviceException, IOException {
        return getFrameWidth() * getFrameHeight();
    }

    @Override
    public int getPhysicalFrameSize() throws IOException, DeviceException {
        return getPhysicalFrameWidth() * getPhysicalFrameHeight();
    }

    @Override
    public int getSensorWidth() throws DeviceException, IOException {
        return getInt("SensorWidth");
    }

    @Override
    public int getSensorHeight() throws DeviceException, IOException {
        return getInt("SensorHeight");
    }

    @Override
    public int getBinningX() throws DeviceException, IOException {
        return getInt("AOIHBin");
    }

    @Override
    public void setBinningX(int x) throws DeviceException, IOException {
        setInt("AOIHBin", x);
        checkSymmetricalBinning();
    }

    @Override
    public int getBinningY() throws DeviceException, IOException {
        return getInt("AOIVBin");
    }

    @Override
    public void setBinningY(int y) throws DeviceException, IOException {
        setInt("AOIVBin", y);
        checkSymmetricalBinning();
    }

    @Override
    public void setBinning(int x, int y) throws DeviceException, IOException {
        setInt("AOIHBin", x);
        setInt("AOIVBin", y);
        checkSymmetricalBinning();
    }

    @Override
    public double getPixelReadoutRate() throws IOException, DeviceException {
        return Integer.parseInt(getEnum("PixelReadoutRate").getText().replace("MHz", "").trim()) * 1e6;
    }

    @Override
    public List<Double> getPixelReadoutRates() {

        try {
            return getEnumOptions("PixelReadoutRate").stream().filter(Enum::isImplemented).map(e -> Integer.parseInt(e.getText().replace("MHz", "").trim()) * 1e6).collect(Collectors.toList());
        } catch (Exception e) {
            return Collections.emptyList();
        }

    }

    @Override
    public void setPixelReadoutRate(double rate) throws IOException, DeviceException {

        Enum option = getEnumOptions("PixelReadoutRate")
            .stream()
            .min(Comparator.comparingDouble(e -> Math.abs(rate - Integer.parseInt(e.getText().replace("MHz", "").trim()) * 1e6)))
            .orElseThrow(() -> new DeviceException("Cannot find a readout rate."));

        setEnum("PixelReadoutRate", option);

    }

    @Override
    public boolean isTimestampEnabled() throws IOException, DeviceException {
        return getBoolean("MetadataTimestamp") && getBoolean("MetadataEnable");
    }

    @Override
    public void setTimestampEnabled(boolean timestamping) throws IOException, DeviceException {

        setBoolean("MetadataTimestamp", timestamping);

        if (timestamping) {
            setBoolean("MetadataEnable", true);
        }

    }

    @Override
    public boolean isOverlapEnabled() throws IOException, DeviceException {
        return getBoolean("Overlap");
    }

    @Override
    public void setOverlapEnabled(boolean overlapEnabled) throws IOException, DeviceException {
        setBoolean("Overlap", overlapEnabled);
    }

    @Override
    public void setFrameWidth(int width) throws DeviceException, IOException {
        setInt("AOIWidth", width);
        setFrameCentredX(centreX);
    }

    @Override
    public int getPhysicalFrameWidth() throws DeviceException, IOException {
        return getFrameWidth() * getBinningX();
    }

    @Override
    public void setFrameHeight(int height) throws DeviceException, IOException {
        setInt("AOIHeight", height);
    }

    @Override
    public int getPhysicalFrameHeight() throws DeviceException, IOException {
        return getFrameHeight() * getBinningY();
    }

    @Override
    public int getFrameOffsetX() throws DeviceException, IOException {
        return getInt("AOILeft");
    }

    @Override
    public void setFrameCentredX(boolean centredX) throws DeviceException, IOException {

        if (centredX) {
            setInt("AOILeft", (int) Math.floor((getSensorWidth() - getPhysicalFrameWidth()) / 2.0));
        }

        centreX = centredX;

    }

    @Override
    public boolean isFrameCentredX() {
        return centreX;
    }

    @Override
    public void setFrameOffsetX(int offsetX) throws DeviceException, IOException {

        if (isFrameCentredX()) {
            throw new DeviceException("Cannot change frame x offset when x-centring is enabled.");
        }

        setInt("AOILeft", offsetX);

    }

    @Override
    public int getFrameOffsetY() throws DeviceException, IOException {
        return getInt("AOITop");
    }

    @Override
    public void setFrameOffsetY(int offsetY) throws DeviceException, IOException {

        if (isFrameCentredY()) {
            throw new DeviceException("Cannot change frame y offset when y-centring is enabled.");
        }

        setInt("AOITop", offsetY);

    }

    @Override
    public void setFrameCentredY(boolean centredY) throws DeviceException, IOException {
        setBoolean("VerticallyCentreAOI", centredY);
    }

    @Override
    public boolean isFrameCentredY() throws DeviceException, IOException {
        return getBoolean("VerticallyCentreAOI");
    }

    @Override
    public String getIDN() throws DeviceException, IOException {
        return getString("CameraModel");
    }

    @Override
    public String getName() {

        try {
            return getString("CameraName");
        } catch (Exception e) {
            return "AndorSDK 3 Camera";
        }

    }

    @Override
    public void close() throws DeviceException {

        int result = core.AT_Close(handle);

        if (result != AT_SUCCESS) {
            throw new DeviceException("Close failed: %d (%s)", result, ERROR_NAMES.getOrDefault(result, "UNKNOWN"));
        }

    }

    @Override
    public Address getAddress() {
        return null;
    }

    @Override
    public void setTemperatureControlEnabled(boolean enabled) throws DeviceException, IOException {
        setBoolean("SensorCooling", enabled);
        setEnum("FanSpeed", enabled ? "On" : "Off");
    }

    @Override
    public boolean isTemperatureControlEnabled() throws DeviceException, IOException {
        return getBoolean("SensorCooling");
    }

    @Override
    public void setTemperatureControlTarget(double targetTemperature) throws DeviceException, IOException {

        IntBuffer buffer = IntBuffer.allocate(1);
        core.AT_IsWritable(handle, new WString("TemperatureControl"), buffer);

        if (buffer.get(0) == 0) {
            return;
        }

        double     degC    = targetTemperature - 273.15;
        List<Enum> options = getEnumOptions("TemperatureControl");

        // Find enum option with value closest to that requested, but not larger than it
        Enum value = options.stream()
                            .map(e -> Map.entry(e, Double.parseDouble(e.getText().replaceAll("[A-z]", "").trim())))
                            .filter(v -> v.getValue() <= degC)
                            .max(Comparator.comparingDouble(Map.Entry::getValue))
                            .map(Map.Entry::getKey)
                            .orElse(null);

        if (value == null) {

            // If that failed, just use the minimum possible value
            Enum min = options.stream()
                              .map(e -> Map.entry(e, Double.parseDouble(e.getText().replaceAll("[A-z]", "").trim())))
                              .min(Comparator.comparingDouble(Map.Entry::getValue))
                              .map(Map.Entry::getKey)
                              .orElse(options.get(0));

            setEnum("TemperatureControl", min);

        } else {
            setEnum("TemperatureControl", value);
        }

    }

    @Override
    public double getTemperatureControlTarget() throws DeviceException, IOException {
        return Double.parseDouble(getEnum("TemperatureControl").getText().replaceAll("[A-z]", "").trim()) + 273.15;
    }

    @Override
    public double getControlledTemperature() throws IOException, DeviceException {
        return getFloat("SensorTemperature") + 273.15;
    }

    public boolean isTemperatureControlStable() throws DeviceException, IOException {
        return getEnum("TemperatureStatus").getText().equals("Stabilised");
    }

    @Override
    public void setMultiTrackEnabled(boolean enabled) throws IOException, DeviceException {
        setEnum("AOILayout", enabled ? "Multitrack" : "Image");
    }

    @Override
    public boolean isMultiTrackEnabled() throws IOException, DeviceException {
        return getEnum("AOILayout").getText().equalsIgnoreCase("MULTITRACK");
    }

    @Override
    public void setMultiTracks(Collection<Track> tracks) throws IOException, DeviceException {

        if (tracks.size() > 256) {
            throw new DeviceException("Maximum number of tracks is 256, %d supplied.", tracks.size());
        }

        setInt("MultitrackCount", tracks.size());

        int i = 0;
        for (Track track : tracks) {

            setInt("MultitrackSelector", i++);
            setInt("MultitrackStart", track.getStartRow());
            setInt("MultitrackEnd", track.getEndRow());
            setBoolean("MultitrackBinned", track.isBinned());

        }

    }

    @Override
    public List<Track> getMultiTracks() throws IOException, DeviceException {

        int         count  = getInt("MultitrackCount");
        List<Track> tracks = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {

            setInt("MultitrackSelector", i);

            tracks.add(new Track(
                getInt("MultitrackStart"),
                getInt("MultitrackEnd"),
                getBoolean("MultitrackBinned")
            ));

        }

        return tracks;

    }

    @Override
    public void setShutterOpen(boolean open) throws IOException, DeviceException {
        setEnum("ShutterMode", open ? "Open" : "Closed");
    }

    @Override
    public boolean isShutterOpen() throws IOException, DeviceException {
        return getBoolean("ShutterState");
    }

    @Override
    public void setShutterAuto(boolean auto) throws IOException, DeviceException {
        setEnum("ShutterMode", auto ? "Auto" : "Closed");
    }

    @Override
    public boolean isShutterAuto() throws IOException, DeviceException {
        return getEnum("ShutterMode").getText().trim().equalsIgnoreCase("Auto");
    }

    protected static class Frame extends U16Frame {

        public Frame(short[] data, int width, int height) {
            super(data, width, height);
        }

        public Frame(Short[] data, int width, int height) {
            super(data, width, height);
        }

        public Frame(int width, int height) {
            super(new short[width * height], width, height);
        }

        protected void update(short[] data, long timestamp) {
            System.arraycopy(data, 0, this.data, 0, this.data.length);
            this.timestamp = timestamp;
        }

        protected void update(short[] data) {
            update(data, System.nanoTime());
        }

    }

    public Listener<U16Frame> sendFramesTo(HeatMap drawer) {
        return addFrameListener(drawer::drawIntFrame);
    }

    public static class Enum {

        private final int     index;
        private final String  text;
        private final boolean available;
        private final boolean implemented;

        public Enum(int index, String text, boolean available, boolean implemented) {
            this.index       = index;
            this.text        = text;
            this.available   = available;
            this.implemented = implemented;
        }

        public Enum(int index, String text) {
            this(index, text, true, true);
        }

        public int getIndex() {
            return index;
        }

        public String getText() {
            return text;
        }

        public boolean isAvailable() {
            return available;
        }

        public boolean isImplemented() {
            return implemented;
        }

    }

}
