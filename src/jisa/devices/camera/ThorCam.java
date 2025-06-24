package jisa.devices.camera;

import com.google.common.primitives.Ints;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;
import jisa.Util;
import jisa.addresses.Address;
import jisa.addresses.IDAddress;
import jisa.devices.DeviceException;
import jisa.devices.ParameterList;
import jisa.devices.camera.feature.Amplified;
import jisa.devices.camera.frame.*;
import jisa.devices.camera.nat.ThorCamLibrary;
import jisa.devices.camera.nat.ThorCamMosaicLibrary;
import jisa.visa.NativeDevice;

import java.io.*;
import java.nio.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Driver class for ThorLabs cameras.
 */
public abstract class ThorCam<F extends Frame<?, F>, D> extends NativeDevice implements Camera<F>, Amplified {

    // CONSTANTS
    public static final int ERROR_NONE                    = 0;
    public static final int ERROR_COMMAND_NOT_FOUND       = 1001;
    public static final int ERROR_TOO_MANY_ARGUMENTS      = 1002;
    public static final int ERROR_NOT_ENOUGH_ARGUMENTS    = 1003;
    public static final int ERROR_INVALID_COMMAND         = 1004;
    public static final int ERROR_DUPLICATE_COMMAND       = 1005;
    public static final int ERROR_MISSING_JSON_COMMAND    = 1006;
    public static final int ERROR_INITIALIZING            = 1007;
    public static final int ERROR_NOTSUPPORTED            = 1008;
    public static final int ERROR_FPGA_NOT_PROGRAMMED     = 1009;
    public static final int ERROR_ROI_WIDTH_ERROR         = 1010;
    public static final int ERROR_ROI_RANGE_ERROR         = 1011;
    public static final int ERROR_RANGE_ERROR             = 1012;
    public static final int ERROR_COMMAND_LOCKED          = 1013;
    public static final int ERROR_CAMERA_MUST_BE_STOPPED  = 1014;
    public static final int ERROR_ROI_BIN_COMBO_ERROR     = 1015;
    public static final int ERROR_IMAGE_DATA_SYNC_ERROR   = 1016;
    public static final int ERROR_CAMERA_MUST_BE_DISARMED = 1017;
    public static final int ERROR_MAX_ERRORS              = 1018;

    public static final int META_TAG_PCKH = Ints.fromByteArray("PCKH".getBytes(StandardCharsets.US_ASCII));
    public static final int META_TAG_PCKL = Ints.fromByteArray("PCKL".getBytes(StandardCharsets.US_ASCII));

    public static final int BYTES_PER_MONO_PIXEL   = 2;
    public static final int BYTES_PER_COLOUR_PIXEL = 6;

    public static final Map<Integer, String> ERROR_NAMES =
        Util.map(ERROR_NONE, "No Error")
            .map(ERROR_COMMAND_NOT_FOUND, "Unknown Command")
            .map(ERROR_TOO_MANY_ARGUMENTS, "Too Many Arguments sent with Command")
            .map(ERROR_NOT_ENOUGH_ARGUMENTS, "Too Few Arguments sent with Command")
            .map(ERROR_INVALID_COMMAND, "Invalid Command")
            .map(ERROR_DUPLICATE_COMMAND, "Duplicate Command")
            .map(ERROR_MISSING_JSON_COMMAND, "Command not Documented in JSON")
            .map(ERROR_INITIALIZING, "Camera Still Initialising")
            .map(ERROR_NOTSUPPORTED, "Command Not Supported")
            .map(ERROR_FPGA_NOT_PROGRAMMED, "No Firmware Image on FPGA")
            .map(ERROR_ROI_WIDTH_ERROR, "Invalid ROI Width Value")
            .map(ERROR_ROI_RANGE_ERROR, "Invalid ROI Range Value")
            .map(ERROR_RANGE_ERROR, "Value out of Range for Command")
            .map(ERROR_COMMAND_LOCKED, "Command Locked")
            .map(ERROR_CAMERA_MUST_BE_STOPPED, "Command Requires Camera to be Stopped")
            .map(ERROR_ROI_BIN_COMBO_ERROR, "ROI/Binning Error")
            .map(ERROR_IMAGE_DATA_SYNC_ERROR, "Data Sync Error")
            .map(ERROR_CAMERA_MUST_BE_DISARMED, "Command Requires Camera to be Disarmed")
            .map(ERROR_MAX_ERRORS, "END OF ENUMERATION");

    private final ListenerManager<F> listenerManager = new ListenerManager<>();

    protected final ThorCamLibrary sdk;
    protected final Pointer        handle;
    protected final long[]         stats   = {0, 0, System.nanoTime()};
    private         double         lastFPS = 0;

    private boolean acquiring         = false;
    private Thread  acquisitionThread = null;
    private boolean centredX          = false;
    private boolean centredY          = false;
    private boolean timestamping      = true;


