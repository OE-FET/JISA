package jisa.experiment.queue;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SimpleAction implements Action {

    private final ActionRunner          action;
    private final String                name;
    private final List<StatusListener>  statusListeners  = new LinkedList<>();
    private final List<MessageListener> messageListeners = new LinkedList<>();
    private final Map<String, Object>   data             = new LinkedHashMap<>();
    private       boolean               critical         = false;
    private       Status                status           = Status.QUEUED;

    public SimpleAction(String name, ActionRunner action) {
        this.name   = name;
        this.action = action;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Result run() {

        List<Message>   messages = new LinkedList<>();
        MessageListener listener = addMessageListener(messages::add);

        try {

            message(MessageType.INFO, name + " Started");
            setStatus(Status.RUNNING);

            action.run(this);
            setStatus(Status.SUCCESS);
            return new Result(Status.SUCCESS, messages);

        } catch (InterruptedException ex) {

            setStatus(Status.INTERRUPTED);
            return new Result(Status.INTERRUPTED, messages);

        } catch (Throwable ex) {

            setStatus(isCritical() ? Status.CRITICAL_ERROR : Status.ERROR);

            Message message = new Message(Action.MessageType.ERROR, ex.getMessage(), ex, List.of(new ActionPathPart(this, null)));

            messages.add(message);
            messageListeners.forEach(l -> l.newMessage(message));

            return new Result(getStatus(), messages);

        } finally {
            message(MessageType.INFO, name + " Finished");
        }

    }

    public void message(MessageType type, String message) {
        messageListeners.forEach(l -> l.newMessage(new Message(type, message, null, List.of(new ActionPathPart(this, null)))));
    }

    @Override
    public synchronized Status getStatus() {
        return status;
    }

    protected synchronized void setStatus(Status status) {
        this.status = status;
        statusListeners.forEach(l -> l.statusChanged(status));
    }

    @Override
    public synchronized StatusListener addStatusListener(StatusListener listener) {
        statusListeners.add(listener);
        return listener;
    }

    @Override
    public synchronized void removeStatusListener(StatusListener listener) {
        statusListeners.remove(listener);
    }

    @Override
    public boolean isCritical() {
        return critical;
    }

    @Override
    public MessageListener addMessageListener(MessageListener listener) {
        messageListeners.add(listener);
        return listener;
    }

    @Override
    public void removeMessageListener(MessageListener listener) {
        messageListeners.remove(listener);
    }

    public void setCritical(boolean critical) {
        this.critical = critical;
    }

    @Override
    public void setData(String key, Object data) {
        this.data.put(key, data);
    }

    @Override
    public <T> T getData(String key, Class<T> type) {
        return (T) data.get(key);
    }

    @Override
    public boolean hasData(String key) {
        return data.containsKey(key);
    }

    @Override
    public void removeData(String key) {
        data.remove(key);
    }

    @Override
    public synchronized void reset() {
        setStatus(Status.QUEUED);
    }

    public interface ActionRunner {
        void run(SimpleAction action) throws Exception;
    }

}
