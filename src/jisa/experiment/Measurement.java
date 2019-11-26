package jisa.experiment;

import jisa.Util;

import java.io.IOException;

public abstract class Measurement {

    private boolean     running   = false;
    private boolean     stopped   = false;
    private ResultTable results   = null;
    private Thread      runThread = Thread.currentThread();

    protected abstract void run() throws Exception;

    protected abstract void onInterrupt() throws Exception;

    protected abstract void onFinish() throws Exception;

    public abstract Col[] getColumns();

    public ResultTable newResults() {
        results = new ResultList(getColumns());
        return results;
    }

    public ResultTable newResults(String path) throws IOException {
        results = new ResultStream(path, getColumns());
        return results;
    }

    public ResultTable getResults() {
        return results;
    }

    public void performMeasurement() throws Exception {

        runThread = Thread.currentThread();
        running = true;
        stopped = false;

        try {
            run();
        } catch (InterruptedException e) {
            stopped = true;
            try {
                onInterrupt();
            } catch (Exception ee) {
                Util.exceptionHandler(ee);
            }
        } finally {
            running = false;
            try {
                onFinish();
            } catch (Exception ee) {
                Util.exceptionHandler(ee);
            }
        }

    }

    public boolean isRunning() {
        return running;
    }

    public boolean wasStopped() {
        return stopped;
    }

    public void stop() {
        if (isRunning()) {
            stopped = true;
            runThread.interrupt();
        }
    }

    public void sleep(int mSec) throws InterruptedException {

        if (stopped) {
            throw new InterruptedException();
        } else {
            Thread.sleep(mSec);
        }

    }

}

