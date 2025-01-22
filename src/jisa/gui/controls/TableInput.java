package jisa.gui.controls;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import jisa.gui.Form;
import jisa.gui.GUI;
import jisa.gui.form.Field;
import jisa.results.Column;
import jisa.results.ResultList;
import jisa.results.ResultTable;
import jisa.results.Row;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class TableInput extends VBox {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final Button addButton = new Button("✚");
    private final Button remButton = new Button("✕");
    private final Button edtButton = new Button("✎");
    private final Button mUpButton = new Button("▲");
    private final Button mDnButton = new Button("▼");

    private final TableView<Row>     table     = new TableView<>();
    private final HBox               buttonBar = new HBox(5.0, addButton, remButton, edtButton, mUpButton, mDnButton);
    private final List<Column>       columns   = new ArrayList<>();
    private final Map<Column, Field> fields    = new LinkedHashMap<>();

    private final Form input = new Form("Input Row");

    public TableInput(Column... columns) {

        super(GUI.SPACING);
        getChildren().addAll(buttonBar, table);

        setColumns(columns);
        setUpButtonBar();

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setMinHeight(150.0);
        table.setPrefWidth(75.0);

    }

    protected void setUpButtonBar() {

        addButton.setOnAction(e -> executor.submit(this::addRow));
        remButton.setOnAction(e -> executor.submit(this::removeRow));
        edtButton.setOnAction(e -> executor.submit(this::editRow));
        mUpButton.setOnAction(e -> executor.submit(this::moveRowUp));
        mDnButton.setOnAction(e -> executor.submit(this::moveRowDown));

        table.setOnMouseClicked(e -> {
            if (e.getClickCount() > 1 && table.getSelectionModel().getSelectedIndex() > -1) {
                executor.submit(this::editRow);
            }
        });

    }

    public void addRow() {

        if (input.showAsConfirmation()) {

            GUI.runNow(() -> table.getItems().add(
                new Row(
                    fields.entrySet()
                          .stream()
                          .collect(Collectors.toMap(Map.Entry::getKey, r -> r.getValue().get()))
                )
            ));

        }

    }

    public void removeRow() {

        Row selected = table.getSelectionModel().getSelectedItem();

        if (selected != null) {
            GUI.runNow(() -> table.getItems().remove(selected));
        }

    }

    public void editRow() {

        Row selected = table.getSelectionModel().getSelectedItem();

        if (selected == null) {
            return;
        }

        fields.forEach((c, f) -> f.set(selected.get(c)));

        if (input.showAsConfirmation()) {

            int index = table.getItems().indexOf(selected);

            GUI.runNow(() -> table.getItems().set(
                index,
                new Row(
                    fields.entrySet()
                          .stream()
                          .collect(Collectors.toMap(Map.Entry::getKey, r -> r.getValue().get()))
                )
            ));

        }

    }

    public void moveRowUp() {

        int index = table.getSelectionModel().getSelectedIndex();

        if (index > 0) {

            Row toMove = table.getItems().get(index);

            GUI.runNow(() -> {
                table.getItems().set(index, table.getItems().get(index - 1));
                table.getItems().set(index - 1, toMove);
                table.getSelectionModel().select(index - 1);
            });

        }

    }

    public void moveRowDown() {

        int index = table.getSelectionModel().getSelectedIndex();

        if (index < table.getItems().size() - 1 && index > -1) {

            Row toMove = table.getItems().get(index);

            GUI.runNow(() -> {
                table.getItems().set(index, table.getItems().get(index + 1));
                table.getItems().set(index + 1, toMove);
                table.getSelectionModel().select(index + 1);
            });

        }

    }

    public TableInput(ResultTable table) {

        this(table.getColumnsAsArray());
        setContents(table);

    }

    public void setColumns(Column... columns) {

        this.columns.clear();
        Collections.addAll(this.columns, columns);

        regenerateColumns();

    }

    public List<Column> getColumns() {
        return List.copyOf(columns);
    }

    public ResultTable getContents() {

        ResultTable list = new ResultList(columns);
        table.getItems().forEach(list::addRow);
        return list;

    }

    public void setContents(ResultTable table) {
        setColumns(table.getColumnsAsArray());
        GUI.runNow(() -> this.table.getItems().addAll(table.getRows()));
    }

    protected void regenerateColumns() {

        List<TableColumn<Row, ?>> cols = new ArrayList<>(columns.size());

        for (Column column : columns) {

            Class columnType = column.getType();

            if (Number.class.isAssignableFrom(columnType)) {
                TableColumn<Row, Number> col = new TableColumn<>(column.getTitle());
                col.setCellValueFactory(row -> new ReadOnlyObjectWrapper<>(row.getValue().get((Column<Number>) column)));
                cols.add(col);
            } else if (columnType == Boolean.class) {
                TableColumn<Row, String> col = new TableColumn<>(column.getTitle());
                col.setCellValueFactory(row -> new ReadOnlyObjectWrapper<>(row.getValue().get((Column<Boolean>) column) ? "✓" : "×"));
                cols.add(col);
            } else {
                TableColumn<Row, String> col = new TableColumn<>(column.getTitle());
                col.setCellValueFactory(row -> new ReadOnlyObjectWrapper<>(row.getValue().get(column).toString()));
                cols.add(col);
            }

        }

        GUI.runNow(() -> {

            table.getItems().clear();
            table.getColumns().clear();
            table.getColumns().addAll(cols);

        });

        input.clear();
        fields.clear();

        for (Column column : columns) {

            Class columnType = column.getType();

            if (columnType == Double.class) {
                fields.put(column, input.addDoubleField(column.getTitle(), 0.0));
            } else if (columnType == Integer.class || columnType == Long.class) {
                fields.put(column, input.addIntegerField(column.getTitle(), 0));
            } else if (columnType == String.class) {
                fields.put(column, input.addTextField(column.getTitle(), ""));
            } else if (columnType == Boolean.class) {
                fields.put(column, input.addCheckBox(column.getTitle(), false));
            }

        }

    }

    public TableView<Row> getTableView() {
        return table;
    }

}
