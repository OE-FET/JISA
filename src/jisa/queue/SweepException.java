package jisa.queue;

public class SweepException extends Exception {

    private final Object    sweepValue;
    private final Action    action;
    private final Throwable exception;

    public SweepException(Object sweepValue, Action action, Throwable exception) {
        this.sweepValue = sweepValue;
        this.action     = action;
        this.exception  = exception;
    }

    public Object getSweepValue() {
        return sweepValue;
    }

    public Action getAction() {
        return action;
    }

    public Throwable getException() {
        return exception;
    }

    public String getMessage() {
        return String.format("(%s) %s: %s", sweepValue, action.getName(), exception.getMessage());
    }

}
