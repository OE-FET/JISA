package jisa.experiment.queue;

import jisa.experiment.MeasurementOld;
import jisa.gui.queue.SweepActionDisplay;

import java.util.*;
import java.util.stream.Collectors;

public class SweepAction<T> extends AbstractAction<Void> {

    private       int                lastIndex;
    private Action         lastAction;
    private MeasurementOld sweepMeasure = null;
    private boolean        isRunning    = false;
    private       Formatter<T>       formatter      = String::valueOf;
    private       boolean            isStopped      = false;
    private final List<Listener<T>>  valueListeners = new LinkedList<>();
    private final List<T>            sweepValues    = new LinkedList<>();
    private final List<Action>       subActions     = new LinkedList<>();
    private final List<Action>       finalActions   = new LinkedList<>();
    private final List<List<Action>> children       = new LinkedList<>();
    private final List<Exception>    exceptions     = new LinkedList<>();
    private final ActionGenerator<T> generator;

    public SweepAction(String name, Iterable<T> sweepValues, ActionGenerator<T> generator) {
        setName(name);
        sweepValues.forEach(this.sweepValues::add);
        this.generator = generator;
        regenerateActions();
    }

    /**
     * Adds a listener that is triggered every time the current sweep value is changed.
     *
     * @param listener Listener to add
     *
     * @return The listener that was added
     */
    public Listener<T> addSweepValueListener(Listener<T> listener) {
        valueListeners.add(listener);
        return listener;
    }

    /**
     * Removes the specified listener from this action - if it was added to begin with.
     *
     * @param listener The listener to remove
     */
    public void removeSweepValueListener(Listener listener) {
        valueListeners.remove(listener);
    }

    @Override
    public void reset() {
        setStatus(Status.NOT_STARTED);
        children.stream().flatMap(List::stream).forEach(Action::reset);
    }

    /**
     * Returns an unmodifiable list of values that this action will sweep over.
     *
     * @return Sweep values
     */
    public List<T> getSweepValues() {
        return List.copyOf(sweepValues);
    }

    /**
     * Returns an unmodifiable list of the string representations of the values this action will sweep over.
     *
     * @return String representation of sweep values
     */
    public List<String> getSweepStrings() {
        return sweepValues.stream().map(formatter::format).collect(Collectors.toUnmodifiableList());
    }

    /**
     * Sets the values for this action to sweep over.
     *
     * @param values Sweep values to use
     */
    public void setSweepValues(T... values) {
        sweepValues.clear();
        sweepValues.addAll(List.of(values));
        regenerateActions();
    }

    /**
     * Sets the values for this action to sweep over.
     *
     * @param values Sweep values to use
     */
    public void setSweepValues(Iterable<T> values) {
        sweepValues.clear();
        values.forEach(sweepValues::add);
        regenerateActions();
    }

    /**
     * Generates the set of actions for the iteration of the sweep with the given value.
     *
     * @param value Value of iteration
     *
     * @return List of actions
     */
    public List<Action> generateActionsForValue(T value) {

        List<Action<?>> copies = new LinkedList<>();

        for (Action<?> action : subActions) {

            Action<?> copy = action.copy();
            action.addNameListener(it -> copy.setName(it.getName()));
            copies.add(copy);

        }

        return generator.generate(value, copies);

    }

    /**
     * Returns the current value of the swept variable.
     *
     * @return Current sweep value
     */
    public int getCurrentSweepIndex() {
        return lastIndex;
    }

    /**
     * Returns the string representation of the current value of the swept variable.
     *
     * @return String representation of current sweep value
     */
    public String getCurrentSweepString() {
        return format(sweepValues.get(lastIndex));
    }

    public void setAttribute(String key, String value) {
        super.setAttribute(key, value);
        children.stream().flatMap(List::stream).forEach(it -> it.setAttribute(key, value));
    }

    @Override
    public void addTag(String tag) {
        super.addTag(tag);
        children.stream().flatMap(List::stream).forEach(it -> it.addTag(tag));
    }

    @Override
    public void removeTag(String tag) {
        super.removeTag(tag);
        children.stream().flatMap(List::stream).forEach(it -> it.removeTag(tag));
    }

    public void clearTags() {
        super.clearTags();
        children.stream().flatMap(List::stream).forEach(Action::clearTags);
    }

    /**
     * Specifies how the sweep variable should be converted to a string.
     *
     * @param formatter Formatter to use
     */
    public void setFormatter(Formatter<T> formatter) {
        this.formatter = formatter;
        regenerateActions();
    }

    public String format(T value) {
        return formatter.format(value);
    }

    /**
     * Regenerates all the sub-actions for this sweep.
     */
    protected synchronized void regenerateActions() {

        children.clear();
        lastIndex = 0;

        for (T value : sweepValues) {
            children.add(generateActionsForValue(value));
        }

        childrenChanged();
        valueListeners.forEach(l -> l.updateRegardless(sweepValues.get(lastIndex)));

    }

    public void resume() {
        start(true);
    }

    @Override
    public void start() {
        start(false);
    }

