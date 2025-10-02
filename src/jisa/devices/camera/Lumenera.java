package jisa.devices.camera;

import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.NativeLongByReference;
import jisa.addresses.Address;
import jisa.devices.DeviceException;
import jisa.devices.camera.feature.Amplified;
import jisa.devices.camera.frame.U16RGBFrame;
import jisa.devices.camera.nat.LuCamAPI;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.FloatBuffer;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Lumenera extends ManagedCamera<U16RGBFrame> implements Amplified {

    private final LuCamAPI                        api;
    private final Pointer                         handle;
    private final LinkedBlockingQueue<ByteBuffer> returned    = new LinkedBlockingQueue<>(1);
    private       float                           timeout     = 0;
    private       long[]                          data        = null;
    private final LuCamAPI.LUCAM_FRAME_FORMAT     frameFormat = new LuCamAPI.LUCAM_FRAME_FORMAT();
    private final LuCamAPI.LUCAM_CONVERSION       conversion  = new LuCamAPI.LUCAM_CONVERSION();
    private       int                             imageWidth  = 0;
    private       int                             imageHeight = 0;
    private       int                             pixelSize   = 0;
    private       int                             rawSize     = 0;
    private       int                             conSize     = 0;
    private       Memory                          rawMemory   = null;
    private       Memory                          conMemory   = null;
    private       ByteBuffer                      rawBuffer   = null;
    private       ByteBuffer                      conBuffer   = null;
    private       boolean                         centredX;
    private       boolean                         centredY;

    public Lumenera(int index) throws IOException, DeviceException {

        super("Lumenera Camera");

        api    = findLibrary(LuCamAPI.class, "lucamapi");
        handle = api.LucamCameraOpen(new NativeLong(index, true));

        if (handle == null) {
            throw new IOException(String.format("Unable to open connection to camera index %d. Error code: %d.", index, api.LucamGetLastError().longValue()));
        }

        LuCamAPI.LUCAM_FRAME_FORMAT frameFormat = new LuCamAPI.LUCAM_FRAME_FORMAT();
        LuCamAPI.LUCAM_CONVERSION   conversion  = new LuCamAPI.LUCAM_CONVERSION();
        FloatBuffer                 frameRate   = FloatBuffer.allocate(1);

        if (!api.LucamGetFormat(handle, frameFormat, frameRate)) {
            throw new DeviceException("Error getting frame format data. Error code %d.", getLastError());
        }

        frameFormat.pixelFormat.setValue(1);

        if (!api.LucamSetFormat(handle, frameFormat, frameRate.get(0))) {
            throw new DeviceException("Error setting frame bit depth. Error code %d.", getLastError());
        }

    }

    protected long getLastError() {
        return api.LucamGetLastErrorForCamera(handle).longValue();
    }

    protected int getPixelSize(int pixelFormat) {

        switch (pixelFormat) {

            case 1:
                return 2;
            case 2:
                return 3;
            case 6:
                return 4;
            case 7:
                return 6;
            default:
                return 1;

        }

    }

    @Override
    public double getIntegrationTime() throws IOException, DeviceException {

        FloatBuffer           exposure = FloatBuffer.allocate(1);
        NativeLongByReference flags    = new NativeLongByReference();


        if (!api.LucamGetProperty(handle, LuCamAPI.LUCAM_PROP_EXPOSURE, exposure, flags)) {
            throw new DeviceException("Unable to read property LUCAM_PROP_EXPOSURE. Error code: %d", getLastError());
        }

        return exposure.get(0) / 1e3;


    }

    @Override
    public void setIntegrationTime(double time) throws IOException, DeviceException {

        if (!api.LucamSetProperty(handle, LuCamAPI.LUCAM_PROP_EXPOSURE, (float) (time * 1e3), new NativeLong(0))) {
            throw new DeviceException("Unable to set property LUCAM_PROP_EXPOSURE to %f. Error code: %d.", time, getLastError());
        }

        LuCamAPI.LUCAM_FRAME_FORMAT frameFormat = new LuCamAPI.LUCAM_FRAME_FORMAT();
        LuCamAPI.LUCAM_CONVERSION   conversion  = new LuCamAPI.LUCAM_CONVERSION();
        FloatBuffer                 frameRate   = FloatBuffer.allocate(1);

        if (!api.LucamGetFormat(handle, frameFormat, frameRate)) {
            throw new DeviceException("Error getting frame format. Error code %d.", getLastError());
        }

        if (!api.LucamSetFormat(handle, frameFormat, (float) (1.0f / getIntegrationTime()))) {
            throw new DeviceException("Error setting framerate. Error code %d.", getLastError());
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

        FloatBuffer frameRate = FloatBuffer.allocate(1);

        conversion.CorrectionMatrix = LuCamAPI.LUCAM_CM_NONE;
        conversion.DemosaicMethod   = LuCamAPI.LUCAM_DM_FAST;

        if (!api.LucamGetFormat(handle, frameFormat, frameRate)) {
            throw new DeviceException("Error getting frame format info. Error code: %d", getLastError());
        }

        imageWidth  = frameFormat.width.intValue() / frameFormat.binX.binningX;
        imageHeight = frameFormat.height.intValue() / frameFormat.binY.binningY;
        pixelSize   = getPixelSize(frameFormat.pixelFormat.intValue());
        rawSize     = imageWidth * imageHeight * pixelSize;
        conSize     = imageWidth * imageHeight * 6;
        rawMemory   = new Memory(rawSize);
        conMemory   = new Memory(conSize);
        rawBuffer   = rawMemory.getByteBuffer(0, rawSize);
        conBuffer   = conMemory.getByteBuffer(0, conSize);
        data        = new long[imageWidth * imageHeight];

        api.LucamStreamVideoControl(handle, LuCamAPI.START_STREAMING, null);

    }

    @Override
    protected U16RGBFrame createEmptyFrame() {
        return new U16RGBFrame(data, imageWidth, imageHeight, System.nanoTime());
    }

    @Override
    protected void acquisitionLoop(U16RGBFrame frameBuffer) throws IOException, DeviceException, InterruptedException, TimeoutException {

        if (!api.LucamTakeVideo(handle, 1, rawBuffer.rewind())) {

            long error = getLastError();

            if (error == 19) {
                throw new TimeoutException();
            }

            throw new DeviceException("Error acquiring frame. Error code: %d", getLastError());

        }

        if (!api.LucamConvertFrameToRgb48(handle, conBuffer.rewind().asShortBuffer(), rawBuffer.rewind().asShortBuffer(), imageWidth, imageHeight, frameFormat.pixelFormat.longValue(), conversion)) {
            throw new DeviceException("Error converting frame data. Error code: %d", getLastError());
        }

        CharBuffer shorts = conBuffer.rewind().asCharBuffer();

        for (int i = 0; i < data.length; i++) {
            data[i] = ((long) Character.MAX_VALUE << 48) | (((long) shorts.get())) | (((long) shorts.get()) << 16) | ((((long) shorts.get()) << 32));
        }

        frameBuffer.setTimestamp(System.nanoTime());

    }

    @Override
    protected void cleanupAcquisition() throws IOException, DeviceException {
        api.LucamStreamVideoControl(handle, LuCamAPI.STOP_STREAMING, null);
        rawMemory.close();
        conMemory.close();
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

        frameFormat.width.setValue((long) width * frameFormat.binX.binningX);

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

        frameFormat.height.setValue((long) height * frameFormat.binY.binningY);

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

        frameFormat.xOffset.setValue((long) offsetX * frameFormat.binX.binningX);

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

        frameFormat.yOffset.setValue((long) offsetY * frameFormat.binY.binningY);

        if (!api.LucamSetFormat(handle, frameFormat, frameRate.get(0))) {
            throw new DeviceException("Error setting frame y-offset. Error code %d.", getLastError());
        }

    }

    @Override
    public synchronized void setFrameCentredY(boolean centredY) throws IOException, DeviceException {

        this.centredY = centredY;

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

    @Override
    public void setAmplifierGain(double gain) throws DeviceException, IOException {

        if (!api.LucamSetProperty(handle, LuCamAPI.LUCAM_PROP_GAIN, (float) gain, new NativeLong(0))) {
            throw new DeviceException("Error setting gain. Error code: %d", getLastError());
        }

    }

    @Override
    public double getAmplifierGain() throws DeviceException, IOException {

        FloatBuffer           buffer = FloatBuffer.allocate(1);
        NativeLongByReference ref    = new NativeLongByReference();

        if (!api.LucamGetProperty(handle, LuCamAPI.LUCAM_PROP_GAIN, buffer, ref)) {
            throw new DeviceException("Error getting gain. Error code: %d", getLastError());
        }

        return buffer.get(0);

    }

}