    public ThorCam() throws DeviceException, IOException {

        super("ThorCam SDK Camera");

        sdk = findLibrary(ThorCamLibrary.class, "thorlabs_tsi_camera_sdk");

        ByteBuffer serials = ByteBuffer.allocate(1024);
        sdk.tl_camera_discover_available_cameras(serials, 1024);

        String[] serialNumbers = new String(serials.array(), StandardCharsets.US_ASCII).trim().split(" ");

        handle = getPointer(ref -> sdk.tl_camera_open_camera(serialNumbers[0], ref), "tl_camera_open_camera");

    }


    public ThorCam(String serial) throws DeviceException, IOException {

        super("ThorCam SDK Camera");

        sdk = findLibrary(ThorCamLibrary.class, "thorlabs_tsi_camera_sdk");

        ByteBuffer serials = ByteBuffer.allocate(1024);
        sdk.tl_camera_discover_available_cameras(serials, 1024);

        String[] serialNumbers = new String(serials.array(), StandardCharsets.US_ASCII).trim().split(" ");

        handle = getPointer(ref -> sdk.tl_camera_open_camera(serial, ref), "tl_camera_open_camera");

    }


    public ThorCam(Address address) throws DeviceException, IOException {

        super("ThorCam SDK Camera");

        sdk = findLibrary(ThorCamLibrary.class, "thorlabs_tsi_camera_sdk");

        ByteBuffer serials = ByteBuffer.allocate(1024);
        sdk.tl_camera_discover_available_cameras(serials, 1024);

        String[] serialNumbers = new String(serials.array(), StandardCharsets.US_ASCII).trim().split(" ");

        if (address instanceof IDAddress) {
            handle = getPointer(ref -> sdk.tl_camera_open_camera(((IDAddress) address).getID(), ref), "tl_camera_open_camera");
        } else {
            throw new DeviceException("Only IDAddress objects are supported.");
        }

    }

    @Override
    public List<Parameter<?>> getInstrumentParameters(Class<?> target) {

        ParameterList parameters = new ParameterList();

        parameters.addOptional("Hot Pixel Correction", this::isHotPixelCorrectionEnabled, false, this::getHotPixelCorrectionThreshold, 1, q -> this.setHotPixelCorrectionEnabled(false), q -> {
            this.setHotPixelCorrectionThreshold(q);
            this.setHotPixelCorrectionEnabled(true);
        });

        parameters.addValue("LED Enabled", this::isLEDEnabled, true, this::setLEDEnabled);

        return parameters;

    }

    @Override
    public void setAmplifierGain(double gain) throws DeviceException, IOException {

        int index = getInt((h, b) -> sdk.tl_camera_convert_decibels_to_gain(h, gain, b), "tl_camera_convert_decibels_to_gain");
        process(sdk.tl_camera_set_gain(handle, index), "tl_camera_set_gain");

    }

    @Override
    public double getAmplifierGain() throws DeviceException, IOException {

        int index = getInt(sdk::tl_camera_get_gain, "tl_camera_get_gain");

        DoubleBuffer buffer = DoubleBuffer.allocate(1);
        process(sdk.tl_camera_convert_gain_to_decibels(handle, index, buffer), "tl_camera_convert_gain_to_decibels");
        return buffer.get(0);

    }

    public interface BufferConverter<T> {
        int get(T buffer);
    }

    public interface HandledBufferConverter<T> {
        int get(Pointer handle, T buffer);
    }

    public interface HandledQuadBufferConverter<T> {
        int get(Pointer handle, T buffer1, T buffer2, T buffer3, T buffer4);
    }

    protected int getInt(BufferConverter<IntBuffer> converter, String name) throws DeviceException, IOException {

        try (Memory memory = new Memory(Integer.BYTES)) {
            process(converter.get(memory.getByteBuffer(0, Integer.BYTES).asIntBuffer()), name);
            return memory.getInt(0);
        }

    }

    protected int getInt(HandledBufferConverter<IntBuffer> converter, String name) throws DeviceException, IOException {

        try (Memory memory = new Memory(Integer.BYTES)) {
            process(converter.get(handle, memory.getByteBuffer(0, Integer.BYTES).asIntBuffer()), name);
            return memory.getInt(0);
        }

    }

    protected long getLong(HandledBufferConverter<LongBuffer> converter, String name) throws DeviceException, IOException {

        try (Memory memory = new Memory(Long.BYTES)) {
            process(converter.get(handle, memory.getByteBuffer(0, Long.BYTES).asLongBuffer()), name);
            return memory.getLong(0);
        }

    }

    protected int getInt(HandledBufferConverter<IntBuffer> converter) throws DeviceException, IOException {
        return getInt(converter, "not specified");
    }

    protected int getInt(BufferConverter<IntBuffer> converter) throws DeviceException, IOException {
        return getInt(converter, "not specified");
    }

    protected Pointer getPointer(BufferConverter<PointerByReference> converter, String name) throws DeviceException, IOException {

        PointerByReference reference = new PointerByReference();
        process(converter.get(reference), name);
        return reference.getValue();

    }

