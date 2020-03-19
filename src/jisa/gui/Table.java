package jisa.gui;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import jisa.control.SRunnable;
import jisa.experiment.Result;
import jisa.experiment.ResultTable;

import java.util.Arrays;

public class Table extends JFXWindow implements Element, Clearable {

    public TableView  table;
    public BorderPane pane;
    public ToolBar    toolBar;

    /**
     * Creates an empty table.
     *
     * @param title Window title
     */
    public Table(String title) {

        super(title, Table.class.getResource("fxml/TableWindow.fxml"));

        GUI.runNow(() -> {
            toolBar.setVisible(false);
            toolBar.setManaged(false);
        });

        toolBar.getItems().addListener((InvalidationListener) observable -> {

            GUI.runNow(() -> {
                boolean flag = !toolBar.getItems().isEmpty();
                toolBar.setVisible(flag);
                toolBar.setManaged(flag);
            });

        });

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

        list.addOnUpdate(this::update);
        setUp(list);
        list.addClearable(this);

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

        int numCols = list.getNumCols();

        for (int i = 0; i < numCols; i++) {
            final int                                   finalI = i;
            TableColumn<ObservableList<Double>, Double> col    = new TableColumn(list.getTitle(i));
            col.setCellValueFactory(param ->
                    new ReadOnlyObjectWrapper<>(param.getValue().get(finalI))
            );
            GUI.runNow(() -> table.getColumns().add(col));
        }

        for (Result row : list) {

            double[] data  = row.getData();
            Double[] oData = new Double[data.length];

            for (int j = 0; j < data.length; j++) {
                oData[j] = data[j];
            }

            GUI.runNow(() -> {
                table.getItems().add(FXCollections.observableArrayList(Arrays.asList(oData)));
                table.scrollTo(table.getItems().size() - 1);
            });
        }


    }

    public synchronized void update(Result row) {

        Platform.runLater(() -> {

            double[] data  = row.getData();
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

    public Button addToolbarButton(String text, SRunnable onClick) {

        javafx.scene.control.Button button = new javafx.scene.control.Button(text);
        button.setOnAction(event -> onClick.start());
        GUI.runNow(() -> toolBar.getItems().add(button));

        return new Button.ButtonWrapper(button) {

            @Override
            public void remove() {
                GUI.runNow(() -> toolBar.getItems().remove(button));
            }

        };

    }

    public Separator addToolbarSeparator() {

        javafx.scene.control.Separator separator = new javafx.scene.control.Separator();
        GUI.runNow(() -> toolBar.getItems().add(separator));

        return new Separator.SeparatorWrapper(separator) {

            @Override
            public void remove() {
                GUI.runNow(() -> toolBar.getItems().remove(separator));
            }

        };

    }

}
