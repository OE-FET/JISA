package jisa.gui.form;

import javafx.beans.InvalidationListener;
import javafx.scene.control.Labeled;
import jisa.gui.GUI;
import jisa.gui.controls.TableInput;
import jisa.results.Column;
import jisa.results.DataTable;

import java.util.LinkedList;
import java.util.List;

public class TableInputField implements TableField {

    private final List<Listener<DataTable>> listeners = new LinkedList<>();
    private final Form                      form;
    private final Labeled                     label;
    private final TableInput                  table;

    public TableInputField(Form form, Labeled label, TableInput table) {

        this.form  = form;
        this.label = label;
        this.table = table;

        table.getTableView().getItems().addListener((InvalidationListener) il -> {

            DataTable value = get();

            synchronized (listeners) {

                for (Listener<DataTable> listener : listeners) {
                    form.executor.submit(() -> listener.valueChanged(value));
                }

            }

        });

    }

    @Override
    public void setColumns(Column... columns) {
        table.setColumns(columns);
    }

    @Override
    public List<Column> getColumns() {
        return table.getColumns();
    }

    @Override
    public void set(DataTable value) {
        table.setContents(value);
    }

    @Override
    public DataTable get() {
        return table.getContents();
    }

    @Override
    public Listener<DataTable> addChangeListener(Listener<DataTable> onChange) {

        synchronized (listeners) {
            listeners.add(onChange);
        }

        return onChange;

    }

    @Override
    public void removeChangeListener(Listener<DataTable> onChange) {

        synchronized (listeners) {
            listeners.remove(onChange);
        }

    }

    @Override
    public boolean isDisabled() {
        return table.isDisabled();
    }

    @Override
    public void setDisabled(boolean disabled) {
        GUI.runNow(() -> table.setDisable(disabled));
    }

    @Override
    public boolean isVisible() {
        return table.isVisible();
    }

    @Override
    public void setVisible(boolean visible) {

        GUI.runNow(() -> {
            table.setVisible(visible);
            table.setManaged(visible);
            label.setVisible(visible);
            label.setManaged(visible);
        });

    }

    @Override
    public void remove() {

        GUI.runNow(() -> {
            form.list.getChildren().removeAll(label, table);
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
}