    protected int[] getInts(HandledQuadBufferConverter<IntBuffer> converter, String name) throws DeviceException, IOException {

        try (Memory memory = new Memory(4 * Integer.BYTES)) {

            process(
                converter.get(
                    handle,
                    memory.getByteBuffer(0, Integer.BYTES).asIntBuffer(),
                    memory.getByteBuffer(Integer.BYTES, Integer.BYTES).asIntBuffer(),
                    memory.getByteBuffer(2 * Integer.BYTES, Integer.BYTES).asIntBuffer(),
                    memory.getByteBuffer(3 * Integer.BYTES, Integer.BYTES).asIntBuffer()
                ),
                name
            );

            return new int[]{memory.getInt(0), memory.getInt(Integer.BYTES), memory.getInt(2 * Integer.BYTES), memory.getInt(3 * Integer.BYTES)};

        }

    }

    public void process(int result, String method) throws IOException, DeviceException {

        if (method.equals("tl_camera_open_camera") && result == ERROR_INVALID_COMMAND) {
            throw new IOException("ThorCam SDK function \"tl_camera_open_camera\" returned an error: Camera Not Found (1004).");
        }

        if (result != ERROR_NONE) {

            if (result == ERROR_IMAGE_DATA_SYNC_ERROR) {
                throw new IOException(String.format("ThorCam SDK function \"%s\" returned an I/O error: %s (%d)", method, ERROR_NAMES.getOrDefault(result, "UNKNOWN"), result));
            } else {
                throw new DeviceException("ThorCam SDK function \"%s\" returned an error: %s (%d)", method, ERROR_NAMES.getOrDefault(result, "UNKNOWN"), result);
            }

        }

    }

    @Override
    public double getIntegrationTime() throws IOException, DeviceException {
        return getLong(sdk::tl_camera_get_exposure_time, "tl_camera_get_exposure_time") * 1e-6;
    }

    @Override
    public void setIntegrationTime(double time) throws IOException, DeviceException {
        process(sdk.tl_camera_set_exposure_time(handle, (long) (time * 1e6)), "tl_camera_set_exposure_time");
    }

    protected abstract D createBuffer(int length);

    protected abstract F createFrame(int width, int height, D array, long timestamp);

    protected abstract void populateBuffer(D array, ByteBuffer data, int width, int height);

    @Override
    public F getFrame() throws IOException, DeviceException, InterruptedException, TimeoutException {

        if (isAcquiring()) {

            FrameQueue<F> queue = openFrameQueue(1);

            try {

                return queue.nextFrame(getAcquisitionTimeout());

            } finally {

                queue.clear();
                queue.close();

            }

        } else {

            int frequency = getInt(sdk::tl_camera_get_timestamp_clock_frequency, "tl_camera_get_timestamp_clock_frequency");

            process(sdk.tl_camera_set_frames_per_trigger_zero_for_unlimited(handle, 1), "tl_camera_set_frames_per_trigger_zero");
            process(sdk.tl_camera_arm(handle, 2), "tl_camera_arm");
            process(sdk.tl_camera_issue_software_trigger(handle), "tl_camera_issue_software_trigger");

            final int pixel          = getInt(sdk::tl_camera_get_sensor_pixel_size_bytes, "tl_camera_get_sensor_pixel_size_bytes");
            final int width          = getFrameWidth();
            final int height         = getFrameHeight();
            final int count          = width * height;
            final int imageSizeBytes = pixel * count;

            final PointerByReference frameReference = new PointerByReference();
            final PointerByReference metaReference  = new PointerByReference();
            final IntBuffer          frameCount     = IntBuffer.allocate(1);
            final IntBuffer          metaSize       = IntBuffer.allocate(1);

            process(sdk.tl_camera_get_pending_frame_or_null(handle, frameReference, frameCount, metaReference, metaSize), "tl_camera_get_pending_frame_or_null");

            long timestamp = determineTimestamp(metaReference.getValue(), metaSize.get(0), frequency, System.nanoTime());

            process(sdk.tl_camera_disarm(handle), "tl_camera_disarm");

            Pointer framePointer = frameReference.getValue();

            if (framePointer == null) {
                throw new TimeoutException("Timed out waiting for frame from ThorCam camera.");
            }

            ByteBuffer frameBuffer = ByteBuffer.wrap(framePointer.getByteArray(0, imageSizeBytes));
            D          argb        = createBuffer(count);

            populateBuffer(argb, frameBuffer, width, height);

            return createFrame(width, height, argb, timestamp);

        }

    }

    @Override
    public void setAcquisitionTimeout(int timeout) throws IOException, DeviceException {

        if (timeout == 0) {
            timeout = Integer.MAX_VALUE;
        }

        process(sdk.tl_camera_set_image_poll_timeout(handle, timeout), "tl_camera_set_image_poll_timeout");

    }

    @Override
    public int getAcquisitionTimeout() throws IOException, DeviceException {

        int value = getInt(sdk::tl_camera_get_image_poll_timeout, "tl_camera_get_image_poll_timeout");

        return value == Integer.MAX_VALUE ? 0 : value;

    }