    protected void start(boolean resume) {

        isRunning = true;
        isStopped = false;
        setStatus(Status.RUNNING);

        boolean failed = false;

        onStart();

        int start = 0;

        if (resume && lastIndex >= 0 && lastIndex < sweepValues.size()) {
            start = lastIndex;
        } else {
            resume = false;
        }

        for (int i = 0; i < sweepValues.size(); i++) {

            lastIndex = i;

            T            value   = sweepValues.get(i);
            List<Action> actions = children.get(i);

            valueListeners.forEach(l -> l.updateRegardless(value));

            if (resume && actions.contains(lastAction)) {
                actions = getChildrenByValue(value).subList(actions.indexOf(lastAction), actions.size());
            } else {
                resume = false;
            }

            for (Action action : actions) {

                lastAction = action;
                setCritical(action.isCritical());

                if (isStopped) {
                    action.setStatus(Status.INTERRUPTED);
                    setStatus(Status.INTERRUPTED);
                    isRunning = false;
                    runFinalActions();
                    onFinish();
                    return;
                }

                if (resume) {
                    action.resume();
                    resume = false;
                } else {
                    action.start();
                }

                if (action.getStatus() == Status.INTERRUPTED) {
                    setStatus(Status.INTERRUPTED);
                    isRunning = false;
                    runFinalActions();
                    onFinish();
                    return;
                }

                if (action.getStatus() == Status.ERROR) {

                    failed = true;

                    if (action.isCritical()) {
                        setStatus(Status.ERROR);
                        isRunning = false;
                        runFinalActions();
                        onFinish();
                        return;
                    }

                }

            }

        }

        isRunning = false;

        setStatus(failed ? Status.ERROR : Status.COMPLETED);

        runFinalActions();

        onFinish();

    }

    @Override
    public void stop() {

        isStopped = true;

        while (lastAction.isRunning()) {
            lastAction.stop();
        }

    }

    @Override
    public void skip() {
        lastAction.skip();
    }

    protected void runFinalActions() {
        finalActions.forEach(Action::start);
    }

    @Override
    public Exception getError() {
        return new MultiException(exceptions);
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public Void getData() {
        return null;
    }

    public synchronized <R extends Action> R addAction(R action) {
        subActions.add(action);
        action.addChildrenListener(it -> regenerateActions());
        regenerateActions();
        return action;
    }

    public synchronized void addActions(Collection<Action> actions) {
        subActions.addAll(actions);
        actions.forEach(a -> a.addChildrenListener(it -> regenerateActions()));
        regenerateActions();
    }

    public synchronized void removeAction(Action action) {
        subActions.remove(action);
        regenerateActions();
    }

    public synchronized void removeActions(Collection<Action> actions) {
        subActions.removeAll(actions);
        regenerateActions();
    }

    public synchronized void clearActions() {
        subActions.clear();
        regenerateActions();
    }

    public List<Action> getActions() {
        return List.copyOf(subActions);
    }

    @Override
    public List<Action> getChildren() {
        return children.stream().flatMap(List::stream).collect(Collectors.toList());
    }

    /**
     * Returns an unmodifiable list of the actions in the iteration of the sweep with the given value.
     *
     * @param value Value
     *
     * @return List of actions
     */
    public List<Action> getChildrenByValue(T value) {

        int index = sweepValues.indexOf(value);

        if (index < 0) {
            return Collections.emptyList();
        }

        return List.copyOf(children.get(index));

    }

    public List<Action> getChildrenByIndex(int index) {
        return List.copyOf(children.get(index));
    }

    public List<Action> getFinalActions() {
        return List.copyOf(finalActions);
    }

    public synchronized <R extends Action> R addFinalAction(R action) {
        finalActions.add(action);
        action.addChildrenListener(it -> regenerateActions());
        childrenChanged();
        return action;
    }

    public synchronized void addFinalActions(Collection<Action> actions) {
        finalActions.addAll(actions);
        actions.forEach(a -> a.addChildrenListener(it -> regenerateActions()));
        childrenChanged();
    }

    public synchronized void removeFinalAction(Action action) {
        finalActions.remove(action);
        childrenChanged();
    }

    public synchronized void clearFinalActions() {
        finalActions.clear();
        childrenChanged();
    }

    @Override
    public SweepActionDisplay<T> getDisplay() {
        return new SweepActionDisplay<T>(this);
    }

    public void setMeasurement(MeasurementOld measure) {
        sweepMeasure = measure;
    }

    public MeasurementOld getMeasurement() {
        return sweepMeasure;
    }

    @Override
    public SweepAction<T> copy() {

        SweepAction<T> copy = new SweepAction<>(getName(), getSweepValues(), generator);
        getAttributes().forEach(copy::setAttribute);
        copy.addActions(subActions);

        return copy;

    }

    public static class MultiException extends Exception {

        private final List<Exception> exceptions;

        public MultiException(Exception... exceptions) {
            super(Arrays.stream(exceptions).map(Exception::getMessage).collect(Collectors.joining(", ")));
            this.exceptions = List.of(exceptions);
        }

        public MultiException(Collection<Exception> exceptions) {
            super(exceptions.stream().map(Exception::getMessage).collect(Collectors.joining(", ")));
            this.exceptions = List.copyOf(exceptions);
        }

        public List<Exception> getExceptions() {
            return exceptions;
        }

    }

    public interface ActionGenerator<T> {
        List<Action> generate(T value, List<Action<?>> actions);
    }

    public interface Formatter<T> {
        String format(T value);
    }

}
