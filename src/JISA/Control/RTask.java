package JISA.Control;

import java.util.Timer;
import java.util.TimerTask;

public class RTask {

    private long      interval;
    private SRunnable toRun;
    private boolean   running = false;
    private long      started;
    private Timer     timer   = new Timer();
    private TimerTask task;

    public RTask(long interval, SRunnable toRun) {
        this.interval = interval;
        this.toRun = toRun;
    }

    public synchronized void start() {

        if (running) {
            return;
        }
        started = System.currentTimeMillis();

        task = new TimerTask() {
            @Override
            public void run() {
                try {
                    toRun.run();
                } catch (Throwable e) {
                    System.err.printf("Exception encountered running repeat task: \"%s\"\n", e.getMessage());
                }
            }
        };

        timer.scheduleAtFixedRate(task, 0, interval);
        running = true;

    }

    public synchronized void stop() {

        if (!running) {
            return;
        }

        task.cancel();
        running = false;

    }

    public boolean isRunning() {
        return running;
    }

    public long getMSecFromStart() {
        return System.currentTimeMillis() - started;
    }

    public double getSecFromStart() {
        return ((double) getMSecFromStart()) / 1000D;
    }

}
