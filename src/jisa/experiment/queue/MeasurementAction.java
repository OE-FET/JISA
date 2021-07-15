package jisa.experiment.queue;

import jisa.experiment.Measurement;
import jisa.gui.queue.SimpleActionDisplay;
import jisa.results.ResultTable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class MeasurementAction extends AbstractAction<ResultTable> {

    private final Measurement   measurement;
    private       int           retryCount    = 1;
    private       Exception     lastException = null;
    private       ResultTable   data          = null;
    private       NameGenerator generator     = (parameters, label) -> null;

    public MeasurementAction(String name, Measurement measurement) {
        this.measurement = measurement;
        setName(name);
    }

    public MeasurementAction(Measurement measurement) {
        this(String.format("%s (%s)", measurement.getName(), measurement.getLabel()), measurement);
    }

    @Override
    public void reset() {
        setStatus(Status.NOT_STARTED);
    }

    @Override
    public void start() {

        int     count   = 0;
        boolean success = false;

        do {

            setStatus(count > 0 ? Status.RETRY : Status.RUNNING);

            try {

                String path = generateFilePath();

                if (path != null) {
                    data = measurement.newResults(path);
                } else {
                    data = measurement.newResults();
                }

            } catch (IOException e) {
                data = measurement.newResults();
            }

            getAttributes().forEach(data::setAttribute);

            onStart();

            try {

                measurement.start();
                setStatus(Status.COMPLETED);

            } catch (InterruptedException e) {

                lastException = e;
                setStatus(Status.INTERRUPTED);
                break;

            } catch (Exception e) {

                lastException = e;
                setStatus(Status.ERROR);

                if (isCritical()) {
                    break;
                }

            } finally {
                count++;
            }


        } while (getStatus() != Status.COMPLETED && count < retryCount);

        onFinish();

    }

    public String generateFilePath() {

        String path;
        int    count = 0;

        do {

            if (count > 0) {
                path = generator.makeName(getAttributeString("-", "="), measurement.getLabel() + count);
            } else {
                path = generator.makeName(getAttributeString("-", "="), measurement.getLabel());
            }

            if (path == null) {
                return null;
            }

            count++;

        } while (Files.exists(Path.of(path)));

        return path;

    }

    public Measurement getMeasurement() {
        return measurement;
    }

    @Override
    public void stop() {

        int count = 0;
        while (measurement.isRunning() && count < 500) {
            measurement.stop();
            count++;
        }

    }

    public void setOnMeasurementStart(Listener<MeasurementAction> listener) {
        setOnStart(a -> listener.updateRegardless((MeasurementAction) a));
    }

    public void setOnMeasurementFinish(Listener<MeasurementAction> listener) {
        setOnFinish(a -> listener.updateRegardless((MeasurementAction) a));
    }

    public void setAttribute(String key, String value) {

        super.setAttribute(key, value);

        if (data != null) {
            data.setAttribute(key, value);
        }

    }

    public void userEdit() {
        super.userEdit();
        childrenChanged();
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public int getRetryCount() {
        return retryCount;
    }

    @Override
    public Exception getError() {
        return lastException;
    }

    @Override
    public boolean isRunning() {
        return getStatus() == Status.RUNNING || getStatus() == Status.RETRY;
    }

    @Override
    public ResultTable getData() {
        return data;
    }

    @Override
    public List<Action> getChildren() {
        return measurement.getActions();
    }

    @Override
    public SimpleActionDisplay getDisplay() {
        return new SimpleActionDisplay(this);
    }

    @Override
    public MeasurementAction copy() {

        MeasurementAction copy = new MeasurementAction(getName(), getMeasurement());
        copyBasicParametersTo(copy);
        copy.setFileNameGenerator(generator);
        return copy;

    }

    public void setFileNameGenerator(NameGenerator generator) {
        this.generator = generator;
    }

    public interface NameGenerator {
        String makeName(String parameters, String label);
    }

}