    @Override
    public double getAcquisitionFPS() throws IOException, DeviceException {

        // Update if new frames have come in since last time, and it's been at least 10us since last call
        if ((stats[0] != stats[1]) && ((System.nanoTime() - stats[2]) >= 10000)) {

            synchronized (stats) {

                long frames  = stats[0];
                long dFrames = frames - stats[1];
                long time    = System.nanoTime();
                long dTime   = time - stats[2];

                stats[1] = frames;
                stats[2] = time;

                lastFPS = 1e9 * dFrames / dTime;

            }

        }

        return lastFPS;

    }

    protected void acquisition(int pixel, int width, int height, int pCount) {

        final int imageSizeBytes = pixel * pCount;

        int frequency;

        try {
            frequency = getInt(sdk::tl_camera_get_timestamp_clock_frequency, "tl_camera_get_timestamp_clock_frequency");
        } catch (Exception e) {
            frequency = -1;
        }

        final D    argb        = createBuffer(pCount);
        final F    frame       = createFrame(width, height, argb, System.nanoTime());
        byte[]     buffer      = new byte[imageSizeBytes];
        ByteBuffer frameBuffer = ByteBuffer.wrap(buffer);

        synchronized (stats) {
            stats[0] = 0;
            stats[1] = 0;
            stats[2] = System.nanoTime();
        }

        while (acquiring) {

            try {

                PointerByReference frameReference = new PointerByReference();
                PointerByReference metaReference  = new PointerByReference();
                IntBuffer          frameCount     = IntBuffer.allocate(1);
                IntBuffer          metaSize       = IntBuffer.allocate(1);

                process(sdk.tl_camera_get_pending_frame_or_null(handle, frameReference, frameCount, metaReference, metaSize), "tl_camera_get_pending_frame_or_null");

                frame.setTimestamp(determineTimestamp(metaReference.getValue(), metaSize.get(0), frequency, System.nanoTime()));

                if (Thread.interrupted()) {
                    break;
                }

                Pointer framePointer = frameReference.getValue();

                if (framePointer == null) {
                    throw new TimeoutException("Timed out waiting for frame from ThorCam camera.");
                }

                framePointer.read(0, buffer, 0, imageSizeBytes);

                populateBuffer(argb, frameBuffer.rewind(), width, height);

                listenerManager.trigger(frame);
                stats[0]++;

            } catch (Exception e) {
                System.err.printf("Error acquiring frame from ThorCam camera: %s%n.", e.getMessage());
            }

        }

        synchronized (stats) {
            lastFPS = 0;
        }

    }

    @Override
    public void startAcquisition() throws IOException, DeviceException {

        if (acquiring) {
            return;
        }

        acquiring = true;

        final int pixel  = getInt(sdk::tl_camera_get_sensor_pixel_size_bytes, "tl_camera_get_pixel_size");
        final int width  = getFrameWidth();
        final int height = getFrameHeight();
        final int count  = width * height;

        acquisitionThread = new Thread(() -> acquisition(pixel, width, height, count));

        process(sdk.tl_camera_set_operation_mode(handle, 0), "tl_camera_set_operation_mode");
        process(sdk.tl_camera_set_frames_per_trigger_zero_for_unlimited(handle, 0), "tl_camera_set_frames_per_trigger_zero_for_unlimited");
        process(sdk.tl_camera_arm(handle, 2), "tl_camera_arm");
        process(sdk.tl_camera_issue_software_trigger(handle), "tl_camera_issue_software_trigger");

        acquisitionThread.start();

    }

    @Override
    public void stopAcquisition() throws IOException, DeviceException {

        if (!acquiring) {
            return;
        }

        process(sdk.tl_camera_disarm(handle), "tl_camera_disarm");

        acquiring = false;
        acquisitionThread.interrupt();

        try {
            acquisitionThread.join();
        } catch (InterruptedException ignored) { }

    }

    @Override
    public boolean isAcquiring() throws IOException, DeviceException {
        return acquiring;
    }

