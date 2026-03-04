package jisa.queue;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

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
    Result run();

    /**
     * Returns the current status of this action.
     *
     * @return Status, enum.
     */
    Status getStatus();

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

    MessageListener addMessageListener(MessageListener listener);

    void removeMessageListener(MessageListener listener);

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
        void statusChanged(Status status);
    }

    class Result {

        private final Status        finalStatus;
        private final List<Message> messages;

        public Result(Status finalStatus, List<Message> messages) {
            this.finalStatus = finalStatus;
            this.messages    = messages;
        }

        public Result(Status finalStatus) {
            this(finalStatus, Collections.emptyList());
        }

        public Status getFinalStatus() {
            return finalStatus;
        }

        public List<Message> getMessages() {
            return messages;
        }

        public List<Message> getErrors() {
            return messages.stream().filter(m -> m.getType() == MessageType.ERROR).collect(Collectors.toList());
        }

    }

    class Message {

        private final long                 timestamp;
        private final MessageType          type;
        private final String               message;
        private final Throwable            exception;
        private final List<ActionPathPart> actionPath;

        public Message(long timestamp, MessageType type, String message, Throwable exception, List<ActionPathPart> actionPath) {
            this.timestamp  = timestamp;
            this.type       = type;
            this.message    = message;
            this.exception  = exception;
            this.actionPath = actionPath;
        }

        public Message(MessageType type, String message, Throwable exception, List<ActionPathPart> actionPath) {
            this(System.currentTimeMillis(), type, message, exception, actionPath);
        }

        public long getTimestamp() {
            return timestamp;
        }

        public MessageType getType() {
            return type;
        }

        public String getMessage() {
            return message;
        }

        public Throwable getException() {
            return exception;
        }

        public List<ActionPathPart> getActionPath() {
            return actionPath;
        }

        public Message propagate(ActionPathPart actionPathPart) {

            List<ActionPathPart> path = new LinkedList<>();

            path.add(actionPathPart);
            path.addAll(actionPath);

            return new Message(timestamp, type, message, exception, path);

        }

    }

    enum MessageType {
        INFO,
        WARNING,
        ERROR
    }

    class ActionPathPart<T> {

        private final Action action;
        private final T      sweepValue;

        public ActionPathPart(Action action, T sweepValue) {
            this.action     = action;
            this.sweepValue = sweepValue;
        }

        public Action getAction() {
            return action;
        }

        public T getSweepValue() {
            return sweepValue;
        }

        public String toString() {

            if (sweepValue == null) {
                return action.getName();
            } else {
                return String.format("%s (%s)", action.getName(), sweepValue);
            }

        }

    }

    interface MessageListener {
        void newMessage(Message message);
    }

}
