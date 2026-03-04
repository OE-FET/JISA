package jisa.experiment.queue;

import jisa.Util;
import org.json.JSONObject;

import java.io.*;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

public class ActionQueue implements Serializable {

    private final List<Action<?>>               queue         = new LinkedList<>();
    private       ActionQueue                   startActions  = null;
    private       ActionQueue                   stopActions   = null;
    private final List<ListListener<Action<?>>> listListeners = new LinkedList<>();

    private boolean isRunning = false;
    private boolean isStopped = false;

    public ActionQueue() {
        this(true);
    }

    protected ActionQueue(boolean subQueues) {

        if (subQueues) {
            startActions = new ActionQueue(false);
            stopActions  = new ActionQueue(false);
        }

    }

    /**
     * Adds an action to the queue.
     *
     * @param action Action to add
     * @param <T>    Action type
     *
     * @return The action that was added
     */
    public synchronized <T extends Action> T addAction(T action) {
        checkRunning();
        queue.add(action);
        listListeners.forEach(l -> l.added(action));
        return action;
    }

    public void saveActions(String path) throws IOException {
        JSONObject       object = new JSONObject(queue);
        FileOutputStream out    = new FileOutputStream(path);
        out.write(object.toString(4).getBytes());
        out.close();
    }

    public void loadActions(String path) throws IOException, ClassNotFoundException {
        ObjectInputStream in     = new ObjectInputStream(new FileInputStream(path));
        List<Action<?>>   loaded = (List<Action<?>>) in.readObject();
        addActions(loaded);
        in.close();
    }

    /**
     * Adds all the given actions to the queue.
     *
     * @param actions Actions to add
     */
    public synchronized void addActions(Action... actions) {
        addActions(List.of(actions));
    }

    /**
     * Adds all the given actions to the queue.
     *
     * @param actions Actions to add
     */
    public synchronized void addActions(Collection<Action<?>> actions) {
        checkRunning();
        queue.addAll(actions);
        listListeners.forEach(l -> l.added(actions));
    }

    /**
     * Removes the supplied action from the queue.
     *
     * @param action Action to remove
     */
    public synchronized void removeAction(Action action) {
        checkRunning();
        queue.remove(action);
        listListeners.forEach(l -> l.removed(action));
    }

    /**
     * Removes all the supplied actions from the queue.
     *
     * @param actions Actions to remove
     */
    public synchronized void removeActions(Action... actions) {
        removeActions(List.of(actions));
    }

    /**
     * Removes all the supplied actions from the queue.
     *
     * @param actions Actions to remove
     */
    public synchronized void removeActions(Collection<Action<?>> actions) {
        checkRunning();
        queue.removeAll(actions);
        listListeners.forEach(l -> l.removed(actions));
    }

    /**
     * Returns the number of actions in the queue.
     *
     * @return Number of actions in queue
     */
    public int size() {
        return queue.size();
    }

    /**
     * Returns the index of a given action in the queue
     *
     * @param action Action to find index of
     *
     * @return Index of action
     */
    public int indexOf(Action<?> action) {
        return queue.indexOf(action);
    }

    /**
     * Swaps the positions of two actions in the queue.
     *
     * @param indexA Index of action to swap
     * @param indexB Index of action to swap
     */
    public synchronized void swapActions(int indexA, int indexB) {

        checkRunning();

        if (!Util.isBetween(indexA, 0, queue.size() - 1) || !Util.isBetween(indexB, 0, queue.size() - 1)) {
            throw new IllegalArgumentException("Both actions must be in the queue before their positions can be swapped.");
        }

        Action<?> a = queue.get(indexA);
        Action<?> b = queue.get(indexB);

        queue.set(indexA, b);
        queue.set(indexB, a);

        Map<Integer, Action<?>> moves = Map.of(
            indexA, b,
            indexB, a
        );

        listListeners.forEach(l -> l.moved(moves));

    }

    /**
     * Swaps the positions of two actions in the queue.
     *
     * @param a Action to swap
     * @param b Action to swap
     */
    public synchronized void swapActions(Action a, Action b) {

        checkRunning();

        int indexA = queue.indexOf(a);
        int indexB = queue.indexOf(b);

        if (indexA < 0 || indexB < 0) {
            throw new IllegalArgumentException("Both actions must be in the queue before their positions can be swapped.");
        }

        queue.set(indexA, b);
        queue.set(indexB, a);

        Map<Integer, Action<?>> moves = Map.of(
            indexA, b,
            indexB, a
        );

        listListeners.forEach(l -> l.moved(moves));

    }