    @Override
    public List<F> getFrameSeries(int count) throws IOException, DeviceException, InterruptedException, TimeoutException {

        List<F> captured = new ArrayList<>(count);

        if (isAcquiring()) {

            FrameQueue<F> queue = openFrameQueue(count);

            try {

                for (int i = 0; i < count; i++) {
                    captured.add(queue.nextFrame(getAcquisitionTimeout()));
                }

            } finally {

                queue.clear();
                queue.close();

            }

        } else {

            int frequency = getInt(sdk::tl_camera_get_timestamp_clock_frequency, "tl_camera_get_timestamp_clock_frequency");

            process(sdk.tl_camera_set_operation_mode(handle, 0), "tl_camera_set_operation_mode");
            process(sdk.tl_camera_set_frames_per_trigger_zero_for_unlimited(handle, count), "tl_camera_set_frames_per_trigger_zero_for_unlimited");
            process(sdk.tl_camera_arm(handle, count + 1), "tl_camera_arm");
            process(sdk.tl_camera_issue_software_trigger(handle), "tl_camera_issue_software_trigger");

            final int pixel          = getInt(sdk::tl_camera_get_sensor_pixel_size_bytes, "tl_camera_get_sensor_pixel_size_bytes");
            final int width          = getFrameWidth();
            final int height         = getFrameHeight();
            final int pCount         = width * height;
            final int imageSizeBytes = pixel * pCount;

            for (int k = 0; k < count; k++) {

                PointerByReference frameReference = new PointerByReference();
                PointerByReference metaReference  = new PointerByReference();
                IntBuffer          frameCount     = IntBuffer.allocate(1);
                IntBuffer          metaSize       = IntBuffer.allocate(1);

                process(sdk.tl_camera_get_pending_frame_or_null(handle, frameReference, frameCount, metaReference, metaSize), "tl_camera_get_pending_frame_or_null");
                process(sdk.tl_camera_disarm(handle), "tl_camera_disarm");

                Pointer framePointer = frameReference.getValue();
                Pointer metaPointer  = metaReference.getValue();

                if (framePointer == null) {
                    throw new TimeoutException("Timed out waiting for frame from ThorCam camera.");
                }

                long timestamp = determineTimestamp(metaPointer, metaSize.get(0), frequency, System.nanoTime());

                ByteBuffer frameBuffer = ByteBuffer.wrap(framePointer.getByteArray(0, imageSizeBytes));
                D          argb        = createBuffer(pCount);

                populateBuffer(argb, frameBuffer, width, height);

                captured.add(createFrame(width, height, argb, timestamp));

            }

        }

        return captured;

    }

    protected long determineTimestamp(Pointer metaPointer, int size, long frequency, long fallback) {

        if (metaPointer == null || !timestamping || frequency < 1) {
            return fallback;
        }

        int upper = -1;
        int lower = -1;
        int found = 0;

        ByteBuffer metaBuffer = metaPointer.getByteBuffer(0, size);

        metaBuffer.rewind();

        while (metaBuffer.hasRemaining()) {

            int code = metaBuffer.getInt();
            int data = metaBuffer.getInt();

            if (code == META_TAG_PCKH) {
                upper = data;
                found++;
            } else if (code == META_TAG_PCKL) {
                lower = data;
                found++;
            }

        }

        if (found >= 2) {
            long clock = ((long) upper) << 32 | ((long) lower);
            return ((long) 1e9) * clock / frequency;
        } else {
            return fallback;
        }

    }

    public void setHotPixelCorrectionEnabled(boolean enabled) throws IOException, DeviceException {
        process(sdk.tl_camera_set_is_hot_pixel_correction_enabled(handle, enabled ? 1 : 0), "tl_camera_set_is_hot_pixel_correction_enabled");
    }

    public boolean isHotPixelCorrectionEnabled() throws IOException, DeviceException {
        return getInt(sdk::tl_camera_get_is_hot_pixel_correction_enabled, "tl_camera_get_is_hot_pixel_correction_enabled") != 0;
    }

    public void setHotPixelCorrectionThreshold(int threshold) throws IOException, DeviceException {
        process(sdk.tl_camera_set_hot_pixel_correction_threshold(handle, threshold), "tl_camera_set_hot_pixel_correction_threshold");
    }

    public int getHotPixelCorrectionThreshold() throws IOException, DeviceException {
        return getInt(sdk::tl_camera_get_hot_pixel_correction_threshold, "tl_camera_get_hot_pixel_correction_threshold");
    }

    @Override
    public Listener<F> addFrameListener(Listener<F> listener) {
        listenerManager.addListener(listener);
        return listener;
    }

    @Override
    public void removeFrameListener(Listener<F> listener) {
        listenerManager.removeListener(listener);
    }

    @Override
    public FrameQueue<F> openFrameQueue(int capacity) {

        FrameQueue<F> queue = new FrameQueue<>(this, capacity);
        listenerManager.addQueue(queue);

        return queue;

    }

    @Override
    public void closeFrameQueue(FrameQueue<F> queue) {

        listenerManager.removeQueue(queue);
        queue.close();

    }

    @Override
    public int getFrameWidth() throws IOException, DeviceException {
        return getInt(sdk::tl_camera_get_image_width, "tl_camera_get_image_width");
    }

    @Override
    public void setFrameWidth(int width) throws IOException, DeviceException {


        int[] points          = getInts(sdk::tl_camera_get_roi, "tl_camera_get_roi");
        int   newBottomRightX = points[0] + width;

        process(sdk.tl_camera_set_roi(handle, points[0], points[1], points[0] + width, points[3]), "tl_camera_set_roi");

        setFrameCentredX(centredX);

    }

    @Override
    public int getPhysicalFrameWidth() throws IOException, DeviceException {
        return getFrameWidth() * getBinningX();
    }

    @Override
    public int getFrameHeight() throws IOException, DeviceException {
        return getInt(sdk::tl_camera_get_image_height, "tl_camera_get_image_height");
    }

