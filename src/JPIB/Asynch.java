package JPIB;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class Asynch {

    public static void onConditionMet(final ICondition condition, final int checkInterval, final SRunnable onMet, final ERunnable onException) {

        Timer timer = new Timer();

        TimerTask task = new TimerTask() {

            private int count = 0;

            @Override
            public void run() {
                try {

                    if (condition.isMet(count)) {
                        timer.cancel();
                        onMet.run();
                        return;
                    }

                    count++;

                } catch (Exception e) {
                    timer.cancel();
                    onException.run(e);
                }

            }
        };

        timer.scheduleAtFixedRate(task, 0, checkInterval);

    }

    public static void onInterval(final ICondition until, final int interval, final IRunnable onInterval, final SRunnable onEnd, final ERunnable onException) {

        Timer timer = new Timer();

        TimerTask task = new TimerTask() {

            private int count = 0;

            @Override
            public void run() {
                try {

                    if (until.isMet(count)) {
                        timer.cancel();
                        onEnd.run();
                        return;
                    } else {
                        onInterval.run(count);
                        count++;
                    }

                    if (until.isMet(count)) {
                        timer.cancel();
                        onEnd.run();
                    }

                } catch (Exception e) {
                    timer.cancel();
                    onException.run(e);
                }

            }
        };

        timer.scheduleAtFixedRate(task, 0, interval);

    }

    public static void onParamWithinError(final DoubleReturn valueToCheck, final double targetValue, final double percError, final long duration, final int interval, final SRunnable onStable, final ERunnable onException) {

        final double minValue = (1 - (percError / 100D)) * targetValue;
        final double maxValue = (1 + (percError / 100D)) * targetValue;

        onParamWithinRange(valueToCheck, minValue, maxValue, duration, interval, onStable, onException);

    }

    public static void onParamWithinRange(final DoubleReturn valueToCheck, final double minValue, final double maxValue, final long duration, final int interval, final SRunnable onStable, final ERunnable onException) {

        final Timer timer = new Timer();

        TimerTask task = new TimerTask() {

            private double start = System.currentTimeMillis();

            @Override
            public void run() {

                try {

                    // Get the current value of the parameter to check
                    double currentValue = valueToCheck.getValue();

                    // If it's outside our required value range, reset the timer
                    if (currentValue < minValue || currentValue > maxValue) {
                        start = System.currentTimeMillis();
                    }

                    // Check to see if we've met our duration requirement
                    if (System.currentTimeMillis() - start >= duration) {
                        timer.cancel();
                        onStable.run();
                    }

                } catch (Exception e) {
                    onException.run(e);
                }

            }
        };

        timer.scheduleAtFixedRate(task, 0, interval);

    }

    public static void onParamStable(final DoubleReturn valueToCheck, final double percentage, final long duration, final int interval, final SRunnable onStable, final ERunnable onException) {

        final Timer timer = new Timer();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {

                ArrayList<Double> list = new ArrayList<>();

                try {

                    // Get current value
                    double currentValue = valueToCheck.getValue();

                    // Go back through the list of past values
                    for (int i = list.size() - 1; i >=0; i--) {

                        double pastValue = list.get(i);
                        double min = pastValue * (1 - (percentage / 100D));
                        double max = pastValue * (1 + (percentage / 100D));

                        // When/if we find a value outside our error range, cut out all values before it
                        if (!Util.isBetween(currentValue, min, max)) {
                            list.subList(0, i).clear();
                            break;
                        }

                    }

                    // Add our new value to the end
                    list.add(currentValue);

                    // Check if we've been going long enough
                    if (list.size() * interval >= duration) {
                        timer.cancel();
                        onStable.run();
                    }


                } catch (Exception e) {
                    onException.run(e);
                }

            }
        };

    }

}
