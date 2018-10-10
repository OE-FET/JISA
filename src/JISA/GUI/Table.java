package JISA.GUI;

import JISA.Experiment.Result;
import JISA.Experiment.ResultList;
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

public class Table implements Gridable {

    public  TableView  table;
    public  BorderPane pane;
    private Stage      stage;

    public Table(String title) {

        try {

            FXMLLoader loader = new FXMLLoader(this.getClass().getResource("FXML/TableWindow.fxml"));
            loader.setController(this);
            Parent root  = loader.load();
            Scene  scene = new Scene(root);

            Semaphore semaphore = new Semaphore(0);

            Platform.runLater(() -> {
                Stage stage = new Stage();
                stage.setTitle(title);
                stage.setScene(scene);
                this.stage = stage;
                semaphore.release();
            });

            semaphore.acquire();

        } catch (Exception e) {

        }

    }

    public Table(String title, ResultList list) {
        this(title);
        watchList(list);
    }

    public void watchList(ResultList list) {

        list.setOnUpdate(() -> update(list));

        setUp(list);

    }

    private void setUp(ResultList list) {

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
        });


    }

    public void update(ResultList list) {

        final Result row = list.getLastRow();

        Platform.runLater(() -> {
            table.getItems().add(FXCollections.observableArrayList(Arrays.asList(row.getData())));
            table.scrollTo(table.getItems().size() - 1);
        });


    }

    public void show() {
        Platform.runLater(() -> stage.show()
        );
    }

    public void hide() {
        Platform.runLater(() -> stage.hide()
        );
    }

    public void close() {
        Platform.runLater(() -> stage.close()
        );
    }

    public Pane getPane() {
        return pane;
    }

    @Override
    public String getTitle() {
        return stage.getTitle();
    }

}
