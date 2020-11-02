package jisa.experiment;

import jisa.control.RTask;
import jisa.gui.Field;
import jisa.gui.Fields;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Abstract class for implementing measurement routines. Allows for procedures that can be easily and safely
 * interrupted.
 */
public abstract class Measurement {

    private boolean            running    = false;
    private boolean            stopped    = false;
    private ResultTable        results    = null;
    private ResultTable        log        = null;
    private RTask              logger     = null;
    private Thread             runThread  = Thread.currentThread();
    private List<Parameter<?>> parameters = new LinkedList<>();

    /**
     * Returns the name of this measurement.
     *
     * @return Name of the measurement
     */
    public abstract String getName();

    /**
     * Returns a user-given label for this measurement instance.
     *
     * @return Label
     */
    public abstract String getLabel();

    /**
     * Sets the user-given label for this measurement instance.
     *
     * @param value Label to set
     */
    public abstract void setLabel(String value);

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
     * This method is called on at every log interval to update the log ResultTable.
     *
     * @param task The RTask that the log is running in.
     * @param log  The ResultTable to be used for logging.
     *
     * @throws Exception Upon error
     */
    protected abstract void log(RTask task, ResultTable log) throws Exception;

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
     * This method should return an array of columns to be used when generator a new log ResultTable for logging.
     *
     * @return Array of columns.
     */
    public abstract Col[] getLogColumns();

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
     * Starts the measurements logging procedure (if it has one), writing the log file to the given path at the
     * specified interval.
     *
     * @param path         Path to write log to
     * @param intervalMSec Interval to log, in milliseconds
     *
     * @throws IOException Upon Read/Write error
     */
    public ResultTable startLogging(String path, int intervalMSec) throws IOException {

        log    = new ResultStream(path, getLogColumns());
        logger = new RTask(intervalMSec, (task) -> {

            try {
                log(task, log);
            } catch (Exception e) {
                System.err.printf("Error logging: %s", e.getMessage());
            }

        });

        logger.start();

        return log;

    }

    /**
     * Returns whichever ResultTable object is currently being used for logging purposes.
     *
     * @return The log ResultTable
     */
    public ResultTable getLog() {
        return log;
    }

    /**
     * Returns whether the measurement's logger is currently running or not.
     *
     * @return Is it logging?
     */
    public boolean isLogging() {
        return logger != null && logger.isRunning();
    }

    /**
     * Stops any currently running logger for this measurement.
     */
    public void stopLogging() {
        if (logger != null) logger.stop();
        if (log != null)    log.finalise();
    }

    public List<Parameter<?>> getParameters() {
        return new ArrayList<>(parameters);
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
    protected void checkPoint() throws InterruptedException {

        if (stopped) {
            throw new InterruptedException();
        }

    }

    public abstract class Parameter<T> {

        private final String   section;
        private final String   name;
        private final String   units;
        private       T        value;
        private       Field<T> field = null;

        public Parameter(String section, String name, String units, T defaultValue) {

            this.section = section;
            this.name    = name;
            this.units   = units;
            this.value   = defaultValue;

            parameters.add(this);

        }

        public String getSection() {
            return section;
        }

        public String getName() {
            return name;
        }

        public String getUnits() {
            return units;
        }

        public String getTitle() {
            return units == null ? getName() : String.format("%s [%s]", name, units);
        }

        public T getValue() {
            return value;
        }

        public void setValue(T value) {
            this.value = value;
        }

        protected abstract Field<T> makeField(Fields fields);

        public Field<T> createField(Fields fields) {
            field = makeField(fields);
            return field;
        }

        public void update() {
            setValue(field.get());
        }

    }

    public class DoubleParameter extends Parameter<Double> {

        public DoubleParameter(String section, String name, String units, Double defaultValue) {
            super(section, name, units, defaultValue);
        }

        @Override
        protected Field<Double> makeField(Fields fields) {
            return fields.addDoubleField(getTitle(), getValue());
        }

    }

    public class IntegerParameter extends Parameter<Integer> {

        public IntegerParameter(String section, String name, String units, Integer defaultValue) {
            super(section, name, units, defaultValue);
        }

        @Override
        protected Field<Integer> makeField(Fields fields) {
            return fields.addIntegerField(getTitle(), getValue());
        }

    }

    public class BooleanParameter extends Parameter<Boolean> {

        public BooleanParameter(String section, String name, String units, Boolean defaultValue) {
            super(section, name, units, defaultValue);
        }

        @Override
        protected Field<Boolean> makeField(Fields fields) {
            return fields.addCheckBox(getTitle(), getValue());
        }

    }

    public class StringParameter extends Parameter<String> {

        public StringParameter(String section, String name, String units, String defaultValue) {
            super(section, name, units, defaultValue);
        }

        @Override
        protected Field<String> makeField(Fields fields) {
            return fields.addTextField(getTitle(), getValue());
        }

    }

}

