package jisa.gui.form;

import javafx.beans.property.Property;
import javafx.scene.Node;
import javafx.scene.control.Labeled;
import jisa.gui.GUI;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

public class BasicChoiceField<T, F extends Node> implements ChoiceField<T> {

    private final   Form form;
    protected final F    field;

    private final List<Listener<T>> listeners = new LinkedList<>();
    private final Labeled           label;
    private final Property<T>       property;
    private final List<String>      choices;
    private final List<Node>        extras;

    public BasicChoiceField(Form form, Labeled label, F node, Property<T> property, List<String> choices, Node... extras) {

        this.form    = form;
        this.choices = choices;
        this.extras  = List.of(extras);

        field         = node;
        this.label    = label;
        this.property = property;

        property.addListener((observable, oldValue, newValue) -> {

            synchronized (listeners) {

                for (Listener<T> l : listeners) {
                    form.executor.submit(() -> l.valueChanged(newValue));
                }

            }

        });

    }

    public void editValues(String... values) {
    }

    public void set(T value) {
        GUI.runNow(() -> property.setValue(value));
    }

    public T get() {
        return property.getValue();
    }

    @Override
    public Listener<T> addChangeListener(Listener<T> onChange) {

        synchronized (listeners) {
            listeners.add(onChange);
        }

        return onChange;

    }

    @Override
    public void removeChangeListener(Listener<T> onChange) {

        synchronized (listeners) {
            listeners.remove(onChange);
        }

    }

    @Override
    public boolean isDisabled() {
        return field.isDisabled();
    }

    @Override
    public void setDisabled(boolean disabled) {
        GUI.runNow(() -> field.setDisable(disabled));
    }

    @Override
    public boolean isVisible() {
        return field.isVisible();
    }

    @Override
    public void setVisible(boolean visible) {

        GUI.runNow(() -> {
            field.setVisible(visible);
            field.setManaged(visible);
        });

    }

    @Override
    public void remove() {

        GUI.runNow(() -> {
            form.list.getChildren().removeAll(label, field);
            form.list.getChildren().removeAll(extras);
            form.updateGridding();
        });

    }

    @Override
    public String getText() {
        return label.getText();
    }

    @Override
    public void setText(String text) {
        GUI.runNow(() -> label.setText(text));
    }

    @Override
    public void setChoices(String... options) {
        GUI.runNow(() -> {
            choices.clear();
            choices.addAll(List.of(options));
        });
    }

    @Override
    public List<String> getChoices() {
        return List.copyOf(choices);
    }

}
