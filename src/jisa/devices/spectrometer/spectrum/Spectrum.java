package jisa.devices.spectrometer.spectrum;

import com.google.common.primitives.Doubles;
import jisa.Util;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;


public class Spectrum implements Iterable<Spectrum.Point> {

    protected final double[] wavelengths;
    protected final long[]   counts;

    public Spectrum(double[] wavelengths, long[] counts) {

        if (wavelengths.length != counts.length) {
            throw new IllegalArgumentException("Wavelength and Value arrays must have the same length.");
        }

        this.wavelengths = wavelengths;
        this.counts      = counts;

    }

    /**
     * Returns a new Spectrum object whose points are the result of adding the provided spectrum from this one.
     *
     * @param other The spectrum to add.
     *
     * @return Summed spectrum.
     */
    public Spectrum add(Spectrum other) {

        // If they match, then this is easy
        if (Arrays.equals(wavelengths, other.wavelengths)) {
            return new Spectrum(wavelengths, Util.arrayAdd(counts, other.counts));
        }

        // Otherwise, we need to subtract matching points only
        List<Double> wl1  = Doubles.asList(wavelengths);
        List<Double> wl2  = Doubles.asList(other.wavelengths);
        double[]     wls  = wl1.stream().filter(wl2::contains).mapToDouble(Double::doubleValue).toArray();
        long[]       vals = DoubleStream.of(wls).mapToLong(wl -> counts[wl1.indexOf(wl)] + other.counts[wl2.indexOf(wl)]).toArray();

        return new Spectrum(wls, vals);

    }

    /**
     * Returns a new Spectrum object whose points are the result of subtracting the provided spectrum from this one.
     *
     * @param other The spectrum to subtract.
     *
     * @return Subtracted spectrum.
     */
    public Spectrum subtract(Spectrum other) {

        // If they match, then this is easy
        if (Arrays.equals(wavelengths, other.wavelengths)) {
            return new Spectrum(wavelengths, Util.arrayDiff(counts, other.counts));
        }

        // Otherwise, we need to subtract matching points only
        List<Double> wl1  = Doubles.asList(wavelengths);
        List<Double> wl2  = Doubles.asList(other.wavelengths);
        double[]     wls  = wl1.stream().filter(wl2::contains).mapToDouble(Double::doubleValue).toArray();
        long[]       vals = DoubleStream.of(wls).mapToLong(wl -> counts[wl1.indexOf(wl)] - other.counts[wl2.indexOf(wl)]).toArray();

        return new Spectrum(wls, vals);

    }

    /**
     * Returns a deep copy of this Spectrum object.
     *
     * @return Deep copy.
     */
    public Spectrum copy() {
        return new Spectrum(getWavelengths(), getCounts());
    }

    /**
     * Returns a new Spectrum object whose points are the result of subtracting the provided spectrum from this one.
     * (Alias for subtract(...) to enable Kotlin operator overloading).
     *
     * @param other The spectrum to subtract.
     *
     * @return Subtracted spectrum.
     */
    public Spectrum sub(Spectrum other) {
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

    /**
     * Returns a Stream of each Point object in this Spectrum.
     *
     * @return Point Stream.
     */
    public Stream<Point> stream() {
        return IntStream.range(0, size()).mapToObj(i -> new Point(getWavelength(i), getCounts(i)));
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
            data[i][1] = getCounts(i);
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
     * Returns a copy of the values in this spectrum, as an array.
     *
     * @return Array of values.
     */
    public long[] getCounts() {
        return counts.clone();
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

    public long getCounts(int index) {
        return counts[index];
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

        return new Point(getWavelength(index), getCounts(index));

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
        private final long   counts;

        public Point(double wavelength, long value) {
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

        public long getCounts() {
            return counts;
        }

    }

}
