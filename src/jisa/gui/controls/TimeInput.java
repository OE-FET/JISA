package jisa.gui.controls;


import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.converter.IntegerStringConverter;
import jisa.gui.GUI;
import jisa.gui.form.Field;

import java.util.function.UnaryOperator;

public class TimeInput extends HBox {

    public TextField               hours;
    public TextField               minutes;
    public TextField               seconds;
    public TextField               millis;
    public HBox                    root;
    public Field.Listener<Integer> onChange = null;

    public TimeInput() {

        try {

            FXMLLoader loader = new FXMLLoader(GUI.getFXML("TimeInput"));

            loader.setController(this);
            root = loader.load();

            GUI.runNow(() -> getChildren().add(root));
            HBox.setHgrow(root, Priority.ALWAYS);

            hours.setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(), 0, INT_FILTER));
            minutes.setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(), 0, INT_FILTER));
            seconds.setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(), 0, INT_FILTER));
            millis.setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(), 0, INT_FILTER));

            root.setPadding(Insets.EMPTY);

            hours.textProperty().addListener((observable, oldValue, newValue) -> {
                if (onChange != null) {
                    onChange.valueChanged(getValue());
                }
            });

            minutes.textProperty().addListener((observable, oldValue, newValue) -> {
                if (onChange != null) {
                    onChange.valueChanged(getValue());
                }
            });

            seconds.textProperty().addListener((observable, oldValue, newValue) -> {
                if (onChange != null) {
                    onChange.valueChanged(getValue());
                }
            });

            millis.textProperty().addListener((observable, oldValue, newValue) -> {
                if (onChange != null) {
                    onChange.valueChanged(getValue());
                }
            });

            hours.focusedProperty().addListener((observable, oldValue, newValue) -> {

                if (hours.isFocused()) {
                    root.getStyleClass().add("focused");
                } else {
                    root.getStyleClass().remove("focused");
                }

            });

            minutes.focusedProperty().addListener((observable, oldValue, newValue) -> {

                if (minutes.isFocused()) {
                    root.getStyleClass().add("focused");
                } else {
                    root.getStyleClass().remove("focused");
                }

            });

            seconds.focusedProperty().addListener((observable, oldValue, newValue) -> {

                if (seconds.isFocused()) {
                    root.getStyleClass().add("focused");
                } else {
                    root.getStyleClass().remove("focused");
                }

            });

            millis.focusedProperty().addListener((observable, oldValue, newValue) -> {

                if (millis.isFocused()) {
                    root.getStyleClass().add("focused");
                } else {
                    root.getStyleClass().remove("focused");
                }

            });


        } catch (Exception ignored) {
        }

    }

    public int getValue() {

        return Integer.parseInt(millis.getText())
            + 1000 * Integer.parseInt(seconds.getText())
            + 60000 * Integer.parseInt(minutes.getText())
            + 3600000 * Integer.parseInt(hours.getText());

    }

    public int getHours() {

        try {
            return Integer.parseInt(hours.getText());
        } catch (Exception e) {
            return 0;
        }

    }

    public int getMinutes() {

        try {
            return Integer.parseInt(minutes.getText());
        } catch (Exception e) {
            return 0;
        }

    }

    public int getSeconds() {

        try {
            return Integer.parseInt(seconds.getText());
        } catch (Exception e) {
            return 0;
        }

    }

    public int getMillis() {

        try {
            return Integer.parseInt(millis.getText());
        } catch (Exception e) {
            return 0;
        }

    }


    public void setValue(int value) {

        int hours   = (int) Math.floor((double) value / 3600000.0);
        int minutes = (int) Math.floor(((double) value / 60000.0) - (60.0 * hours));
        int seconds = (int) Math.floor(((double) value / 1e3) - (60.0 * minutes) - (3600.0 * hours));
        int millis  = value - (1000 * seconds) - (60000 * minutes) - (3600000 * hours);

        GUI.runNow(() -> {
            this.hours.setText(Integer.toString(hours));
            this.minutes.setText(Integer.toString(minutes));
            this.seconds.setText(Integer.toString(seconds));
            this.millis.setText(Integer.toString(millis));
        });

    }

    public void setOnChange(Field.Listener<Integer> onChange) {
        this.onChange = onChange;
    }

    public void disabled(boolean disable) {

        hours.setDisable(disable);
        minutes.setDisable(disable);
        seconds.setDisable(disable);
        millis.setDisable(disable);
        root.setDisable(disable);

    }

    public boolean disabled() {
        return hours.isDisable();
    }

    private static final UnaryOperator<TextFormatter.Change> DBL_FILTER = change -> {

        String newText = change.getControlNewText();

        if (newText.matches("[-+]?([0-9]*)?(\\.[0-9]*)?")) {
            return change;
        }

        return null;

    };

    private static final UnaryOperator<TextFormatter.Change> INT_FILTER = change -> {

        String newText = change.getControlNewText();

        if (newText.matches("[-+]?([0-9]*)?")) {
            return change;
        }

        return null;

    };
}
