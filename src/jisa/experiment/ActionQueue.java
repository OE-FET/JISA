package jisa.experiment;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import jisa.Util;
import jisa.control.SRunnable;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ActionQueue implements Iterable<ActionQueue.Action> {

    private static final ExecutorService              statusExecutor   = Executors.newSingleThreadExecutor();
    private static final ExecutorService              queueExecutor    = Executors.newSingleThreadExecutor();
    private static final ExecutorService              currentExecutor  = Executors.newSingleThreadExecutor();
    private final        ObservableList<Action>       queue            = FXCollections.observableList(new LinkedList<>());
    private final        SimpleObjectProperty<Action> current          = new SimpleObjectProperty<>(null);
    private final        List<Listener<Action>>       currentListeners = new LinkedList<>();
    private final        List<ListListener<Action>>   queueListeners   = new LinkedList<>();
    private              boolean                      isRunning        = false;
    private              boolean                      isStopped        = false;

    public ActionQueue() {

        current.addListener((o, oldValue, newValue) -> currentExecutor.submit(() -> {
            for (Listener<Action> listener : currentListeners) listener.updated(oldValue, newValue);
        }));

        queue.addListener((ListChangeListener<? super Action>) change -> queueExecutor.submit(
                () -> {

                    List<Action> added   = new LinkedList<>();
                    List<Action> removed = new LinkedList<>();

                    while (change.next()) {
                        added.addAll(change.getAddedSubList());
                        removed.addAll(change.getRemoved());
                    }

                    for (ListListener<Action> listener : queueListeners) listener.updated(added, removed);

                }
        ));

    }

    public void clear() {

        if (isRunning) {
            throw new IllegalStateException("Cannot modify action queue while it is running");
        }

        queue.clear();

    }

    /**
     * Adds an action to the queue.
     *
     * @param action Action to add to queue
     */
    public Action addAction(Action action) {

        if (isRunning) {
            throw new IllegalStateException("Cannot modify action queue while it is running");
        }

        queue.add(action);

        return action;

    }

    public void removeAction(Action action) {

        if (isRunning) {
            throw new IllegalStateException("Cannot modify action queue while it is running");
        }

        queue.remove(action);

    }

    /**
     * Adds an action to the queue.
     *
     * @param name  Name of the action
     * @param toRun Code to execute to perform action
     */
    public Action addAction(String name, SRunnable toRun) {
        return addAction(new Action(name, toRun));
    }

    /**
     * Adds a measurement to run as an action to the queue.
     *
     * @param measurement Measurement to run
     */
    public MeasureAction addMeasurement(String name, Measurement measurement, SRunnable before, SRunnable after) {
        return (MeasureAction) addAction(new MeasureAction(name, measurement, before, after));
    }

    public MeasureAction addMeasurement(String name, Measurement measurement) {
        return addMeasurement(name, measurement, () -> {
        }, () -> {
        });
    }

    /**
     * Adds an action to the queue that causes the run thread to wait for the given number of milliseconds.
     *
     * @param millis Time to wait, in milliseconds
     */
    public WaitAction addWait(long millis) {
        return (WaitAction) addAction(new WaitAction(millis));
    }

    /**
     * Starts the queue running, from the top.
     */
    public Result start() {

        isStopped = false;
        isRunning = true;

        for (Action action : queue) {
            action.status.set(Status.NOT_STARTED);
        }

        for (Action action : queue) {

            action.prepare();
            current.set(action);
            action.start();
            action.cleanUp();

            if (isStopped) {
                isRunning = false;
                return Result.INTERRUPTED;
            }

        }

        current.set(null);
        isRunning = false;

        for (Action action : queue) {
            if (action.getStatus() != Status.COMPLETED) {
                return Result.ERROR;
            }
        }

        return Result.COMPLETED;

    }

    /**
     * Attempts to stop the currently running action and stop the queue.
     */
    public void stop() {

        isStopped = true;

        if (current.isNotNull().get()) {
            current.get().stop();
        }

    }

    /**
     * Adds a listener that is triggered when the currently running action changes.
     *
     * @param listener Current action listener
     *
     * @return The added listener
     */
    public Listener<Action> addCurrentListener(Listener<Action> listener) {
        currentListeners.add(listener);
        return listener;
    }

    /**
     * Adds a listener that is triggered when the currently running action changes.
     *
     * @param listener Current action listener
     *
     * @return The added listener
     */
    public Listener<Action> addCurrentListener(SRunnable listener) {
        return addCurrentListener(((oldValue, newValue) -> listener.runRegardless()));
    }

    /**
     * Removes a listener that is triggered when the currently running action changes.
     *
     * @param listener Listener to removed
     */
    public void removeCurrentListener(Listener<Action> listener) {
        currentListeners.remove(listener);
    }

    public ListListener<Action> addQueueListener(ListListener<Action> listener) {
        queueListeners.add(listener);
        return listener;
    }

    public ListListener<Action> addQueueListener(SRunnable listener) {
        return addQueueListener(((oldValue, newValue) -> listener.runRegardless()));
    }

    public void removeQueueListener(ListListener<Action> listener) {
        queueListeners.remove(listener);
    }

    @Override
    public Iterator<Action> iterator() {
        return queue.iterator();
    }

    public int getSize() {
        return queue.size();
    }

    public int getMeasurementCount(Class<? extends Measurement> mClass) {

        int total = 0;
        for (Action action : queue) {

            if (action instanceof MeasureAction && ((MeasureAction) action).measurement.getClass().equals(mClass)) {
                total++;
            }

        }

        return total;

    }

    public enum Status {
        NOT_STARTED,
        RUNNING,
        INTERRUPTED,
        COMPLETED,
        ERROR
    }

    public enum Result {
        COMPLETED,
        INTERRUPTED,
        ERROR
    }

    public interface Listener<T> {
        void updated(T oldValue, T newValue);

    }

    public interface ListListener<T> {
        void updated(List<T> added, List<T> removed);

    }

    public static class Action {

        private final SRunnable                    runnable;
        private final SimpleObjectProperty<Status> status    = new SimpleObjectProperty<>(Status.NOT_STARTED);
        private final List<Listener<Status>>       listeners = new LinkedList<>();
        private       String                       name;
        private       Exception                    exception;
        private       Thread                       runThread;
        private       ResultTable                  data      = null;

        public Action(String name, SRunnable runnable) {

            this.name     = name;
            this.runnable = runnable;

            status.addListener((v, o, n) -> statusExecutor.submit(() -> {
                for (Listener<Status> listener : listeners) listener.updated(o, n);
            }));

        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public ResultTable getData() {
            return data;
        }

        public void setData(ResultTable data) {
            this.data = data;
        }

        public void prepare() {

        }

        public void cleanUp() {

        }

        public void start() {

            runThread = Thread.currentThread();

            try {
                status.set(Status.RUNNING);
                runnable.run();
                status.set(Status.COMPLETED);
            } catch (InterruptedException e) {
                status.set(Status.INTERRUPTED);
                exception = e;
            } catch (Exception e) {
                status.set(Status.ERROR);
                exception = e;
            }

        }

        public void stop() {
            runThread.interrupt();
        }

        public Status getStatus() {
            return status.get();
        }

        public Listener<Status> addStatusListener(Listener<Status> listener) {
            listeners.add(listener);
            return listener;
        }

        public Listener<Status> addStatusListener(SRunnable onChange) {
            return addStatusListener((o, n) -> onChange.runRegardless());
        }

        public void removeStatusListener(Listener<Status> listener) {
            listeners.remove(listener);
        }

        public Exception getException() {
            return exception;
        }

        public Action copy() {
            return new Action(getName(), runnable);
        }

    }

    public static class MeasureAction extends Action {

        private final Measurement         measurement;
        private final Map<String, String> attributes = new HashMap<>();
        private       SRunnable           before;
        private       SRunnable           after;
        private       String              name;
        private       String              resultPath = null;

        public MeasureAction(String name, Measurement measurement, SRunnable before, SRunnable after) {

            super(String.format("%s (%s)", measurement.getName(), name), measurement::start);

            this.measurement = measurement;
            this.before      = before;
            this.after       = after;
            this.name        = name;

        }

        public void setResultsPath(String path) {
            resultPath = path;
        }

        public String getResultPath() {
            return resultPath;
        }

        public void doNotOutputResults() {
            resultPath = null;
        }

        public void setAttribute(String name, Object value) {
            attributes.put(name, value.toString());
            if (getData() != null) getData().setAttribute(name, value.toString());
        }

        public void stop() {
            measurement.stop();
            super.stop();
        }

        public void prepare() {

            if (resultPath == null) {
                setData(measurement.newResults());
            } else {
                try {
                    setData(measurement.newResults(resultPath));
                } catch (Exception e) {
                    setData(measurement.newResults());
                }
            }

            attributes.forEach((k,v) -> getData().setAttribute(k,v));

            before.runRegardless();

        }

        public void cleanUp() {
            getData().finalise();
            after.runRegardless();
        }

        public MeasureAction setBefore(SRunnable before) {
            this.before = before;
            return this;
        }

        public MeasureAction setAfter(SRunnable after) {
            this.after = after;
            return this;
        }

        public Measurement getMeasurement() {
            return measurement;
        }

        public MeasureAction copy() {
            MeasureAction action =  new MeasureAction(name, measurement, before, after);
            action.attributes.putAll(attributes);
            return action;
        }

    }

    public static class WaitAction extends Action {

        public WaitAction(long millis) {

            super(String.format("Wait %s", Util.msToString(millis)), () -> Thread.sleep(millis));
        }

    }

}
