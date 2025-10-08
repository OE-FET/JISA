package jisa.devices.spectrometer.spectrum;

import com.google.common.primitives.Doubles;
import org.jetbrains.annotations.NotNull;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.util.Arrays;
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

    public Spectrum(Iterable<? extends Number> wavelengths, Iterable<? extends Number> counts, long timestamp) {

        this(
            StreamSupport.stream(wavelengths.spliterator(), false).mapToDouble(Number::doubleValue).toArray(),
            StreamSupport.stream(counts.spliterator(), false).mapToDouble(Number::doubleValue).toArray(),
            timestamp
        );

    }

    public Spectrum(Iterable<? extends Number> wavelengths, Iterable<? extends Number> counts) {

        this(
            StreamSupport.stream(wavelengths.spliterator(), false).mapToDouble(Number::doubleValue).toArray(),
            StreamSupport.stream(counts.spliterator(), false).mapToDouble(Number::doubleValue).toArray()
        );

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

    public boolean isCompatibleWith(Spectrum other) {
        return Arrays.equals(wavelengths, other.wavelengths);
    }

    /**
     * Returns a new Spectrum object whose points are the result of adding the counts of each point in the supplied spectrum
     * to those of this spectrum.
     *
     * @param other The spectrum to add to this one.
     *
     * @return Added spectrum.
     *
     * @throws IllegalArgumentException If the spectra have different sizes.
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
     * Returns a new Spectrum object whose points are the result of subtracting the counts of each point in the supplied spectrum
     * from those of this spectrum.
     *
     * @param other The spectrum to subtract from this one.
     *
     * @return Subtracted spectrum.
     *
     * @throws IllegalArgumentException If the spectra have different sizes.
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

    /**
     * Returns a new Spectrum object whose points are the result of dividing the counts of each point in this spectrum
     * by those of the supplied spectrum.
     *
     * @param other The spectrum to divide this one by.
     *
     * @return Divided spectrum.
     *
     * @throws IllegalArgumentException If the spectra have different sizes.
     */
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

    /**
     * Returns a new Spectrum object whose points are the result of multiplying the counts of each point in this spectrum
     * by those of the supplied spectrum.
     *
     * @param other The spectrum to multiply this one by.
     *
     * @return Multiplied spectrum.
     *
     * @throws IllegalArgumentException If the spectra have different sizes.
     */
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

    /**
     * Returns a new Spectrum object whose points are the result of multiplying the counts of each point in this spectrum
     * by the supplied scalar value.
     *
     * @param other The scalar to multiply this spectrum by.
     *
     * @return Multiplied spectrum.
     */
    public Spectrum multiply(Number scalar) {
        double factor = scalar.doubleValue();
        return new Spectrum(wavelengths.clone(), DoubleStream.of(wavelengths).map(v -> v * factor).toArray(), timestamp);
    }

    /**
     * Returns a new Spectrum object whose points are the result of dividing the counts of each point in this spectrum
     * by the supplied scalar value.
     *
     * @param other The scalar to divide this spectrum by.
     *
     * @return Divided spectrum.
     */
    public Spectrum divide(Number scalar) {
        double factor = scalar.doubleValue();
        return new Spectrum(wavelengths.clone(), DoubleStream.of(wavelengths).map(v -> v / factor).toArray(), timestamp);
    }

    /**
     * Returns a new Spectrum object whose points are the result of adding the supplied scalar value to the counts of each point in this spectrum.
     *
     * @param other The scalar to add to this spectrum.
     *
     * @return Added spectrum.
     */
    public Spectrum add(Number scalar) {
        double factor = scalar.doubleValue();
        return new Spectrum(wavelengths.clone(), DoubleStream.of(wavelengths).map(v -> v + factor).toArray(), timestamp);
    }

    /**
     * Returns a new Spectrum object whose points are the result of subtracting the supplied scalar value from the counts of each point in this spectrum.
     *
     * @param other The scalar to subtract from this spectrum.
     *
     * @return Subtracted spectrum.
     */
    public Spectrum subtract(Number scalar) {
        double factor = scalar.doubleValue();
        return new Spectrum(wavelengths.clone(), DoubleStream.of(wavelengths).map(v -> v - factor).toArray(), timestamp);
    }

    /**
     * Sets the timestamp of this frame.
     *
     * @param timestamp Timestep to set.
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Returns the timestamp stored with this frame. This value may not necessarily refer to specific date/time, but should
     * at least be comparable with timestamps of other frames that came from the same series of frames from the same camera.
     *
     * @return Timestamp.
     */
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

    /**
     * Copies the counts from the supplied spectrum to this one in a memory-efficient way.
     *
     * @param other The spectrum from which to copy.
     */
    public void copyFrom(Spectrum other) {
        System.arraycopy(other.counts, 0, counts, 0, counts.length);
        this.timestamp = other.timestamp;
    }

    /**
     * Copies the counts in the supplied array, overriding those stored in this frame in a memory-efficient way.
     *
     * @param counts The array to copy from.
     */
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
     * Kotlin operator overloads
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

    public Spectrum minus(Number scalar) {
        return subtract(scalar);
    }

    public Spectrum plus(Number scalar) {
        return add(scalar);
    }

    public Spectrum times(Number scalar) {
        return multiply(scalar);
    }

    public Spectrum div(Number scalar) {
        return divide(scalar);
    }


    /**
     * Returns a new spectrum only containing the points within the specified index range.
     *
     * @param start First index to include.
     * @param end   Last index to include.
     *
     * @return Spectrum only containing specified subset of points.
     */
    public Spectrum subSpectrumByIndex(int start, int end) {

        double[] newWl = new double[end - start + 1];
        double[] newCt = new double[end - start + 1];

        System.arraycopy(wavelengths, start, newWl, 0, newWl.length);
        System.arraycopy(counts, start, newCt, 0, newCt.length);

        return new Spectrum(newWl, newCt, timestamp);

    }

    /**
     * Returns a new spectrum only containing the points within the specified wavelength range.
     *
     * @param startWL First wavelength to include.
     * @param endWL   Last wavelength to include.
     *
     * @return Spectrum only containing specified subset of points.
     */
    public Spectrum subSpectrum(double startWL, double endWL) {
        return subSpectrumByIndex(indexByWavelength(startWL), indexByWavelength(endWL));
    }

    /**
     * Removes the specified wavelength range from this spectrum, returning a spectrum without it.
     *
     * @param startWL The first wavelength in the region to remove.
     * @param endWL   The last wavelength in the region to remove.
     *
     * @return Cropped spectrum.
     */
    public Spectrum cropOutRange(double startWL, double endWL) {

        Spectrum first  = subSpectrum(Arrays.stream(wavelengths).min().orElse(Double.NEGATIVE_INFINITY), startWL);
        Spectrum second = subSpectrum(endWL, Arrays.stream(wavelengths).max().orElse(Double.POSITIVE_INFINITY));

        return first.concatonate(second);

    }

    /**
     * Concatonate other spectra objects onto the end of this one to make a new spectum object.
     *
     * @param spectra The spectra to concatonate to this one.
     *
     * @return Concatonated spectrum.
     */
    public Spectrum concatonate(Spectrum... spectra) {

        int          newSize  = wavelengths.length + Arrays.stream(spectra).mapToInt(Spectrum::size).sum();
        DoubleBuffer wlBuffer = DoubleBuffer.wrap(new double[newSize]);
        DoubleBuffer ctBuffer = DoubleBuffer.wrap(new double[newSize]);

        wlBuffer.put(wavelengths);
        ctBuffer.put(counts);

        for (Spectrum spectrum : spectra) {
            wlBuffer.put(spectrum.wavelengths);
            ctBuffer.put(spectrum.counts);
        }

        return new Spectrum(wlBuffer.array(), ctBuffer.array(), Arrays.stream(spectra).mapToLong(Spectrum::getTimestamp).max().orElse(0L));

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

    /**
     * Returns a copy of the wavelengths in this spectrum, as a List object.
     *
     * @return List of wavelengths.
     */
    public List<Double> listWavelengths() {
        return Doubles.asList(getWavelengths());
    }

    /**
     * Returns a copy of the inverse wavelengths in this spectrum, as an array.
     *
     * @return Array of wavenumbers.
     */
    public double[] getWavenumbers() {
        return DoubleStream.of(wavelengths).map(l -> 1.0 / l).toArray();
    }

    /**
     * Returns a copy of the inverse wavelengths in this spectrum, as a List object.
     *
     * @return List of wavenumbers.
     */
    public List<Double> listWavenumbers() {
        return Doubles.asList(getWavenumbers());
    }

    /**
     * Returns a copy of the inverse wavelengths multiplied by 2 PI in this spectrum, as an array.
     *
     * @return Array of angular wavenumbers.
     */
    public double[] getAngularWavenumbers() {
        return DoubleStream.of(wavelengths).map(l -> 2.0 * Math.PI / l).toArray();
    }

    /**
     * Returns a copy of the inverse wavelengths multiplied by 2 PI in this spectrum, as a List object.
     *
     * @return List of angular wavenumbers.
     */
    public List<Double> listAngularWavenumbers() {
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
     * Returns a copy of the count values in this spectrum, as a List,
     *
     * @return List of count values.
     */
    public List<Double> listCounts() {
        return Doubles.asList(getCounts());
    }

    /**
     * Returns the wavelength of the data point with a given index.
     *
     * @param index The index.
     *
     * @return Wavelength value.
     */
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

    /**
     * Finds the index of the data point within this spectrum with a wavelength as close to the specified value as possible.
     *
     * @param wavelength Wavelength to find, in metres.
     *
     * @return Index of data point as close to specified wavelength as possible.
     */
    public int indexByWavelength(double wavelength) {

        int    minIndex    = 0;
        double minDistance = Double.MAX_VALUE;

        for (int i = 0; i < wavelengths.length; i++) {

            double wl       = wavelengths[i];
            double distance = Math.abs(wl - wavelength);

            if (wl == wavelength) {
                return i;
            }

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
