package jisa.experiment;

import javafx.beans.InvalidationListener;
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
    private final        List<Action>                 oldList          = new LinkedList<>(queue);
    private final        SimpleObjectProperty<Action> current          = new SimpleObjectProperty<>(null);
    private final        List<Listener<Action>>       currentListeners = new LinkedList<>();
    private final        List<ListListener<Action>>   queueListeners   = new LinkedList<>();
    private              boolean                      isRunning        = false;
    private              boolean                      isStopped        = false;

    public ActionQueue() {

        current.addListener((o, oldValue, newValue) -> currentExecutor.submit(() -> {
            for (Listener<Action> listener : currentListeners) listener.updated(oldValue, newValue);
        }));

        queue.addListener((InvalidationListener) change -> queueExecutor.submit(this::updateQueueListeners));

    }

    private synchronized void updateQueueListeners() {

        List<Action> added = new LinkedList<>(queue);
        added.removeAll(oldList);
        List<Action> removed = new LinkedList<>(oldList);
        removed.removeAll(queue);

        oldList.clear();
        oldList.addAll(queue);

        for (ListListener<Action> listener : queueListeners) listener.updated(added, removed);

    }

    public int getVariableCount(String name) {

        int count = 0;
        for (Action action : queue) if (action.variables.containsKey(name)) count++;
        return count;

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
    public MeasureAction addMeasurement(String name, Measurement measurement, ARunnable before, ARunnable after) {
        return (MeasureAction) addAction(new MeasureAction(name, measurement, before, after));
    }

    public MeasureAction addMeasurement(String name, Measurement measurement) {
        return addMeasurement(name, measurement, a -> {}, a -> {});
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
        private final Map<String, String>          variables = new LinkedHashMap<>();
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

        public void setVariable(String name, String value) {
            variables.put(name, value);
        }

        public String getVariable(String name) {
            return variables.get(name);
        }

        public String getVariableOrDefault(String name, String defaultValue) {
            return variables.getOrDefault(name, defaultValue);
        }

        public Map<String, String> getVariables() {
            return variables;
        }

        public Action copy() {
            Action action = new Action(getName(), runnable);
            action.variables.putAll(variables);
            return action;
        }

        public String getVariableString() {

            List<String> parts = new LinkedList<>();
            variables.forEach((name, value) -> parts.add(String.format("%s = %s", name, value)));
            Collections.reverse(parts);
            return String.join(", ", parts);

        }

    }

    public static class MeasureAction extends Action {

        private final Measurement         measurement;
        private final Map<String, String> attributes = new HashMap<>();
        private       ARunnable           before;
        private       ARunnable           after;
        private       String              name;
        private       String              resultPath = null;

        public MeasureAction(String name, Measurement measurement, ARunnable before, ARunnable after) {

            super(name, measurement::start);

            this.measurement = measurement;
            this.before      = before;
            this.after       = after;
            this.name        = name;

        }

        public String getName() {

            if (getVariables().isEmpty()) {
                return String.format("%s (%s)", measurement.getName(), name);
            } else {
                return String.format("%s (%s) (%s)", measurement.getName(), name, getVariableString());
            }

        }

        public String getVariablePathString() {

            List<String> parts = new LinkedList<>();
            getVariables().forEach((name, value) -> parts.add(String.format("%s=%s", name.replace(" ", ""), value.replace(" ", ""))));
            Collections.reverse(parts);
            return String.join("-", parts);

        }

        public void setResultsPath(String path) {
            resultPath = path;
        }

        public String getResultsPath() {
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

                    String path;

                    if (resultPath.contains("%s")) {
                        path = String.format(resultPath, getVariablePathString());
                    } else {
                        path = resultPath;
                    }

                    setData(measurement.newResults(path));

                } catch (Exception e) {
                    setData(measurement.newResults());
                }

            }

            attributes.forEach((k, v) -> getData().setAttribute(k, v));

            before.runRegardless(this);

        }

        public void cleanUp() {
            getData().finalise();
            after.runRegardless(this);
        }

        public MeasureAction setBefore(ARunnable before) {
            this.before = before;
            return this;
        }

        public MeasureAction setAfter(ARunnable after) {
            this.after = after;
            return this;
        }

        public Measurement getMeasurement() {
            return measurement;
        }

        public MeasureAction copy() {

            MeasureAction action = new MeasureAction(name, measurement, before, after);
            action.attributes.putAll(attributes);
            action.getVariables().putAll(getVariables());
            action.resultPath = resultPath;
            return action;

        }

    }

    public static class WaitAction extends Action {

        public WaitAction(long millis) {

            super(String.format("Wait %s", Util.msToString(millis)), () -> Thread.sleep(millis));
        }

    }

    public interface ARunnable {

        void run(MeasureAction action) throws Exception;

        default void runRegardless(MeasureAction action) {
            try { run(action); } catch (Exception e) { e.printStackTrace(); }
        }

    }

}
