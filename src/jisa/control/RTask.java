package jisa.control;

import jisa.Util;

import java.util.Timer;
import java.util.TimerTask;

public class RTask {

    private final Timer     timer   = new Timer();
    private       Task      toRun;
    private       long      interval;
    private       boolean   running = false;
    private       long      started;
    private       int       iteration;
    private       int       total   = 0;
    private       TimerTask task;

    /**
     * Creates a repeating task to run at the given interval.
     *
     * @param interval Interval, in milliseconds
     * @param toRun    Code to run at each interval
     */
    public RTask(long interval, Task toRun) {
        this.interval = interval;
        this.toRun    = toRun;
    }

    public RTask(long interval, SRunnable toRun) {
        this(interval, (task) -> toRun.run());
    }

    public void setInterval(long interval) {

        if (isRunning()) {
            throw new IllegalStateException("Cannot edit RTask while it is running");
        }

        this.interval = interval;

    }

    public long getInterval() {
        return interval;
    }

    public void setTask(Task task) {

        if (isRunning()) {
            throw new IllegalStateException("Cannot edit RTask while it is running");
        }

        this.toRun = task;

    }

    public Task getTask() {
        return toRun;
    }

    /**
     * Start the timer to periodically run the task.
     */
    public synchronized void start() {

        if (running) {
            return;
        }

        started   = System.currentTimeMillis();
        iteration = 0;

        task = new TimerTask() {

            public void run() {

                try {
                    toRun.run(RTask.this);
                } catch (Throwable e) {
                    Util.errLog.printf("Exception encountered running repeat task: \"%s\"\n", e.getMessage());
                    e.printStackTrace();
                }

                iteration++;
                total++;

            }

        };

        timer.scheduleAtFixedRate(task, 0, interval);
        running = true;

    }

    /**
     * Stop the timer from running the task.
     */
    public synchronized void stop() {

        if (!running) {
            return;
        }

        task.cancel();
        running = false;

    }

    /**
     * Is the task currently set to run?
     *
     * @return Running?
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Returns the number of milliseconds that have elapsed since the task was started.
     *
     * @return Time running, in milliseconds
     */
    public long getMSecFromStart() {
        return System.currentTimeMillis() - started;
    }

    /**
     * Returns the time that has elapsed since the task was started, in seconds.
     *
     * @return Time running, in seconds
     */
    public double getSecFromStart() {
        return ((double) getMSecFromStart()) / 1000D;
    }

    /**
     * Returns how many times the task has been run since the timer was last started.
     *
     * @return Run count
     */
    public int getCount() {
        return iteration;
    }

    public int getTotalCount() {
        return total;
    }

    public interface Task {

        void run(RTask task) throws Exception;

    }

}
