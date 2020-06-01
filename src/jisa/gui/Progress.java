package jisa.gui;

import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;

/**
 * GUI element consisting of a progress bar with title, percentage and status text.
 */
public class Progress extends JFXElement {

    private final Timeline    timeline = new Timeline();
    @FXML
    protected     Label       titleText;
    @FXML
    protected     ProgressBar progressBar;
    @FXML
    protected     Label       statusText;
    @FXML
    protected     Label       pctLabel;
    private       double      max      = 1.0;

    public Progress(String title) {

        super(title, Progress.class.getResource("fxml/ProgressWindow.fxml"));
        setTitle(title);

    }

    /**
     * Sets the current value of the progress as value/max.
     *
     * @param value Progress value
     * @param max   Max value
     */
    public void setProgress(Number value, Number max) {

        this.max = max.doubleValue();

        GUI.runNow(() -> {

            progressBar.setProgress(value.doubleValue() / max.doubleValue());

            if (value.doubleValue() == -1) {
                pctLabel.setText("");
            } else {
                pctLabel.setText(
                        String.format("%d%%", (int) Math.round(100 * (value.doubleValue() / this.max)))
                );
            }

        });

    }

    /**
     * Increments the current progress value by a given amount.
     *
     * @param increment Increment to use
     */
    public void incrementProgress(Number increment) {
        setProgress(getProgress() + increment.doubleValue());
    }

    /**
     * Increments the current progress value by 1.0.
     */
    public void incrementProgress() {
        incrementProgress(1.0);
    }

    /**
     * Returns the current progress value.
     *
     * @return Progress value
     */
    public double getProgress() {
        return progressBar.getProgress() * max;
    }

    /**
     * Sets the progress value, while leaving the max value unchanged.
     *
     * @param value Progress value
     */
    public void setProgress(double value) {
        setProgress(value, this.max);
    }

    public void setProgress(int value) {
        setProgress((double) value);
    }

    /**
     * Returns the progress as a fraction of the maximum (ie value/max). That is a value between 0 and 1.
     *
     * @return Progress fraction
     */
    public double getFractionProgress() {
        return progressBar.getProgress();
    }

    /**
     * Returns the progress as a percentage of the maximum. That is a value between 0 and 100.
     *
     * @return Percentage progress
     */
    public double getPercentProgress() {
        return getFractionProgress() * 100.0;
    }

    /**
     * Sets the title of the element, including the title text above the progress bar.
     *
     * @param text New title text
     */
    public void setTitle(String text) {
        Platform.runLater(() -> titleText.setText(text));
        super.setTitle(text);
    }

    /**
     * Returns the current status text displayed underneath the progress bar.
     *
     * @return Current status text
     */
    public String getStatus() {
        return statusText.getText();
    }

    /**
     * Sets the status text to display underneath the progress bar.
     *
     * @param text New status text to display
     */
    public void setStatus(String text) {
        GUI.runNow(() -> statusText.setText(text));
    }

}