    @Override
    public void setFrameHeight(int height) throws IOException, DeviceException {

        int[] points          = getInts(sdk::tl_camera_get_roi, "tl_camera_get_roi");
        int   newBottomRightY = points[1] + height;

        process(sdk.tl_camera_set_roi(handle, points[0], points[1], points[2], newBottomRightY), "tl_camera_set_roi");

        setFrameCentredY(centredY);

    }

    @Override
    public int getPhysicalFrameHeight() throws IOException, DeviceException {
        return getFrameHeight() * getBinningY();
    }

    @Override
    public int getFrameOffsetX() throws IOException, DeviceException {
        return getInts(sdk::tl_camera_get_roi, "tl_camera_get_roi")[0];
    }

    @Override
    public void setFrameOffsetX(int offsetX) throws IOException, DeviceException {

        if (isFrameCentredX()) {
            throw new DeviceException("Cannot change frame x offset when x-centring is enabled.");
        }

        int[] points          = getInts(sdk::tl_camera_get_roi, "tl_camera_get_roi");
        int   newBottomRightX = offsetX + (points[2] - points[0]);

        process(sdk.tl_camera_set_roi(handle, offsetX, points[1], newBottomRightX, points[3]), "tl_camera_set_roi");

    }

    @Override
    public void setFrameCentredX(boolean centredX) throws IOException, DeviceException {

        if (centredX) {

            int[] points = getInts(sdk::tl_camera_get_roi, "tl_camera_get_roi");

            int width  = points[2] - points[0];
            int border = (getSensorWidth() - width) / 2;

            process(sdk.tl_camera_set_roi(handle, border, points[1], border + width, points[3]), "tl_camera_set_roi");

        }

        this.centredX = centredX;

    }

    @Override
    public boolean isFrameCentredX() throws IOException, DeviceException {
        return centredX;
    }

    @Override
    public int getFrameOffsetY() throws IOException, DeviceException {
        return getInts(sdk::tl_camera_get_roi, "tl_camera_get_roi")[1];
    }

    @Override
    public void setFrameOffsetY(int offsetY) throws IOException, DeviceException {

        if (isFrameCentredY()) {
            throw new DeviceException("Cannot change frame y offset when y-centring is enabled.");
        }

        int[] points          = getInts(sdk::tl_camera_get_roi, "tl_camera_get_roi");
        int   newBottomRightY = offsetY + (points[3] - points[1]);

        process(sdk.tl_camera_set_roi(handle, points[0], offsetY, points[2], newBottomRightY), "tl_camera_set_roi");

    }

    @Override
    public void setFrameCentredY(boolean centredY) throws IOException, DeviceException {

        if (centredY) {

            int[] points = getInts(sdk::tl_camera_get_roi, "tl_camera_get_roi");

            int height = points[3] - points[1];
            int border = (getSensorHeight() - height) / 2;

            process(sdk.tl_camera_set_roi(handle, points[0], border, points[2], border + height), "tl_camera_set_roi");

        }

        this.centredY = centredY;

    }

    @Override
    public boolean isFrameCentredY() throws IOException, DeviceException {
        return centredY;
    }

    @Override
    public int getFrameSize() throws IOException, DeviceException {
        return getFrameWidth() * getFrameHeight();
    }

    @Override
    public int getPhysicalFrameSize() throws IOException, DeviceException {
        return getPhysicalFrameWidth() * getPhysicalFrameHeight();
    }

    @Override
    public int getSensorWidth() throws IOException, DeviceException {
        return getInt(sdk::tl_camera_get_sensor_width, "tl_camera_get_sensor_width");
    }

    @Override
    public int getSensorHeight() throws IOException, DeviceException {
        return getInt(sdk::tl_camera_get_sensor_height, "tl_camera_get_sensor_height");
    }

    @Override
    public int getBinningX() throws IOException, DeviceException {
        return getInt(sdk::tl_camera_get_binx, "tl_camera_get_binx");
    }

    @Override
    public void setBinningX(int x) throws IOException, DeviceException {
        process(sdk.tl_camera_set_binx(handle, x), "tl_camera_set_binx");
    }

    @Override
    public int getBinningY() throws IOException, DeviceException {
        return getInt(sdk::tl_camera_get_biny, "tl_camera_get_biny");
    }

    @Override
    public void setBinningY(int y) throws IOException, DeviceException {
        process(sdk.tl_camera_set_biny(handle, y), "tl_camera_set_biny");
    }

    @Override
    public void setBinning(int x, int y) throws IOException, DeviceException {
        setBinningX(x);
        setBinningY(y);
    }

    @Override
    public boolean isTimestampEnabled() throws IOException, DeviceException {
        return timestamping;
    }

    @Override
    public void setTimestampEnabled(boolean timestamping) throws IOException, DeviceException {
        this.timestamping = timestamping;
    }

    @Override
    public String getIDN() throws IOException, DeviceException {
        return "ThorLabs Camera";
    }

    @Override
    public String getName() {
        return "ThorLabs Camera";
    }

