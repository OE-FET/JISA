package JISA.GUI;

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

import java.util.concurrent.Semaphore;

public class Progress extends JFXWindow implements Gridable {

    public  Label       titleText;
    public  ProgressBar progressBar;
    public  Label       statusText;
    public  BorderPane  pane;
    public  Label       pctLabel;
    private Stage       stage;
    private double      max;

    public Progress(String title) {

        super(title, "FXML/ProgressWindow.fxml", true);

    }

    public void setProgress(double value, double max) {
        Platform.runLater(() -> {
            this.max = max;

            Timeline timeline = new Timeline();
            KeyValue keyValue = new KeyValue(progressBar.progressProperty(), value / this.max);
            KeyFrame keyFrame = new KeyFrame(new Duration(200), keyValue);
            timeline.getKeyFrames().add(keyFrame);
            timeline.play();

            if (value == -1) {
                pctLabel.setText("");
            } else {
                pctLabel.setText(
                        String.format("%d%%", (int) Math.round(100 * (value / this.max)))
                );
            }

        });
    }

    public void setProgress(double value) {
        setProgress(value, this.max);
    }

    public void setTitle(String text) {
        Platform.runLater(() -> titleText.setText(text));
    }

    public void setStatus(String text) {
        Platform.runLater(() -> statusText.setText(text));
    }

    public Pane getPane() {
        return pane;
    }

}
