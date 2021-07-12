package jisa.maths;

import jisa.maths.functions.GFunction;
import jisa.maths.matrices.RealMatrix;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;

/**
 * Class for defining ranges of numbers to iterate over.
 *
 * @param <T> Type of number (ie Double or Integer).
 */
public class Range<T extends Number> implements Iterable<T> {

    private static final MathContext CONTEXT = MathContext.DECIMAL128;

    private final T[] data;

    public Range(T[] data) {this.data = data;}

    public static Range<Double> manual(Number... values) {
        return new Range<>(Arrays.stream(values).mapToDouble(Number::doubleValue).boxed().toArray(Double[]::new));
    }

    /**
     * Creates a range of equally-spaced numbers with a defined number of steps.
     *
     * @param start    The number to start at
     * @param stop     The number to end at
     * @param numSteps The total number of values to generate
     *
     * @return Linear range
     */
    public static Range<Double> linear(Number start, Number stop, int numSteps) {

        if (numSteps < 1) {
            throw new IllegalArgumentException("You cannot have fewer than 1 step.");
        } else if (numSteps == 1) {
            return new Range<>(new Double[]{start.doubleValue()});
        }

        if (start.intValue() == start.doubleValue() && stop.intValue() == stop.doubleValue() && (stop.intValue() - start.intValue() + 1) == numSteps) {
            return linear(start.intValue(), stop.intValue());
        }

        BigDecimal startV      = BigDecimal.valueOf(start.doubleValue());
        BigDecimal stopV       = BigDecimal.valueOf(stop.doubleValue());
        BigDecimal denominator = BigDecimal.valueOf(numSteps - 1);

        BigDecimal[] values = new BigDecimal[numSteps];
        BigDecimal   step   = stopV.subtract(startV, CONTEXT).divide(denominator, CONTEXT);
        values[0]            = startV;
        values[numSteps - 1] = stopV;

        for (int i = 1; i < numSteps - 1; i++) {
            values[i] = values[i - 1].add(step);
        }

        return manual(values);

    }

    /**
     * Creates a range of integer numbers, represented as doubles.
     *
     * @param start Integer to start at
     * @param stop  Integer to end at
     *
     * @return Integer range
     */
    public static Range<Double> linear(int start, int stop) {

        int      numSteps = Math.abs(stop - start) + 1;
        Double[] range    = new Double[numSteps];
        int      step     = stop < start ? -1 : +1;

        for (int i = 0; i < numSteps; i++) {
            range[i] = (double) (start + (i * step));
        }

        return new Range<>(range);

    }

    /**
     * Creates a range of integer numbers, represented as integers.
     *
     * @param start Integer to start at
     * @param stop  Integer to end at
     *
     * @return Integer range
     */
    public static Range<Integer> count(int start, int stop) {

        int       numSteps = Math.abs(stop - start) + 1;
        Integer[] range    = new Integer[numSteps];
        int       step     = stop < start ? -1 : +1;

        for (int i = 0; i < numSteps; i++) {
            range[i] = (start + (i * step));
        }

        return new Range<>(range);

    }

    /**
     * Creates a geometric series of values from "start" and ending at "stop" with the geometric factor determined by
     * a fixed number of steps.
     *
     * @param start   Number to start at
     * @param stop    Number to end at
     * @param noSteps Total number of elements
     *
     * @return
     */
    public static Range<Double> exponential(Number start, Number stop, int noSteps) {

        if (noSteps < 1) {
            throw new IllegalArgumentException("You cannot have fewer than 1 step.");
        } else if (noSteps == 1) {
            return new Range<>(new Double[]{start.doubleValue()});
        }

        BigDecimal a = BigDecimal.valueOf(start.doubleValue());
        BigDecimal b = BigDecimal.valueOf(stop.doubleValue());
        BigDecimal o = b.divide(a, CONTEXT);
        BigDecimal s = nthRoot(o, noSteps - 1, CONTEXT);

        BigDecimal[] values = new BigDecimal[noSteps];

        values[0]           = a;
        values[noSteps - 1] = b;

        for (int i = 1; i < noSteps - 1; i++) {
            values[i] = values[i - 1].multiply(s, CONTEXT);
        }

        return manual(values);

    }

    private static BigDecimal nthRoot(final BigDecimal a, final int n, final MathContext context) {

        final BigDecimal N   = BigDecimal.valueOf(n);
        final int        n_1 = n - 1;

        final int newPrecision = context.getPrecision() + n;

        final MathContext c = expandContext(context, newPrecision);

        final int limit = n * n * (31 - Integer.numberOfLeadingZeros(newPrecision)) >>> 1;

        BigDecimal x = guessRoot(a, n);
        BigDecimal x0;

        for (int i = 0; i < limit; i++) {
            x0 = x;
            BigDecimal delta = a.divide(x0.pow(n_1), c)
                                .subtract(x0, c)
                                .divide(N, c);
            x = x0.add(delta, c);
        }

        return x.round(c);
    }

