package jisa.gui.queue;

import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import jisa.experiment.queue.Action;
import jisa.experiment.queue.Listener;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class ActionDisplay<T extends Action> extends StackPane {

    public static final Background SELECTED = new Background(new BackgroundFill(Color.web("#0096C9"), null, null));
    public static final Background NORMAL   = new Background(new BackgroundFill(Color.WHITE, null, null));
    public static final Background HOVER    = new Background(new BackgroundFill(Color.gray(0.98), null, null));

    private final T                                action;
    private final List<Listener<ActionDisplay<?>>> runningListeners = new LinkedList<>();
    private       boolean                          selected         = false;

    public ActionDisplay(T action) {

        this.action = action;
        setBackground(NORMAL);
        setBorder(new Border(new BorderStroke(Color.SILVER, BorderStrokeStyle.SOLID, null, new BorderWidths(1))));

        setOnMouseEntered(e -> setBackground(selected ? SELECTED : HOVER));
        setOnMouseExited(e -> setBackground(selected ? SELECTED : NORMAL));

    }

    public T getAction() {
        return action;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        setBackground(selected ? SELECTED : NORMAL);
    }

    public boolean isSelected() {
        return selected;
    }

    public Listener<ActionDisplay<?>> addRunningListener(Listener<ActionDisplay<?>> listener) {
        runningListeners.add(listener);
        return listener;
    }

    public void removeRunningListener(Listener<ActionDisplay<?>> listener) {
        runningListeners.remove(listener);
    }

    public void removeRunningListeners(Collection<Listener<ActionDisplay<?>>> listeners) {
        runningListeners.removeAll(listeners);
    }

    public synchronized void triggerRunningListeners(ActionDisplay<?> triggeredBy) {
        runningListeners.forEach(l -> l.update(triggeredBy));
    }

    public void setShowAll(boolean show) {

    }

}
