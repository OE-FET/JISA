package jisa.queue;

import java.util.List;

public interface Action {

    /**
     * Returns the name of this action.
     *
     * @return Name, String.
     */
    String getName();

    /**
     * Runs the action on the current thread.
     */
    Status run();

    /**
     * Returns a list of all exceptions thrown during the last execution of this action.
     *
     * @return List of exceptions.
     */
    List<Throwable> getErrors();

    /**
     * Returns the current status of this action.
     *
     * @return Status, enum.
     */
    Status getStatus();

    /**
     * Returns the current status message of this action.
     *
     * @return Status message, String.
     */
    String getStatusMessage();

    /**
     * Adds a listener that gets called every time the status or status message of this action is changed.
     *
     * @param listener Listener.
     *
     * @return The listener that was added.
     */
    StatusListener addStatusListener(StatusListener listener);

    /**
     * Removes the given listener from this action, if it was indeed attached in the first place.
     *
     * @param listener The listener to remove.
     */
    void removeStatusListener(StatusListener listener);

    /**
     * Returns whether this action is considered "critical" or not. If it is, then it failing will cause an entire ActionQueue to fail.
     *
     * @return Critical?
     */
    boolean isCritical();

    void setData(String key, Object data);

    <T> T getData(String key, Class<T> type);

    default Object getData(String key) {
        return getData(key, Object.class);
    }

    boolean hasData(String key);

    void removeData(String key);

    void reset();

    enum Status {
        QUEUED,
        RUNNING,
        SUCCESS,
        INTERRUPTED,
        ERROR,
        CRITICAL_ERROR,
    }

    interface StatusListener {
        void statusChanged(Status status, String message);
    }

}
