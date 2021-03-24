package jisa.experiment;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;
import jisa.Util;
import jisa.control.SRunnable;
import jisa.gui.GUI;
import jisa.maths.functions.GFunction;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ActionQueue implements Iterable<ActionQueue.Action> {

    private static final ExecutorService              statusExecutor   = Executors.newSingleThreadExecutor();
    private static final ExecutorService              queueExecutor    = Executors.newSingleThreadExecutor();
    private static final ExecutorService              currentExecutor  = Executors.newSingleThreadExecutor();
    private final        List<Action>                 queue            = new LinkedList<>();
    private final        List<Action>                 oldList          = new LinkedList<>(queue);
    private final        SimpleObjectProperty<Action> current          = new SimpleObjectProperty<>(null);
    private final        List<Listener<Action>>       currentListeners = new LinkedList<>();
    private final        List<ListListener<Action>>   queueListeners   = new LinkedList<>();
    private              boolean                      abortOnError     = false;
    private              int                          maxAttempts      = 1;
    private              boolean                      isRunning        = false;
    private              boolean                      isStopped        = false;

    public ActionQueue() {

        current.addListener((o, oldValue, newValue) -> currentExecutor.submit(() -> {
            for (Listener<Action> listener : currentListeners) {
                listener.updated(oldValue, newValue);
            }
        }));

    }

    public int getVariableCount(String name) {

        int count = 0;
        for (Action action : queue) {
            if (action.variables.containsKey(name)) {
                count++;
            }
        }
        return count;

    }

    public synchronized void clear() {

        if (isRunning) {
            throw new IllegalStateException("Cannot modify action queue while it is running");
        }

        List<Action> removed = new LinkedList<>(queue);
        for (ListListener<Action> listener : queueListeners) {
            queueExecutor.submit(() -> listener.updated(Collections.emptyList(), removed));
        }

        queue.clear();

    }

    /**
     * Adds an action to the queue.
     *
     * @param action Action to add to queue
     */
    public synchronized Action addAction(Action action) {

        if (isRunning) {
            throw new IllegalStateException("Cannot modify action queue while it is running");
        }

        queue.add(action);

        for (ListListener<Action> listener : queueListeners) {
            queueExecutor.submit(() -> listener.updated(Collections.singletonList(action), Collections.emptyList()));
        }

        return action;

    }

    public synchronized List<Action> addQueue(ActionQueue queue, GFunction<Action, Action> mapping) {

        List<Action> list = new LinkedList<>();

        for (Action action : queue.getQueue()) {

            Action copy = mapping.value(action.copy());
            addAction(copy);
            list.add(copy);

        }

        return list;

    }

    public synchronized void removeAction(Action action) {

        if (isRunning) {
            throw new IllegalStateException("Cannot modify action queue while it is running");
        }

        for (ListListener<Action> listener : queueListeners) {
            queueExecutor.submit(() -> listener.updated(Collections.emptyList(), Collections.singletonList(action)));
        }

        queue.remove(action);

    }

    /**
     * Adds an action to the queue.
     *
     * @param name  Name of the action
     * @param toRun Code to execute to perform action
     */
    public synchronized Action addAction(String name, SRunnable toRun) {
        return addAction(new Action(name, toRun));
    }

    public synchronized void swapOrder(Action a, Action b) {

        if (isRunning) {
            throw new IllegalStateException("Cannot modify action queue while it is running");
        }

        int indA = queue.indexOf(a);
        int indB = queue.indexOf(b);

        queue.set(indA, b);
        queue.set(indB, a);

        Map<Integer, Action> changes = new HashMap<>();

        changes.put(indA, b);
        changes.put(indB, a);

        for (ListListener<Action> listener : queueListeners) {
            queueExecutor.submit(() -> listener.updated(changes));
        }

    }

    public synchronized void swapOrder(int indA, int indB) {

        if (isRunning) {
            throw new IllegalStateException("Cannot modify action queue while it is running");
        }

        Action a = queue.get(indA);
        Action b = queue.get(indB);

        queue.set(indA, b);
        queue.set(indB, a);

        Map<Integer, Action> changes = Map.of(indA, b, indB, a);

        for (ListListener<Action> listener : queueListeners) {
            queueExecutor.submit(() -> listener.updated(changes));
        }

    }

    public synchronized void replaceAction(Action toReplace, Action replaceWith) {

        if (isRunning) {
            throw new IllegalStateException("Cannot modify action queue while it is running");
        }

        int ind = queue.indexOf(toReplace);

        if (ind > -1) {

            queue.set(ind, replaceWith);

            Map<Integer, Action> changes = Map.of(ind, replaceWith);

            for (ListListener<Action> listener : queueListeners) {
                queueExecutor.submit(() -> listener.updated(changes));
            }

        }

    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {

        if (isRunning) {
            throw new IllegalStateException("Cannot max attempts setting while running.");
        }

        this.maxAttempts = maxAttempts;

    }

    public boolean isAbortOnError() {
        return abortOnError;
    }

    public void setAbortOnError(boolean abortOnError) {

        if (isRunning) {
            throw new IllegalStateException("Cannot change abort on error setting while running.");
        }

        this.abortOnError = abortOnError;

    }

    public int indexOf(Action a) {
        return queue.indexOf(a);
    }

    public List<Action> getActions() {
        return List.copyOf(queue);
    }

    /**
     * Adds a measurement to run as an action to the queue.
     *
     * @param measurement Measurement to run
     */
    public synchronized MeasureAction addMeasurement(String name, Measurement measurement, ARunnable before, ARunnable after) {
        return (MeasureAction) addAction(new MeasureAction(name, measurement, before, after));
    }

    public synchronized MeasureAction addMeasurement(String name, Measurement measurement) {
        return addMeasurement(name, measurement, a -> {}, a -> {});
    }

    /**
     * Adds an action to the queue that causes the run thread to wait for the given number of milliseconds.
     *
     * @param millis Time to wait, in milliseconds
     */
    public synchronized WaitAction addWait(long millis) {
        return (WaitAction) addAction(new WaitAction(millis));
    }

    public Result start() {
        return start(0);
    }

    public Result start(int from) {

        if (queue.size() == 0) {
            throw new IllegalStateException("There are no actions in this queue.");
        }

        if (from < 0 || from >= queue.size()) {
            throw new IndexOutOfBoundsException("There is no action with that index.");
        }

        isStopped = false;
        isRunning = true;

        List<Action> queue = from > 0 ? this.queue.subList(from, this.queue.size()) : this.queue;

        for (Action action : queue) {
            action.status.set(Status.NOT_STARTED);
        }

        for (Action action : queue) {

            int    count = 0;
            Status result;

            do {

                result = runAction(action);
                count++;

                if (isStopped) {
                    isRunning = false;
                    return Result.INTERRUPTED;
                }

            } while (result != Status.COMPLETED && count < maxAttempts);

            if (abortOnError && result == Status.ERROR) {
                break;
            }

        }

        current.set(null);
        isRunning = false;

        if (queue.stream().anyMatch(a -> a.getStatus() != Status.COMPLETED)) {
            return Result.ERROR;
        }

        return Result.COMPLETED;

    }

    private Status runAction(Action action) {

        action.prepare();
        current.set(action);
        action.start();
        action.cleanUp();

        return action.getStatus();

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
        return addQueueListener(((oldValue, newValue, changes) -> listener.runRegardless()));
    }

    public void removeQueueListener(ListListener<Action> listener) {
        queueListeners.remove(listener);
    }

    @Override
    public Iterator<Action> iterator() {
        return queue.iterator();
    }

    public List<Action> getQueue() {
        return List.copyOf(queue);
    }

    public int getSize() {
        return queue.size();
    }

    public int getMeasurementCount(Class<? extends Measurement> mClass) {

        return (int) queue.stream()
                          .filter(a -> a instanceof MeasureAction && ((MeasureAction) a).measurement.getClass().equals(mClass))
                          .count();

    }

    public enum Status {

        NOT_STARTED("Not Started", "queued"),
        RUNNING("Running", "progress"),
        RETRY("Running (Retry)", "progress"),
        INTERRUPTED("Interrupted", "cancelled"),
        COMPLETED("Completed", "complete"),
        ERROR("Error Encountered", "error");

        private final Image  image;
        private final String text;

        Status(String text, String imageName) {
            this.text = text;
            image     = new Image(GUI.class.getResourceAsStream(String.format("images/%s.png", imageName)));
        }

        public Image getImage() {
            return image;
        }

        public String getText() {
            return text;
        }

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

        void updated(List<T> added, List<T> removed, Map<Integer, Action> orderChanges);

        default void updated(List<T> added, List<T> removed) {
            updated(added, removed, Collections.emptyMap());
        }

        default void updated(Map<Integer, Action> changes) {
            updated(Collections.emptyList(), Collections.emptyList(), changes);
        }

    }

    public interface ARunnable {

        void run(MeasureAction action) throws Exception;

        default void runRegardless(MeasureAction action) {

            try {
                run(action);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    public static class Action {

        private final SRunnable                    runnable;
        private final SimpleObjectProperty<Status> status    = new SimpleObjectProperty<>(Status.NOT_STARTED);
        private final List<Listener<Status>>       listeners = new LinkedList<>();
        private final Map<String, String>          variables = new LinkedHashMap<>();
        private final Property<String>             name      = new SimpleObjectProperty<>();
        private       Exception                    exception = null;
        private       Thread                       runThread = null;
        private       ResultTable                  data      = null;

        public Action(String name, SRunnable runnable) {

            this.name.setValue(name);
            this.runnable = runnable;

            status.addListener((v, o, n) -> statusExecutor.submit(() -> {
                for (Listener<Status> listener : listeners) {
                    listener.updated(o, n);
                }
            }));

        }


        public Property<String> nameProperty() {
            return name;
        }

        public String getName() {
            return name.getValue();
        }

        public void setName(String name) {
            this.name.setValue(name);
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
                status.set(getStatus() == Status.NOT_STARTED ? Status.RUNNING : Status.RETRY);
                runnable.run();
                status.set(Status.COMPLETED);
            } catch (InterruptedException e) {
                status.set(Status.INTERRUPTED);
                exception = e;
            } catch (Exception e) {
                status.set(Status.ERROR);
                e.printStackTrace();
                exception = e;
            }

        }

        public void stop() {

            if (runThread != null) {
                runThread.interrupt();
            }

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
        private       StringReturnable    resultPath = null;

        public MeasureAction(String name, Measurement measurement, ARunnable before, ARunnable after) {

            super(name, measurement::start);

            this.measurement = measurement;
            this.before      = before;
            this.after       = after;

        }

        public String getName() {

            if (getVariables().isEmpty()) {
                return String.format("%s (%s)", measurement.getName(), super.getName());
            } else {
                return String.format("%s (%s) (%s)", measurement.getName(), super.getName(), getVariableString());
            }

        }

        public String getVariablePathString() {

            List<String> parts = new LinkedList<>();
            getVariables().forEach((name, value) -> parts.add(String.format("%s=%s", name.replace(" ", ""), value.replace(" ", ""))));
            Collections.reverse(parts);
            return String.join("-", parts);

        }

        public String getResultsPath() {
            return resultPath.getValue();
        }

        public void setResultsPath(String path) {
            resultPath = () -> path;
        }

        public void setResultsPath(StringReturnable pathGenerator) {
            resultPath = pathGenerator;
        }

        public void doNotOutputResults() {
            resultPath = null;
        }

        public void setAttribute(String name, Object value) {
            attributes.put(name, value.toString());
            if (getData() != null) {
                getData().setAttribute(name, value.toString());
            }
        }

        public void stop() {
            measurement.stop();
            super.stop();
        }

        public void prepare() {

            if (resultPath == null) {

                setData(measurement.newResults());

            } else {

                String resultPath = this.resultPath.getValue();

                try {

                    String resPath;

                    if (resultPath.contains("%s")) {
                        resPath = String.format(resultPath, getVariablePathString());
                    } else {
                        resPath = resultPath;
                    }

                    String[] parts = resPath.split("\\.");
                    String   last  = parts[parts.length - 2];

                    for (int i = 1; Files.exists(Path.of(resPath)); i++) {
                        parts[parts.length - 2] = String.format("%s (%d)", last, i);
                        resPath                 = String.join(".", parts);
                    }

                    setData(measurement.newResults(resPath));

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

            MeasureAction action = new MeasureAction(nameProperty().getValue(), measurement, before, after);
            action.attributes.putAll(attributes);
            action.getVariables().putAll(getVariables());
            action.resultPath = resultPath;
            return action;

        }

        public interface StringReturnable {

            String getValue();

        }

    }

    public static class WaitAction extends Action {

        public WaitAction(long millis) {

            super(String.format("Wait %s", Util.msToString(millis)), () -> Thread.sleep(millis));
        }

    }

}