    @Override
    public void close() throws IOException, DeviceException {
        process(sdk.tl_camera_close_camera(handle), "tl_camera_close");
    }

    @Override
    public Address getAddress() {

        ByteBuffer serial = ByteBuffer.allocate(1024);

        try {
            process(sdk.tl_camera_get_serial_number(handle, serial, 1024), "tl_camera_get_serial_number");
        } catch (Exception e) {
            return new IDAddress("SERIAL_NUMBER");
        }

        String serialNumber = new String(serial.array(), StandardCharsets.US_ASCII).trim();

        return new IDAddress(serialNumber);

    }

    /**
     * Sets whether the LED on the camera is turned on or not.
     *
     * @param enabled Turned on?
     *
     * @throws IOException     Upon communications error.
     * @throws DeviceException Upon device compatibility error.
     */
    public void setLEDEnabled(boolean enabled) throws IOException, DeviceException {
        process(sdk.tl_camera_set_is_led_on(handle, enabled ? 1 : 0), "tl_camera_set_is_led_on");
    }

    /**
     * Returns whether the LED on the camera is turned on or not.
     *
     * @return Turned on?
     *
     * @throws IOException     Upon communications error.
     * @throws DeviceException Upon device compatibility error.
     */
    public boolean isLEDEnabled() throws IOException, DeviceException {
        return getInt(sdk::tl_camera_get_is_led_on, "tl_camera_get_is_led_on") != 0;
    }

    public static class Colour extends ThorCam<U16RGBFrame, long[]> {

        public static FrameReader<U16RGBFrame> openFrameReader(String path) throws IOException {

            ColourFrame[] buffer  = new ColourFrame[1];
            long[][]      dBuffer = new long[1][];

            return new FrameReader<>(path, (width, height, bpp, timestamp, data) -> {

                if (buffer[0] == null || buffer[0].getWidth() != width || buffer[0].getHeight() != height) {
                    dBuffer[0] = new long[width * height];
                    buffer[0]  = new ColourFrame(dBuffer[0], width, height, timestamp);
                }

                ByteBuffer.wrap(data).asLongBuffer().rewind().get(dBuffer[0]);
                buffer[0].setTimestamp(timestamp);

                return buffer[0];

            });

        }

        public static void convertOldFile(String oldFile, String newFile) throws IOException {

            FileInputStream      fis = new FileInputStream(oldFile);
            BufferedInputStream  bis = new BufferedInputStream(fis);
            DataInputStream      dis = new DataInputStream(bis);
            FileOutputStream     fos = new FileOutputStream(newFile);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            DataOutputStream     dos = new DataOutputStream(bos);

            while (dis.available() > 0) {

                int  width     = dis.readInt();
                int  height    = dis.readInt();
                long timestamp = dis.readLong();
                int  length    = width * height * Long.BYTES;

                dos.writeInt(width);
                dos.writeInt(height);
                dos.writeInt(Long.BYTES);
                dos.writeLong(timestamp);

                dos.write(dis.readNBytes(length));

            }

            dis.close();
            dos.close();

        }

        public static String getDescription() {
            return "ThorLabs Colour Camera";
        }

        private final ThorCamMosaicLibrary mosaic = findLibrary(ThorCamMosaicLibrary.class, "thorlabs_tsi_mono_to_color_processing");

        private Pointer mosaicHandle = null;

        public Colour() throws DeviceException, IOException {
            super();
        }

        public Colour(String serial) throws DeviceException, IOException {
            super(serial);
        }

        public Colour(Address address) throws DeviceException, IOException {
            super(address);
        }

        protected void refreshProcessor() throws IOException, DeviceException {

            if (mosaicHandle != null) {
                mosaic.tl_mono_to_color_destroy_mono_to_color_processor(mosaicHandle);
                mosaicHandle = null;
            }

            PointerByReference ref = new PointerByReference();

            int sensorType = getInt(sdk::tl_camera_get_camera_sensor_type);
            int phaseArray = getInt(sdk::tl_camera_get_color_filter_array_phase);
            int bitDepth   = getInt(sdk::tl_camera_get_bit_depth);

            FloatBuffer ccMatrix = FloatBuffer.allocate(9);
            FloatBuffer wbMatrix = FloatBuffer.allocate(9);
            sdk.tl_camera_get_color_correction_matrix(handle, ccMatrix);
            sdk.tl_camera_get_default_white_balance_matrix(handle, wbMatrix);

            int result = mosaic.tl_mono_to_color_create_mono_to_color_processor(sensorType, phaseArray, ccMatrix, wbMatrix, bitDepth, ref);

            if (result != 0) {
                throw new DeviceException("Mosaic colour processor could not be created: %d", result);
            }

            Pointer mHandle = ref.getValue();

            mosaic.tl_mono_to_color_set_color_space(mHandle, 1);
            mosaic.tl_mono_to_color_set_output_format(mHandle, 1);

            mosaicHandle = mHandle;

        }

        public U16RGBFrame getFrame() throws IOException, DeviceException, InterruptedException, TimeoutException {

            if (!isAcquiring()) {
                refreshProcessor();
            }

            return super.getFrame();

        }

