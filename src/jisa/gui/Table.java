package jisa.gui;

import jisa.experiment.Result;
import jisa.experiment.ResultTable;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import java.util.Arrays;

public class Table extends JFXWindow implements Element, Clearable {

    public TableView  table;
    public BorderPane pane;

    /**
     * Creates an empty table.
     *
     * @param title Window title
     */
    public Table(String title) {
        super(title, Table.class.getResource("FXML/TableWindow.fxml"));
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

        list.addOnUpdate((r) -> update(r));
        setUp(list);
        list.addClearable(this);

    }

    private synchronized void setUp(ResultTable list) {

        Platform.runLater(() -> {

            table.getItems().clear();
            table.getColumns().clear();

            int numCols = list.getNumCols();

            for (int i = 0; i < numCols; i++) {
                final int                                   finalI = i;
                TableColumn<ObservableList<Double>, Double> col    = new TableColumn(list.getTitle(i));
                col.setCellValueFactory(param ->
                        new ReadOnlyObjectWrapper<>(param.getValue().get(finalI))
                );
                table.getColumns().add(col);
            }

            GUI.runNow(() -> {
                for (Result row : list) {

                    double[] data = row.getData();
                    Double[] oData = new Double[data.length];

                    for (int j = 0; j < data.length; j++) {
                        oData[j] = data[j];
                    }

                    table.getItems().add(FXCollections.observableArrayList(Arrays.asList(oData)));
                    table.scrollTo(table.getItems().size() - 1);
                }
            });

        });


    }

    public synchronized void update(Result row) {

        Platform.runLater(() -> {

            double[] data = row.getData();
            Double[] oData = new Double[data.length];

            for (int j = 0; j < data.length; j++) {
                oData[j] = data[j];
            }
            int index = table.getItems().size();
            table.getItems().add(FXCollections.observableArrayList(Arrays.asList(oData)));
            table.scrollTo(index);
        });


    }

    public Pane getPane() {
        return pane;
    }

    @Override
    public synchronized void clear() {
        GUI.runNow(() -> {
            table.getItems().clear();
        });
    }
}
