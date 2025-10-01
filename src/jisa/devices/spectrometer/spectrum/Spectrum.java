package jisa.devices.spectrometer.spectrum;

import com.google.common.primitives.Doubles;
import org.jetbrains.annotations.NotNull;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.stream.*;


public class Spectrum implements Iterable<Spectrum.Point> {

    protected final double[] wavelengths;
    protected final double[] counts;
    protected       long     timestamp;

    public Spectrum(double[] wavelengths, double[] counts, long timestamp) {

        if (wavelengths.length != counts.length) {
            throw new IllegalArgumentException("Wavelength and Value arrays must have the same length.");
        }

        this.wavelengths = wavelengths;
        this.counts      = counts;
        this.timestamp   = timestamp;

    }

    public Spectrum(double[] wavelengths, double[] counts) {
        this(wavelengths, counts, System.nanoTime());
    }

    public Spectrum(double[] wavelengths, float[] counts, long timestamp) {
        this(wavelengths, IntStream.range(0, counts.length).mapToDouble(i -> counts[i]).toArray(), timestamp);
    }

    public Spectrum(double[] wavelengths, float[] counts) {
        this(wavelengths, IntStream.range(0, counts.length).mapToDouble(i -> counts[i]).toArray());
    }

    public Spectrum(double[] wavelengths, int[] counts) {
        this(wavelengths, IntStream.of(counts).mapToDouble(i -> (double) i).toArray());
    }

    public Spectrum(double[] wavelengths, int[] counts, long timestamp) {
        this(wavelengths, IntStream.of(counts).mapToDouble(i -> (double) i).toArray(), timestamp);
    }

    public Spectrum(double[] wavelengths, long[] counts) {
        this(wavelengths, LongStream.of(counts).mapToDouble(i -> (double) i).toArray());
    }

    public Spectrum(double[] wavelengths, long[] counts, long timestamp) {
        this(wavelengths, LongStream.of(counts).mapToDouble(i -> (double) i).toArray(), timestamp);
    }

    /**
     * Returns a new Spectrum object whose points are the result of adding the provided spectrum from this one.
     *
     * @param other The spectrum to add.
     *
     * @return Summed spectrum.
     */
    public Spectrum add(Spectrum other) {

        if (size() != other.size()) {
            throw new IllegalArgumentException(String.format("Spectrum sizes are not equal (trying to add %d points to %d).", other.size(), size()));
        }

        return new Spectrum(
            wavelengths.clone(),
            IntStream.range(0, size()).mapToDouble(i -> counts[i] + other.counts[i]).toArray(),
            Math.max(timestamp, other.timestamp)
        );

    }

    /**
     * Returns a new Spectrum object whose points are the result of subtracting the provided spectrum from this one.
     *
     * @param other The spectrum to subtract.
     *
     * @return Subtracted spectrum.
     */
    public Spectrum subtract(Spectrum other) {

        if (size() != other.size()) {
            throw new IllegalArgumentException("Spectrum sizes are not equal.");
        }

        return new Spectrum(
            wavelengths.clone(),
            IntStream.range(0, size()).mapToDouble(i -> counts[i] - other.counts[i]).toArray(),
            Math.max(timestamp, other.timestamp)
        );

    }

    public Spectrum divide(Spectrum other) {

        if (size() != other.size()) {
            throw new IllegalArgumentException("Spectrum sizes are not equal.");
        }

        return new Spectrum(
            wavelengths.clone(),
            IntStream.range(0, counts.length).mapToDouble(i -> {
                try {
                    return counts[i] / other.counts[i];
                } catch (Throwable e) {
                    return Double.NaN;
                }
            }).toArray(),
            Math.max(timestamp, other.timestamp)
        );

    }

