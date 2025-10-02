package jisa.devices.camera;

import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.NativeLongByReference;
import jisa.addresses.Address;
import jisa.devices.DeviceException;
import jisa.devices.camera.feature.Amplified;
import jisa.devices.camera.frame.U16RGBFrame;
import jisa.devices.camera.nat.LucamAPI;
import jisa.devices.camera.nat.LucamError;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.FloatBuffer;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeoutException;

public class Lumenera extends ManagedCamera<U16RGBFrame> implements Amplified {

    private final LucamAPI                        api;
    private final Pointer                         handle;
    private final LinkedBlockingQueue<ByteBuffer> returned    = new LinkedBlockingQueue<>(1);
    private       float                           timeout     = 0;
    private       long[]                          data        = null;
    private final LucamAPI.LUCAM_FRAME_FORMAT     frameFormat = new LucamAPI.LUCAM_FRAME_FORMAT();
    private final LucamAPI.LUCAM_CONVERSION       conversion  = new LucamAPI.LUCAM_CONVERSION();
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

        api    = findLibrary(LucamAPI.class, "lucamapi");
        handle = api.LucamCameraOpen(new NativeLong(index, true));

        if (handle == null) {
            throw new IOException(String.format("Unable to open connection to camera index %d. Error code: %d.", index, api.LucamGetLastError().longValue()));
        }

        LucamAPI.LUCAM_FRAME_FORMAT frameFormat = new LucamAPI.LUCAM_FRAME_FORMAT();
        LucamAPI.LUCAM_CONVERSION   conversion  = new LucamAPI.LUCAM_CONVERSION();
        FloatBuffer                 frameRate   = FloatBuffer.allocate(1);

        if (!api.LucamGetFormat(handle, frameFormat, frameRate)) {
            throwError("LucamGetFormat");
        }

        frameFormat.pixelFormat.setValue(1);

        if (!api.LucamSetFormat(handle, frameFormat, frameRate.get(0))) {
            throwError("LucamSetFormat");
        }

