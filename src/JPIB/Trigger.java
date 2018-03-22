package JPIB;

import java.util.Timer;
import java.util.TimerTask;

public class Trigger {

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

                    count ++;

                } catch (Exception e) {
                    timer.cancel();
                    onException.run(e);
                }

            }
        };

        (new Timer()).scheduleAtFixedRate(task, 0, checkInterval);

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
                        count ++;
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

}
