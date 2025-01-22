package jisa.gui;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import jisa.results.ResultTable;
import jisa.results.Row;

import java.util.stream.Collectors;

public class Table extends JFXElement implements Element, Clearable {

    public  TableView   table;
    private ResultTable watching = null;

    /**
     * Creates an empty table.
     *
     * @param title Window title
     */
    public Table(String title) {

        super(title, Table.class.getResource("fxml/TableWindow.fxml"));

    }

    /**
     * Creates a table that watches and displays the contents of a ResultTable object.
     *
     * @param title Window title
     * @param list  ResultTable to display
     */
    public Table(String title, ResultTable list) {
        this(title);
        watchList(list);
    }

    /**
     * Watch the given ResultTable to display its contents.
     *
     * @param list ResultTable to watch
     */
    public synchronized void watchList(ResultTable list) {

        list.addRowListener(this::update);
        setUp(list);
        list.addClearListener(this::clear);

    }

    /**
     * Watch the given ResultTable to display its contents.
     *
     * @param list ResultTable to watch
     */
    public void watch(ResultTable list) {
        watchList(list);
    }

    private synchronized void setUp(ResultTable list) {

        GUI.runNow(() -> {
            table.getItems().clear();
            table.getColumns().clear();
        });

        int numCols = list.getColumnCount();

        for (int i = 0; i < numCols; i++) {
            final int                                   finalI = i;
            TableColumn<ObservableList<Object>, Object> col    = new TableColumn(list.getColumn(i).getTitle());
            col.setCellValueFactory(param ->
                new ReadOnlyObjectWrapper<>(param.getValue().get(finalI))
            );
            GUI.runNow(() -> table.getColumns().add(col));
        }

        for (Row row : list) {

            GUI.runNow(() -> {
                table.getItems().add(FXCollections.observableArrayList(
                    list.getColumns().stream().map(row::get).collect(Collectors.toUnmodifiableList())
                ));
                table.scrollTo(table.getItems().size() - 1);
            });
        }

        this.watching = list;

    }

    public synchronized void update(Row row) {

        Platform.runLater(() -> {
            int index = table.getItems().size();
            table.getItems().add(FXCollections.observableArrayList(
                watching.getColumns().stream().map(row::get).collect(Collectors.toUnmodifiableList())
            ));
            table.scrollTo(index);
        });


    }

    @Override
    public synchronized void clear() {
        GUI.runNow(() -> {
            table.getItems().clear();
        });
    }

}
