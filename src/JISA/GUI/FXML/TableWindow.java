package JISA.GUI.FXML;

import JISA.Experiment.*;
import JISA.GUI.Gridable;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Arrays;

public class TableWindow {

    public  TableView  table;
    public  BorderPane pane;
    private Stage      stage;

    public static TableWindow create(String title) {

        try {
            FXMLLoader  loader     = new FXMLLoader(TableWindow.class.getResource("TableWindow.fxml"));
            Parent      root       = loader.load();
            Scene       scene      = new Scene(root);
            TableWindow controller = (TableWindow) loader.getController();
            Platform.runLater(() -> {
                Stage stage = new Stage();
                stage.setTitle(title);
                stage.setScene(scene);
                controller.stage = stage;
            });
            return controller;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    public static TableWindow create(String title, ResultList list) {
        TableWindow window = create(title);
        window.watchList(list);
        return window;
    }

    public void watchList(ResultList list) {

        list.setOnUpdate(() -> {
            update(list);
        });

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

        Platform.runLater(() -> {
            table.getItems().add(FXCollections.observableArrayList(Arrays.asList(list.getLastRow().getData())));
            table.scrollTo(table.getItems().size() - 1);
        });


    }

    public void show() {
        Platform.runLater(() -> {
                    stage.show();
                }
        );
    }

    public void hide() {
        Platform.runLater(() -> {
                    stage.hide();
                }
        );
    }

    public void close() {
        Platform.runLater(() -> {
                    stage.close();
                }
        );
    }

    public Pane getPane() {
        return pane;
    }
}
