package jisa.devices.spectrometer.spectrum;

import io.jhdf.HdfFile;
import io.jhdf.WritableHdfFile;
import io.jhdf.api.WritableDataset;
import jisa.devices.spectrometer.Spectrometer;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public class SpectrumReader {

    private final String          path;
    private final DataInputStream dis;

    public SpectrumReader(String path) throws IOException {

        this.path = path;

        FileInputStream fis = new FileInputStream(path);
        DataInputStream dis = new DataInputStream(new BufferedInputStream(fis));

        String header = new String(dis.readNBytes(Spectrometer.SPECTRUM_STREAM_HEADER.length()), StandardCharsets.US_ASCII);

        if (header.startsWith("JISA SPECTRUM STREAM")) {
            this.dis = dis;
        } else {

            dis.close();
            fis    = new FileInputStream(path);
            dis    = new DataInputStream(new InflaterInputStream(new BufferedInputStream(fis)));
            header = new String(dis.readNBytes(Spectrometer.SPECTRUM_STREAM_HEADER.length()), StandardCharsets.US_ASCII);

            if (header.startsWith("JISA SPECTRUM STREAM")) {
                this.dis = dis;
            } else {
                throw new IOException(String.format("\"%s\" is not a valid spectrum stream file", path));
            }

        }

    }

    public synchronized boolean hasSpectrum() throws IOException {
        return dis.available() > 0;
    }

    public synchronized Spectrum readSpectrum() throws IOException {

        int      length      = dis.readInt();
        long     timestamp   = dis.readLong();
        double[] wavelengths = new double[length];
        double[] counts      = new double[length];

        ByteBuffer.wrap(dis.readNBytes(Double.BYTES * length)).asDoubleBuffer().get(wavelengths);
        ByteBuffer.wrap(dis.readNBytes(Double.BYTES * length)).asDoubleBuffer().get(counts);

        return new Spectrum(wavelengths, counts, timestamp);

    }

    public synchronized void convertToHDF5(String path) throws IOException {

        Path file = Path.of(path);

        try (WritableHdfFile hdf = HdfFile.write(file)) {

            int i = 0;
            while (hasSpectrum()) {

                Spectrum        spectrum = readSpectrum();
                double[][]      data     = spectrum.stream().map(s -> new double[]{s.getWavelength(), s.getCounts()}).toArray(double[][]::new);
                WritableDataset dataset  = hdf.putDataset(String.format("Spectrum %d", i++), data);

                dataset.putAttribute("Timestamp", spectrum.getTimestamp());

            }

        } finally {
            close();
        }

    }

    public synchronized void compress() throws IOException {

        File                 temp = new File(path + ".temp");
        DeflaterOutputStream os   = new DeflaterOutputStream(new BufferedOutputStream(new FileOutputStream(temp)));

        os.write(Spectrometer.SPECTRUM_STREAM_HEADER.getBytes(StandardCharsets.US_ASCII));
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

        os.write(Spectrometer.SPECTRUM_STREAM_HEADER.getBytes(StandardCharsets.US_ASCII));
        dis.transferTo(os);

        os.flush();
        os.close();

        close();

    }

    public synchronized void close() throws IOException {
        dis.close();
    }

}
