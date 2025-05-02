package jisa.experiment;

import jisa.devices.Configuration;
import jisa.devices.DeviceException;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Abstract class for encapsulating measurement routines. Allows one to define user-configurable parameters and
 * instruments.
 *
 * @param <R> The class of the object used to store the data output of the measurement.
 */
public abstract class Measurement<R> {

    private final List<InstrumentValue> instruments;
    private final List<ParamValue>      parameters;
    private final List<Listener>        listeners  = new LinkedList<>();
    private final List<Throwable>       exceptions = new LinkedList<>();
    private final String                name;

    private String  label;
    private boolean running    = false;
    private boolean interrupt  = false;
    private Thread  runThread  = null;
    private Status  status     = Status.STOPPED;
    private R       cachedData = null;

    protected Measurement(String name, String label) {

        this.name  = name;
        this.label = label;

        parameters = Arrays.stream(getClass().getDeclaredFields())
                           .filter(f -> f.isAnnotationPresent(Parameter.class))
                           .map(f -> {
                               f.setAccessible(true);
                               Parameter annotation = f.getAnnotation(Parameter.class);
                               return new ParamValue(annotation.section(), annotation.name(), f.getType(), annotation.type(), () -> f.get(this), v -> f.set(this, v), annotation.options());
                           })
                           .collect(Collectors.toList());

        instruments = Arrays.stream(getClass().getDeclaredFields())
                            .filter(f -> f.isAnnotationPresent(Instrument.class))
                            .filter(f -> jisa.devices.Instrument.class.isAssignableFrom(f.getType()))
                            .map(f -> {
                                f.setAccessible(true);
                                Instrument a = f.getAnnotation(Instrument.class);
                                return new InstrumentValue(a.name() + (a.required() ? " (Required)" : " (Optional)"), f.getType(), () -> f.get(this), v -> f.set(this, v), a.required());
                            })
                            .collect(Collectors.toList());

    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public synchronized Status getStatus() {
        return status;
    }

    protected void setStatus(Status status) {

        synchronized (name) {

            this.status = status;

            for (Listener listener : listeners) {
                listener.update(status);
            }

        }

    }

    public synchronized Listener addListener(Listener listener) {
        listeners.add(listener);
        return listener;
    }

    public synchronized void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    /**
     * Creates a new instance of whatever data structure this measurement is to use. This will be passed to all the
     * measurement methods.
     * <p>
     * For instance, one might want this method to return a new ResultTable.
     *
     * @return New measurement data structure
     */
    protected abstract R createData();

    protected R getCachedData() {

        R toReturn;

        if (cachedData == null) {
            toReturn = createData();
        } else {
            toReturn   = cachedData;
            cachedData = null;
        }

        return toReturn;

    }

    /**
     * This method should contain the main logic of the measurement.
     *
     * @param data The data structure to use for this run of the measurement.
     *
     * @throws Exception This method can throw exceptions, which will be caught by the measurement structure.
     */
    protected abstract void main(R data) throws Exception;

    /**
     * This method is always called after a measurement has finished --- regardless of how it finished. That is, it will
     * be called if it succeeded, ended in error, or was interrupted.
     *
     * @param data The data structure used for the measurement.
     */
    protected abstract void after(R data) throws Exception;

    /**
     * This method is called if the run() method is interrupted. This will run before after() is called.
     *
     * @param data The data structure used for the measurement.
     */
    protected abstract void interrupted(R data);

    /**
     * This method is called if the run() method throws an exception (other than InterruptedException). This is will
     * before after() is called.
     *
     * @param data      The data structure used for the measurement.
     * @param exception The exception that was thrown.
     */
    protected abstract void error(R data, List<Throwable> exception);

    public List<InstrumentValue> getInstruments() {
        return List.copyOf(instruments);
    }

    public List<ParamValue> getParameters() {
        return List.copyOf(parameters);
    }

    protected void thread(R data) {

        boolean interrupted = false;
        exceptions.clear();

        for (InstrumentValue instrument : instruments) {

            try {

                instrument.set(instrument.getConfiguration().getInstrument());

                if (instrument.isRequired() && instrument.get() == null) {
                    throw new MissingInstrumentException("Required instrument \"" + instrument.name + "\" is not configured.");
                }

            } catch (Throwable e) {
                exceptions.add(e);
            }

        }

        if (!exceptions.isEmpty()) {
            setStatus(Status.ERROR);
            error(data, exceptions);
            running   = false;
            runThread = null;
            return;
        }

        try {

            setStatus(Status.RUNNING);
            main(data);

        } catch (InterruptedException e) {

            interrupted = true;
            interrupted(data);

        } catch (Throwable e) {

            exceptions.add(e);

        } finally {

            try {
                setStatus(Status.POST_RUN);
                after(data);
            } catch (Throwable e) {
                exceptions.add(e);
            }

            if (interrupted) {
                setStatus(Status.INTERRUPTED);
            } else if (!exceptions.isEmpty()) {
                setStatus(Status.ERROR);
                error(data, exceptions);
            } else {
                setStatus(Status.COMPLETE);
            }

            runThread = null;
            running   = false;

        }

    }

    public synchronized R getData() {

        if (cachedData == null) {
            cachedData = createData();
        }

        return cachedData;

    }

    public synchronized R start() {

        if (running || runThread != null) {
            throw new IllegalStateException("Measurement already started.");
        }

        running   = true;
        interrupt = false;

        R data = getCachedData();

        runThread = new Thread(() -> thread(data));
        runThread.start();

        return data;

    }

    public Result run() {

        if (running || runThread != null) {
            throw new IllegalStateException("Measurement already started.");
        }

        running   = true;
        interrupt = false;

        R data = getCachedData();

        thread(data);

        return awaitResult();

    }

    public synchronized void stop() {

        if (!running || interrupt) {
            throw new IllegalStateException("Measurement already stopped.");
        }

        interrupt = true;
        runThread.interrupt();

    }

    public synchronized boolean isRunning() {
        return running;
    }

    public synchronized Result awaitResult() {

        if (running && runThread != null) {
            try {
                runThread.join();
            } catch (InterruptedException ignored) { }
        }

        switch (status) {

            case ERROR:
                return new Result(ResultType.ERROR, getExceptions(), getData());

            case INTERRUPTED:
                return new Result(ResultType.INTERRUPTED, getExceptions(), getData());

            case COMPLETE:
                return new Result(ResultType.SUCCESS, Collections.emptyList(), getData());

            default:
                return new Result(ResultType.DID_NOT_RUN, Collections.emptyList(), getData());

        }

    }

    public List<Throwable> getExceptions() {
        return List.copyOf(exceptions);
    }

    /**
     * Call this to cause a delay in your routine. Includes checks for interrupts making it also double
     * as a safe place to break your code should stop() be called.
     *
     * @param timeout The amount of time to sleep for, in milliseconds.
     *
     * @throws InterruptedException If interrupted.
     */
    public void sleep(long timeout) throws InterruptedException {

        if (interrupt) {
            throw new InterruptedException();
        }

        Thread.sleep(timeout);

        if (interrupt) {
            throw new InterruptedException();
        }

    }

    /**
     * Call this to cause a delay in your routine. Includes checks for interrupts making it also double
     * as a safe place to break your code should stop() be called.
     *
     * @param timeout The amount of time to sleep for, in milliseconds.
     *
     * @throws InterruptedException If interrupted.
     */
    public void sleep(int timeout) throws InterruptedException {
        sleep((long) timeout);
    }

    /**
     * Defines a point in your routine at which it is safe to break if stop() is called. If neither this nor sleep() are
     * called anywhere in run(), the calling stop() may have no effect.
     *
     * @throws InterruptedException If stop() has been called.
     */
    public void checkPoint() throws InterruptedException {

        if (interrupt) {
            throw new InterruptedException();
        }

    }

    public static class ParamValue<T> {

        private final String    section;
        private final String    name;
        private final Class<T>  dataType;
        private final Type      type;
        private final Getter<T> getter;
        private final Setter<T> setter;
        private final String[]  options;

        public ParamValue(String section, String name, Class<T> dataType, Type type, Getter<T> getter, Setter<T> setter, String... options) {
            this.section  = section;
            this.name     = name;
            this.dataType = dataType;
            this.type     = type;
            this.getter   = getter;
            this.setter   = setter;
            this.options  = options;
        }

        public String getName() {
            return name;
        }

        public String getSection() {
            return section;
        }

        public Class<T> getDataType() {
            return dataType;
        }

        public Type getType() {
            return type;
        }

        public T get() {
            try {
                return getter.get();
            } catch (IllegalAccessException ignored) {
                return null;
            }
        }

        public void set(T value) {
            try {
                setter.set(value);
            } catch (IllegalAccessException ignored) {
            }
        }

        public boolean isChoice() {
            return options.length > 0;
        }

        public List<String> getOptions() {
            return List.of(options);
        }

        public interface Getter<T> {
            T get() throws IllegalAccessException;
        }

        public interface Setter<T> {
            void set(T value) throws IllegalAccessException;
        }

    }

    public static class InstrumentValue<I extends jisa.devices.Instrument> {

        private final String           name;
        private final Configuration<I> configuration;
        private final Getter<I>        getter;
        private final Setter<I>        setter;
        private final boolean          isRequired;

        public InstrumentValue(String name, Class<I> type, Getter<I> getter, Setter<I> setter, boolean isRequired) {
            this.name          = name;
            this.getter        = getter;
            this.setter        = setter;
            this.isRequired    = isRequired;
            this.configuration = new Configuration<>(name, type);
        }

        public String getName() {
            return name;
        }

        public I get() {
            try {
                return getter.get();
            } catch (IllegalAccessException ignored) {
                return null;
            }
        }

        public void set(I value) {
            try {
                setter.set(value);
            } catch (IllegalAccessException ignored) {
            }
        }

        public Configuration<I> getConfiguration() {
            return configuration;
        }

        public boolean isRequired() {
            return isRequired;
        }

        public interface Getter<T> {
            T get() throws IllegalAccessException;
        }

        public interface Setter<T> {
            void set(T value) throws IllegalAccessException;
        }

    }

    public enum Status {

        STOPPED,
        RUNNING,
        POST_RUN,
        INTERRUPTED,
        ERROR,
        COMPLETE

    }

    public enum ResultType {
        SUCCESS,
        ERROR,
        INTERRUPTED,
        DID_NOT_RUN
    }

    public class Result {

        private final ResultType      resultType;
        private final List<Throwable> exceptions;
        private final R               data;

        public Result(ResultType resultType, List<Throwable> exceptions, R data) {
            this.resultType = resultType;
            this.exceptions = exceptions;
            this.data       = data;
        }

        public ResultType getType() {
            return resultType;
        }

        public List<Throwable> getExceptions() {
            return exceptions;
        }

        public R getData() {
            return data;
        }

    }

    public interface Listener {
        void update(Status status);
    }

    public interface Getter<I, T> {
        T get(I instrument) throws DeviceException, IOException;
    }

    public interface Runner<I> {
        void run(I instrument) throws DeviceException, IOException;
    }

    public static class MissingInstrumentException extends Exception {

        public MissingInstrumentException(String message) {
            super(message);
        }

    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Parameter {

        String section() default "Basic";

        String name();

        String[] options() default {};

        Type type() default Type.AUTO;

    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Instrument {

        String name();

        boolean required() default true;

    }

    public enum Type {
        AUTO,
        TIME,
        FILE_OPEN,
        FILE_SAVE,
        DIRECTORY
    }

}
