package jisa.experiment.queue;

import jisa.control.SRunnable;
import jisa.gui.queue.ActionDisplay;
import jisa.gui.queue.SimpleActionDisplay;

import java.util.*;

public class SimpleAction extends AbstractAction<Void> {

    private       String    name;
    private       boolean   isRunning     = false;
    private       Thread    runningThread = null;
    private       Exception lastException = null;
    private final SRunnable runnable;

    public SimpleAction(String name, SRunnable action) {
        this.runnable = action;
        setName(name);
    }

    @Override
    public void reset() {
        setStatus(Status.NOT_STARTED);
    }

    @Override
    public void start() {

        runningThread = Thread.currentThread();

        setStatus(Status.RUNNING);
        isRunning = true;

        onStart();

        try {
            runnable.run();
            setStatus(Status.COMPLETED);
        } catch (InterruptedException e) {
            lastException = e;
            setStatus(Status.INTERRUPTED);
        } catch (Exception e) {
            lastException = e;
            setStatus(Status.ERROR);
        } finally {
            isRunning = false;
        }

        onFinish();

    }

    @Override
    public void stop() {

        setStatus(Status.STOPPING);

        int i = 0;
        while (isRunning && i < 500) {
            runningThread.interrupt();
            i++;
        }

    }

    @Override
    public Exception getError() {
        return lastException;
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public Void getData() {
        return null;
    }

    @Override
    public List<Action> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public SimpleActionDisplay getDisplay() {
        return new SimpleActionDisplay(this);
    }

    @Override
    public SimpleAction copy() {

        SimpleAction copy = new SimpleAction(getName(), runnable);
        copyBasicParametersTo(copy);
        return copy;

    }

}
