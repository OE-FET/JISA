package jisa.experiment.queue;

import jisa.experiment.Measurement;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MeasurementAction<M extends Measurement> implements Action {

    private final M                     measurement;
    private final DataHandler<M>        dataHandler;
    private final List<StatusListener>  statusListeners  = new LinkedList<>();
    private final List<MessageListener> messageListeners = new LinkedList<>();
    private final Map<String, Object>   data             = new LinkedHashMap<>();

    private Status status  = Status.QUEUED;

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

    }

    protected synchronized void setStatus(Status status) {
        this.status = status;
        statusListeners.forEach(l -> l.statusChanged(status));
    }

    @Override
    public String getName() {
        return String.format("%s (%s)", measurement.getName(), measurement.getLabel());
    }

    public M getMeasurement() {
        return measurement;
    }

    @Override
    public Result run() {

        // Initialise measurement
        measurement.reset();
        dataHandler.handleData(measurement, data);
        measurement.newData();

        // Record messages from measurement routine
        List<Message>        messages = new LinkedList<>();
        ActionPathPart<Void> pathPart = new ActionPathPart<>(this, null);

        Measurement.MessageListener listener = measurement.addMessageListener((type, message) -> {

            Message newMessage;

            switch (type) {

                case WARNING:
                    newMessage = new Message(MessageType.WARNING, message, null, List.of(pathPart));
                    break;

                case ERROR:
                    newMessage = new Message(MessageType.ERROR, message, null, List.of(pathPart));
                    break;

                case INFO:
                default:
                    newMessage = new Message(MessageType.INFO, message, null, List.of(pathPart));
                    break;

            }

            messages.add(newMessage);
            messageListeners.forEach(l -> l.newMessage(newMessage));

        });

        Message startMessage = new Message(MessageType.INFO, getName() + " Started", null, List.of(pathPart));
        messageListeners.forEach(l -> l.newMessage(startMessage));
        messages.add(startMessage);

        // Run measurement
        Measurement<?>.Result result = measurement.run();

        measurement.removeMessageListener(listener);

        result.getExceptions().stream().map(e -> new Message(Action.MessageType.ERROR, e.getMessage(), e, List.of(new ActionPathPart(this, null)))).forEach(m -> {
            messages.add(m);
            messageListeners.forEach(l -> l.newMessage(m));
        });

        Message endMessage = new Message(MessageType.INFO, getName() + " Finished", null, List.of(pathPart));
        messageListeners.forEach(l -> l.newMessage(endMessage));
        messages.add(endMessage);

        // Determine what to return based on the measurement result type
        switch (result.getType()) {

            case SUCCESS:
                setStatus(Status.SUCCESS);
                return new Result(Status.SUCCESS, messages);

            case ERROR:
                setStatus(Status.ERROR);
                result.getExceptions().stream().map(e -> new Message(Action.MessageType.ERROR, e.getMessage(), e, List.of(new ActionPathPart(this, null)))).forEach(messages::add);
                return new Result(Status.ERROR, messages);

            case INTERRUPTED:
                setStatus(Status.INTERRUPTED);
                return new Result(Status.INTERRUPTED, messages);

            default:
                setStatus(Status.QUEUED);
                return new Result(Status.QUEUED, messages);

        }

    }

    @Override
    public Status getStatus() {
        return status;
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
    public MessageListener addMessageListener(MessageListener listener) {
        messageListeners.add(listener);
        return listener;
    }

    @Override
    public void removeMessageListener(MessageListener listener) {
        messageListeners.remove(listener);
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
        setStatus(Status.QUEUED);

    }

    public interface DataHandler<M extends Measurement> {
        void handleData(M Measurement, Map<String, Object> data);
    }

}
