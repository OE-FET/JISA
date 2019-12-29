package jisa.maths;

import jisa.maths.matrices.RealMatrix;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Range<T extends Number> implements Iterable<T> {

    private final T[] data;

    public Range(T[] data) {this.data = data;}

    public static Range<Double> toDoubleRange(Number... values) {

        Double[] toReturn = new Double[values.length];

        for (int i = 0; i < toReturn.length; i++) {
            toReturn[i] = values[i].doubleValue();
        }

        return new Range<>(toReturn);

    }

    public static Range<Double> linear(Number start, Number stop, int numSteps) {

        if (numSteps < 1) {
            throw new IllegalArgumentException("You cannot have fewer than 1 step.");
        }

        BigDecimal[] values = new BigDecimal[numSteps];
        BigDecimal   step   = (BigDecimal.valueOf(stop.doubleValue()).subtract(BigDecimal.valueOf(start.doubleValue()))).divide(BigDecimal.valueOf(numSteps - 1), RoundingMode.HALF_UP);

        values[0]            = BigDecimal.valueOf(start.doubleValue());
        values[numSteps - 1] = BigDecimal.valueOf(stop.doubleValue());

        for (int i = 1; i < numSteps - 1; i++) {
            values[i] = values[i - 1].add(step);
        }

        return toDoubleRange(values);

    }

    public static Range<Integer> linear(int start, int stop) {

        int       numSteps = Math.abs(stop - start) + 1;
        Integer[] range    = new Integer[numSteps];
        int       step     = stop < start ? -1 : +1;

        for (int i = 0; i < numSteps; i++) {
            range[i] = start + (i * step);
        }

        return new Range<>(range);

    }

    public static Range<Double> geometric(Number start, Number stop, int noSteps) {

        BigDecimal a = BigDecimal.valueOf(start.doubleValue());
        BigDecimal b = BigDecimal.valueOf(stop.doubleValue());
        BigDecimal o = b.divide(a, RoundingMode.HALF_UP);
        BigDecimal s = BigDecimal.valueOf(Math.pow(o.doubleValue(), 1D / (noSteps - 1)));

        BigDecimal[] values = new BigDecimal[noSteps];

        values[0]           = a;
        values[noSteps - 1] = b;

        for (int i = 1; i < noSteps - 1; i++) {
            values[i] = values[i - 1].multiply(s);
        }

        return toDoubleRange(values);

    }

    public static Range<Double> geometricStep(Number start, Number stop, Number factor) {

        BigDecimal       a      = BigDecimal.valueOf(start.doubleValue());
        BigDecimal       b      = BigDecimal.valueOf(stop.doubleValue());
        BigDecimal       s      = BigDecimal.valueOf(factor.doubleValue());
        List<BigDecimal> values = new LinkedList<>();

        for (BigDecimal v = a; v.compareTo(b) <= 0; v = v.multiply(s)) {
            values.add(v);
        }

        return toDoubleRange(values.toArray(new BigDecimal[0]));

    }

    public static Range<Double> step(Number start, Number stop, Number step) {

        BigDecimal startN = BigDecimal.valueOf(start.doubleValue());
        BigDecimal stopN  = BigDecimal.valueOf(stop.doubleValue());
        BigDecimal stepN  = BigDecimal.valueOf(step.doubleValue());

        if (stepN.equals(BigDecimal.ZERO)) {
            throw new IllegalArgumentException("Step size must not be zero!");
        }

        int steps = stopN.subtract(startN).abs().divide(stepN.abs().add(BigDecimal.ONE), RoundingMode.HALF_UP).intValue();

        stepN = (stopN.compareTo(startN) < 0 ? stepN.abs().negate() : stepN.abs());

        BigDecimal[] values = new BigDecimal[steps];

        values[0] = startN;

        for (int i = 1; i < steps; i++) {
            values[i] = values[i - 1].add(stepN);
        }

        return toDoubleRange(values);

    }

    public Range<T> reverse() {

        T[] newData = Arrays.copyOf(data, data.length);

        for (int i = 0; i < data.length; i++) {
            newData[i] = data[data.length - i - 1];
        }

        return new Range<>(newData);

    }

    public Range<T> mirror() {

        T[] newData = Arrays.copyOf(data, data.length * 2);

        for (int i = 0; i < data.length; i++) {
            newData[i + data.length] = data[data.length - i - 1];
        }

        return new Range<>(newData);

    }

    public Range<T> repeat(int times) {

        T[] newData = Arrays.copyOf(data, data.length * (times + 1));

        for (int i = 0; i < times; i++) {
            System.arraycopy(data, 0, newData, data.length * (i + 1), data.length);
        }

        return new Range<>(newData);

    }

    public T[] array() {
        return data;
    }

    public double[] doubleArray() {

        double[] toReturn = new double[size()];

        for (int i = 0; i < toReturn.length; i++) {
            toReturn[i] = data[i].doubleValue();
        }

        return toReturn;

    }

    public int size() {
        return data.length;
    }

    public T get(int index) {
        return data[index];
    }

    public double getDouble(int index) {
        return data[index].doubleValue();
    }

    public RealMatrix reshape(int rows, int cols) {

        if (rows * cols != data.length) {
            throw new IllegalArgumentException("Number of elements must match.");
        }

        return new RealMatrix(rows, cols, doubleArray());

    }

    public String toString() {

        String[] values = new String[size()];

        for (int i = 0; i < size(); i++) {
            values[i] = get(i).toString();
        }

        return "[" + String.join(", ", values) + "]";

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
}