    public Spectrum multiply(Spectrum other) {

        if (size() != other.size()) {
            throw new IllegalArgumentException("Spectrum sizes are not equal.");
        }

        return new Spectrum(
            wavelengths.clone(),
            IntStream.range(0, size()).mapToDouble(i -> counts[i] * other.counts[i]).toArray(),
            Math.max(timestamp, other.timestamp)
        );

    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Returns a deep copy of this Spectrum object.
     *
     * @return Deep copy.
     */
    public Spectrum copy() {
        return new Spectrum(getWavelengths(), getCounts(), getTimestamp());
    }

    public void copyFrom(Spectrum other) {
        System.arraycopy(other.counts, 0, counts, 0, counts.length);
        this.timestamp = other.timestamp;
    }

    public void copyFrom(double[] counts) {
        System.arraycopy(counts, 0, this.counts, 0, this.counts.length);
    }

    /**
     * Returns a new Spectrum object whose points are the result of subtracting the provided spectrum from this one.
     * (Alias for subtract(...) to enable Kotlin operator overloading).
     *
     * @param other The spectrum to subtract.
     *
     * @return Subtracted spectrum.
     */
    public Spectrum minus(Spectrum other) {
        return subtract(other);
    }

    /**
     * Returns a new Spectrum object whose points are the result of adding the provided spectrum from this one.
     * (Alias for add(...) to enable Kotlin operator overloading).
     *
     * @param other The spectrum to add.
     *
     * @return Summed spectrum.
     */
    public Spectrum plus(Spectrum other) {
        return add(other);
    }

    public Spectrum times(Spectrum other) {
        return multiply(other);
    }

    public Spectrum div(Spectrum other) {
        return divide(other);
    }

    public Spectrum subSpectrum(int start, int end) {

        double[] newWl = new double[end - start + 1];
        double[] newCt = new double[end - start + 1];

        System.arraycopy(wavelengths, start, newWl, 0, newWl.length);
        System.arraycopy(counts, start, newCt, 0, newCt.length);

        return new Spectrum(newWl, newCt, timestamp);

    }

    public Spectrum subSpectrumByWavelength(double startWL, double endWL) {
        return subSpectrum(indexByWavelength(startWL), indexByWavelength(endWL));
    }

    /**
     * Returns a Stream of each Point object in this Spectrum.
     *
     * @return Point Stream.
     */
    public Stream<Point> stream() {
        return IntStream.range(0, size()).mapToObj(i -> new Point(getWavelength(i), getCount(i)));
    }

    /**
     * Returns a copy of the data in this Spectrum represented as a List of Point objects.
     *
     * @return List of Point objects.
     */
    public List<Point> toList() {
        return stream().collect(Collectors.toList());
    }

    /**
     * Returns a copy of the data in this spectrum represented as a 2D array.
     *
     * @return 2D data array.
     */
    public double[][] toArray() {

        int size = size();

        double[][] data = new double[size][2];

        for (int i = 0; i < size; i++) {
            data[i][0] = getWavelength(i);
            data[i][1] = getCount(i);
        }

        return data;

    }

    /**
     * Returns a copy of the wavelengths in this spectrum, as an array.
     *
     * @return Array of wavelengths.
     */
    public double[] getWavelengths() {
        return wavelengths.clone();
    }

    public List<Double> getWavelengthList() {
        return Doubles.asList(getWavelengths());
    }

    /**
     * Returns a copy of the inverse wavelengths in this spectrum, as an array.
     *
     * @return Array of wavenumbers.
     */
    public double[] getWavenumbers() {
        return DoubleStream.of(wavelengths).map(v -> 1.0 / v).toArray();
    }

    public List<Double> getWavenumberList() {
        return Doubles.asList(getWavenumbers());
    }

    /**
     * Returns a copy of the inverse wavelengths multiplied by 2 PI in this spectrum, as an array.
     *
     * @return Array of angular wavenumbers.
     */
    public double[] getAngularWavenumbers() {
        return DoubleStream.of(wavelengths).map(v -> 2.0 * Math.PI / v).toArray();
    }

    public List<Double> getAngularWavelenumberList() {
        return Doubles.asList(getAngularWavenumbers());
    }

    /**
     * Returns a copy of the values in this spectrum, as an array.
     *
     * @return Array of values.
     */
    public double[] getCounts() {
        return counts.clone();
    }

    /**
     * Returns a copy of the values in this spectrum, as a List,
     *
     * @return List of values.
     */
    public List<Double> getCountList() {
        return Doubles.asList(getCounts());
    }

    public double getWavelength(int index) {
        return wavelengths[index];
    }

    public double getWavenumber(int index) {
        return 1.0 / getWavelength(index);
    }

    public double getAngularWavenumber(int index) {
        return 2.0 * Math.PI / getWavelength(index);
    }

    public double getCount(int index) {
        return counts[index];
    }

    public int indexByWavelength(double wavelength) {

        int    minIndex    = 0;
        double minDistance = Double.MAX_VALUE;

        for (int i = 0; i < wavelengths.length; i++) {

            double wl       = wavelengths[i];
            double distance = Math.abs(wl - wavelength);

            if (distance < minDistance) {
                minDistance = distance;
                minIndex    = i;
            }

        }

        return minIndex;

    }

    public double getCountByWavelength(double wavelength) {
        return counts[indexByWavelength(wavelength)];
    }

    public Point get(double wavelength) {
        return get(indexByWavelength(wavelength));
    }

    /**
     * Returns the nth point in the Spectrum.
     *
     * @param index n.
     *
     * @return nth point.
     */
    public Point get(int index) {

        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException();
        }

        return new Point(getWavelength(index), getCount(index));

    }

    public void writeToStream(DataOutputStream out) throws IOException {

        out.writeInt(wavelengths.length);
        out.writeLong(timestamp);

        ByteBuffer wl = ByteBuffer.allocate(wavelengths.length * Double.BYTES);
        ByteBuffer ct = ByteBuffer.allocate(counts.length * Double.BYTES);

        wl.asDoubleBuffer().put(wavelengths);
        ct.asDoubleBuffer().put(counts);

        out.write(wl.array());
        out.write(ct.array());

    }

    /**
     * Returns the number of points in this spectrum.
     *
     * @return Size of spectrum.
     */
    public int size() {
        return wavelengths.length;
    }

    @NotNull
    @Override
    public Iterator<Point> iterator() {
        return stream().iterator();
    }

    public static class Point {

        private final double wavelength;
        private final double counts;

        public Point(double wavelength, double value) {
            this.wavelength = wavelength;
            this.counts     = value;
        }

        public double getWavelength() {
            return wavelength;
        }

        public double getWavenumber() {
            return 1.0 / wavelength;
        }

        public double getAngularWavenumber() {
            return 2.0 * Math.PI / wavelength;
        }

        public double getCounts() {
            return counts;
        }

    }

}
