package jisa.queue;

import jisa.experiment.Measurement;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MeasurementAction<M extends Measurement> implements Action {

    private final M                    measurement;
    private final DataHandler<M>       dataHandler;
    private final List<StatusListener> statusListeners = new LinkedList<>();
    private final Map<String, Object>  data            = new LinkedHashMap<>();

    private Status status  = Status.QUEUED;
    private String message = "";

    public MeasurementAction(M measurement, DataHandler<M> dataHandler) {

        this.measurement = measurement;
        this.dataHandler = dataHandler;

        measurement.addStatusListener(status -> {

            switch (status) {

                case STOPPED:
                    setStatus(Status.QUEUED);
                    break;

                case RUNNING:
                case POST_RUN:
                    setStatus(Status.RUNNING);
                    break;

                case INTERRUPTED:
                    setStatus(Status.INTERRUPTED);
                    break;

                case ERROR:
                    setStatus(Status.ERROR);
                    break;

                case COMPLETE:
                    setStatus(Status.SUCCESS);
                    break;

            }

        });

        measurement.addMessageListener((type, message) -> {

            if (type == Measurement.MessageType.INFO) {
                setStatusMessage(message);
            } else {
                setStatusMessage(String.format("%s: %s", type, message));
            }

        });

    }

    protected synchronized void setStatus(Status status) {
        this.status = status;
        statusListeners.forEach(l -> l.statusChanged(status, message));
    }

    protected synchronized void setStatusMessage(String message) {
        this.message = message;
        statusListeners.forEach(l -> l.statusChanged(status, message));
    }

    @Override
    public String getName() {
        return String.format("%s (%s)", measurement.getName(), measurement.getLabel());
    }

    public M getMeasurement() {
        return measurement;
    }

    @Override
    public Status run() {

        measurement.reset();
        dataHandler.handleData(measurement, data);

        Measurement.Result result = measurement.run();

        switch (result.getType()) {

            case SUCCESS:
                setStatus(Status.SUCCESS);
                return Status.SUCCESS;

            case ERROR:
                setStatus(Status.ERROR);
                return Status.ERROR;

            case INTERRUPTED:
                setStatus(Status.INTERRUPTED);
                return Status.INTERRUPTED;

            default:
                setStatus(Status.QUEUED);
                return Status.QUEUED;

        }

    }

    @Override
    public List<Throwable> getErrors() {
        return measurement.getExceptions();
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public String getStatusMessage() {
        return message;
    }

    @Override
    public StatusListener addStatusListener(StatusListener listener) {
        statusListeners.add(listener);
        return listener;
    }

    @Override
    public void removeStatusListener(StatusListener listener) {
        statusListeners.remove(listener);
    }

    @Override
    public boolean isCritical() {
        return false;
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
    public void reset() {

        measurement.reset();

        status  = Status.QUEUED;
        message = "";

        statusListeners.forEach(l -> l.statusChanged(status, message));

    }

    public interface DataHandler<M extends Measurement> {
        void handleData(M Measurement, Map<String, Object> data);
    }

}
