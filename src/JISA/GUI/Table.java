package JISA.GUI;

import JISA.Experiment.Result;
import JISA.Experiment.ResultTable;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.concurrent.Semaphore;

public class Table extends JFXWindow implements Gridable, Clearable {

    public  TableView  table;
    public  BorderPane pane;

    public Table(String title) {
        super(title, "FXML/TableWindow.fxml", true);
    }

    public Table(String title, ResultTable list) {
        this(title);
        watchList(list);
    }

    public void watchList(ResultTable list) {

        list.addOnUpdate((r) -> update(r));
        setUp(list);
        list.addClearable(this);

    }

    private void setUp(ResultTable list) {

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

            Platform.runLater(() -> {
                for (Result row : list) {
                    table.getItems().add(FXCollections.observableArrayList(Arrays.asList(row.getData())));
                    table.scrollTo(table.getItems().size() - 1);
                }
            });

        });


    }

    public void update(Result row) {

        Platform.runLater(() -> {
            table.getItems().add(FXCollections.observableArrayList(Arrays.asList(row.getData())));
            table.scrollTo(table.getItems().size() - 1);
        });


    }

    public Pane getPane() {
        return pane;
    }

    @Override
    public void clear() {
        GUI.runNow(() -> {
            table.getItems().clear();
        });
    }
}
