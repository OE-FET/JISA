package jisa.experiment.queue;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ActionQueue {

    private final List<Action>                 actions          = new LinkedList<>();
    private final List<ActionListener>         actionListeners  = new LinkedList<>();
    private final List<Action.Message>         messages         = new LinkedList<>();
    private final List<Action.MessageListener> messageListeners = new LinkedList<>();
    private       boolean                      running          = false;

    public synchronized Action.Result run() throws IllegalStateException {

        if (running) {
            throw new IllegalStateException("ActionQueue is already running.");
        }

        try {

            List<Action.Message> runMessages = new LinkedList<>();

            running = true;

            message(Action.MessageType.INFO, "Queue Started");

            actions.forEach(Action::reset);

            boolean error = false;

            for (Action action : actions) {

                Action.MessageListener listener = action.addMessageListener(m -> {
                    messages.add(m);
                    runMessages.add(m);
                    messageListeners.forEach(l -> l.newMessage(m));
                });

                Action.Result result = action.run();

                action.removeMessageListener(listener);

                switch (result.getFinalStatus()) {

                    case CRITICAL_ERROR:
                        message(Action.MessageType.ERROR, "Action (" + action.getName() + ") ended with critical error, ending queue.");
                        return new Action.Result(Action.Status.CRITICAL_ERROR, runMessages);

                    case INTERRUPTED:
                        message(Action.MessageType.ERROR, "Action (" + action.getName() + ") was interrupted, ending queue.");
                        return new Action.Result(Action.Status.INTERRUPTED, runMessages);

                    case ERROR:
                        message(Action.MessageType.ERROR, "Action (" + action.getName() + ") ended with an error, continuing to next action.");
                        error = true;
                        break;

                }

            }

            return new Action.Result(error ? Action.Status.ERROR : Action.Status.SUCCESS, runMessages);

        } finally {
            running = false;
            message(Action.MessageType.INFO, "Queue Ended");
        }

    }

    protected void message(Action.MessageType type, String message) {
        Action.Message msg = new Action.Message(type, message, null, Collections.emptyList());
        messages.add(msg);
        messageListeners.forEach(l -> l.newMessage(msg));
    }

    public List<Action.Message> getMessages() {
        return List.copyOf(messages);
    }

    public void clearMessages() {
        messages.clear();
    }

    public synchronized <A extends Action> A addAction(A action) {

        if (running) {
            throw new IllegalStateException("Cannot modify actions in an ActionQueue that is running.");
        }

        actions.add(action);
        actionListeners.forEach(l -> l.changed(Collections.unmodifiableList(actions)));

        return action;
    }

    public synchronized void removeAction(Action action) {

        if (running) {
            throw new IllegalStateException("Cannot modify actions in an ActionQueue that is running.");
        }

        actions.remove(action);
        actionListeners.forEach(l -> l.changed(Collections.unmodifiableList(actions)));

    }

    public synchronized void clearActions() {

        if (running) {
            throw new IllegalStateException("Cannot modify actions in an ActionQueue that is running.");
        }

        actions.clear();
        actionListeners.forEach(l -> l.changed(Collections.unmodifiableList(actions)));

    }

    public synchronized void swapActions(Action a, Action b) {

        int indexA = actions.indexOf(a);
        int indexB = actions.indexOf(b);

        actions.set(indexA, b);
        actions.set(indexB, a);

        actionListeners.forEach(l -> l.changed(Collections.unmodifiableList(actions)));

    }

    public List<Action> getActions() {
        return Collections.unmodifiableList(actions);
    }

    public Action.MessageListener addMessageListener(Action.MessageListener listener) {
        messageListeners.add(listener);
        return listener;
    }

    public void removeMessageListener(Action.MessageListener listener) {
        messageListeners.remove(listener);
    }

    public ActionListener addActionListener(ActionListener listener) {
        actionListeners.add(listener);
        return listener;
    }

    public void removeActionListener(ActionListener listener) {
        actionListeners.remove(listener);
    }

    public interface ActionListener {
        void changed(List<Action> actions);
    }

    public boolean isRunning() {
        return running;
    }

}