    private static BigDecimal guessRoot(BigDecimal a, int n) {
        BigInteger magnitude = a.unscaledValue();
        final int  length    = magnitude.bitLength() * (n - 1) / n;
        magnitude = magnitude.shiftRight(length);
        final int newScale = a.scale() / n;
        return new BigDecimal(magnitude, newScale);
    }

    private static MathContext expandContext(MathContext c0, int newPrecision) {
        return new MathContext(
            newPrecision,
            c0.getRoundingMode()    // Retain rounding mode
        );
    }

    /**
     * Creates a geometric series of numbers starting at "start", ending before exceeding "stop" with a defined
     * geometric/multiplicative factor.
     *
     * @param start  Number to start at
     * @param stop   Number to stop at or before
     * @param factor Multiplicative or geometric factor
     *
     * @return Geometric range
     */
    public static Range<Double> geometric(Number start, Number stop, Number factor) {

        BigDecimal       a      = BigDecimal.valueOf(start.doubleValue());
        BigDecimal       b      = BigDecimal.valueOf(stop.doubleValue());
        BigDecimal       s      = BigDecimal.valueOf(factor.doubleValue());
        List<BigDecimal> values = new LinkedList<>();

        for (BigDecimal v = a; v.compareTo(b) <= 0; v = v.multiply(s, CONTEXT)) {
            values.add(v);
        }

        return manual(values.toArray(new BigDecimal[0]));

    }

    public static Range<Double> polynomial(Number start, Number stop, int noSteps, int order) {

        BigDecimal    startBD = BigDecimal.valueOf(start.doubleValue()).abs().pow(order).multiply(BigDecimal.valueOf(Math.signum(start.doubleValue())));
        BigDecimal    stopBD  = BigDecimal.valueOf(stop.doubleValue()).abs().pow(order).multiply(BigDecimal.valueOf(Math.signum(stop.doubleValue())));
        Range<Double> linear  = Range.linear(startBD, stopBD, noSteps);
        BigDecimal[]  values  = new BigDecimal[noSteps];

        for (int i = 0; i < values.length; i++) {
            values[i] = BigDecimal.valueOf(linear.get(i));
        }

        BigDecimal[] roots = Arrays.stream(values)
                                   .map(v -> v.abs().doubleValue() > 0 ? nthRoot(v.abs(), order, CONTEXT).multiply(BigDecimal.valueOf(v.signum())) : BigDecimal.ZERO)
                                   .toArray(BigDecimal[]::new);

        return manual(roots);

    }

    /**
     * Creates an arithmetic series, starting at "start", stopping before exceeding "stop" with a defined step size.
     *
     * @param start Number to start at
     * @param stop  Number to end at, or before
     * @param step  Step size
     *
     * @return Arithmetic range
     */
    public static Range<Double> step(Number start, Number stop, Number step) {

        BigDecimal startN = BigDecimal.valueOf(start.doubleValue());
        BigDecimal stopN  = BigDecimal.valueOf(stop.doubleValue());
        BigDecimal stepN  = BigDecimal.valueOf(step.doubleValue());

        if (stepN.equals(BigDecimal.ZERO)) {
            throw new IllegalArgumentException("Step size must not be zero!");
        }

        int steps = stopN.subtract(startN).abs().divide(stepN.abs(), RoundingMode.HALF_UP).add(BigDecimal.ONE).intValue();

        stepN = (stopN.compareTo(startN) < 0 ? stepN.abs().negate() : stepN.abs());

        BigDecimal[] values = new BigDecimal[steps];

        values[0] = startN;

        for (int i = 1; i < steps; i++) {
            values[i] = values[i - 1].add(stepN, CONTEXT);
        }

        return manual(values);

    }

    /**
     * Creates a range of the same number repeated n times.
     *
     * @param value    Value to repeat
     * @param numTimes Number of elements
     *
     * @return Repeated range
     */
    public static Range<Double> repeat(Number value, int numTimes) {

        Double[] values = new Double[numTimes];
        Arrays.fill(values, value.doubleValue());
        return new Range<>(values);

    }

    /**
     * Creates a range of numbers defined by a custom series function.
     *
     * @param start    Index to start at
     * @param stop     Index to stop at
     * @param function Function to generate values
     *
     * @return Custom range
     */
    public static Range<Double> function(int start, int stop, GFunction<Double, Double> function) {

        int numSteps = Math.abs(stop - start) + 1;
        int step     = stop > start ? +1 : -1;

        Double[] values = new Double[numSteps];

        for (int i = 0; i < numSteps; i++) {
            values[i] = function.value((double) start + (step * i));
        }

        return new Range<>(values);

    }

    /**
     * Returns a copy of this range but with its elements in reverse-order.
     * <p>
     * Example: [4, 2, 7] becomes [7, 2, 4]
     *
     * @return Reversed range
     */
    public Range<T> reverse() {

        T[] newData = Arrays.copyOf(data, data.length);

        for (int i = 0; i < data.length; i++) {
            newData[i] = data[data.length - i - 1];
        }

        return new Range<>(newData);

    }