        public List<U16RGBFrame> getFrameSeries(int count) throws IOException, DeviceException, InterruptedException, TimeoutException {

            if (!isAcquiring()) {
                refreshProcessor();
            }

            return super.getFrameSeries(count);

        }

        public void startAcquisition() throws IOException, DeviceException {

            if (!isAcquiring()) {
                refreshProcessor();
                super.startAcquisition();
            }

        }

        @Override
        protected long[] createBuffer(int length) {
            return new long[length];
        }

        @Override
        protected U16RGBFrame createFrame(int width, int height, long[] array, long timestamp) {
            return new ColourFrame(array, width, height, timestamp);
        }

        @Override
        protected void populateBuffer(long[] array, ByteBuffer input, int width, int height) {

            try (Memory memory = new Memory(array.length * 6L)) {

                ByteBuffer output = memory.getByteBuffer(0, array.length * 6L);
                int        result = mosaic.tl_mono_to_color_transform_to_48(mosaicHandle, input, width, height, output);

                CharBuffer shorts = output.rewind().asCharBuffer();

                for (int i = 0; i < array.length; i++) {
                    array[i] = ((long) Character.MAX_VALUE << 48) | ((long) shorts.get()) | ((long) shorts.get() << 16) | ((long) shorts.get() << 32);
                }

            }

        }

        public void close() throws IOException, DeviceException {
            mosaic.tl_mono_to_color_destroy_mono_to_color_processor(mosaicHandle);
            super.close();
        }

    }

    public static class Mono extends ThorCam<U16Frame, short[]> {

        public static FrameReader<U16Frame> openFrameReader(String path) throws IOException {

            MonoFrame[] buffer  = new MonoFrame[1];
            short[][]   dBuffer = new short[1][];

            return new FrameReader<>(path, (width, height, bpp, timestamp, data) -> {

                if (buffer[0] == null || buffer[0].getWidth() != width || buffer[0].getHeight() != height) {
                    dBuffer[0] = new short[width * height];
                    buffer[0]  = new MonoFrame(dBuffer[0], width, height, timestamp);
                }
                ByteBuffer.wrap(data).asShortBuffer().rewind().get(dBuffer[0]);
                buffer[0].setTimestamp(timestamp);

                return buffer[0];

            });

        }

        public static String getDescription() {
            return "ThorLabs Mono Camera";
        }

        public Mono() throws DeviceException, IOException {
            super();
        }

        public Mono(String serial) throws DeviceException, IOException {
            super(serial);
        }

        public Mono(Address address) throws DeviceException, IOException {
            super(address);
        }

        @Override
        protected short[] createBuffer(int length) {
            return new short[length];
        }

        @Override
        protected U16Frame createFrame(int width, int height, short[] array, long timestamp) {
            return new MonoFrame(array, width, height, timestamp);
        }

        @Override
        protected void populateBuffer(short[] array, ByteBuffer data, int width, int height) {
            data.asShortBuffer().get(array);
        }

    }

    protected static class ColourFrame extends U16RGBFrame {

        private final static U16RGB MAX = new U16RGB(((long) Character.MAX_VALUE << 48 | (long) 4096 << 32 | (long) 4096 << 16 | 4096));

        public ColourFrame(char[] red, char[] green, char[] blue, int width, int height, long timestamp) {
            super(red, green, blue, width, height, timestamp);
        }

        public ColourFrame(long[] argb, int width, int height, long timestamp) {
            super(argb, width, height, timestamp);
        }

        public U16RGB getMax() {
            return MAX;
        }

        public ColourFrame copy() {
            return new ColourFrame(argb.clone(), width, height, timestamp);
        }

        @Override
        public void readARGBData(int[] destination) {

            long v;

            for (int i = 0; i < argb.length; i++) {

                v = argb[i];

                destination[i] = (int) (((0xFF << 24)
                    | (((v >> 32) & 0xFFFF) >> 4) << 16)
                    | (((v >> 16) & 0xFFFF) >> 4) << 8
                    | ((v & 0xFFFF) >> 4));

            }

        }

    }

    protected static class MonoFrame extends U16Frame {

        public MonoFrame(short[] data, int width, int height, long timestamp) {
            super(data, width, height, timestamp);
        }

        public MonoFrame(short[] data, int width, int height) {
            super(data, width, height);
        }

        public MonoFrame(Short[] data, int width, int height, long timestamp) {
            super(data, width, height, timestamp);
        }

        public MonoFrame(Short[] data, int width, int height) {
            super(data, width, height);
        }

        public Integer getMax() {
            return 4096;
        }

        public MonoFrame copy() {
            return new MonoFrame(data.clone(), width, height, timestamp);
        }

        @Override
        public void readARGBData(int[] argb) {

            byte value;

            for (int i = 0; i < data.length; i++) {
                value   = (byte) ((data[i] >> 4) & 0xFF);
                argb[i] = (255 << 24) | value << 16 | value << 8 | value;
            }

        }
    }

}
