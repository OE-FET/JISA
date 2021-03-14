package jisa.control;

import jisa.Util;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;

public class Repeat {

    private final Measurement measurement;
    private final int         count;
    private final int         delay;
    private final Double[]    values;

    public Repeat(int count, int delay, Measurement measurement) {

        this.measurement = measurement;
        this.count       = count;
        this.delay       = delay;
        this.values      = new Double[count];

    }

    public static Repeat run(int count, int delay, Measurement measurement) throws Exception {
        Repeat repeat = new Repeat(count, delay, measurement);
        repeat.run();
        return repeat;
    }

    public static void runTogether(Repeat... repeats) throws Exception {

        int delay = repeats[0].delay;
        int count = repeats[0].count;

        for (int i = 0; i < count; i++) {
            Thread.sleep(delay);
            for (Repeat repeat : repeats) repeat.runStep(i);
        }

    }

    public void run() throws Exception {

        if (delay > 0) {

            for (int i = 0; i < count; i++) {
                Thread.sleep(delay);
                runStep(i);
            }

        } else {

            for (int i = 0; i < count; i++) {
                runStep(i);
            }

        }

    }

    private void runStep(int step) throws Exception {
        values[step] = measurement.measure();
    }

    public List<Double> getValues() {
        return List.of(values);
    }

    public double getMin() {

        double min = Double.POSITIVE_INFINITY;

        for (double value : values) {
            if (value < min) min = value;
        }

        return min;

    }

    public double getMax() {

        double max = Double.NEGATIVE_INFINITY;

        for (double value : values) {

            if (value > max) {
                max = value;
            }

        }

        return max;

    }

    public double getRange() {
        return getMax() - getMin();
    }

    public double getSum() {

        double total = 0;

        for (double value : values) {
            total += value;
        }

        return total;

    }

    public double getCount() {
        return count;
    }

    public double getMean() {

        if (getCount() < 1) {
            return 0.0;
        }

        return getSum() / getCount();

    }

    public double getVariance() {

        if (getCount() < 2) {
            return 0.0;
        }

        double mean  = getMean();
        double total = 0;

        for (double value : values) {
            total += Math.pow(value - mean, 2);
        }

        return total / (getCount() - 1);

    }

    public double getStandardDeviation() {
        return Math.sqrt(getVariance());
    }

    public interface Measurement {
        double measure() throws Exception;
    }

}
