package jisa.queue;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SimpleAction implements Action {

    private final ActionRunner         action;
    private final String               name;
    private final List<StatusListener> statusListeners = new LinkedList<>();
    private final Map<String, Object>  data            = new LinkedHashMap<>();
    private       boolean              critical        = false;
    private       Throwable            error           = null;
    private       Status               status          = Status.QUEUED;
    private       String               message         = "";

    public SimpleAction(String name, ActionRunner action) {
        this.name   = name;
        this.action = action;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Status run() {

        error = null;
        setStatus(Status.RUNNING);

        try {
            action.run(this);
            setStatus(Status.SUCCESS);
        } catch (InterruptedException ex) {
            setStatus(Status.INTERRUPTED);
        } catch (Throwable ex) {
            error = ex;
            setStatus(isCritical() ? Status.CRITICAL_ERROR : Status.ERROR);
        }

        return getStatus();

    }

    @Override
    public List<Throwable> getErrors() {
        return List.of(error);
    }

    @Override
    public synchronized Status getStatus() {
        return status;
    }

    protected synchronized void setStatus(Status status) {
        this.status = status;
        statusListeners.forEach(l -> l.statusChanged(status, message));
    }

    @Override
    public synchronized String getStatusMessage() {
        return message;
    }

    public synchronized void setStatusMessage(String message) {
        this.message = message;
        statusListeners.forEach(l -> l.statusChanged(status, message));
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

        status  = Status.QUEUED;
        message = "";

        statusListeners.forEach(l -> l.statusChanged(status, message));

    }

    public interface ActionRunner {
        void run(SimpleAction action) throws Exception;
    }

}
