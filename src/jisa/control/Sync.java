package jisa.control;

import jisa.Util;
import jisa.devices.DeviceException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

public class Sync {

    /**
     * Blocks the current thread until the provided condition lambda returns true.
     *
     * @param condition Condition lambda, takes integer as argument representing iteration count and returns a boolean.
     * @param interval  Interval to check the lambda at, in milliseconds.
     *
     * @throws InterruptedException Upon the thread being interrupted while waiting.
     * @throws IOException          Upon the condition lambda throwing an IOException.
     * @throws DeviceException      Upon the condition lambda throwing a DeviceException.
     */
    public static void waitForCondition(@NotNull Condition condition, int interval) throws InterruptedException, IOException, DeviceException {

        int i = 0;

        while (!condition.isMet(i)) {

            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException("Interrupted");
            }

            Thread.sleep(interval);
            i++;

        }


    }

    /**
     * Blocks the current thread, with a time-out, until the provided condition lambda returns true.
     *
     * @param condition Condition lambda, takes integer as argument representing iteration count and returns a boolean.
     * @param interval  Interval to check the lambda at, in milliseconds.
     * @param timeOut   The maximum amount of time to wait, in milliseconds, before throwing a TimeoutException
     *
     * @throws InterruptedException Upon the thread being interrupted while waiting.
     * @throws IOException          Upon the condition lambda throwing an IOException.
     * @throws DeviceException      Upon the condition lambda throwing a DeviceException.
     * @throws TimeoutException     Upon exceeding the specified time-out value.
     */
    public static void waitForCondition(@NotNull ICondition condition, int interval, long timeOut) throws InterruptedException, IOException, DeviceException, TimeoutException {

        int i = 0;

        while (!condition.isMet(i)) {

            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException("Interrupted");
            }

            if ((long) i * interval > timeOut) {
                throw new TimeoutException("Timed out waiting for condition");
            }

            Thread.sleep(interval);
            i++;

        }


    }

    public static void waitForParamWithinRange(@NotNull ValueChecker valueToCheck, double minValue, double maxValue, int interval) throws IOException, DeviceException, InterruptedException {

        double value = valueToCheck.getValue();

        while (minValue > value || maxValue < value) {

            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException("Interrupted");
            }

            Thread.sleep(interval);
            value = valueToCheck.getValue();

        }

    }

    public static void waitForParamWithinError(@NotNull ValueChecker valueToCheck, double target, double errorPct, int interval) throws IOException, DeviceException, InterruptedException {

        double minValue = (1D - (errorPct / 100D)) * target;
        double maxValue = (1D + (errorPct / 100D)) * target;

        waitForParamWithinRange(valueToCheck, minValue, maxValue, interval);

    }

    public static void waitForParamStable(@NotNull ValueChecker valueToCheck, double errorPct, int interval, long duration) throws IOException, DeviceException, InterruptedException {

        final ArrayList<Double> list      = new ArrayList<>();
        final double            minFactor = (1 - (errorPct / 100D));
        final double            maxFactor = (1 + (errorPct / 100D));

        while ((long) list.size() * interval < duration) {

            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException("Interrupted");
            }

            double value = valueToCheck.getValue();
            list.add(value);

            // Go back through the list of past values
            for (int i = list.size() - 2; i >= 0; i--) {

                double pastValue = list.get(i);
                double min       = pastValue * minFactor;
                double max       = pastValue * maxFactor;

                // When/if we find a value outside our error range, cut out all values before it
                if (!Util.isBetween(value, min, max)) {
                    list.subList(0, i + 1).clear();
                    break;
                }

            }

            Thread.sleep(interval);

        }


    }

    public static void waitForParamStable(@NotNull ValueChecker valueToCheck, double errorPct, int interval, long duration, long timeOut) throws IOException, DeviceException, InterruptedException, TimeoutException {

        final ArrayList<Double> list      = new ArrayList<>((int) (duration / interval));
        final double            minFactor = (1 - (errorPct / 100D));
        final double            maxFactor = (1 + (errorPct / 100D));

        while ((long) list.size() * interval < duration) {

            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException("Interrupted");
            }

            if ((long) list.size() * interval >= timeOut) {
                throw new TimeoutException("Waiting for stable parameter timed out.");
            }

            double value = valueToCheck.getValue();
            list.add(value);

            // Go back through the list of past values
            for (int i = list.size() - 2; i >= 0; i--) {

                double pastValue = list.get(i);
                double min       = pastValue * minFactor;
                double max       = pastValue * maxFactor;

                // When/if we find a value outside our error range, cut it and all values before it out
                if (!Util.isBetween(value, min, max)) {
                    list.subList(0, i + 1).clear();
                    break;
                }

            }

            Thread.sleep(interval);

        }

    }

    public static void waitForStableTarget(@NotNull ValueChecker valueToCheck, double target, double pctMargin, int interval, long duration) throws IOException, DeviceException, InterruptedException {

        long   time = 0;
        double min  = target * (1 - (pctMargin / 100D));
        double max  = target * (1 + (pctMargin / 100D));

        while (time < duration) {

            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException("Interrupted");
            }

            double value = valueToCheck.getValue();

            if (Util.isBetween(value, min, max)) {
                time += interval;
            } else {
                time = 0;
            }

            Thread.sleep(interval);

        }

    }

    public static void waitForStableTarget(@NotNull ValueChecker valueToCheck, double target, double pctMargin, int interval, long duration, long timeOut) throws IOException, DeviceException, InterruptedException, TimeoutException {

        long   time = 0;
        double min  = target * (1 - (pctMargin / 100D));
        double max  = target * (1 + (pctMargin / 100D));

        while (time < duration) {

            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException("Interrupted");
            }

            if (time > timeOut) {
                throw new TimeoutException("Waiting for stable target timed out.");
            }

            double value = valueToCheck.getValue();

            if (Util.isBetween(value, min, max)) {
                time += interval;
            } else {
                time = 0;
            }

            Thread.sleep(interval);

        }

    }

    public interface ValueChecker {
        double getValue() throws IOException, DeviceException;
    }

    public interface Condition {
        boolean isMet(int i) throws IOException, DeviceException;
    }


}
