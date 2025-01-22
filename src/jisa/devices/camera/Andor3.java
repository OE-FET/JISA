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
import jisa.devices.camera.features.CMOS;
import jisa.devices.camera.features.MultiTrack;
import jisa.devices.camera.features.Overlap;
import jisa.devices.camera.frame.FrameQueue;
import jisa.devices.camera.frame.U16Frame;
import jisa.devices.camera.nat.ATCoreLibrary;
import jisa.devices.camera.nat.ATUtilityLibrary;
import jisa.devices.features.TemperatureControlled;
import jisa.visa.NativeDevice;

import java.io.IOException;
import java.nio.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class Andor3 extends NativeDevice<ATCoreLibrary> implements Camera<U16Frame>, CMOS, Overlap, MultiTrack, TemperatureControlled {

    protected final static int  AT_ERR_STRINGNOTAVAILABLE      = 18;
    protected final static int  AT_ERR_NULL_COUNT_VAR          = 30;
    protected final static int  AT_HANDLE_SYSTEM               = 1;
    protected final static int  AT_ERR_INDEXNOTIMPLEMENTED     = 8;
    protected final static int  AT_ERR_NULL_READABLE_VAR       = 23;
    protected final static int  AT_CALLBACK_SUCCESS            = 0;
    protected final static int  AT_ERR_NULL_WRITABLE_VAR       = 25;
    protected final static int  AT_ERR_NULL_QUEUE_PTR          = 34;
    protected final static int  AT_ERR_NOTWRITABLE             = 5;
    protected final static int  AT_ERR_HARDWARE_OVERFLOW       = 100;
    protected final static int  AT_ERR_BUFFERFULL              = 14;
    protected final static int  AT_ERR_COMM                    = 17;
    protected final static int  AT_ERR_NOTINITIALISED          = 1;
    protected final static int  AT_ERR_NULL_READONLY_VAR       = 24;
    protected final static int  AT_ERR_NULL_MINVALUE           = 26;
    protected final static int  AT_ERR_NULL_MAXSTRINGLENGTH    = 32;
    protected final static int  AT_ERR_NOTREADABLE             = 4;
    protected final static int  AT_ERR_NULL_WAIT_PTR           = 35;
    protected final static int  AT_ERR_NULL_EVCALLBACK         = 33;
    protected final static long AT_INFINITE                    = 0xFFFFFFFFL;
    protected final static int  AT_ERR_OUTOFRANGE              = 6;
    protected final static int  AT_ERR_STRINGNOTIMPLEMENTED    = 19;
    protected final static int  AT_ERR_READONLY                = 3;
    protected final static int  AT_ERR_EXCEEDEDMAXSTRINGLENGTH = 9;
    protected final static int  AT_TRUE                        = 1;
    protected final static int  AT_ERR_INDEXNOTAVAILABLE       = 7;
    protected final static int  AT_ERR_CONNECTION              = 10;
    protected final static int  AT_FALSE                       = 0;
    protected final static int  AT_HANDLE_UNINITIALISED        = -1;
    protected final static int  AT_ERR_NULL_PTRSIZE            = 36;
    protected final static int  AT_ERR_INVALIDALIGNMENT        = 16;
    protected final static int  AT_ERR_INVALIDHANDLE           = 12;
    protected final static int  AT_ERR_NULL_STRING             = 29;
    protected final static int  AT_ERR_NULL_MAXVALUE           = 27;
    protected final static int  AT_ERR_NOTIMPLEMENTED          = 2;
    protected final static int  AT_ERR_NULL_IMPLEMENTED_VAR    = 22;
    protected final static int  AT_ERR_INVALIDSIZE             = 15;
    protected final static int  AT_ERR_NULL_HANDLE             = 21;
    protected final static int  AT_ERR_DEVICEINUSE             = 38;
    protected final static int  AT_ERR_NULL_VALUE              = 28;
    protected final static int  AT_ERR_NOMEMORY                = 37;
    protected final static int  AT_ERR_NODATA                  = 11;
    protected final static int  AT_ERR_TIMEDOUT                = 13;
    protected final static int  AT_ERR_DEVICENOTFOUND          = 39;
    protected final static int  AT_SUCCESS                     = 0;
    protected final static int  AT_ERR_NULL_FEATURE            = 20;
    protected final static int  AT_ERR_NULL_ISAVAILABLE_VAR    = 31;

    protected final static Map<Integer, String> ERROR_NAMES =
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

    private final int handle;

    private final ListenerManager<U16Frame> listenerManager = new ListenerManager<U16Frame>();

    private final Object                queuedLock        = new Object();
    private final BlockingQueue<byte[]> queued            = new LinkedBlockingQueue<>();
    private       Frame                 frameBuffer       = null;
    private       boolean               centreX           = false;
    private       int                   timeout           = 0;
    private       boolean               acquiring         = false;
    private       Thread                acquisitionThread = null;
    private       Thread                processingThread  = null;
    private       double                acquireFPS        = 0;
    private       double                processFPS        = 0;

    private final ATUtilityLibrary utilityLibrary;

    @Override
    public List<Parameter<?>> getInstrumentParameters(Class<?> target) {

        ParameterList parameters = new ParameterList();

        parameters.addValue("Fast AOI Readout mode", this::isFastAOIFrameRateEnabled, false, this::setFastAOIFrameRateEnabled);

        return parameters;

    }

    public Andor3(Address address) throws DeviceException {

        super("atcore", ATCoreLibrary.getInstance());

        utilityLibrary = ATUtilityLibrary.getInstance();

        if (!(address instanceof IDAddress)) {
            throw new DeviceException("Address must be an instance of IDAddress");
        }

        try {
            handle = open(Integer.parseInt(((IDAddress) address).getID()));
        } catch (NumberFormatException e) {
            throw new DeviceException("ID must be an integer");
        }

        setEnum("TriggerMode", "Internal");
        setBoolean("RollingShutterGlobalClear", false);

    }

    public Andor3(int cameraIndex) throws DeviceException {

        super("atcore", ATCoreLibrary.getInstance());
        utilityLibrary = ATUtilityLibrary.getInstance();
        handle = open(cameraIndex);

        setEnum("TriggerMode", "Internal");
        setBoolean("RollingShutterGlobalClear", false);

    }

    private synchronized int open(int cameraIndex) throws DeviceException {
        
        IntBuffer intBuffer = IntBuffer.allocate(1);

        int result = nativeLibrary.AT_Open(cameraIndex, intBuffer.clear());

        if (result != AT_SUCCESS) {
            throw new DeviceException("Open failed: %d (%s)", result, ERROR_NAMES.getOrDefault(result, "UNKNOWN"));
        }

        return intBuffer.get(0);

    }

    protected synchronized String getString(String feature) throws DeviceException {

        CharBuffer charBuffer = CharBuffer.allocate(1024);

        int result = nativeLibrary.AT_GetString(handle, new WString(feature), charBuffer.clear(), 1024);

        if (result != AT_SUCCESS) {
            throw new DeviceException("GetString for feature \"%s\" failed: %d (%s)", feature, result, ERROR_NAMES.getOrDefault(result, "UNKNOWN"));
        }

        return charBuffer.rewind().toString().trim();

    }

    protected synchronized void setString(String feature, String value) throws DeviceException {

        int result = nativeLibrary.AT_SetString(handle, new WString(feature), new WString(value));

        if (result != AT_SUCCESS) {
            throw new DeviceException("SetString for feature \"%s\" failed: %d (%s)", feature, result, ERROR_NAMES.getOrDefault(result, "UNKNOWN"));
        }

    }

    protected synchronized double getFloat(String feature) throws DeviceException {

        DoubleBuffer doubleBuffer = DoubleBuffer.allocate(1);

        int result = nativeLibrary.AT_GetFloat(handle, new WString(feature), doubleBuffer.clear());

        if (result != AT_SUCCESS) {
            throw new DeviceException("GetFloat for feature \"%s\" failed: %d (%s)", feature, result, ERROR_NAMES.getOrDefault(result, "UNKNOWN"));
        }

        return doubleBuffer.get(0);

    }

    protected synchronized void setFloat(String feature, double value) throws DeviceException {

        int result = nativeLibrary.AT_SetFloat(handle, new WString(feature), value);

        if (result != AT_SUCCESS) {
            throw new DeviceException("SetFloat for feature \"%s\" failed: %d (%s)", feature, result, ERROR_NAMES.getOrDefault(result, "UNKNOWN"));
        }

    }

    protected synchronized long getLong(String feature) throws DeviceException {

        LongBuffer longBuffer = LongBuffer.allocate(1);

        int result = nativeLibrary.AT_GetInt(handle, new WString(feature), longBuffer.clear());

        if (result != AT_SUCCESS) {
            throw new DeviceException("GetInt for feature \"%s\" failed: %d (%s)", feature, result, ERROR_NAMES.getOrDefault(result, "UNKNOWN"));
        }

        return longBuffer.get(0);

    }

    protected synchronized int getInt(String feature) throws DeviceException {
        return (int) getLong(feature);
    }

    protected synchronized void setLong(String feature, long value) throws DeviceException {

        int result = nativeLibrary.AT_SetInt(handle, new WString(feature), value);

        if (result != AT_SUCCESS) {
            throw new DeviceException("SetInt for feature \"%s\" failed: %d (%s)", feature, result, ERROR_NAMES.getOrDefault(result, "UNKNOWN"));
        }

    }

    protected synchronized void setInt(String feature, int value) throws DeviceException {
        setLong(feature, value);
    }

    protected synchronized boolean getBoolean(String feature) throws DeviceException {

        IntBuffer intBuffer = IntBuffer.allocate(1);

        int result = nativeLibrary.AT_GetBool(handle, new WString(feature), intBuffer.clear());

        if (result != AT_SUCCESS) {
            throw new DeviceException("GetBool for feature \"%s\" failed: %d (%s)", feature, result, ERROR_NAMES.getOrDefault(result, "UNKNOWN"));
        }

        return intBuffer.get(0) == 1;

    }

    protected synchronized void setBoolean(String feature, boolean value) throws DeviceException {

        int result = nativeLibrary.AT_SetBool(handle, new WString(feature), value ? 1 : 0);

        if (result != AT_SUCCESS) {
            throw new DeviceException("SetBool for feature \"%s\" failed: %d (%s)", feature, result, ERROR_NAMES.getOrDefault(result, "UNKNOWN"));
        }

    }

    protected synchronized Enum getEnum(String feature) throws DeviceException {

        IntBuffer intBuffer = IntBuffer.allocate(1);

        WString wFeature = new WString(feature);
        int     result   = nativeLibrary.AT_GetEnumIndex(handle, wFeature, intBuffer.clear());

        if (result != AT_SUCCESS) {
            throw new DeviceException("GetEnumIndex for feature \"%s\" failed: %d (%s)", feature, result, ERROR_NAMES.getOrDefault(result, "UNKNOWN"));
        }

        CharBuffer buffer = CharBuffer.allocate(1024);

        result = nativeLibrary.AT_GetEnumStringByIndex(handle, wFeature, intBuffer.get(0), buffer, 1024);

        if (result != AT_SUCCESS) {
            throw new DeviceException("GetEnumStringByIndex for feature \"%s\" failed: %d (%s)", feature, result, ERROR_NAMES.getOrDefault(result, "UNKNOWN"));
        }

        return new Enum(intBuffer.get(0), buffer.rewind().toString().trim());

    }

    protected synchronized List<Enum> getEnumOptions(String feature) throws DeviceException {

        IntBuffer  intBuffer  = IntBuffer.allocate(1);
        WString    wFeature   = new WString(feature);
        int        result     = nativeLibrary.AT_GetEnumCount(handle, wFeature, intBuffer.clear());

        if (result != AT_SUCCESS) {
            throw new DeviceException("GetEnumCount for feature \"%s\" failed: %d (%s)", feature, result, ERROR_NAMES.getOrDefault(result, "UNKNOWN"));
        }

        int        count = intBuffer.get(0);
        List<Enum> list  = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {

            CharBuffer charBuffer = CharBuffer.allocate(1024);

            result = nativeLibrary.AT_GetEnumStringByIndex(handle, wFeature, i, charBuffer.clear(), 1024);

            if (result != AT_SUCCESS) {
                throw new DeviceException("GetEnumStringByIndex for feature \"%s\", index %d, failed: %d (%s)", feature, i, result, ERROR_NAMES.getOrDefault(result, "UNKNOWN"));
            }

            String text = charBuffer.rewind().toString().trim();

            result = nativeLibrary.AT_IsEnumeratedIndexAvailable(handle, wFeature, i, intBuffer.clear());

            if (result != AT_SUCCESS) {
                throw new DeviceException("IsEnumeratedIndexAvailable for feature \"%s\", index %d, failed:%d (%s)", feature, i, result, ERROR_NAMES.getOrDefault(result, "UNKNOWN"));
            }

            boolean available = intBuffer.get(0) == 1;

            result = nativeLibrary.AT_IsEnumIndexImplemented(handle, wFeature, i, intBuffer.clear());

            if (result != AT_SUCCESS) {
                throw new DeviceException("IsEnumIndexImplemented for feature \"%s\", index %d, failed: %d (%s)", feature, i, result, ERROR_NAMES.getOrDefault(result, "UNKNOWN"));
            }

            boolean implemented = intBuffer.get(0) == 1;

            list.add(new Enum(i, text, available, implemented));

        }

        return list;

    }

    protected synchronized void setEnum(String feature, int index) throws DeviceException {

        int result = nativeLibrary.AT_SetEnumIndex(handle, new WString(feature), index);

        if (result != AT_SUCCESS) {
            throw new DeviceException("SetEnumIndex for feature \"%s\" failed: %d (%s)", feature, result, ERROR_NAMES.getOrDefault(result, "UNKNOWN"));
        }

    }

    protected synchronized void setEnum(String feature, Enum value) throws DeviceException {
        setEnum(feature, value.getIndex());
    }

    protected synchronized void setEnum(String feature, String text) throws DeviceException {

        List<Enum> available = getEnumOptions(feature);
        Enum       match     = available.stream().filter(e -> e.getText().trim().equalsIgnoreCase(text.trim())).findFirst().orElse(null);

        if (match == null) {
            throw new DeviceException("Invalid enum option \"%s\" for feature \"%s\" (Available: %s)", text, feature, available.stream().map(Enum::getText).collect(Collectors.joining(", ")));
        }

        setEnum(feature, match);

    }

    protected synchronized void command(String feature) throws DeviceException {

        int result = nativeLibrary.AT_Command(handle, new WString(feature));

        if (result != AT_SUCCESS) {
            throw new DeviceException("Command \"%s\" failed: %d (%s)", feature, result, ERROR_NAMES.getOrDefault(result, "UNKNOWN"));
        }

    }

    protected void flush() throws DeviceException {

        int result = nativeLibrary.AT_Flush(handle);

        if (result != AT_SUCCESS) {
            throw new DeviceException("Flush failed: %d (%s)", result, ERROR_NAMES.getOrDefault(result, "UNKNOWN"));
        }

    }

    protected synchronized void acquisitionStart() throws DeviceException {
        command("AcquisitionStart");
    }

    protected synchronized void acquisitionStop() throws DeviceException {
        command("AcquisitionStop");
    }

    protected void checkSymmetricalBinning() throws DeviceException {

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

    public int getFrameBinning() throws IOException, DeviceException {
        return getInt("AccumulateCount");
    }

    public void setFrameBinning(int count) throws IOException, DeviceException {
        setInt("AccumulateCount", count);
    }

    @Override
    public U16Frame getFrame() throws DeviceException, IOException, InterruptedException, TimeoutException {

        // If we're already continuously acquiring, we can just open a FrameQueue and wait for the next frame from that.
        if (isAcquiring()) {

            FrameQueue<U16Frame> queue = openFrameQueue();
            U16Frame             frame = queue.nextFrame(timeout);

            queue.close();
            queue.clear();

            return frame;

        }

        final long    tmo        = (timeout == Integer.MAX_VALUE || timeout < 1) ? AT_INFINITE : timeout;
        final int     bufferSize = getInt("ImageSizeBytes");
        final long    startTime  = System.nanoTime();
        final long    startClock = getLong("TimestampClock");
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

        ByteBuffer frameBuffer = ByteBuffer.allocateDirect(bufferSize);

        flush();

        int result = nativeLibrary.AT_QueueBuffer(handle, frameBuffer.clear().rewind(), bufferSize);

        if (result != AT_SUCCESS) {
            throw new DeviceException("QueueBuffer failed: %d (%s)", result, ERROR_NAMES.getOrDefault(result, "UNKNOWN"));
        }


        PointerByReference reference = new PointerByReference();
        IntBuffer          intBuffer = IntBuffer.allocate(1);

        acquisitionStart();
        result = nativeLibrary.AT_WaitBuffer(handle, reference, intBuffer, tmo);
        acquisitionStop();

        flush();

        if (result != AT_SUCCESS) {

            if (result == AT_ERR_TIMEDOUT) {
                throw new TimeoutException("Timed out waiting for frame from Andor SDK3 camera.");
            }

            throw new DeviceException("WaitBuffer failed: %d (%s)", result, ERROR_NAMES.getOrDefault(result, "UNKNOWN"));

        }

        long returnedSize = Integer.toUnsignedLong(intBuffer.get(0));

        ByteBuffer returned  = reference.getValue().getByteBuffer(0, returnedSize);
        ByteBuffer converted = ByteBuffer.allocateDirect(imageSize * 2);

        System.out.println(returned.isDirect());
        System.out.println(converted.isDirect());

        result = utilityLibrary.AT_ConvertBufferUsingMetadata(returned.rewind(), converted.clear().rewind(), returnedSize, mono16);

        if (result != AT_SUCCESS) {

            result = utilityLibrary.AT_ConvertBuffer(returned.rewind(), converted.clear().rewind(), width, height, stride, encText, mono16);

            if (result != AT_SUCCESS) {
                throw new DeviceException("ConvertBuffer failed: %d (%s)", result, ERROR_NAMES.getOrDefault(result, "UNKNOWN"));
            }

        }

        LongByReference timeStampBuffer = new LongByReference();

        result = utilityLibrary.AT_GetTimeStampFromMetadata(returned.rewind(), returnedSize, timeStampBuffer);

        Frame frame = new Frame(width, height);
        converted.asShortBuffer().get(frame.array(), 0, imageSize);

        if (result != AT_SUCCESS) {
            frame.setTimestamp(startTime + (nsPerTick * (timeStampBuffer.getValue() - startClock)));
        }

        flush();

        return frame;

    }

    @Override
    public List<U16Frame> getFrameSeries(int count) throws DeviceException, IOException, InterruptedException, TimeoutException {

        // If we're already continuously acquiring, we can just open a FrameQueue and wait for the next n frames from that
        if (isAcquiring()) {

            List<U16Frame>       frames = new ArrayList<>(count);
            FrameQueue<U16Frame> queue  = openFrameQueue();

            for (int i = 0; i < count; i++) {
                frames.add(queue.nextFrame(timeout));
            }

            queue.close();
            queue.clear();

            return frames;

        }

        // Check for values that might indicate no timeout
        long tmo = (timeout == Integer.MAX_VALUE || timeout < 1) ? AT_INFINITE : timeout;

        long  bufferSize = getLong("ImageSizeBytes");
        int  width      = getInt("AOIWidth");
        int  height     = getInt("AOIHeight");
        int  stride     = getInt("AOIStride");
        int  imageSize  = width * height;
        long startTime  = System.nanoTime();
        long startClock = getLong("TimestampClock");
        long frequency  = getLong("TimestampClockFrequency");
        long nsPerTick  = (long) (1e9 / frequency);
        Enum encoding   = getEnum("PixelEncoding");
        WString mono16  = new WString("Mono16");
        WString encText = new WString(encoding.getText());
        setEnum("CycleMode", "Fixed");
        setLong("FrameCount", count);

        flush();

        for (int i = 0; i < count; i++) {

            ByteBuffer frameBuffer = ByteBuffer.allocateDirect((int) bufferSize);

            int result;

            synchronized (this) {
                 result = nativeLibrary.AT_QueueBuffer(handle, frameBuffer.clear().rewind(), (int) bufferSize);
            }

            if (result != AT_SUCCESS) {
                throw new DeviceException("QueueBuffer failed: %d (%s)", result, ERROR_NAMES.getOrDefault(result, "UNKNOWN"));
            }

        }

        acquisitionStart();

        Pointer[] pointers  = new Pointer[count];
        IntBuffer intBuffer = IntBuffer.allocate(count);

        for (int i = 0; i < count; i++) {

            PointerByReference pointer = new PointerByReference();

            int result;

            synchronized (this) {
                result = nativeLibrary.AT_WaitBuffer(handle, pointer, intBuffer.clear(), tmo);
            }

            if (result != AT_SUCCESS) {
                throw new DeviceException("WaitBuffer failed: %d (%s)", result, ERROR_NAMES.getOrDefault(result, "UNKNOWN"));
            }

            pointers[i] = pointer.getValue();

            if (Thread.interrupted()) {
                throw new InterruptedException();
            }

        }

        acquisitionStop();
        flush();

        List<U16Frame> frames = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {

            ByteBuffer returned  = pointers[i].getByteBuffer(0, bufferSize);
            ByteBuffer converted = ByteBuffer.allocateDirect(imageSize * 2);

            int result = utilityLibrary.AT_ConvertBufferUsingMetadata(returned.rewind(), converted.clear().rewind(), bufferSize, mono16);

            if (result != AT_SUCCESS) {

                result = utilityLibrary.AT_ConvertBuffer(returned.rewind(), converted.clear().rewind(), width, height, stride, encText, mono16);

                if (result != AT_SUCCESS) {
                    throw new DeviceException("ConvertBuffer failed: %d (%s)", result, ERROR_NAMES.getOrDefault(result, "UNKNOWN"));
                }

            }

            LongByReference timeStampBuffer = new LongByReference();


            result = utilityLibrary.AT_GetTimeStampFromMetadata(returned.rewind(), bufferSize, timeStampBuffer);

            short[] frameData = new short[imageSize];
            converted.asShortBuffer().get(frameData, 0, imageSize);

            Frame frame = new Frame(frameData, width, height);

            if (result != AT_SUCCESS) {
                frame.setTimestamp(startTime + (nsPerTick * (timeStampBuffer.getValue() - startClock)));
            }

            frames.add(frame);

        }

        return frames;

    }

    public double getAcquisitionFPS() {

        synchronized (queuedLock) {
            return acquireFPS;
        }

    }

    public double getProcessingFPS() {

        synchronized (queuedLock) {
            return processFPS;
        }

    }

    @Override
    public void setAcquisitionTimeout(int timeout) throws IOException, DeviceException {
        this.timeout = Math.max(0, timeout);
    }

    @Override
    public int getAcquisitionTimeout() throws IOException, DeviceException {
        return timeout;
    }

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

                result = nativeLibrary.AT_QueueBuffer(handle, frameBuffer.clear(), bufferSize);

                if (result != AT_SUCCESS) {
                    System.err.printf("Error queueing buffer: %d (%s)%n", result, ERROR_NAMES.getOrDefault(result, "UNKNOWN"));
                    continue;
                }

                if (tmo == AT_INFINITE) {

                    // Infinite timeout is a problem because we can't interrupt it, so let's do a loooop
                    do {

                        if (Thread.interrupted()) {
                            break acq;
                        }

                        result = nativeLibrary.AT_WaitBuffer(handle, reference, intBuffer.clear(), ftm);

                    } while (result == AT_ERR_TIMEDOUT);

                } else {
                    result = nativeLibrary.AT_WaitBuffer(handle, reference, intBuffer.clear(), tmo);
                }

                if (result != AT_SUCCESS) {

                    if (result == AT_ERR_NODATA) {
                        break;
                    }

                    System.err.printf("Error waiting for buffer: %d (%s)%n", result, ERROR_NAMES.getOrDefault(result, "UNKNOWN"));

                    flush();
                    continue;

                }

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

            synchronized (queuedLock) {
                acquireFPS = 0.0;
            }

            task.stop();

        }

    }

    private void processing(int bufferSize, int width, int height, int stride, short[] data, long startClock, long startTime, long frequency, Enum encoding) {

        boolean timestampEnabled;
        try {
             timestampEnabled = isTimestampEnabled();
        } catch (Exception e) {
            timestampEnabled = false;
        }

        final long[] stats = {0, 0, System.nanoTime()};

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

                    result = utilityLibrary.AT_ConvertBuffer(buffer.rewind(), converted.clear(), width, height, stride, encText, mono16);

                    if (result != AT_SUCCESS) {
                        System.err.printf("Error converting buffer: %d (%s)%n", result, ERROR_NAMES.getOrDefault(result, "UNKNOWN"));
                        continue;
                    }

                    converted.rewind().asShortBuffer().get(data, 0, imageSize);

                    if (timestampEnabled) {

                        result = utilityLibrary.AT_GetTimeStampFromMetadata(buffer.rewind(), bufferSize, timeStamp);

                        if (result == AT_SUCCESS) {
                            frameBuffer.setTimestamp(startTime + (nsPerTick * (timeStamp.getValue() - startClock)));
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

            System.err.println("Andor3 Processing Thread Halted!");
            t.printStackTrace();

        } finally {

            synchronized (queuedLock) {
                processFPS = 0.0;
            }

            task.stop();
        }

    }

    @Override
    public synchronized void startAcquisition() throws IOException, DeviceException {

        if (isAcquiring()) {
            return;
        }

        final int     width      = getFrameWidth();
        final int     height     = getFrameHeight();
        final int     stride     = getInt("AOIStride");
        final int     size       = getFrameSize();
        final int     bufferSize = getInt("ImageSizeBytes");
        final short[] data       = new short[size];
        final long    clock      = getLong("TimestampClock");
        final long    time       = System.nanoTime();
        final long    frequency  = getLong("TimestampClockFrequency");
        final Enum    encoding   = getEnum("PixelEncoding");

        frameBuffer = new Frame(data, width, height);
        queued.clear();

        setEnum("CycleMode", "Continuous");
        flush();

        acquisitionThread = new Thread(() -> acquisition(bufferSize));
        processingThread  = new Thread(() -> processing(bufferSize, width, height, stride, data, clock, time, frequency, encoding));

        acquiring = true;

        acquisitionStart();

        acquisitionThread.start();
        processingThread.start();

    }

    @Override
    public synchronized void stopAcquisition() throws IOException, DeviceException {

        if (!isAcquiring()) {
            return;
        }

        acquiring = false;

        acquisitionStop();
        flush();

        acquisitionThread.interrupt();
        processingThread.interrupt();

        try {
            acquisitionThread.join();
            processingThread.join();
        } catch (InterruptedException ignored) { }

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
    public int getFrameWidth() throws DeviceException {
        return getInt("AOIWidth");
    }

    @Override
    public int getFrameHeight() throws DeviceException {
        return getInt("AOIHeight");
    }

    @Override
    public int getFrameSize() throws DeviceException {
        return getFrameWidth() * getFrameHeight();
    }

    @Override
    public int getPhysicalFrameSize() throws IOException, DeviceException {
        return getPhysicalFrameWidth() * getPhysicalFrameHeight();
    }

    @Override
    public int getSensorWidth() throws DeviceException {
        return getInt("SensorWidth");
    }

    @Override
    public int getSensorHeight() throws DeviceException {
        return getInt("SensorHeight");
    }

    @Override
    public int getBinningX() throws DeviceException {
        return getInt("AOIHBin");
    }

    @Override
    public void setBinningX(int x) throws DeviceException {
        setInt("AOIHBin", x);
        checkSymmetricalBinning();
    }

    @Override
    public int getBinningY() throws DeviceException {
        return getInt("AOIVBin");
    }

    @Override
    public void setBinningY(int y) throws DeviceException {
        setInt("AOIVBin", y);
        checkSymmetricalBinning();
    }

    @Override
    public void setBinning(int x, int y) throws DeviceException {
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
        } catch (DeviceException e) {
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
    public boolean isAlternatingReadoutEnabled() throws IOException, DeviceException {
        return getBoolean("AlternatingReadoutDirection");
    }

    @Override
    public void setAlternatingReadoutEnabled(boolean enabled) throws IOException, DeviceException {
        setBoolean("AlternatingReadoutDirection", enabled);
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
    public void setFrameWidth(int width) throws DeviceException {
        setInt("AOIWidth", width);
    }

    @Override
    public int getPhysicalFrameWidth() throws DeviceException {
        return getFrameWidth() * getBinningX();
    }

    @Override
    public void setFrameHeight(int height) throws DeviceException {
        setInt("AOIHeight", height);
    }

    @Override
    public int getPhysicalFrameHeight() throws DeviceException {
        return getFrameHeight() * getBinningY();
    }

    @Override
    public int getFrameOffsetX() throws DeviceException {
        return getInt("AOILeft");
    }

    @Override
    public void setFrameCentredX(boolean centredX) throws DeviceException {

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
    public void setFrameOffsetX(int offsetX) throws DeviceException {

        if (isFrameCentredX()) {
            throw new DeviceException("Cannot change frame x offset when x-centring is enabled.");
        }

        setInt("AOILeft", offsetX);

    }

    @Override
    public int getFrameOffsetY() throws DeviceException {
        return getInt("AOITop");
    }

    @Override
    public void setFrameOffsetY(int offsetY) throws DeviceException {

        if (isFrameCentredY()) {
            throw new DeviceException("Cannot change frame y offset when y-centring is enabled.");
        }

        setInt("AOITop", offsetY);

    }

    @Override
    public void setFrameCentredY(boolean centredY) throws DeviceException {
        setBoolean("VerticallyCentreAOI", centredY);
    }

    @Override
    public boolean isFrameCentredY() throws DeviceException {
        return getBoolean("VerticallyCentreAOI");
    }

    @Override
    public String getIDN() throws DeviceException {
        return getString("CameraModel");
    }

    @Override
    public String getName() {

        try {
            return getString("CameraName");
        } catch (DeviceException e) {
            return "AndorSDK 3 Camera";
        }

    }

    @Override
    public void close() throws DeviceException {

        int result = nativeLibrary.AT_Close(handle);

        if (result != AT_SUCCESS) {
            throw new DeviceException("Close failed: %d (%s)", result, ERROR_NAMES.getOrDefault(result, "UNKNOWN"));
        }

    }

    @Override
    public Address getAddress() {
        return null;
    }

    @Override
    public void setTemperatureControlEnabled(boolean enabled) throws DeviceException {
        setBoolean("SensorCooling", enabled);
        setEnum("FanSpeed", enabled ? "On" : "Off");
    }

    @Override
    public boolean isTemperatureControlEnabled() throws DeviceException {
        return getBoolean("SensorCooling");
    }

    @Override
    public void setTemperatureControlTarget(double targetTemperature) throws DeviceException {

        IntBuffer buffer = IntBuffer.allocate(1);
        nativeLibrary.AT_IsWritable(handle, new WString("TemperatureControl"), buffer);

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
    public double getTemperatureControlTarget() throws DeviceException {
        return Double.parseDouble(getEnum("TemperatureControl").getText().replaceAll("[A-z]", "").trim()) + 273.15;
    }

    @Override
    public double getControlledTemperature() throws IOException, DeviceException {
        return getFloat("SensorTemperature") + 273.15;
    }

    public boolean isTemperatureControlStable() throws DeviceException {
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
