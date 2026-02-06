package jisa.experiment.queue;

import java.util.*;

public class SweepAction<SweepValue> implements Action {

    private final String                         name;
    private final IterationGenerator<SweepValue> sweepGenerator;
    private final DataGenerator<SweepValue>      dataGenerator;
    private final List<Action>                   sweepActions         = new LinkedList<>();
    private final List<Action>                   currentActions       = new LinkedList<>();
    private final List<SweepValue>               sweepValues          = new LinkedList<>();
    private final List<StatusListener>           statusListeners      = new LinkedList<>();
    private final List<SweepActionListener>      sweepActionListeners = new LinkedList<>();
    private final List<MessageListener>          messageListeners     = new LinkedList<>();
    private final Map<String, Object>            data                 = new LinkedHashMap<>();
    private       boolean                        critical             = false;
    private       Status status            = Status.QUEUED;
    private       int    currentSweepIndex = 0;

    public SweepAction(String name, IterationGenerator<SweepValue> sweepGenerator, DataGenerator<SweepValue> dataGenerator, Collection<SweepValue> sweepValues) {
        this.name           = name;
        this.sweepGenerator = sweepGenerator;
        this.dataGenerator  = dataGenerator;
        this.sweepValues.addAll(sweepValues);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Result run() {

        List<Message> messages = new LinkedList<>();

        Message startMessage = new Message(MessageType.INFO, "Starting Sweep", null, List.of(new ActionPathPart(this, null)));
        messages.add(startMessage);
        messageListeners.forEach(l -> l.newMessage(startMessage));

        setStatus(Status.RUNNING);

        currentSweepIndex = -1;

        for (SweepValue sweepValue : sweepValues) {

            currentSweepIndex++;

            Message sweepMessage = new Message(MessageType.INFO, String.format("Sweep Value = %s (%d / %d)", sweepValue, currentSweepIndex + 1, sweepValues.size()), null, List.of(new ActionPathPart(this, sweepValue)));
            messages.add(sweepMessage);
            messageListeners.forEach(l -> l.newMessage(sweepMessage));

            currentActions.clear();
            currentActions.addAll(generateActions(sweepValue));

            triggerSweepActionListeners();

            Map<String, Object> sweepData = dataGenerator.generateData(sweepValue);

            currentActions.forEach(Action::reset);
            currentActions.forEach(action -> data.forEach(action::setData));
            currentActions.forEach(action -> sweepData.forEach(action::setData));

            for (Action action : currentActions) {

                MessageListener messageListener = action.addMessageListener(m -> {
                    Message message = m.propagate(new ActionPathPart(this, sweepValue));
                    messages.add(message);
                    messageListeners.forEach(l -> l.newMessage(message));
                });

                Result result = action.run();

                action.removeMessageListener(messageListener);

                switch (result.getFinalStatus()) {

                    case CRITICAL_ERROR:
                        setStatus(Status.CRITICAL_ERROR);
                        return new Result(getStatus(), messages);

                    case INTERRUPTED:
                        setStatus(Status.INTERRUPTED);
                        return new Result(getStatus(), messages);

                }

            }

        }

        setStatus(messages.stream().noneMatch(m -> m.getType() == MessageType.ERROR) ? Status.SUCCESS : Status.ERROR);

        Message endMessage = new Message(MessageType.INFO, "Sweep Finished", null, List.of(new ActionPathPart(this, null)));
        messages.add(endMessage);
        messageListeners.forEach(l -> l.newMessage(endMessage));

        return new Result(getStatus(), messages);

    }

    public synchronized List<Action> getSweepActions() {
        return List.copyOf(sweepActions);
    }

    protected void triggerSweepActionListeners() {
        sweepActionListeners.forEach(l -> l.changed(currentActions));
    }

    public synchronized void clearSweepActions() {
        sweepActions.clear();
        currentActions.clear();
        currentActions.addAll(generateActions(sweepValues.get(currentSweepIndex)));
        triggerSweepActionListeners();
    }

    public synchronized void setSweepActions(Collection<Action> sweepActions) {
        this.sweepActions.clear();
        this.sweepActions.addAll(sweepActions);
        currentActions.clear();
        currentActions.addAll(generateActions(sweepValues.get(currentSweepIndex)));
        triggerSweepActionListeners();
    }

    public synchronized void addSweepAction(Action sweepAction) {
        sweepActions.add(sweepAction);
        currentActions.clear();
        currentActions.addAll(generateActions(sweepValues.get(currentSweepIndex)));
        triggerSweepActionListeners();
    }

    public void removeSweepAction(Action sweepAction) {
        sweepActions.remove(sweepAction);
        currentActions.clear();
        currentActions.addAll(generateActions(sweepValues.get(currentSweepIndex)));
        triggerSweepActionListeners();
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

    public SweepActionListener addSweepActionListener(SweepActionListener sweepActionListener) {
        sweepActionListeners.add(sweepActionListener);
        return sweepActionListener;
    }

    public void removeSweepActionListener(SweepActionListener sweepActionListener) {
        sweepActionListeners.remove(sweepActionListener);
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

    public List<SweepValue> getSweepValues() {
        return List.copyOf(sweepValues);
    }

    public List<Action> generateActions(SweepValue sweepValue) {
        return sweepGenerator.generateActions(sweepValue, sweepActions);
    }

    public int getCurrentSweepIndex() {
        return currentSweepIndex;
    }

    public SweepValue getCurrentSweepValue() {
        return sweepValues.get(currentSweepIndex);
    }

    public void setSweepValues(Collection<SweepValue> sweepValues) {
        this.sweepValues.clear();
        this.sweepValues.addAll(sweepValues);
        sweepActionListeners.forEach(l -> l.changed(this.sweepActions));
    }

}