    public List<Action<?>> getActions() {
        return List.copyOf(queue);
    }

    /**
     * Removes all actions from the queue.
     */
    public synchronized void clear() {
        checkRunning();
        List<Action<?>> removed = List.copyOf(queue);
        queue.clear();
        listListeners.forEach(l -> l.removed(removed));
    }

    /**
     * Adds a listener to be triggered every time the queue is changed (actions added, removed or changed order).
     *
     * @param listener Listener to add
     *
     * @return The listener that was added
     */
    public ListListener<Action<?>> addListener(ListListener<Action<?>> listener) {
        listListeners.add(listener);
        return listener;
    }

    /**
     * Removes a listener from the queue.
     *
     * @param listener The listener to be removed
     */
    public void removeListener(ListListener<Action> listener) {
        listListeners.remove(listener);
    }

    /**
     * Internal method for checking whether the queue is running before making queue alterations. Throws an
     * IllegalStateException if it is running.
     *
     * @throws IllegalStateException If the queue is running
     */
    protected void checkRunning() throws IllegalStateException {

        if (isRunning) {
            throw new IllegalStateException("Cannot modify action queue: queue is running.");
        }

    }

    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Returns whether the last run of the queue was interrupted or not.
     *
     * @return Was it interrupted?
     */
    public boolean isInterrupted() {
        return queue.stream().anyMatch(a -> a.getStatus() == Action.Status.INTERRUPTED);
    }

    public Action<?> getInterruptedAction() {
        return this.queue.stream().filter(a -> a.getStatus() == Action.Status.INTERRUPTED).findFirst().orElse(null);
    }

    /**
     * Starts the queue from the beginning. Returns the overall result of the queue once completed.
     *
     * @return Result of queue execution (SUCCESS, INTERRUPTED or ERROR)
     */
    public Result start() {
        return start(false);
    }

    /**
     * Starts the queue from the last interrupted action.
     *
     * @return Result of queue execution (SUCCESS, INTERRUPTED or ERROR)
     */
    public Result resume() {
        return start(true);
    }

    public ActionQueue getStartActions() {
        return startActions;
    }

    public ActionQueue getStopActions() {
        return stopActions;
    }

    protected Result start(boolean resume) {

        isRunning = true;
        isStopped = false;

        List<Action<?>> queue;

        if (resume && isInterrupted()) {
            int start = this.queue.indexOf(getInterruptedAction());
            queue = this.queue.subList(start, this.queue.size());
        } else {
            resume = false;
            queue  = this.queue;
        }

        queue.forEach(Action::reset);

        if (startActions != null) {
            startActions.start(false);
        }

        for (Action action : queue) {

            if (isStopped) {
                action.setStatus(Action.Status.INTERRUPTED);
                isRunning = false;
                return Result.INTERRUPTED;
            }

            if (resume) {
                resume = false;
                action.resume();
            } else {
                action.start();
            }

            switch (action.getStatus()) {

                case INTERRUPTED:

                    if (stopActions != null) {
                        stopActions.start(false);
                    }

                    isRunning = false;
                    return Result.INTERRUPTED;

                case ERROR:

                    if (action.isCritical()) {

                        if (stopActions != null) {
                            stopActions.start(false);
                        }

                        isRunning = false;
                        return Result.ERROR;

                    }
                    break;

            }

        }

        if (stopActions != null) {
            stopActions.start(false);
        }

        isRunning = false;

        if (queue.stream().anyMatch(a -> a.getStatus() == Action.Status.ERROR)) {
            return Result.ERROR;
        } else {
            return Result.SUCCESS;
        }

    }

    public void stop() {
        isStopped = true;
        queue.forEach(Action::stop);
    }

    public enum Result {
        SUCCESS,
        INTERRUPTED,
        ERROR
    }

    public interface ListListener<T> {

        void update(Collection<T> added, Collection<T> removed, Map<Integer, T> moved);

        default void moved(Map<Integer, T> changes) {
            update(emptyList(), emptyList(), changes);
        }

        default void added(T added) {
            added(List.of(added));
        }

        default void removed(T removed) {
            removed(List.of(removed));
        }

        default void added(Collection<T> added) {
            update(added, emptyList(), emptyMap());
        }

        default void removed(Collection<T> removed) {
            update(emptyList(), removed, emptyMap());
        }

        default void addedAndRemoved(Collection<T> added, Collection<T> removed) {
            update(added, removed, emptyMap());
        }

    }

}
