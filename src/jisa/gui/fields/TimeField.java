package jisa.gui.fields;

import javafx.scene.control.Label;
import jisa.control.SRunnable;
import jisa.gui.DoubleInput;
import jisa.gui.Field;
import jisa.gui.GUI;
import jisa.gui.TimeInput;


public abstract class TimeField implements Field<Integer> {

    private final TimeInput field;
    private final Label     label;

    protected TimeField(Label label, TimeInput field) {
        this.field = field;
        this.label = label;
    }


    @Override
    public void set(Integer value) {

        field.setValue(value);
    }

    @Override
    public Integer get() {
        return field.getValue();
    }

    @Override
    public void setOnChange(SRunnable onChange) {
        field.setOnChange(e -> SRunnable.start(onChange));
    }

    @Override
    public void editValues(String... values) {

    }

    @Override
    public boolean isVisible() {
        return field.isVisible();
    }

    @Override
    public String getText() {
        return label.getText();
    }

    @Override
    public boolean isDisabled() {

        return field.disabled();
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

        field.disabled(disabled);
    }


}
