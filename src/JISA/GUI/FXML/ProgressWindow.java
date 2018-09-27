package JISA.GUI.FXML;

import JISA.GUI.Gridable;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class ProgressWindow {

    public  Label       titleText;
    public  ProgressBar progressBar;
    public  Label       statusText;
    public  BorderPane  pane;
    public  Label       pctLabel;
    private Stage       stage;
    private double      max;

    public static ProgressWindow create(String title) {

        try {
            FXMLLoader     loader     = new FXMLLoader(TableWindow.class.getResource("ProgressWindow.fxml"));
            Parent         root       = loader.load();
            Scene          scene      = new Scene(root);
            ProgressWindow controller = (ProgressWindow) loader.getController();
            Platform.runLater(() -> {
                Stage stage = new Stage();
                controller.stage = stage;
                stage.setTitle(title);
                controller.setTitleText(title);
                stage.setScene(scene);
            });
            return controller;
        } catch (IOException e) {
            e.printStackTrace();
            return new ProgressWindow();
        }

    }

    public void setProgress(double value, double max) {
        Platform.runLater(() -> {
            this.max = max;

            Timeline timeline = new Timeline();
            KeyValue keyValue = new KeyValue(progressBar.progressProperty(), value / this.max);
            KeyFrame keyFrame = new KeyFrame(new Duration(200), keyValue);
            timeline.getKeyFrames().add(keyFrame);
            timeline.play();

            pctLabel.setText(
                    String.format("%d%%", (int) Math.round(100 * (value/this.max)))
            );

        });
    }

    public void setProgress(double value) {
        setProgress(value, this.max);
    }

    public void setTitleText(String text) {
        Platform.runLater(() -> {
            titleText.setText(text);
        });
    }

    public void setStatusText(String text) {
        Platform.runLater(() -> {
            statusText.setText(text);
        });
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
}
