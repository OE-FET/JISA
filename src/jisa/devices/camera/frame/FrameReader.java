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
import org.jcodec.scale.AWTUtil;

import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
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


    public synchronized void convertToMP4(String path) throws IOException {

        try {

            Frame  frame1 = readFrame().copy();
            Frame  frame2 = readFrame().copy();
            double diff   = (frame2.getTimestamp() - frame1.getTimestamp()) / 1e9;
            int    fps    = (int) (1.0 / diff);
            Path   file   = Path.of(path);

            Picture       picture1 = Picture.create(frame1.getWidth(), frame1.getHeight(), ColorSpace.RGB);
            Picture       picture2 = Picture.create(frame2.getWidth(), frame2.getHeight(), ColorSpace.RGB);
            BufferedImage image1   = frame1.toBufferedImage();
            BufferedImage image2   = frame2.toBufferedImage();

            AWTUtil.fromBufferedImage(image1, picture1);
            AWTUtil.fromBufferedImage(image2, picture2);

            SequenceEncoder enc = SequenceEncoder.createWithFps(NIOUtils.writableChannel(file.toFile()), new Rational(fps, 1));

            enc.encodeNativeFrame(picture1);
            enc.encodeNativeFrame(picture2);

            while (hasFrame()) {

                F frame = readFrame();

                Picture       picture = Picture.create(frame.getWidth(), frame.getHeight(), ColorSpace.RGB);
                BufferedImage image   = frame.toBufferedImage();

                AWTUtil.fromBufferedImage(image, picture);
                enc.encodeNativeFrame(picture);

            }

            enc.finish();

        } finally {
            close();
        }

    }


    public synchronized void convertToMP4(String path, int fps) throws IOException {

        try {

            Frame frame1  = readFrame().copy();
            Path  file    = Path.of(path);
            int   between = 1000000 / fps;

            Picture       picture1 = Picture.create(frame1.getWidth(), frame1.getHeight(), ColorSpace.RGB);
            BufferedImage image1   = frame1.toBufferedImage();

            AWTUtil.fromBufferedImage(image1, picture1);

            SequenceEncoder enc = SequenceEncoder.createWithFps(NIOUtils.writableChannel(file.toFile()), new Rational(fps, 1));

            enc.encodeNativeFrame(picture1);

            long last = frame1.getTimestamp();

            while (hasFrame()) {

                F frame = readFrame();

                if (frame.getTimestamp() - last >= between) {

                    Picture       picture = Picture.create(frame.getWidth(), frame.getHeight(), ColorSpace.RGB);
                    BufferedImage image   = frame.toBufferedImage();

                    AWTUtil.fromBufferedImage(image, picture);
                    enc.encodeNativeFrame(picture);

                    last = frame.getTimestamp();

                }

            }

            enc.finish();

        } finally {
            close();
        }

    }

    public synchronized void convertToGIF(String path, int fps) throws IOException {

        Frame         frame1  = readFrame().copy();
        BufferedImage image1  = frame1.toBufferedImage();
        int           between = 1000 / fps;

        ImageOutputStream output = new FileImageOutputStream(new File(path));
        GifSequenceWriter writer = new GifSequenceWriter(output, image1.getType(), between, true);

        long last = frame1.getTimestamp();

        while (hasFrame()) {

            Frame frame = readFrame();

            if ((frame.getTimestamp() - last) >= (between * 1000000)) {

                BufferedImage image = frame.toBufferedImage();
                writer.writeToSequence(image);

                last = frame.getTimestamp();

            }

        }

        writer.close();
        output.close();

    }

    public synchronized void convertToGIF(String path) throws IOException {

        Frame         frame1 = readFrame().copy();
        Frame         frame2 = readFrame().copy();
        BufferedImage image1 = frame1.toBufferedImage();
        BufferedImage image2 = frame2.toBufferedImage();

        int between = (int) ((frame2.getTimestamp() - frame1.getTimestamp()) / 1000000);

        ImageOutputStream output = new FileImageOutputStream(new File(path));
        GifSequenceWriter writer = new GifSequenceWriter(output, image1.getType(), between, true);

        writer.writeToSequence(image2);

        while (hasFrame()) {

            Frame frame = readFrame();

            BufferedImage image = frame.toBufferedImage();
            writer.writeToSequence(image);

        }

        writer.close();
        output.close();

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
