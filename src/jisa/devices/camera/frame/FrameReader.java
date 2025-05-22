package jisa.devices.camera.frame;

import io.jhdf.HdfFile;
import io.jhdf.WritableHdfFile;
import io.jhdf.api.WritableDataset;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public class FrameReader<F extends Frame> {

    private final String          path;
    private final FrameCreator<F> frameCreator;
    private final FileInputStream fis;
    private final DataInputStream dis;
    private       F               cache = null;

    public FrameReader(String path, boolean compressed, FrameCreator<F> frameCreator) throws IOException {

        this.path         = path;
        this.frameCreator = frameCreator;
        this.fis          = new FileInputStream(path);

        if (compressed) {
            this.dis = new DataInputStream(new InflaterInputStream(new BufferedInputStream(fis)));
        } else {
            this.dis = new DataInputStream(new BufferedInputStream(fis));
        }

    }

    public synchronized F readFrame() throws IOException {

        if (cache != null) {
            F frame = cache;
            cache = null;
            return frame;
        }

        int    width         = dis.readInt();
        int    height        = dis.readInt();
        int    bytesPerPixel = dis.readInt();
        long   timestamp     = dis.readLong();
        byte[] data          = dis.readNBytes(width * height * bytesPerPixel);

        return frameCreator.createFrame(width, height, bytesPerPixel, timestamp, data);

    }

    public synchronized boolean hasFrame() throws IOException {

        if (dis.available() == 0) {
            return false;
        }

        if (cache == null) {

            try {

                int    width         = dis.readInt();
                int    height        = dis.readInt();
                int    bytesPerPixel = dis.readInt();
                long   timestamp     = dis.readLong();
                byte[] data          = dis.readNBytes(width * height * bytesPerPixel);

                cache = frameCreator.createFrame(width, height, bytesPerPixel, timestamp, data);

            } catch (Throwable e) {
                return false;
            }

        }

        return true;

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

        File                temp    = new File(path + ".temp");
        WritableByteChannel tempOut = Channels.newChannel(new DeflaterOutputStream(new BufferedOutputStream(new FileOutputStream(temp)), false));

        fis.getChannel().transferTo(0, fis.available(), tempOut);

        close();

        File f = new File(path);
        f.delete();

        Files.move(temp.toPath(), f.toPath());
        tempOut.close();

    }

    public synchronized void compress(String newPath) throws IOException {

        File                temp    = new File(newPath);
        WritableByteChannel tempOut = Channels.newChannel(new DeflaterOutputStream(new BufferedOutputStream(new FileOutputStream(temp)), false));
        fis.getChannel().transferTo(0, fis.available(), tempOut);
        close();
        tempOut.close();

    }

}
