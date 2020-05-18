package jisa.gui;

import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Progress extends JFXElement implements Element {

    public  Label       titleText;
    public  ProgressBar progressBar;
    public  Label       statusText;
    public  BorderPane  pane;
    public  Label       pctLabel;
    private Stage       stage;
    private double      max;
    private Timeline    timeline = new Timeline();

    public Progress(String title) {

        super(title, Progress.class.getResource("fxml/ProgressWindow.fxml"));

    }

    public void setProgress(double value, double max) {

        this.max = max;

        GUI.runNow(() -> {

            progressBar.setProgress(value / max);

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

    public Pane getTabPane() {
        return pane;
    }

}
