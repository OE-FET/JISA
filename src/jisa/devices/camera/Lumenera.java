package jisa.devices.camera;

import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.NativeLongByReference;
import jisa.addresses.Address;
import jisa.devices.DeviceException;
import jisa.devices.camera.frame.FrameQueue;
import jisa.devices.camera.frame.U16RGBFrame;
import jisa.devices.camera.nat.LuCamAPI;
import jisa.visa.NativeDevice;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.FloatBuffer;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class Lumenera extends NativeDevice implements Camera<U16RGBFrame> {

    private final LuCamAPI api;
    private final Pointer  handle;

    public Lumenera(int index) throws IOException, DeviceException {

        super("Lumenera Camera");

        api    = findLibrary(LuCamAPI.class, "lucamapi");
        handle = api.LucamCameraOpen(new NativeLong(index, true));

        if (handle == null) {
            throw new IOException(String.format("Unable to open connection to camera index %d. Error code: %d.", index, api.LucamGetLastError().longValue()));
        }

    }

    protected long getLastError() {
        return api.LucamGetLastErrorForCamera(handle).longValue();
    }

    @Override
    public double getIntegrationTime() throws IOException, DeviceException {

        FloatBuffer           exposure = FloatBuffer.allocate(1);
        NativeLongByReference flags    = new NativeLongByReference();


        if (!api.LucamGetProperty(handle, LuCamAPI.LUCAM_PROP_EXPOSURE, exposure, flags)) {
            throw new DeviceException("Unable to read property LUCAM_PROP_EXPOSURE. Error code: %d", getLastError());
        }

        return exposure.get(0);


    }

    @Override
    public void setIntegrationTime(double time) throws IOException, DeviceException {

        if (!api.LucamSetProperty(handle, LuCamAPI.LUCAM_PROP_EXPOSURE, (float) time, new NativeLong(0))) {
            throw new DeviceException("Unable to set property LUCAM_PROP_EXPOSURE to %f. Error code: %d.", time, getLastError());
        }

    }

    @Override
    public U16RGBFrame getFrame() throws IOException, DeviceException, InterruptedException, TimeoutException {

        LuCamAPI.LUCAM_FRAME_FORMAT frameFormat = new LuCamAPI.LUCAM_FRAME_FORMAT();
        LuCamAPI.LUCAM_CONVERSION   conversion  = new LuCamAPI.LUCAM_CONVERSION();
        FloatBuffer                 frameRate   = FloatBuffer.allocate(1);

        conversion.CorrectionMatrix = LuCamAPI.LUCAM_CM_FLUORESCENT;
        conversion.DemosaicMethod   = LuCamAPI.LUCAM_DM_HIGHER_QUALITY;

        api.LucamGetFormat(handle, frameFormat, frameRate);

        int width     = frameFormat.width.intValue() / frameFormat.binX.subSampleX;
        int height    = frameFormat.height.intValue() / frameFormat.binY.subSampleY;
        int pixelSize = frameFormat.pixelFormat.intValue();
        int size      = width * height * pixelSize;

        try (Memory memory = new Memory(size)) {

            ByteBuffer buffer = memory.getByteBuffer(0, size);

            if (!api.LucamTakeVideo(handle, new NativeLong(1, true), buffer)) {
                throw new DeviceException("Error taking frame. Error code %d.", getLastError());
            }

            try (Memory convertedMemory = new Memory(width * height * 6L)) {

                ByteBuffer converted = convertedMemory.getByteBuffer(0, size);

                if (!api.LucamConvertFrameToRgb48(handle, converted.asShortBuffer(), buffer.asShortBuffer(), frameFormat.width, frameFormat.height, frameFormat.pixelFormat, conversion)) {

                    long lastError = getLastError();

                    throw new DeviceException("Error converting frame from camera. Error code: %d", getLastError());
                }

                long[] data = new long[width * height];

                CharBuffer shorts = converted.rewind().asCharBuffer();

                for (int i = 0; i < data.length; i++) {
                    data[i] = ((long) Character.MAX_VALUE << 48) | ((long) shorts.get()) | ((((long) shorts.get()) << 8) & 0xFF) | ((((long) shorts.get()) << 16) & 0xFF);
                }

                return new U16RGBFrame(data, width, height, System.nanoTime());

            }

        }


    }

    @Override
    public void setAcquisitionTimeout(int timeout) throws IOException, DeviceException {

    }

    @Override
    public int getAcquisitionTimeout() throws IOException, DeviceException {
        return 0;
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
    public List<U16RGBFrame> getFrameSeries(int count) throws IOException, DeviceException, InterruptedException, TimeoutException {
        return List.of();
    }

    @Override
    public Listener<U16RGBFrame> addFrameListener(Listener<U16RGBFrame> listener) {
        return null;
    }

    @Override
    public void removeFrameListener(Listener<U16RGBFrame> listener) {

    }

    @Override
    public FrameQueue<U16RGBFrame> openFrameQueue(int capacity) {
        return null;
    }

    @Override
    public void closeFrameQueue(FrameQueue<U16RGBFrame> queue) {

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
        return "Lumenera Lucam SDK Camera";
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public void close() throws IOException, DeviceException {

        if (!api.LucamCameraClose(handle)) {
            throw new DeviceException("Error closing Lumenera camera. Error code: %d.", getLastError());
        }

    }

    @Override
    public Address getAddress() {
        return null;
    }

}
