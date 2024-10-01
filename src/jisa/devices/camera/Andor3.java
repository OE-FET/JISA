package jisa.devices.camera;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.google.common.primitives.Shorts;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.ptr.PointerByReference;
import jisa.Util;
import jisa.addresses.Address;
import jisa.addresses.IDAddress;
import jisa.devices.DeviceException;
import jisa.devices.camera.frame.FrameQueue;
import jisa.devices.camera.frame.U16Frame;
import jisa.devices.camera.nat.ATCoreLibrary;
import jisa.devices.features.TemperatureControlled;
import jisa.visa.NativeDevice;

import java.io.IOException;
import java.nio.*;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class Andor3 extends NativeDevice<ATCoreLibrary> implements MTCamera<U16Frame>, TemperatureControlled {

    static {
        ATCoreLibrary.INSTANCE.AT_InitialiseLibrary();
    }

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

    protected final static Map<Integer, String> ERROR_NAMES = Util.buildMap(map -> {

        map.put(AT_ERR_STRINGNOTAVAILABLE, "ERR_STRINGNOTAVAILABLE");
        map.put(AT_ERR_NULL_COUNT_VAR, "ERR_NULL_COUNT_VAR");
        map.put(AT_ERR_INDEXNOTIMPLEMENTED, "ERR_INDEXNOTIMPLEMENTED");
        map.put(AT_ERR_NULL_READABLE_VAR, "ERR_NULL_READABLE_VAR");
        map.put(AT_ERR_NULL_WRITABLE_VAR, "ERR_NULL_WRITABLE_VAR");
        map.put(AT_ERR_NULL_QUEUE_PTR, "ERR_NULL_QUEUE_PTR");
        map.put(AT_ERR_NOTWRITABLE, "ERR_NOTWRITABLE");
        map.put(AT_ERR_HARDWARE_OVERFLOW, "ERR_HARDWARE_OVERFLOW");
        map.put(AT_ERR_BUFFERFULL, "ERR_BUFFERFULL");
        map.put(AT_ERR_COMM, "ERR_COMM");
        map.put(AT_ERR_NOTINITIALISED, "ERR_NOTINITIALISED");
        map.put(AT_ERR_NULL_READONLY_VAR, "ERR_NULL_READONLY_VAR");
        map.put(AT_ERR_NULL_MINVALUE, "ERR_NULL_MINVALUE");
        map.put(AT_ERR_NULL_MAXSTRINGLENGTH, "ERR_NULL_MAXSTRINGLENGTH");
        map.put(AT_ERR_NOTREADABLE, "ERR_NOTREADABLE");
        map.put(AT_ERR_NULL_WAIT_PTR, "ERR_NULL_WAIT_PTR");
        map.put(AT_ERR_NULL_EVCALLBACK, "ERR_NULL_EVCALLBACK");
        map.put(AT_ERR_OUTOFRANGE, "ERR_OUTOFRANGE");
        map.put(AT_ERR_STRINGNOTIMPLEMENTED, "ERR_STRINGNOTIMPLEMENTED");
        map.put(AT_ERR_READONLY, "ERR_READONLY");
        map.put(AT_ERR_EXCEEDEDMAXSTRINGLENGTH, "ERR_EXCEEDEDMAXSTRINGLENGTH");
        map.put(AT_ERR_INDEXNOTAVAILABLE, "ERR_INDEXNOTAVAILABLE");
        map.put(AT_ERR_CONNECTION, "ERR_CONNECTION");
        map.put(AT_ERR_NULL_PTRSIZE, "ERR_NULL_PTRSIZE");
        map.put(AT_ERR_INVALIDALIGNMENT, "ERR_INVALIDALIGNMENT");
        map.put(AT_ERR_INVALIDHANDLE, "ERR_INVALIDHANDLE");
        map.put(AT_ERR_NULL_STRING, "ERR_NULL_STRING");
        map.put(AT_ERR_NULL_MAXVALUE, "ERR_NULL_MAXVALUE");
        map.put(AT_ERR_NOTIMPLEMENTED, "ERR_NOTIMPLEMENTED");
        map.put(AT_ERR_NULL_IMPLEMENTED_VAR, "ERR_NULL_IMPLEMENTED_VAR");
        map.put(AT_ERR_INVALIDSIZE, "ERR_INVALIDSIZE");
        map.put(AT_ERR_NULL_HANDLE, "ERR_NULL_HANDLE");
        map.put(AT_ERR_DEVICEINUSE, "ERR_DEVICEINUSE");
        map.put(AT_ERR_NULL_VALUE, "ERR_NULL_VALUE");
        map.put(AT_ERR_NOMEMORY, "ERR_NOMEMORY");
        map.put(AT_ERR_NODATA, "ERR_NODATA");
        map.put(AT_ERR_TIMEDOUT, "ERR_TIMEDOUT");
        map.put(AT_ERR_DEVICENOTFOUND, "ERR_DEVICENOTFOUND");
        map.put(AT_ERR_NULL_FEATURE, "ERR_NULL_FEATURE");
        map.put(AT_ERR_NULL_ISAVAILABLE_VAR, "ERR_NULL_ISAVAILABLE_VAR");

    });

    private final int handle;

    private final LongBuffer   longBuffer   = LongBuffer.allocate(1);
    private final IntBuffer    intBuffer    = IntBuffer.allocate(1);
    private final DoubleBuffer doubleBuffer = DoubleBuffer.allocate(1);
    private final CharBuffer   charBuffer   = CharBuffer.allocate(1024);

    private final ListenerManager<U16Frame> listenerManager = new ListenerManager<U16Frame>();

    private final Object     queuedLock        = new Object();
    private       ByteBuffer queued            = null;
    private       Frame      frameBuffer       = null;
    private       boolean    centreX           = false;
    private       int        timeout           = 0;
    private       boolean    acquiring         = false;
    private       Thread     acquisitionThread = null;
    private       Thread     processingThread  = null;

    public Andor3(Address address) throws DeviceException {

        super("atcore", ATCoreLibrary.INSTANCE);

        if (!(address instanceof IDAddress)) {
            throw new DeviceException("Address must be an instance of IDAddress");
        }

        try {
            handle = open(Integer.parseInt(((IDAddress) address).getID()));
        } catch (NumberFormatException e) {
            throw new DeviceException("ID must be an integer");
        }

        setBoolean("MetadataEnable", true);
        setBoolean("MetadataFrameInfo", false);
        setBoolean("MetadataTimestamp", true);
        setEnum("PixelEncoding", "Mono16");

    }

    public Andor3(int cameraIndex) throws DeviceException {
        super("atcore", ATCoreLibrary.INSTANCE);
        handle = open(cameraIndex);
    }

    protected static void fillFrame(byte[] buffer, Frame frameBuffer, int stride, long startClock, long startTime, long frequency) {

        frameBuffer.setTimestamp(System.nanoTime());

        final long    nsPerTick  = (long) (1e9 / frequency);
        final int     bufferSize = buffer.length;
        final short[] data       = frameBuffer.array();
        final int     height     = frameBuffer.getHeight();
        final int     width      = frameBuffer.getWidth();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                data[y * width + x] = Shorts.fromBytes(buffer[2 * (y * stride + x) + 1], buffer[2 * (y * stride + x)]);
            }
        }

        int length;
        int cid;

        for (int i = bufferSize - 1; i >= data.length; i -= length) {

            length = Ints.fromBytes(buffer[i], buffer[i - 1], buffer[i - 2], buffer[i - 3]);
            cid    = Ints.fromBytes(buffer[i - 4], buffer[i - 5], buffer[i - 6], buffer[i - 7]);

            if (cid == 1) {

                long ticks = Longs.fromBytes(
                    buffer[i - 8],
                    buffer[i - 9],
                    buffer[i - 10],
                    buffer[i - 11],
                    buffer[i - 12],
                    buffer[i - 13],
                    buffer[i - 14],
                    buffer[i - 15]
                );

                frameBuffer.setTimestamp(startTime + (nsPerTick * (ticks - startClock)));

                break;

            }

        }

    }

    private synchronized int open(int cameraIndex) throws DeviceException {

        int result = nativeLibrary.AT_Open(cameraIndex, intBuffer.clear());

        if (result != AT_SUCCESS) {
            throw new DeviceException("Open failed: %d (%s)", result, ERROR_NAMES.getOrDefault(result, "UNKNOWN"));
        }

        return intBuffer.get(0);

    }

    protected synchronized String getString(String feature) throws DeviceException {

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

        WString wFeature = new WString(feature);
        int     result   = nativeLibrary.AT_GetEnumIndex(handle, wFeature, intBuffer.clear());

        if (result != AT_SUCCESS) {
            throw new DeviceException("GetEnumIndex for feature \"%s\" failed: %d (%s)", feature, result, ERROR_NAMES.getOrDefault(result, "UNKNOWN"));
        }

        result = nativeLibrary.AT_GetEnumStringByIndex(handle, wFeature, intBuffer.get(0), charBuffer.clear(), 1024);

        if (result != AT_SUCCESS) {
            throw new DeviceException("GetEnumStringByIndex for feature \"%s\" failed: %d (%s)", feature, result, ERROR_NAMES.getOrDefault(result, "UNKNOWN"));
        }

        return new Enum(intBuffer.get(0), charBuffer.rewind().toString().trim());

    }

    protected synchronized List<Enum> getEnumOptions(String feature) throws DeviceException {

        WString wFeature = new WString(feature);
        int     result   = nativeLibrary.AT_GetEnumCount(handle, wFeature, intBuffer.clear());

        if (result != AT_SUCCESS) {
            throw new DeviceException("GetEnumCount for feature \"%s\" failed: %d (%s)", feature, result, ERROR_NAMES.getOrDefault(result, "UNKNOWN"));
        }

        int        count = intBuffer.get(0);
        List<Enum> list  = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {

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

        Enum match = getEnumOptions(feature).stream().filter(e -> e.getText().equalsIgnoreCase(text.trim())).findFirst().orElse(null);

        if (match == null) {
            throw new DeviceException("Invalid enum option \"%s\" for feature \"%s\"", text, feature);
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

    protected void acquisitionStart() throws DeviceException {
        command("AcquisitionStart");
    }

    protected void acquisitionStop() throws DeviceException {
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
        setFloat("ExposureTime", time);
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

        long       tmo         = (timeout == Integer.MAX_VALUE || timeout < 1) ? AT_INFINITE : timeout;
        int        bufferSize  = getInt("ImageSizeBytes");
        ByteBuffer frameBuffer = ByteBuffer.allocateDirect(bufferSize);
        long       startTime   = System.nanoTime();
        long       startClock  = getLong("TimestampClock");
        long       frequency   = getLong("TimestampClockFrequency");

        flush();

        int result = nativeLibrary.AT_QueueBuffer(handle, frameBuffer, bufferSize);

        if (result != AT_SUCCESS) {
            throw new DeviceException("QueueBuffer failed: %d (%s)", result, ERROR_NAMES.getOrDefault(result, "UNKNOWN"));
        }

        int width     = getInt("AOIWidth");
        int height    = getInt("AOIHeight");
        int stride    = getInt("AOIStride");
        int imageSize = width * height;

        setEnum("CycleMode", "Fixed");
        setLong("FrameCount", 1);

        acquisitionStart();

        PointerByReference pointer = new PointerByReference();

        result      = nativeLibrary.AT_WaitBuffer(handle, pointer, intBuffer.clear(), tmo);
        frameBuffer = pointer.getValue().getByteBuffer(0, bufferSize);

        acquisitionStop();
        flush();

        if (result != AT_SUCCESS) {

            if (result == AT_ERR_TIMEDOUT) {
                throw new TimeoutException("Timed out waiting for frame from Andor SDK3 camera.");
            }

            throw new DeviceException("WaitBuffer failed: %d (%s)", result, ERROR_NAMES.getOrDefault(result, "UNKNOWN"));

        }

        Frame frame = new Frame(width, height);

        fillFrame(frameBuffer.array(), frame, stride, startClock, startTime, frequency);

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

            closeFrameQueue(queue);
            queue.clear();

            return frames;

        }

        long tmo = (timeout == Integer.MAX_VALUE || timeout < 1) ? AT_INFINITE : timeout;

        int  bufferSize = getInt("ImageSizeBytes");
        int  width      = getInt("AOIWidth");
        int  height     = getInt("AOIHeight");
        int  stride     = getInt("AOIStride");
        int  imageSize  = width * height;
        long startTime  = System.nanoTime();
        long startClock = getLong("TimestampClock");
        long frequency  = getLong("TimestampClockFrequency");

        setEnum("CycleMode", "Fixed");
        setLong("FrameCount", count);

        flush();

        for (int i = 0; i < count; i++) {

            ByteBuffer frameBuffer = ByteBuffer.allocateDirect(bufferSize);
            int        result      = nativeLibrary.AT_QueueBuffer(handle, frameBuffer, bufferSize);

            if (result != AT_SUCCESS) {
                throw new DeviceException("QueueBuffer failed: %d (%s)", result, ERROR_NAMES.getOrDefault(result, "UNKNOWN"));
            }

        }

        acquisitionStart();

        Pointer[] pointers = new Pointer[count];

        for (int i = 0; i < count; i++) {

            PointerByReference pointer = new PointerByReference();

            int result = nativeLibrary.AT_WaitBuffer(handle, pointer, intBuffer.clear(), tmo);

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

            ByteBuffer frameBuffer = pointers[i].getByteBuffer(0, bufferSize).rewind();
            short[]    data        = new short[imageSize];

            Frame frame = new Frame(width, height);
            fillFrame(frameBuffer.array(), frame, stride, startClock, startTime, frequency);
            frames.add(frame);

        }

        return frames;

    }

    @Override
    public void setAcquisitionTimeOut(int timeout) throws IOException, DeviceException {
        this.timeout = Math.max(0, timeout);
    }

    @Override
    public int getAcquisitionTimeOut() throws IOException, DeviceException {
        return timeout;
    }

    private void acquisition(int bufferSize) {

        long tmo = (timeout == Integer.MAX_VALUE || timeout < 1) ? AT_INFINITE : timeout;

        ByteBuffer         frameBuffer = ByteBuffer.allocateDirect(bufferSize);
        PointerByReference pointer     = new PointerByReference();
        IntBuffer          intBuffer   = IntBuffer.allocate(1);

        listenerManager.clearListenerBuffers();

        synchronized (queuedLock) {
            queued = ByteBuffer.allocateDirect(bufferSize);
        }

        while (acquiring) {

            int result = nativeLibrary.AT_QueueBuffer(handle, frameBuffer, bufferSize);

            if (result != AT_SUCCESS) {
                continue;
            }

            result = nativeLibrary.AT_WaitBuffer(handle, pointer, intBuffer, tmo);

            if (result != AT_SUCCESS) {
                continue;
            }

            synchronized (queuedLock) {
                queued.rewind().put(frameBuffer.rewind());
            }

            processingThread.notifyAll();

        }

        processingThread.notifyAll();

    }

    private void processing(int bufferSize, int width, int height, int stride, short[] data, long startClock, long startTime, long frequency) {

        while (acquiring) {

            try {
                processingThread.wait();
            } catch (InterruptedException e) {
                break;
            }

            synchronized (queuedLock) {
                fillFrame(queued.array(), frameBuffer, stride, startClock, startTime, frequency);
            }

            frameBuffer.notifyAll();
            listenerManager.trigger(frameBuffer);

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

        frameBuffer = new Frame(data, width, height);

        setEnum("CycleMode", "Continuous");
        flush();

        acquisitionThread = new Thread(() -> acquisition(bufferSize));
        processingThread  = new Thread(() -> processing(bufferSize, width, height, stride, data, clock, time, frequency));

        acquiring = true;

        acquisitionThread.start();
        processingThread.start();

        acquisitionStart();

    }

    @Override
    public synchronized void stopAcquisition() throws IOException, DeviceException {

        if (!isAcquiring()) {
            return;
        }

        acquiring = false;

        acquisitionThread.interrupt();
        processingThread.interrupt();

        try {
            acquisitionThread.join();
            processingThread.join();
        } catch (InterruptedException ignored) { }

        acquisitionStop();

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
            setInt("AOILeft", (getSensorWidth() - getPhysicalFrameWidth()) / 2);
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

        double     degC    = targetTemperature - 273.15;
        List<Enum> options = getEnumOptions("TemperatureControl");

        // Find enum option with value closest to that request, but not larger than it
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
