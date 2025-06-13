package jisa.devices.camera.frame;

import io.jhdf.HdfFile;
import io.jhdf.WritableHdfFile;
import io.jhdf.api.WritableDataset;
import jisa.devices.camera.Camera;

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
