package jisa.control;

import jisa.Util;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Repeat implements Iterable<Double> {

    private final Measurement measurement;
    private final int         count;
    private final int         delay;
    private final double[]    values;
    private       boolean     hasRun = false;

    /**
     * Creates a Repeat object - for performing repeat measurements. The repeat will not run until run() is called.
     *
     * @param count       The number of repeats to perform
     * @param delay       The delay, in milliseconds, to wait before each repeat
     * @param measurement The measurement to repeat
     */
    public Repeat(int count, int delay, Measurement measurement) {

        this.measurement = measurement;
        this.count       = count;
        this.delay       = delay;
        this.values      = new double[count];

    }

    /**
     * Creates and runs a repeat measurement, returning it as a Repeat object after performing all repeat measurements.
     *
     * @param count       Number of repeats
     * @param delay       Delay in milliseconds before each repeat
     * @param measurement The measurement to run
     *
     * @return Repeat object containing the repeated measurements
     *
     * @throws Exception Upon error running measurement
     */
    public static Repeat run(int count, int delay, Measurement measurement) throws Exception {
        Repeat repeat = new Repeat(count, delay, measurement);
        repeat.run();
        return repeat;
    }


    /**
     * Creates a repeat measurement without running it, returning it as a Repeat object.
     * Will not run until run() is called on it.
     *
     * @param count       Number of repeats
     * @param delay       Delay in milliseconds before each repeat
     * @param measurement The measurement to run
     *
     * @return Repeat object, awaiting run.
     */
    public static Repeat prepare(int count, int delay, Measurement measurement) {
        return new Repeat(count, delay, measurement);
    }

    /**
     * Runs multiple repeat measurements together side-by-side. Each Repeat must be configured to have the same count
     * and delay time.
     *
     * @param repeats The Repeat objects to run
     *
     * @throws Exception Upon measurement error, or if the Repeat objects have differing counts and/or delays.
     */
    public static void runTogether(Repeat... repeats) throws Exception {

        IntStream counts = Arrays.stream(repeats).mapToInt(Repeat::getCount).distinct();
        IntStream delays = Arrays.stream(repeats).mapToInt(Repeat::getDelay).distinct();

        if (counts.count() > 1 || delays.count() > 1) {
            throw new IllegalArgumentException("To run repeats together they must have matching repeat counts and delays!");
        }

        int delay = repeats[0].getDelay();
        int count = repeats[0].getCount();

        if (delay > 0) {

            for (int i = 0; i < count; i++) {

                Thread.sleep(delay);

                for (Repeat repeat : repeats) {
                    repeat.runStep(i);
                }

            }

        } else {

            for (int i = 0; i < count; i++) {

                for (Repeat repeat : repeats) {
                    repeat.runStep(i);
                }

            }

        }

        Arrays.stream(repeats).forEach(r -> r.hasRun = true);

    }

    public static void runInParallel(Repeat... repeats) throws InterruptedException, Exception {

        CountDownLatch  start      = new CountDownLatch(1);
        CountDownLatch  finish     = new CountDownLatch(repeats.length);
        List<Exception> exceptions = new LinkedList<>();
        List<Thread>    threads    = new ArrayList<>(repeats.length);

        for (Repeat repeat : repeats) {

            threads.add(new Thread(() -> {

                Util.runRegardless(start::await);

                try {
                    repeat.run();
                } catch (Exception e) {
                    exceptions.add(e);
                } finally {
                    finish.countDown();
                }

            }));

        }

        // Start all threads, which will immediately start waiting on the start latch
        threads.forEach(Thread::start);

        // Release all threads and wait for them to finish
        start.countDown();

        try {
            finish.await();
        } catch (InterruptedException e) {
            threads.forEach(Thread::stop); // Hacky and nasty, but does for now
            throw e;
        }

        if (!exceptions.isEmpty()) {
            throw new Exception(exceptions.stream().map(Exception::getMessage).collect(Collectors.joining("\n\n")));
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

        hasRun = true;

    }

    public boolean isComplete() {
        return hasRun;
    }

    private void runStep(int step) throws Exception {
        values[step] = measurement.measure();
    }

    public List<Double> getValues() {
        return Arrays.stream(values).boxed().collect(Collectors.toList());
    }

    public double getValue(int repeat) {
        return values[repeat];
    }

    public double getMin() {
        return Arrays.stream(values).min().orElse(0.0);
    }

    public double getMax() {
        return Arrays.stream(values).max().orElse(0.0);
    }

    public double getRange() {
        return getMax() - getMin();
    }

    public double getSum() {
        return Arrays.stream(values).sum();
    }

    public int getCount() {
        return count;
    }

    public int getDelay() {
        return delay;
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

        double mean = getMean();

        return Arrays.stream(values).map(v -> Math.pow(v - mean, 2)).sum() / (getCount() - 1);

    }

    public double getStandardDeviation() {
        return Math.sqrt(getVariance());
    }

    @Override
    public Iterator<Double> iterator() {
        return Arrays.stream(values).iterator();
    }

    public interface Measurement {
        double measure() throws Exception;
    }

}
