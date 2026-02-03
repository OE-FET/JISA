package jisa.queue;

import java.util.*;

public class SweepAction<SweepValue> implements Action {

    private final String                         name;
    private final IterationGenerator<SweepValue> sweepGenerator;
    private final DataGenerator<SweepValue>      dataGenerator;
    private final List<Action>                   sweepActions         = new LinkedList<>();
    private final List<SweepValue>               sweepValues          = new LinkedList<>();
    private final List<Throwable>                errors               = new LinkedList<>();
    private final List<StatusListener>           statusListeners      = new LinkedList<>();
    private final List<SweepActionListener>      sweepActionListeners = new LinkedList<>();
    private final Map<String, Object>            data                 = new LinkedHashMap<>();
    private       boolean                        critical             = false;
    private       Status                         status               = Status.QUEUED;
    private       String                         message              = "";

    public SweepAction(String name, IterationGenerator<SweepValue> sweepGenerator, DataGenerator<SweepValue> dataGenerator) {
        this.name           = name;
        this.sweepGenerator = sweepGenerator;
        this.dataGenerator  = dataGenerator;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Status run() {

        errors.clear();
        setStatus(Status.RUNNING);

        for (SweepValue sweepValue : sweepValues) {

            List<Action>        actions   = sweepGenerator.generateActions(sweepValue, sweepActions);
            Map<String, Object> sweepData = dataGenerator.generateData(sweepValue);

            actions.forEach(Action::reset);
            actions.forEach(action -> data.forEach(action::setData));
            actions.forEach(action -> sweepData.forEach(action::setData));

            for (Action action : actions) {

                switch (action.run()) {

                    case CRITICAL_ERROR:
                        action.getErrors().stream().map(e -> new SweepException(sweepValue, action, e)).forEach(errors::add);
                        setStatus(Status.CRITICAL_ERROR);
                        return Status.CRITICAL_ERROR;

                    case ERROR:
                        action.getErrors().stream().map(e -> new SweepException(sweepValue, action, e)).forEach(errors::add);
                        break;

                    case INTERRUPTED:
                        setStatus(Status.INTERRUPTED);
                        return Status.INTERRUPTED;

                }

            }

        }

        setStatus(errors.isEmpty() ? Status.SUCCESS : Status.ERROR);
        return getStatus();

    }

    public synchronized List<Action> getSweepActions() {
        return List.copyOf(sweepActions);
    }

    protected void triggerSweepActionListeners() {
        sweepActionListeners.forEach(l -> l.changed(sweepActions));
    }

    public synchronized void clearSweepActions() {
        sweepActions.clear();
        triggerSweepActionListeners();
    }

    public synchronized void setSweepActions(Collection<Action> sweepActions) {
        this.sweepActions.clear();
        this.sweepActions.addAll(sweepActions);
        triggerSweepActionListeners();
    }

    public synchronized void addSweepAction(Action sweepAction) {
        sweepActions.add(sweepAction);
        triggerSweepActionListeners();
    }

    public void removeSweepAction(Action sweepAction) {
        sweepActions.remove(sweepAction);
        triggerSweepActionListeners();
    }

    @Override
    public List<Throwable> getErrors() {
        return List.copyOf(errors);
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

    @Override
    public void setData(String key, Object data) {
        this.data.put(key, data);
    }

    @Override
    public <D> D getData(String key, Class<D> type) {
        return (D) data.get(key);
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
        setStatus(Status.QUEUED);
        errors.clear();
        sweepActions.forEach(Action::reset);
    }

    public interface IterationGenerator<SweepValue> {
        List<Action> generateActions(SweepValue sweepValue, List<Action> sweepActions);
    }

    public interface DataGenerator<SweepValue> {
        Map<String, Object> generateData(SweepValue sweepValue);
    }

    public interface SweepActionListener {
        void changed(List<Action> actions);
    }
}
