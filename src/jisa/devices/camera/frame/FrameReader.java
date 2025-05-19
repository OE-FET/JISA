package jisa.devices.camera.frame;

import java.io.*;

public class FrameReader<F extends Frame> {

    private final String          path;
    private final int             bytesPerPixel;
    private final FrameCreator<F> frameCreator;
    private final FileInputStream fis;
    private final DataInputStream dis;

    public FrameReader(String path, int bytesPerPixel, FrameCreator<F> frameCreator) throws FileNotFoundException {

        this.path          = path;
        this.bytesPerPixel = bytesPerPixel;
        this.frameCreator  = frameCreator;
        this.fis           = new FileInputStream(path);
        this.dis           = new DataInputStream(new BufferedInputStream(fis));

    }

    public F readFrame() throws IOException {

        int    width     = dis.readInt();
        int    height    = dis.readInt();
        long   timestamp = dis.readLong();
        byte[] data      = new byte[width * height * bytesPerPixel];

        dis.read(data);

        return frameCreator.createFrame(width, height, timestamp, data);

    }

    public boolean hasFrame() throws IOException {
        return fis.available() > 0;
    }

    public interface FrameCreator<F extends Frame> {
        F createFrame(int width, int height, long timestamp, byte[] data);
    }

}
