package JISA.GUI;

import JISA.GUI.FXML.ProgressWindow;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

public class Progress implements Gridable {

    private ProgressWindow window;

    public Progress(String title) {
        window = ProgressWindow.create(title);
    }

    public void setProgress(double value, double max) {
        window.setProgress(value, max);
    }

    public void setProgress(double value) {
        window.setProgress(value);
    }

    public void setTitle(String text) {
        window.setTitleText(text);
    }

    public void setStatus(String text) {
        window.setStatusText(text);
    }

    public void show() {
        window.show();
    }

    public void hide() {
        window.hide();
    }

    public void close() {
        window.close();
    }

    @Override
    public Pane getPane() {
        return window.getPane();
    }
}
