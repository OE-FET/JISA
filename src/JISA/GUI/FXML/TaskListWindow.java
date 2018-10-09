package JISA.GUI.FXML;

import JISA.GUI.Gridable;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import java.io.IOException;

public class TaskListWindow {

    public  BorderPane pane;
    public  ListView   list;
    private Stage      stage;

    public static TaskListWindow create(String title) {

        try {
            FXMLLoader     loader     = new FXMLLoader(TaskListWindow.class.getResource("TaskListWindow.fxml"));
            Parent         root       = loader.load();
            Scene          scene      = new Scene(root);
            TaskListWindow controller = (TaskListWindow) loader.getController();
            Platform.runLater(() -> {
                Stage stage = new Stage();
                controller.stage = stage;
                stage.setTitle(title);
                stage.setScene(scene);
            });
            return controller;
        } catch (IOException e) {
            e.printStackTrace();
            return new TaskListWindow();
        }

    }

    public Task addTask(String name) {

        HBox box = new HBox();
        box.setSpacing(15);
        box.setAlignment(Pos.CENTER_LEFT);

        ProgressIndicator indicator = new ProgressIndicator();
        Label             label     = new Label();

        indicator.setProgress(0);
        indicator.setMaxHeight(45);
        indicator.setMaxWidth(35);

        label.setText(name);
        box.getChildren().addAll(indicator, label);
        HBox.setHgrow(label, Priority.ALWAYS);

        Platform.runLater(() -> {
            list.getItems().add(box);
        });

        return new Task(name, indicator, label);

    }

    public Task addTask(String name, double max) {
        HBox box = new HBox();
        box.setSpacing(15);
        box.setAlignment(Pos.CENTER_LEFT);

        ProgressIndicator indicator = new ProgressIndicator();
        Label             label     = new Label();

        indicator.setProgress(0);
        indicator.setMaxHeight(45);
        indicator.setMaxWidth(35);

        label.setText(name);
        box.getChildren().addAll(indicator, label);
        HBox.setHgrow(label, Priority.ALWAYS);

        Platform.runLater(() -> {
            list.getItems().add(box);
        });

        return new Task(name, indicator, label, max);
    }

    public void show() {
        Platform.runLater(() -> {
            stage.show();
        });
    }

    public void hide() {
        Platform.runLater(() -> {
            stage.hide();
        });
    }

    public void close() {
        Platform.runLater(() -> {
            stage.close();
        });
    }

    public Pane getPane() {
        return pane;
    }

    public class Task {

        private double            max;
        private double            value;
        private boolean           progress;
        private String            text;
        private Label             label;
        private ProgressIndicator indicator;

        public Task(String name, ProgressIndicator i, Label l) {

            text = name;
            indicator = i;
            label = l;
            progress = false;

        }

        public Task(String name, ProgressIndicator i, Label l, double m) {
            text = name;
            indicator = i;
            label = l;
            max = m;
            progress = true;
        }

        public void setValue(double value) {
            this.value = value;
            Platform.runLater(() -> {
                indicator.setProgress(this.value / this.max);
            });
        }

        public void setActive() {
            this.value = value;
            Platform.runLater(() -> {
                indicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
            });
        }

        public void setComplete() {

            Platform.runLater(() -> {
                indicator.setProgress(1);
            });
        }

    }

}
