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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Lumenera extends ManagedCamera<U16RGBFrame> {

    private final LuCamAPI                        api;
    private final Pointer                         handle;
    private       float                           timeout     = 0;
    private       long[]                          data        = null;
    private       int                             width       = 0;
    private       int                             height      = 0;
    private       int                             pixelSize   = 0;
    private       long                            rawSize     = 0;
    private       long                            convertSize = 0;
    private       LuCamAPI.LUCAM_CONVERSION       conversion;
    private       boolean                         centredX;
    private       boolean                         centredY;
    private       LinkedBlockingQueue<ByteBuffer> returned    = new LinkedBlockingQueue<>(1);
    private       NativeLong                      callbackID  = null;

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
    public synchronized void setAcquisitionTimeout(int timeout) throws IOException, DeviceException {

        if (!api.LucamSetTimeout(handle, timeout / 1e3f)) {
            throw new DeviceException("Error setting acquisition timeout. Error code: %d.", getLastError());
        }

        this.timeout = timeout;

    }

    @Override
    public synchronized int getAcquisitionTimeout() throws IOException, DeviceException {
        return (int) (timeout * 1e3);
    }

    @Override
    protected void setupAcquisition(int limit) throws IOException, DeviceException {

        LuCamAPI.LUCAM_FRAME_FORMAT frameFormat = new LuCamAPI.LUCAM_FRAME_FORMAT();
        LuCamAPI.LUCAM_CONVERSION   conversion  = new LuCamAPI.LUCAM_CONVERSION();
        FloatBuffer                 frameRate   = FloatBuffer.allocate(1);

        conversion.CorrectionMatrix = LuCamAPI.LUCAM_CM_FLUORESCENT;
        conversion.DemosaicMethod   = LuCamAPI.LUCAM_DM_HIGHER_QUALITY;

        api.LucamGetFormat(handle, frameFormat, frameRate);

        this.width       = frameFormat.width.intValue() / frameFormat.binX.binningX;
        this.height      = frameFormat.height.intValue() / frameFormat.binY.binningY;
        this.pixelSize   = frameFormat.pixelFormat.intValue();
        this.rawSize     = width * height * pixelSize;
        this.convertSize = width * height * 6;
        this.conversion  = conversion;
        this.data        = new long[width * height];

        callbackID = api.LucamAddStreamingCallback(handle, this::callback, null);

        api.LucamStreamVideoControl(handle, LuCamAPI.START_STREAMING, null);

    }

    protected void callback(Pointer context, Pointer data, NativeLong length) {

        if (length.longValue() != width * height) {
            System.err.println("Frame size does not match!");
        }

        try (Memory con = new Memory(convertSize)) {
            ByteBuffer conBuffer = con.getByteBuffer(0, convertSize);
            ByteBuffer rawBuffer = data.getByteBuffer(0, rawSize);
            api.LucamConvertFrameToRgb48(handle, conBuffer.asShortBuffer(), rawBuffer.asShortBuffer(), width, height, pixelSize, conversion);
            returned.offer(conBuffer.rewind().duplicate());
        }

    }

    @Override
    protected U16RGBFrame createEmptyFrame() {
        return new U16RGBFrame(data, width, height, System.nanoTime());
    }

    @Override
    protected void acquisitionLoop(U16RGBFrame frameBuffer) throws IOException, DeviceException, InterruptedException, TimeoutException {

        ByteBuffer frameData = returned.poll((int) (timeout * 1e3), TimeUnit.MILLISECONDS);

        if (frameData == null) {
            throw new TimeoutException("Timed out waiting for frame.");
        }

        ShortBuffer shorts = frameData.asShortBuffer();

        for (int i = 0; i < data.length; i++) {
            data[i] = ((long) Character.MAX_VALUE << 48) | ((long) shorts.get()) | ((((long) shorts.get()) << 8) & 0xFF) | ((((long) shorts.get()) << 16) & 0xFF);
        }

        frameBuffer.setTimestamp(System.nanoTime());

    }

    @Override
    protected void cleanupAcquisition() throws IOException, DeviceException {
        api.LucamStreamVideoControl(handle, LuCamAPI.STOP_STREAMING, null);
        api.LucamRemoveStreamingCallback(handle, callbackID);
        returned.clear();
    }

    @Override
    public int getFrameWidth() throws IOException, DeviceException {

        LuCamAPI.LUCAM_FRAME_FORMAT frameFormat = new LuCamAPI.LUCAM_FRAME_FORMAT();
        LuCamAPI.LUCAM_CONVERSION   conversion  = new LuCamAPI.LUCAM_CONVERSION();
        FloatBuffer                 frameRate   = FloatBuffer.allocate(1);

        if (!api.LucamGetFormat(handle, frameFormat, frameRate)) {
            throw new DeviceException("Error getting frame width. Error code %d.", getLastError());
        }

        return frameFormat.width.intValue() / frameFormat.binX.binningX;

    }

    @Override
    public void setFrameWidth(int width) throws IOException, DeviceException {

        LuCamAPI.LUCAM_FRAME_FORMAT frameFormat = new LuCamAPI.LUCAM_FRAME_FORMAT();
        LuCamAPI.LUCAM_CONVERSION   conversion  = new LuCamAPI.LUCAM_CONVERSION();
        FloatBuffer                 frameRate   = FloatBuffer.allocate(1);

        if (!api.LucamGetFormat(handle, frameFormat, frameRate)) {
            throw new DeviceException("Error getting frame format data. Error code %d.", getLastError());
        }

        frameFormat.width.setValue(width * frameFormat.binX.binningX);

        if (!api.LucamSetFormat(handle, frameFormat, frameRate.get(0))) {
            throw new DeviceException("Error setting frame width. Error code %d.", getLastError());
        }

    }

    @Override
    public int getPhysicalFrameWidth() throws IOException, DeviceException {

        LuCamAPI.LUCAM_FRAME_FORMAT frameFormat = new LuCamAPI.LUCAM_FRAME_FORMAT();
        LuCamAPI.LUCAM_CONVERSION   conversion  = new LuCamAPI.LUCAM_CONVERSION();
        FloatBuffer                 frameRate   = FloatBuffer.allocate(1);

        if (!api.LucamGetFormat(handle, frameFormat, frameRate)) {
            throw new DeviceException("Error getting physical frame width. Error code %d.", getLastError());
        }

        return frameFormat.width.intValue();

    }

    @Override
    public int getFrameHeight() throws IOException, DeviceException {

        LuCamAPI.LUCAM_FRAME_FORMAT frameFormat = new LuCamAPI.LUCAM_FRAME_FORMAT();
        LuCamAPI.LUCAM_CONVERSION   conversion  = new LuCamAPI.LUCAM_CONVERSION();
        FloatBuffer                 frameRate   = FloatBuffer.allocate(1);

        if (!api.LucamGetFormat(handle, frameFormat, frameRate)) {
            throw new DeviceException("Error getting frame height. Error code %d.", getLastError());
        }

        return frameFormat.height.intValue() / frameFormat.binY.binningY;

    }

    @Override
    public void setFrameHeight(int height) throws IOException, DeviceException {

        LuCamAPI.LUCAM_FRAME_FORMAT frameFormat = new LuCamAPI.LUCAM_FRAME_FORMAT();
        LuCamAPI.LUCAM_CONVERSION   conversion  = new LuCamAPI.LUCAM_CONVERSION();
        FloatBuffer                 frameRate   = FloatBuffer.allocate(1);

        if (!api.LucamGetFormat(handle, frameFormat, frameRate)) {
            throw new DeviceException("Error getting frame format data. Error code %d.", getLastError());
        }

        frameFormat.height.setValue(height * frameFormat.binY.binningY);

        if (!api.LucamSetFormat(handle, frameFormat, frameRate.get(0))) {
            throw new DeviceException("Error setting frame height. Error code %d.", getLastError());
        }

    }

    @Override
    public int getPhysicalFrameHeight() throws IOException, DeviceException {

        LuCamAPI.LUCAM_FRAME_FORMAT frameFormat = new LuCamAPI.LUCAM_FRAME_FORMAT();
        LuCamAPI.LUCAM_CONVERSION   conversion  = new LuCamAPI.LUCAM_CONVERSION();
        FloatBuffer                 frameRate   = FloatBuffer.allocate(1);

        if (!api.LucamGetFormat(handle, frameFormat, frameRate)) {
            throw new DeviceException("Error getting physical frame height. Error code %d.", getLastError());
        }

        return frameFormat.height.intValue();

    }

    @Override
    public int getFrameOffsetX() throws IOException, DeviceException {

        LuCamAPI.LUCAM_FRAME_FORMAT frameFormat = new LuCamAPI.LUCAM_FRAME_FORMAT();
        LuCamAPI.LUCAM_CONVERSION   conversion  = new LuCamAPI.LUCAM_CONVERSION();
        FloatBuffer                 frameRate   = FloatBuffer.allocate(1);

        if (!api.LucamGetFormat(handle, frameFormat, frameRate)) {
            throw new DeviceException("Error getting frame x-offset. Error code %d.", getLastError());
        }

        return frameFormat.xOffset.intValue() / frameFormat.binX.binningX;

    }

    @Override
    public void setFrameOffsetX(int offsetX) throws IOException, DeviceException {

        if (isFrameCentredX()) {
            return;
        }

        LuCamAPI.LUCAM_FRAME_FORMAT frameFormat = new LuCamAPI.LUCAM_FRAME_FORMAT();
        LuCamAPI.LUCAM_CONVERSION   conversion  = new LuCamAPI.LUCAM_CONVERSION();
        FloatBuffer                 frameRate   = FloatBuffer.allocate(1);

        if (!api.LucamGetFormat(handle, frameFormat, frameRate)) {
            throw new DeviceException("Error getting frame format data. Error code %d.", getLastError());
        }

        frameFormat.xOffset.setValue(offsetX * frameFormat.binX.binningX);

        if (!api.LucamSetFormat(handle, frameFormat, frameRate.get(0))) {
            throw new DeviceException("Error setting frame x-offset. Error code %d.", getLastError());
        }

    }

    @Override
    public synchronized void setFrameCentredX(boolean centredX) throws IOException, DeviceException {

        this.centredX = centredX;

        if (centredX) {

            LuCamAPI.LUCAM_FRAME_FORMAT frameFormat = new LuCamAPI.LUCAM_FRAME_FORMAT();
            LuCamAPI.LUCAM_CONVERSION   conversion  = new LuCamAPI.LUCAM_CONVERSION();
            FloatBuffer                 frameRate   = FloatBuffer.allocate(1);

            if (!api.LucamGetFormat(handle, frameFormat, frameRate)) {
                throw new DeviceException("Error getting frame format data. Error code %d.", getLastError());
            }

            frameFormat.xOffset.setValue((getSensorWidth() - frameFormat.width.intValue()) / 2);

            if (!api.LucamSetFormat(handle, frameFormat, frameRate.get(0))) {
                throw new DeviceException("Error auto centering frame in x. Error code %d.", getLastError());
            }

        }

    }

    @Override
    public synchronized boolean isFrameCentredX() throws IOException, DeviceException {
        return centredX;
    }

    @Override
    public int getFrameOffsetY() throws IOException, DeviceException {

        LuCamAPI.LUCAM_FRAME_FORMAT frameFormat = new LuCamAPI.LUCAM_FRAME_FORMAT();
        LuCamAPI.LUCAM_CONVERSION   conversion  = new LuCamAPI.LUCAM_CONVERSION();
        FloatBuffer                 frameRate   = FloatBuffer.allocate(1);

        if (!api.LucamGetFormat(handle, frameFormat, frameRate)) {
            throw new DeviceException("Error getting frame y-offset. Error code %d.", getLastError());
        }

        return frameFormat.yOffset.intValue() / frameFormat.binY.binningY;
    }

    @Override
    public void setFrameOffsetY(int offsetY) throws IOException, DeviceException {

        if (isFrameCentredY()) {
            return;
        }

        LuCamAPI.LUCAM_FRAME_FORMAT frameFormat = new LuCamAPI.LUCAM_FRAME_FORMAT();
        LuCamAPI.LUCAM_CONVERSION   conversion  = new LuCamAPI.LUCAM_CONVERSION();
        FloatBuffer                 frameRate   = FloatBuffer.allocate(1);

        if (!api.LucamGetFormat(handle, frameFormat, frameRate)) {
            throw new DeviceException("Error getting frame format data. Error code %d.", getLastError());
        }

        frameFormat.yOffset.setValue(offsetY * frameFormat.binY.binningY);

        if (!api.LucamSetFormat(handle, frameFormat, frameRate.get(0))) {
            throw new DeviceException("Error setting frame y-offset. Error code %d.", getLastError());
        }

    }

    @Override
    public synchronized void setFrameCentredY(boolean centredY) throws IOException, DeviceException {

        this.centredY = centredX;

        if (centredY) {

            LuCamAPI.LUCAM_FRAME_FORMAT frameFormat = new LuCamAPI.LUCAM_FRAME_FORMAT();
            LuCamAPI.LUCAM_CONVERSION   conversion  = new LuCamAPI.LUCAM_CONVERSION();
            FloatBuffer                 frameRate   = FloatBuffer.allocate(1);

            frameFormat.yOffset.setValue((getSensorHeight() - frameFormat.height.intValue()) / 2);

            if (!api.LucamSetFormat(handle, frameFormat, frameRate.get(0))) {
                throw new DeviceException("Error auto centering frame in y. Error code %d.", getLastError());
            }

        }

    }

    @Override
    public synchronized boolean isFrameCentredY() throws IOException, DeviceException {
        return centredY;
    }

    @Override
    public int getFrameSize() throws IOException, DeviceException {

        LuCamAPI.LUCAM_FRAME_FORMAT frameFormat = new LuCamAPI.LUCAM_FRAME_FORMAT();
        LuCamAPI.LUCAM_CONVERSION   conversion  = new LuCamAPI.LUCAM_CONVERSION();
        FloatBuffer                 frameRate   = FloatBuffer.allocate(1);

        if (!api.LucamGetFormat(handle, frameFormat, frameRate)) {
            throw new DeviceException("Error getting frame size. Error code %d.", getLastError());
        }

        return (frameFormat.width.intValue() / frameFormat.binX.binningX) * (frameFormat.height.intValue() / frameFormat.binY.binningY);

    }

    @Override
    public int getPhysicalFrameSize() throws IOException, DeviceException {

        LuCamAPI.LUCAM_FRAME_FORMAT frameFormat = new LuCamAPI.LUCAM_FRAME_FORMAT();
        LuCamAPI.LUCAM_CONVERSION   conversion  = new LuCamAPI.LUCAM_CONVERSION();
        FloatBuffer                 frameRate   = FloatBuffer.allocate(1);

        if (!api.LucamGetFormat(handle, frameFormat, frameRate)) {
            throw new DeviceException("Error getting physical frame size. Error code %d.", getLastError());
        }

        return frameFormat.width.intValue() * frameFormat.height.intValue();

    }

    @Override
    public int getSensorWidth() throws IOException, DeviceException {
        return 1616;
    }

    @Override
    public int getSensorHeight() throws IOException, DeviceException {
        return 1216;
    }

    @Override
    public int getBinningX() throws IOException, DeviceException {

        LuCamAPI.LUCAM_FRAME_FORMAT frameFormat = new LuCamAPI.LUCAM_FRAME_FORMAT();
        LuCamAPI.LUCAM_CONVERSION   conversion  = new LuCamAPI.LUCAM_CONVERSION();
        FloatBuffer                 frameRate   = FloatBuffer.allocate(1);

        if (!api.LucamGetFormat(handle, frameFormat, frameRate)) {
            throw new DeviceException("Error getting x binning. Error code %d.", getLastError());
        }

        return frameFormat.binX.binningX;

    }

    @Override
    public void setBinningX(int x) throws IOException, DeviceException {

        LuCamAPI.LUCAM_FRAME_FORMAT frameFormat = new LuCamAPI.LUCAM_FRAME_FORMAT();
        LuCamAPI.LUCAM_CONVERSION   conversion  = new LuCamAPI.LUCAM_CONVERSION();
        FloatBuffer                 frameRate   = FloatBuffer.allocate(1);

        if (!api.LucamGetFormat(handle, frameFormat, frameRate)) {
            throw new DeviceException("Error getting x binning. Error code %d.", getLastError());
        }

        frameFormat.binX.binningX = (short) x;

        if (!api.LucamSetFormat(handle, frameFormat, frameRate.get(0))) {
            throw new DeviceException("Error setting x binning. Error code %d.", getLastError());
        }

    }

    @Override
    public int getBinningY() throws IOException, DeviceException {

        LuCamAPI.LUCAM_FRAME_FORMAT frameFormat = new LuCamAPI.LUCAM_FRAME_FORMAT();
        LuCamAPI.LUCAM_CONVERSION   conversion  = new LuCamAPI.LUCAM_CONVERSION();
        FloatBuffer                 frameRate   = FloatBuffer.allocate(1);

        if (!api.LucamGetFormat(handle, frameFormat, frameRate)) {
            throw new DeviceException("Error getting y binning. Error code %d.", getLastError());
        }

        return frameFormat.binY.binningY;

    }

    @Override
    public void setBinningY(int y) throws IOException, DeviceException {

        LuCamAPI.LUCAM_FRAME_FORMAT frameFormat = new LuCamAPI.LUCAM_FRAME_FORMAT();
        LuCamAPI.LUCAM_CONVERSION   conversion  = new LuCamAPI.LUCAM_CONVERSION();
        FloatBuffer                 frameRate   = FloatBuffer.allocate(1);

        if (!api.LucamGetFormat(handle, frameFormat, frameRate)) {
            throw new DeviceException("Error getting x binning. Error code %d.", getLastError());
        }

        frameFormat.binY.binningY = (short) y;

        if (!api.LucamSetFormat(handle, frameFormat, frameRate.get(0))) {
            throw new DeviceException("Error setting y binning. Error code %d.", getLastError());
        }

    }

    @Override
    public void setBinning(int x, int y) throws IOException, DeviceException {

        LuCamAPI.LUCAM_FRAME_FORMAT frameFormat = new LuCamAPI.LUCAM_FRAME_FORMAT();
        LuCamAPI.LUCAM_CONVERSION   conversion  = new LuCamAPI.LUCAM_CONVERSION();
        FloatBuffer                 frameRate   = FloatBuffer.allocate(1);

        if (!api.LucamGetFormat(handle, frameFormat, frameRate)) {
            throw new DeviceException("Error getting x binning. Error code %d.", getLastError());
        }

        frameFormat.binX.binningX = (short) x;
        frameFormat.binY.binningY = (short) y;

        if (!api.LucamSetFormat(handle, frameFormat, frameRate.get(0))) {
            throw new DeviceException("Error setting x-y binning. Error code %d.", getLastError());
        }

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
        return "Lumenera Lucam SDK Camera";
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
