package jisa.experiment.queue;

import jisa.control.SRunnable;

public class MeasurementSubAction extends SimpleAction {

    public MeasurementSubAction(String name) {
        super(name, () -> {});
    }

    public void start() {
        setStatus(Status.RUNNING);
    }

    public void fail() {
        setStatus(Status.ERROR);
    }

    public void complete() {
        setStatus(Status.COMPLETED);
    }

}
