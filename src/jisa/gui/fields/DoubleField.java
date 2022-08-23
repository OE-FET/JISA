package jisa.gui.fields;

import javafx.scene.control.Label;
import jisa.control.SRunnable;
import jisa.gui.DoubleInput;
import jisa.gui.Field;
import jisa.gui.GUI;


public abstract class DoubleField implements Field<Double> {

    private final DoubleInput field;
    private final Label       label;

    protected DoubleField(Label label, DoubleInput field) {
        this.field = field;
        this.label = label;
    }


    @Override
    public void set(Double value) {

        field.setValue(value);
    }

    @Override
    public Double get() {

        return field.getValue();
    }

    @Override
    public void setOnChange(SRunnable onChange) {
        field.setOnChange(e -> onChange.start());
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

    public void setLeading(int num) {
        field.leading = num;
    }


}
