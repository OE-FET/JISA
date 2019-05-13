package JISA.Control;

import JISA.Devices.DeviceException;
import JISA.Util;

import java.io.IOException;
import java.util.ArrayList;

public class Synch {

    public static void waitForCondition(ICondition condition, int interval) throws Exception {

        int i = 0;

        while (!condition.isMet(i)) {
            Thread.sleep(interval);
            i++;
        }


    }

    public static void waitForParamWithinRange(DoubleReturn valueToCheck, double minValue, double maxValue, int interval) throws IOException, DeviceException {

        double value = valueToCheck.getValue();

        while (minValue > value || maxValue < value) {
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                throw new DeviceException("Could not sleep!");
            }
            value = valueToCheck.getValue();
        }

    }

    public static void waitForParamWithinError(DoubleReturn valueToCheck, double target, double errorPct, int interval) throws IOException, DeviceException {

        double minValue = (1D - (errorPct / 100D)) * target;
        double maxValue = (1D + (errorPct / 100D)) * target;

        waitForParamWithinRange(valueToCheck, minValue, maxValue, interval);

    }

    public static void waitForParamStable(DoubleReturn valueToCheck, double errorPct, int interval, long duration) throws IOException, DeviceException, InterruptedException {

        ArrayList<Double> list = new ArrayList<>();

        while (list.size() * interval < duration) {

            double value = valueToCheck.getValue();
            list.add(value);

            // Go back through the list of past values
            for (int i = list.size() - 1; i >= 0; i--) {

                double pastValue = list.get(i);
                double min       = pastValue * (1 - (errorPct / 100D));
                double max       = pastValue * (1 + (errorPct / 100D));

                // When/if we find a value outside our error range, cut out all values before it
                if (!Util.isBetween(value, min, max)) {
                    list.subList(0, i).clear();
                    break;
                }

            }

            Thread.sleep(interval);

        }

    }

    public static void waitForStableTarget(Returnable<Double> valueToCheck, double target, double pctMargin, int interval, long duration) throws IOException, DeviceException, InterruptedException {

        long   time = 0;
        double min  = target * (1 - (pctMargin / 100D));
        double max  = target * (1 + (pctMargin / 100D));

        while (time < duration) {

            double value = valueToCheck.get();

            if (Util.isBetween(value, min, max)) {
                time += duration;
            } else {
                time = 0;
            }

            Thread.sleep(duration);

        }

    }

}
