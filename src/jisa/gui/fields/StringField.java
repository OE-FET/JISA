package jisa.gui.fields;

import javafx.beans.value.ChangeListener;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import jisa.Util;
import jisa.control.SRunnable;
import jisa.gui.Field;
import jisa.gui.GUI;

public abstract class StringField implements Field<String> {

    private final TextField              field;
    private final Label                  label;
    private       ChangeListener<String> list = null;

    public StringField(Label label, TextField field) {
        this.field = field;
        this.label = label;
    }

    @Override
    public void set(String value) {

        field.setText(value);
    }

    @Override
    public String get() {

        return field.getText();
    }

    @Override
    public void setOnChange(SRunnable onChange) {

        if (list != null) {
            field.textProperty().removeListener(list);
        }

        list = (observable, oldValue, newValue) -> (new Thread(() -> {
            try {
                onChange.run();
            } catch (Exception e) {
                Util.exceptionHandler(e);
            }
        })).start();
        field.textProperty().addListener(list);

    }

    @Override
    public void editValues(String... values) {

    }

    @Override
    public boolean isDisabled() {
        return field.isDisabled();
    }

    @Override
    public boolean isVisible() {

        return field.isVisible();
    }

    @Override
    public abstract void remove();

    @Override
    public String getText() {

        return label.getText();
    }

    @Override
    public void setVisible(boolean visible) {

        GUI.runNow(() -> {
            label.setVisible(visible);
            label.setManaged(visible);
            field.setVisible(visible);
            field.setManaged(visible);
        });

    }


    @Override
    public void setText(String text) {

        GUI.runNow(() -> label.setText(text));
    }


    @Override
    public void setDisabled(boolean disabled) {

        field.setDisable(disabled);
    }


}