    /**
     * Returns a copy of this range, but with its reverse appended on the end.
     * <p>
     * Example: [1, 2, 3] becomes [1, 2, 3, 3, 2, 1]
     *
     * @return Mirrored range
     */
    public Range<T> mirror() {

        T[] newData = Arrays.copyOf(data, data.length * 2);

        for (int i = 0; i < data.length; i++) {
            newData[i + data.length] = data[data.length - i - 1];
        }

        return new Range<>(newData);

    }

    /**
     * Returns a range containing this range plus itself again n times.
     * <p>
     * Example: [1, 2, 3] -> repeat(1) -> [1, 2, 3, 1, 2, 3]
     *
     * @param times Number of repeats to append
     *
     * @return Repeated range
     */
    public Range<T> repeat(int times) {

        T[] newData = Arrays.copyOf(data, data.length * (times + 1));

        for (int i = 0; i < times; i++) {
            System.arraycopy(data, 0, newData, data.length * (i + 1), data.length);
        }

        return new Range<>(newData);

    }

    /**
     * Returns a copy of this range but with all elements cyclically shifted by n places (+ve to the right, -ve to the left).
     * <p>
     * Example: [1, 2, 3] -> shift(+2) -> [2, 3, 1]
     * Example: [1, 2, 3] -> shift(-2) -> [3, 1, 2]
     *
     * @param places Places to shift by
     *
     * @return Shifted range
     */
    public Range<T> shift(int places) {

        places = places % size();

        T[] newData = Arrays.copyOf(data, data.length);

        for (int i = 0; i < size(); i++) {
            newData[i] = data[(size() + (i - places)) % size()];
        }

        return new Range<>(newData);

    }

    /**
     * Returns a copy of this range but with its elements in a random order.
     * <p>
     * Example: [1, 2, 3, 4] may become [3, 1, 4, 2]
     *
     * @return Shuffled array
     */
    public Range<T> shuffle() {

        List<T> data = Arrays.asList(this.data);
        Collections.shuffle(data);
        return new Range<>(data.toArray(Arrays.copyOf(this.data, this.data.length)));

    }

    /**
     * Returns this range as an array of values.
     *
     * @return Array of values
     */
    public T[] array() {
        return data;
    }

    /**
     * Returns this range as a list of values.
     *
     * @return List of values
     */
    public List<T> list() { return List.of(data); }

    /**
     * Returns this range as an array of double values.
     *
     * @return Array of double values
     */
    public double[] doubleArray() {

        double[] toReturn = new double[size()];

        for (int i = 0; i < toReturn.length; i++) {
            toReturn[i] = data[i].doubleValue();
        }

        return toReturn;

    }

    /**
     * Returns the number of elements in this range
     *
     * @return Size of range
     */
    public int size() {
        return data.length;
    }

    /**
     * Returns the nth element in this range.
     *
     * @param index Index of element to return
     *
     * @return Element at nth position
     */
    public T get(int index) {
        return data[index];
    }

    /**
     * Returns the nth element in this range as a double.
     *
     * @param index Index of element to return
     *
     * @return Element at nth position, as double
     */
    public double getDouble(int index) {
        return data[index].doubleValue();
    }

    /**
     * Reshapes this range into an NxM matrix (where N*M must equal size()).
     *
     * @param rows Number of rows
     * @param cols Number of columns
     *
     * @return Resulting matrix.
     */
    public RealMatrix reshape(int rows, int cols) {

        if (rows * cols != size()) {
            throw new IllegalArgumentException("Number of elements must match.");
        }

        return new RealMatrix(rows, cols, doubleArray());

    }

    /**
     * Reshapes this range into a column matrix.
     *
     * @return Column matrix
     */
    public RealMatrix column() {
        return reshape(size(), 1);
    }

    /**
     * Reshapes this range into a row matrix.
     *
     * @return Row matrix
     */
    public RealMatrix row() {
        return reshape(1, size());
    }

    /**
     * Returns a string representation of this range.
     *
     * @return String representation
     */
    public String toString() {
        return Arrays.toString(data);
    }

    @Override
    public Iterator<T> iterator() {

        return new Iterator<>() {

            private int i = 0;

            @Override
            public boolean hasNext() {
                return i < data.length;
            }

            @Override
            public T next() {
                return data[i++];
            }

        };

    }


    public enum Type {
        LINEAR,
        EXPONENTIAL,
        POLYNOMIAL;
    }

    public static class DoubleRange extends Range<Double> {

        private final Type type;
        private final int  order;

        public DoubleRange(Range<Double> range, Type type, int order) {
            super(range.data);
            this.type  = type;
            this.order = order;
        }

        public Type getType() {
            return type;
        }

        public int getOrder() {
            return order;
        }

    }

}
