package jisa.devices.camera.frame;

import io.jhdf.HdfFile;
import io.jhdf.WritableHdfFile;
import io.jhdf.api.WritableDataset;
import jisa.devices.camera.Camera;
import org.jcodec.api.SequenceEncoder;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Picture;
import org.jcodec.common.model.Rational;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public class FrameReader<F extends Frame> {

    private final String          path;
    private final FrameCreator<F> frameCreator;
    private final DataInputStream dis;

    public FrameReader(String path, FrameCreator<F> frameCreator) throws IOException {

        this.path         = path;
        this.frameCreator = frameCreator;

        FileInputStream fis = new FileInputStream(path);
        DataInputStream dis = new DataInputStream(new BufferedInputStream(fis));

        String header = new String(dis.readNBytes(Camera.IMAGE_STREAM_HEADER.length()), StandardCharsets.US_ASCII);

        if (header.startsWith("JISA IMAGE STREAM")) {
            this.dis = dis;
        } else {

            dis.close();
            fis    = new FileInputStream(path);
            dis    = new DataInputStream(new InflaterInputStream(new BufferedInputStream(fis)));
            header = new String(dis.readNBytes(Camera.IMAGE_STREAM_HEADER.length()), StandardCharsets.US_ASCII);

            if (header.startsWith("JISA IMAGE STREAM")) {
                this.dis = dis;
            } else {
                throw new IOException(String.format("\"%s\" is not a valid image stream file", path));
            }

        }

    }

    public static void upgrade(String oldFile, String newFile) throws IOException {


        FileInputStream  fis = new FileInputStream(oldFile);
        DataInputStream  dis = new DataInputStream(new BufferedInputStream(fis));
        FileOutputStream fos = new FileOutputStream(newFile);
        DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(fos));

        dos.writeBytes(Camera.IMAGE_STREAM_HEADER);
        dis.transferTo(dos);

        dis.close();
        dos.close();

    }

    public synchronized F readFrame() throws IOException {

        int    width         = dis.readInt();
        int    height        = dis.readInt();
        int    bytesPerPixel = dis.readInt();
        long   timestamp     = dis.readLong();
        byte[] data          = dis.readNBytes(width * height * bytesPerPixel);

        return frameCreator.createFrame(width, height, bytesPerPixel, timestamp, data);

    }

    public synchronized boolean hasFrame() throws IOException {
        return dis.available() > 0;
    }

    public void close() throws IOException {
        dis.close();
    }

    public interface FrameCreator<F extends Frame> {
        F createFrame(int width, int height, int bytesPerPixel, long timestamp, byte[] data);
    }

    public synchronized void convertToHDF5(String path) throws IOException {

        Path file = Path.of(path);

        try (WritableHdfFile hdf = HdfFile.write(file)) {

            int i = 0;
            while (hasFrame()) {

                F               frame = readFrame();
                WritableDataset data  = frame.writeToHDF(hdf, String.format("Frame %d", i++));

                data.putAttribute("Timestamp", frame.getTimestamp());

            }

        } finally {
            close();
        }

    }

    public synchronized void convertToMP4(String path) throws IOException, InterruptedException {

        Frame frame1 = readFrame().copy();
        Frame frame2 = readFrame().copy();

        double diffSec = (frame2.getTimestamp() - frame1.getTimestamp()) / 1e9;
        int fps = diffSec > 0 ? Math.max(1, (int) Math.round(1.0 / diffSec)) : 30;

        Path file = Path.of(path);

        SequenceEncoder enc = SequenceEncoder.createWithFps(
                NIOUtils.writableChannel(file.toFile()),
                new Rational(fps, 1)
        );

        encodeFrame(enc, frame1);
        encodeFrame(enc, frame2);

        while (hasFrame()) {
            encodeFrame(enc, readFrame());
        }

        enc.finish();
    }

    private void encodeFrame(SequenceEncoder enc, Frame frame) throws IOException {

        byte[] rgb = frame.getRGBBytes();

        int w = frame.getWidth();
        int h = frame.getHeight();
        int size = w * h;

        byte[] y = new byte[size];
        byte[] u = new byte[size / 4];
        byte[] v = new byte[size / 4];

        int yIndex = 0;
        int uvIndex = 0;

        int rgbIndex = 0;

        for (int j = 0; j < h; j++) {
            for (int i = 0; i < w; i++) {

                int r = rgb[rgbIndex++] & 0xFF;
                int g = rgb[rgbIndex++] & 0xFF;
                int b = rgb[rgbIndex++] & 0xFF;

                // Luma
                int yy = (  66 * r + 129 * g +  25 * b + 128) >> 8;
                y[yIndex++] = (byte) (yy + 16);

                // Chroma subsampling (4:2:0)
                if ((j % 2 == 0) && (i % 2 == 0)) {

                    int uu = (-38 * r - 74 * g + 112 * b + 128) >> 8;
                    int vv = (112 * r - 94 * g - 18 * b + 128) >> 8;

                    u[uvIndex] = (byte) (uu + 128);
                    v[uvIndex] = (byte) (vv + 128);

                    uvIndex++;
                }
            }
        }

        Picture pic = Picture.createPicture(
                w,
                h,
                new byte[][] { y, u, v },
                ColorSpace.YUV420
        );

        enc.encodeNativeFrame(pic);
    }

    public synchronized void compress() throws IOException {

        File                 temp = new File(path + ".temp");
        DeflaterOutputStream os   = new DeflaterOutputStream(new BufferedOutputStream(new FileOutputStream(temp)));

        os.write(Camera.IMAGE_STREAM_HEADER.getBytes(StandardCharsets.US_ASCII));
        dis.transferTo(os);

        close();

        File f = new File(path);
        f.delete();

        Files.move(temp.toPath(), f.toPath());
        os.flush();
        os.close();

    }

    public synchronized void compress(String newPath) throws IOException {

        File                 temp = new File(newPath);
        DeflaterOutputStream os   = new DeflaterOutputStream(new BufferedOutputStream(new FileOutputStream(temp)));

        os.write(Camera.IMAGE_STREAM_HEADER.getBytes(StandardCharsets.US_ASCII));
        dis.transferTo(os);

        os.flush();
        os.close();

        close();

    }

}