        setAcquisitionTimeout(10000);

    }

    protected void throwError(String activity, Object... format) throws DeviceException {

        long   error = getLastError();
        String name  = LucamError.NAMES.getOrDefault(error, "Unknown Error");

        throw new DeviceException("Error encountered with %s: %s (%d)", String.format(activity, format), name, error);

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


        if (!api.LucamGetProperty(handle, LucamAPI.LUCAM_PROP_EXPOSURE, exposure, flags)) {
            throwError("LucamGetProperty(LUCAM_PROP_EXPOSURE)");
        }

        return exposure.get(0) / 1e3;


    }

    @Override
    public void setIntegrationTime(double time) throws IOException, DeviceException {

        if (!api.LucamSetProperty(handle, LucamAPI.LUCAM_PROP_EXPOSURE, (float) (time * 1e3), new NativeLong(0))) {
            throwError("LucamSetProperty(LUCAM_PROP_EXPOSURE, %e)", time);
        }

        LucamAPI.LUCAM_FRAME_FORMAT frameFormat = new LucamAPI.LUCAM_FRAME_FORMAT();
        LucamAPI.LUCAM_CONVERSION   conversion  = new LucamAPI.LUCAM_CONVERSION();
        FloatBuffer                 frameRate   = FloatBuffer.allocate(1);

        if (!api.LucamGetFormat(handle, frameFormat, frameRate)) {
            throwError("LucamGetFormat");
        }

        if (!api.LucamSetFormat(handle, frameFormat, (float) (1.0f / getIntegrationTime()))) {
            throwError("LucamSetFormat");
        }

    }

    @Override
    public synchronized void setAcquisitionTimeout(int timeout) throws IOException, DeviceException {

        if (!api.LucamSetTimeout(handle, timeout)) {
            throwError("LucamSetTimeout(%d)", timeout);
        }

        this.timeout = timeout;

    }

    @Override
    public synchronized int getAcquisitionTimeout() throws IOException, DeviceException {
        return (int) timeout;
    }

    @Override
    protected void setupAcquisition(int limit) throws IOException, DeviceException {

        FloatBuffer frameRate = FloatBuffer.allocate(1);

        conversion.CorrectionMatrix = LucamAPI.LUCAM_CM_NONE;
        conversion.DemosaicMethod   = LucamAPI.LUCAM_DM_FAST;

        if (!api.LucamGetFormat(handle, frameFormat, frameRate)) {
            throwError("LucamGetFormat");
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

        api.LucamStreamVideoControl(handle, LucamAPI.START_STREAMING, null);

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

            throw new DeviceException("Error encountered with %s: %s (%d)", "LucanTakeVideo", LucamError.NAMES.getOrDefault(error, "Unkown Error"), error);

        }

        if (!api.LucamConvertFrameToRgb48(handle, conBuffer.rewind().asShortBuffer(), rawBuffer.rewind().asShortBuffer(), imageWidth, imageHeight, frameFormat.pixelFormat.longValue(), conversion)) {
            throwError("LucamConvertFrameToRgb48");
        }

        CharBuffer shorts = conBuffer.rewind().asCharBuffer();

        for (int i = 0; i < data.length; i++) {
            data[i] = ((long) Character.MAX_VALUE << 48) | (((long) shorts.get())) | (((long) shorts.get()) << 16) | ((((long) shorts.get()) << 32));
        }

        frameBuffer.setTimestamp(System.nanoTime());

    }

    @Override
    protected void cleanupAcquisition() throws IOException, DeviceException {

        api.LucamStreamVideoControl(handle, LucamAPI.STOP_STREAMING, null);

        rawMemory.close();
        conMemory.close();

    }

    @Override
    public int getFrameWidth() throws IOException, DeviceException {

        LucamAPI.LUCAM_FRAME_FORMAT frameFormat = new LucamAPI.LUCAM_FRAME_FORMAT();
        LucamAPI.LUCAM_CONVERSION   conversion  = new LucamAPI.LUCAM_CONVERSION();
        FloatBuffer                 frameRate   = FloatBuffer.allocate(1);

        if (!api.LucamGetFormat(handle, frameFormat, frameRate)) {
            throwError("LucamGetFormat");
        }

        return frameFormat.width.intValue() / frameFormat.binX.binningX;

    }

    @Override
    public void setFrameWidth(int width) throws IOException, DeviceException {

        LucamAPI.LUCAM_FRAME_FORMAT frameFormat = new LucamAPI.LUCAM_FRAME_FORMAT();
        LucamAPI.LUCAM_CONVERSION   conversion  = new LucamAPI.LUCAM_CONVERSION();
        FloatBuffer                 frameRate   = FloatBuffer.allocate(1);

        if (!api.LucamGetFormat(handle, frameFormat, frameRate)) {
            throwError("LucamGetFormat");
        }

        frameFormat.width.setValue((long) width * frameFormat.binX.binningX);

        if (!api.LucamSetFormat(handle, frameFormat, frameRate.get(0))) {
            throwError("LucamSetFormat");
        }

    }

    @Override
    public int getPhysicalFrameWidth() throws IOException, DeviceException {

        LucamAPI.LUCAM_FRAME_FORMAT frameFormat = new LucamAPI.LUCAM_FRAME_FORMAT();
        LucamAPI.LUCAM_CONVERSION   conversion  = new LucamAPI.LUCAM_CONVERSION();
        FloatBuffer                 frameRate   = FloatBuffer.allocate(1);

        if (!api.LucamGetFormat(handle, frameFormat, frameRate)) {
            throwError("LucamGetFormat");
        }

        return frameFormat.width.intValue();

    }

    @Override
    public int getFrameHeight() throws IOException, DeviceException {

        LucamAPI.LUCAM_FRAME_FORMAT frameFormat = new LucamAPI.LUCAM_FRAME_FORMAT();
        LucamAPI.LUCAM_CONVERSION   conversion  = new LucamAPI.LUCAM_CONVERSION();
        FloatBuffer                 frameRate   = FloatBuffer.allocate(1);

        if (!api.LucamGetFormat(handle, frameFormat, frameRate)) {
            throwError("LucamGetFormat");
        }

        return frameFormat.height.intValue() / frameFormat.binY.binningY;

    }

    @Override
    public void setFrameHeight(int height) throws IOException, DeviceException {

        LucamAPI.LUCAM_FRAME_FORMAT frameFormat = new LucamAPI.LUCAM_FRAME_FORMAT();
        LucamAPI.LUCAM_CONVERSION   conversion  = new LucamAPI.LUCAM_CONVERSION();
        FloatBuffer                 frameRate   = FloatBuffer.allocate(1);

        if (!api.LucamGetFormat(handle, frameFormat, frameRate)) {
            throwError("LucamGetFormat");
        }

        frameFormat.height.setValue((long) height * frameFormat.binY.binningY);

        if (!api.LucamSetFormat(handle, frameFormat, frameRate.get(0))) {
            throwError("LucamSetFormat");
        }

    }

    @Override
    public int getPhysicalFrameHeight() throws IOException, DeviceException {

        LucamAPI.LUCAM_FRAME_FORMAT frameFormat = new LucamAPI.LUCAM_FRAME_FORMAT();
        LucamAPI.LUCAM_CONVERSION   conversion  = new LucamAPI.LUCAM_CONVERSION();
        FloatBuffer                 frameRate   = FloatBuffer.allocate(1);

        if (!api.LucamGetFormat(handle, frameFormat, frameRate)) {
            throwError("LucamGetFormat");
        }

        return frameFormat.height.intValue();

    }

    @Override
    public int getFrameOffsetX() throws IOException, DeviceException {

        LucamAPI.LUCAM_FRAME_FORMAT frameFormat = new LucamAPI.LUCAM_FRAME_FORMAT();
        LucamAPI.LUCAM_CONVERSION   conversion  = new LucamAPI.LUCAM_CONVERSION();
        FloatBuffer                 frameRate   = FloatBuffer.allocate(1);

        if (!api.LucamGetFormat(handle, frameFormat, frameRate)) {
            throwError("LucamGetFormat");
        }

        return frameFormat.xOffset.intValue() / frameFormat.binX.binningX;

    }

    @Override
    public void setFrameOffsetX(int offsetX) throws IOException, DeviceException {

        if (isFrameCentredX()) {
            return;
        }

        LucamAPI.LUCAM_FRAME_FORMAT frameFormat = new LucamAPI.LUCAM_FRAME_FORMAT();
        LucamAPI.LUCAM_CONVERSION   conversion  = new LucamAPI.LUCAM_CONVERSION();
        FloatBuffer                 frameRate   = FloatBuffer.allocate(1);

        if (!api.LucamGetFormat(handle, frameFormat, frameRate)) {
            throwError("LucamGetFormat");
        }

        frameFormat.xOffset.setValue((long) offsetX * frameFormat.binX.binningX);

        if (!api.LucamSetFormat(handle, frameFormat, frameRate.get(0))) {
            throwError("LucamSetFormat");
        }

    }

    @Override
    public synchronized void setFrameCentredX(boolean centredX) throws IOException, DeviceException {

        this.centredX = centredX;

        if (centredX) {

            LucamAPI.LUCAM_FRAME_FORMAT frameFormat = new LucamAPI.LUCAM_FRAME_FORMAT();
            LucamAPI.LUCAM_CONVERSION   conversion  = new LucamAPI.LUCAM_CONVERSION();
            FloatBuffer                 frameRate   = FloatBuffer.allocate(1);

            if (!api.LucamGetFormat(handle, frameFormat, frameRate)) {
                throwError("LucamGetFormat");
            }

            frameFormat.xOffset.setValue((getSensorWidth() - frameFormat.width.intValue()) / 2);

            if (!api.LucamSetFormat(handle, frameFormat, frameRate.get(0))) {
                throwError("LucamSetFormat");
            }

        }

    }

    @Override
    public synchronized boolean isFrameCentredX() throws IOException, DeviceException {
        return centredX;
    }

    @Override
    public int getFrameOffsetY() throws IOException, DeviceException {

        LucamAPI.LUCAM_FRAME_FORMAT frameFormat = new LucamAPI.LUCAM_FRAME_FORMAT();
        LucamAPI.LUCAM_CONVERSION   conversion  = new LucamAPI.LUCAM_CONVERSION();
        FloatBuffer                 frameRate   = FloatBuffer.allocate(1);

        if (!api.LucamGetFormat(handle, frameFormat, frameRate)) {
            throwError("LucamGetFormat");
        }

        return frameFormat.yOffset.intValue() / frameFormat.binY.binningY;
    }

    @Override
    public void setFrameOffsetY(int offsetY) throws IOException, DeviceException {

        if (isFrameCentredY()) {
            return;
        }

        LucamAPI.LUCAM_FRAME_FORMAT frameFormat = new LucamAPI.LUCAM_FRAME_FORMAT();
        LucamAPI.LUCAM_CONVERSION   conversion  = new LucamAPI.LUCAM_CONVERSION();
        FloatBuffer                 frameRate   = FloatBuffer.allocate(1);

        if (!api.LucamGetFormat(handle, frameFormat, frameRate)) {
            throwError("LucamGetFormat");
        }

        frameFormat.yOffset.setValue((long) offsetY * frameFormat.binY.binningY);

        if (!api.LucamSetFormat(handle, frameFormat, frameRate.get(0))) {
            throwError("LucamSetFormat");
        }

    }

    @Override
    public synchronized void setFrameCentredY(boolean centredY) throws IOException, DeviceException {

        this.centredY = centredY;

        if (centredY) {

            LucamAPI.LUCAM_FRAME_FORMAT frameFormat = new LucamAPI.LUCAM_FRAME_FORMAT();
            LucamAPI.LUCAM_CONVERSION   conversion  = new LucamAPI.LUCAM_CONVERSION();
            FloatBuffer                 frameRate   = FloatBuffer.allocate(1);

            if (!api.LucamGetFormat(handle, frameFormat, frameRate)) {
                throwError("LucamGetFormat");
            }

            frameFormat.yOffset.setValue((getSensorHeight() - frameFormat.height.intValue()) / 2);

            if (!api.LucamSetFormat(handle, frameFormat, frameRate.get(0))) {
                throwError("LucamSetFormat");
            }

        }

    }

    @Override
    public synchronized boolean isFrameCentredY() throws IOException, DeviceException {
        return centredY;
    }

    @Override
    public int getFrameSize() throws IOException, DeviceException {

        LucamAPI.LUCAM_FRAME_FORMAT frameFormat = new LucamAPI.LUCAM_FRAME_FORMAT();
        LucamAPI.LUCAM_CONVERSION   conversion  = new LucamAPI.LUCAM_CONVERSION();
        FloatBuffer                 frameRate   = FloatBuffer.allocate(1);

        if (!api.LucamGetFormat(handle, frameFormat, frameRate)) {
            throwError("LucamGetFormat");
        }

        return (frameFormat.width.intValue() / frameFormat.binX.binningX) * (frameFormat.height.intValue() / frameFormat.binY.binningY);

    }

    @Override
    public int getPhysicalFrameSize() throws IOException, DeviceException {

        LucamAPI.LUCAM_FRAME_FORMAT frameFormat = new LucamAPI.LUCAM_FRAME_FORMAT();
        LucamAPI.LUCAM_CONVERSION   conversion  = new LucamAPI.LUCAM_CONVERSION();
        FloatBuffer                 frameRate   = FloatBuffer.allocate(1);

        if (!api.LucamGetFormat(handle, frameFormat, frameRate)) {
            throwError("LucamGetFormat");
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

        LucamAPI.LUCAM_FRAME_FORMAT frameFormat = new LucamAPI.LUCAM_FRAME_FORMAT();
        LucamAPI.LUCAM_CONVERSION   conversion  = new LucamAPI.LUCAM_CONVERSION();
        FloatBuffer                 frameRate   = FloatBuffer.allocate(1);

        if (!api.LucamGetFormat(handle, frameFormat, frameRate)) {
            throwError("LucamGetFormat");
        }

        return frameFormat.binX.binningX;

    }

    @Override
    public void setBinningX(int x) throws IOException, DeviceException {

        LucamAPI.LUCAM_FRAME_FORMAT frameFormat = new LucamAPI.LUCAM_FRAME_FORMAT();
        LucamAPI.LUCAM_CONVERSION   conversion  = new LucamAPI.LUCAM_CONVERSION();
        FloatBuffer                 frameRate   = FloatBuffer.allocate(1);

        if (!api.LucamGetFormat(handle, frameFormat, frameRate)) {
            throwError("LucamGetFormat");
        }

        frameFormat.binX.binningX = (short) x;

        if (!api.LucamSetFormat(handle, frameFormat, frameRate.get(0))) {
            throwError("LucamSetFormat");
        }

    }

    @Override
    public int getBinningY() throws IOException, DeviceException {

        LucamAPI.LUCAM_FRAME_FORMAT frameFormat = new LucamAPI.LUCAM_FRAME_FORMAT();
        LucamAPI.LUCAM_CONVERSION   conversion  = new LucamAPI.LUCAM_CONVERSION();
        FloatBuffer                 frameRate   = FloatBuffer.allocate(1);

        if (!api.LucamGetFormat(handle, frameFormat, frameRate)) {
            throwError("LucamGetFormat");
        }

        return frameFormat.binY.binningY;

    }

    @Override
    public void setBinningY(int y) throws IOException, DeviceException {

        LucamAPI.LUCAM_FRAME_FORMAT frameFormat = new LucamAPI.LUCAM_FRAME_FORMAT();
        LucamAPI.LUCAM_CONVERSION   conversion  = new LucamAPI.LUCAM_CONVERSION();
        FloatBuffer                 frameRate   = FloatBuffer.allocate(1);

        if (!api.LucamGetFormat(handle, frameFormat, frameRate)) {
            throwError("LucamGetFormat");
        }

        frameFormat.binY.binningY = (short) y;

        if (!api.LucamSetFormat(handle, frameFormat, frameRate.get(0))) {
            throwError("LucamSetFormat");
        }

    }

    @Override
    public void setBinning(int x, int y) throws IOException, DeviceException {

        LucamAPI.LUCAM_FRAME_FORMAT frameFormat = new LucamAPI.LUCAM_FRAME_FORMAT();
        LucamAPI.LUCAM_CONVERSION   conversion  = new LucamAPI.LUCAM_CONVERSION();
        FloatBuffer                 frameRate   = FloatBuffer.allocate(1);

        if (!api.LucamGetFormat(handle, frameFormat, frameRate)) {
            throwError("LucamGetFormat");
        }

        frameFormat.binX.binningX = (short) x;
        frameFormat.binY.binningY = (short) y;

        if (!api.LucamSetFormat(handle, frameFormat, frameRate.get(0))) {
            throwError("LucamSetFormat");
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
            throwError("LucamCameraClose");
        }

    }

    @Override
    public Address getAddress() {
        return null;
    }

    @Override
    public void setAmplifierGain(double gain) throws DeviceException, IOException {

        if (!api.LucamSetProperty(handle, LucamAPI.LUCAM_PROP_GAIN, (float) gain, new NativeLong(0))) {
            throwError("LucamSetProperty(LUCAM_PROP_GAIN, %e)", gain);
        }

    }

    @Override
    public double getAmplifierGain() throws DeviceException, IOException {

        FloatBuffer           buffer = FloatBuffer.allocate(1);
        NativeLongByReference ref    = new NativeLongByReference();

        if (!api.LucamGetProperty(handle, LucamAPI.LUCAM_PROP_GAIN, buffer, ref)) {
            throwError("LucamGetProperty(LUCAM_PROP_GAIN)");
        }

        return buffer.get(0);

    }

}
