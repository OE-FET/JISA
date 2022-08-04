package jisa.experiment;

import jisa.control.ConfigBlock;
import jisa.devices.Configuration;
import jisa.devices.interfaces.Instrument;
import jisa.experiment.queue.Action;
import jisa.gui.Element;
import jisa.gui.Field;
import jisa.gui.Fields;
import jisa.maths.Range;
import jisa.results.Column;
import jisa.results.ResultList;
import jisa.results.ResultStream;
import jisa.results.ResultTable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Abstract class for implementing measurement routines. Allows for procedures that can be easily and safely
 * interrupted.
 */
public abstract class Measurement {

    private final List<Parameter<?>>     parameters  = new ArrayList<>();
    private final List<Configuration<?>> instruments = new ArrayList<>();
    private       String                 label       = "";
    private       boolean                running     = false;
    private       boolean                stopped     = false;
    private       ResultTable            results     = null;
    private       Thread                 runThread   = Thread.currentThread();

    public <T extends Instrument> Configuration<T> addInstrument(Configuration<T> configuration) {
        instruments.add(configuration);
        return configuration;
    }

    public <T extends Instrument> Configuration<T> addInstrument(String name, Class<T> type) {
        return addInstrument(new Configuration<>(name, type));
    }

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
    public String getLabel() {
        return label;
    }

    /**
     * Sets the user-given label for this measurement instance.
     *
     * @param value Label to set
     */
    public void setLabel(String value) {
        label = value;
    }

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
     * This method is always called when a measurement ends in error (but not interrupt).
     *
     * @throws Exception This method can throw exceptions
     */
    protected abstract void onError() throws Exception;

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
    public abstract Column[] getColumns();

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

        } catch (Exception e) {

            try {
                onError();
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

    public List<Parameter<?>> getParameters() {
        return new ArrayList<>(parameters);
    }

    public List<Configuration<? extends Instrument>> getInstruments() {
        return new ArrayList<>(instruments);
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

    public List<Action> getActions() {
        return Collections.emptyList();
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

            updateValue(value);

            if (field != null) {
                field.set(value);
            }

        }

        protected void updateValue(T value) {
            this.value = value;
        }

        protected abstract Field<T> makeField(Fields fields);

        public Element getElement() {
            return null;
        }

        public Field<T> createField(Fields fields) {
            field = makeField(fields);
            return field;
        }

        public void update() {
            updateValue(field.get());
        }

    }

    public class CustomParameter<T> extends Parameter<T> {

        private final Element   element;
        private final Getter<T> getter;
        private final Setter<T> setter;
        private final Reader<T> reader;
        private final Writer<T> writer;

        public CustomParameter(String name, Element element, Getter<T> getter, Setter<T> setter, Reader<T> reader, Writer<T> writer) {
            super("", name, null, getter.get());
            this.element = element;
            this.getter  = getter;
            this.setter  = setter;
            this.reader  = reader;
            this.writer  = writer;
        }

        public CustomParameter(String name, Element element, Getter<T> getter, Setter<T> setter) {
            this(name, element, getter, setter, b -> (T) b.value(name).getOrDefault(getter.get()), (b,v) -> b.value(name).set(v));
        }

        public void setValue(T value) {
            updateValue(value);
            setter.set(value);
        }

        public void update() {
            updateValue(getter.get());
        }

        @Override
        protected Field<T> makeField(Fields fields) {
            return null;
        }

        public Element getElement() {
            return element;
        }

        public void writeToConfig(ConfigBlock block) {
            writer.write(block, getValue());
        }

        public void loadFromConfig(ConfigBlock block) {
            setValue(reader.read(block));
        }

    }

    public interface Getter<T> {
        T get();
    }

    public interface Setter<T> {
        void set(T value);
    }

    public interface Reader<T> {
        T read(ConfigBlock block);
    }

    public interface Writer<T> {
        void write(ConfigBlock block, T value);
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

    public class TimeParameter extends Parameter<Integer> {

        public TimeParameter(String section, String name, Integer defaultValue) {
            super(section, name, null, defaultValue);
        }

        @Override
        protected Field<Integer> makeField(Fields fields) {
            return fields.addTimeField(getTitle(), getValue());
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

    public class RangeParameter extends Parameter<Range<Double>> {

        private double min   = 0.0;
        private double max   = 10.0;
        private int    count = 11;
        private double step  = 1.0;
        private int    order = 2;

        public RangeParameter(String section, String name, String units, Range<Double> defaultRange, double min, double max, int count, double step, int order) {
            super(section, name, units, defaultRange);
            this.min   = min;
            this.max   = max;
            this.count = count;
            this.step  = step;
            this.order = order;
        }

        public RangeParameter(String section, String name, String units, Range<Double> defaultRange) {
            super(section, name, units, defaultRange);
        }

        public RangeParameter(String section, String name, String units, double min, double max, int steps) {
            this(section, name, units, Range.linear(min, max, steps), min, max, steps, (max - min) / (steps - 1), 2);
        }

        protected Field<Range<Double>> makeField(Fields fields) {
            return fields.addDoubleRange(getTitle(), getValue(), min, max, count, step, order);
        }

    }

    public class ChoiceParameter extends Parameter<Integer> {

        private final String[] options;

        public ChoiceParameter(String section, String name, int defaultValue, String... options) {
            super(section, name, null, defaultValue);
            this.options = options;
        }

        protected Field<Integer> makeField(Fields fields) {
            return fields.addChoice(getTitle(), getValue(), options);
        }

    }

}

