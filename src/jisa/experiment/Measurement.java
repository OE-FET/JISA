package jisa.experiment;

import java.io.IOException;

/**
 * Abstract class for implementing measurement routines. Allows for procedures that can be easily and safely
 * interrupted.
 */
public abstract class Measurement {

    private boolean     running   = false;
    private boolean     stopped   = false;
    private ResultTable results   = null;
    private Thread      runThread = Thread.currentThread();

    /**
     * Returns the name of this measurement.
     *
     * @return Name of the measurement
     */
    public abstract String getName();

    /**
     * This method should perform the measurement itself and throw and exception if either something is wrong (ie
     * missing instruments) or if something goes wrong during the measurement.
     *
     * @param results The ResultTable to be used for results storage.
     *
     * @throws Exception Upon invalid configuration or measurement error
     */
    protected abstract void run(ResultTable results) throws Exception;

    /**
     * This method is always called whenever the measurement is ended prematurely by stop() (or other interrupt).
     * This method in most cases can be left empty, as it would only really serve a purpose for logging an interrupted measurement.
     *
     * @throws Exception This method can throw exceptions
     */
    protected abstract void onInterrupt() throws Exception;

    /**
     * This method is always called whenever a measurement has ended, regardless of if it was successful, ended in error
     * or was interrupted. This method should therefore contain any clean-up code that should be run at the end of a
     * measurement (for instance turning off any instruments etc).
     *
     * @throws Exception This method can throw exceptions
     */
    protected abstract void onFinish() throws Exception;

    /**
     * This method should return an array of columns to be used when generating a new ResultTable for results storage.
     *
     * @return Array of columns
     */
    public abstract Col[] getColumns();

    /**
     * Generates a new ResultTable for storing results, in memory.
     *
     * @return Results storage
     */
    public ResultTable newResults() {
        results = new ResultList(getColumns());
        return results;
    }

    /**
     * Generates a new ResultTable for storing results, directly to a file.
     *
     * @param path Path to file to write to
     *
     * @return Results storage
     *
     * @throws IOException Upon error opening file for writing
     */
    public ResultTable newResults(String path) throws IOException {
        results = new ResultStream(path, getColumns());
        return results;
    }

    /**
     * Returns the currently used ResultTable for results storage
     *
     * @return Results storage being used
     */
    public ResultTable getResults() {
        return results;
    }

    /**
     * Starts the measurement. Will run until completion if no errors are encountered. Will throw an InterruptedException
     * if the measurement is stopped by calling stop(). Can throw any other type of exception if there is a measurement
     * or instrumentation error.
     *
     * @throws Exception Upon measurement error or interruption.
     */
    public void start() throws Exception {

        runThread = Thread.currentThread();
        running   = true;
        stopped   = false;

        try {

            run(getResults());

        } catch (InterruptedException e) {

            stopped = true;

            try {
                onInterrupt();
            } catch (Exception ee) {
                ee.printStackTrace();
            }

            throw e;

        } finally {

            running = false;

            try {
                onFinish();
            } catch (Exception ee) {
                ee.printStackTrace();
            }

        }

    }

    /**
     * Returns whether this measurement is currently running.
     *
     * @return Is it running?
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Returns whether the last execution of this measurement was interrupted by stop().
     *
     * @return Was it stopped?
     */
    public boolean wasStopped() {
        return stopped;
    }

    /**
     * Stops the current execution of this measurement (if it is running at all).
     */
    public void stop() {
        if (isRunning()) {
            stopped = true;
            runThread.interrupt();
        }
    }

    /**
     * Makes the current thread wait for the given number of milliseconds with proper checks for any stop() calls.
     *
     * @param mSec Time to wait for, in milliseconds
     *
     * @throws InterruptedException If stop() is called (or other interrupt occurs) during sleep
     */
    public void sleep(int mSec) throws InterruptedException {

        if (stopped) {
            throw new InterruptedException();
        } else {
            Thread.sleep(mSec);
        }

    }

    /**
     * Checks if the stop() method has been called. To be called at safe points to stop a measurement during run().
     * Will throw an InterruptedException if stop() has indeed been called, causing run() to end early as it should.
     *
     * @throws InterruptedException If stop() has indeed been called.
     */
    public void checkPoint() throws InterruptedException {

        if (stopped) {
            throw new InterruptedException();
        }

    }

}

