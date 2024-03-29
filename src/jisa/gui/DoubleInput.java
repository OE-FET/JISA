package jisa.gui;


import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;

import java.util.function.UnaryOperator;

public class DoubleInput extends HBox {

    public TextField    mantissa;
    public TextField    exponent;
    public int leading = 3;
    public HBox         root;
    public DoubleChange onChange = null;

    public DoubleInput() {

        try {
            FXMLLoader loader = new FXMLLoader(DoubleInput.class.getResource("fxml/DoubleInput.fxml"));
            loader.setController(this);
            root = loader.load();
            GUI.runNow(() -> getChildren().add(root));
            HBox.setHgrow(root, Priority.ALWAYS);
            mantissa.setTextFormatter(new TextFormatter<Double>(new DoubleStringConverter(), 0.0, DBL_FILTER));
            exponent.setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(), 0, INT_FILTER));
            root.setPadding(Insets.EMPTY);

            mantissa.textProperty().addListener((observable, oldValue, newValue) -> {
                if (onChange != null) {
                    onChange.change(getValue());
                }
            });

            exponent.textProperty().addListener((observable, oldValue, newValue) -> {
                if (onChange != null) {
                    onChange.change(getValue());
                }
            });

            mantissa.focusedProperty().addListener((observable, oldValue, newValue) -> {

                if (mantissa.isFocused()) {
                    root.getStyleClass().add("focused");
                } else {
                    root.getStyleClass().remove("focused");
                }

            });

            exponent.focusedProperty().addListener((observable, oldValue, newValue) -> {

                if (exponent.isFocused()) {
                    root.getStyleClass().add("focused");
                } else {
                    root.getStyleClass().remove("focused");
                }

            });

        } catch (Exception ignored) {
        }

    }

    public double getValue() {
        return Double.parseDouble(String.format("%sE%s", mantissa.getText(), exponent.getText()));
    }

    public double getMantissa() {
        try {
            return Double.valueOf(mantissa.getText());
        } catch (Exception e) {
            return 0.0;
        }
    }

    public int getExponent() {
        try {
            return Integer.valueOf(exponent.getText());
        } catch (Exception e) {
            return 0;
        }
    }

    public void setValue(double value) {

        int    exponent = value == 0 ? 0 : (int) (leading * Math.floor(Math.log10(Math.abs(value)) / leading));
        double mantissa = value / Math.pow(10, exponent);

        this.mantissa.setText(String.format("%f", mantissa));
        this.exponent.setText(String.format("%d", exponent));

    }

    public void setOnChange(DoubleChange onChange) {
        this.onChange = onChange;
    }

    public interface DoubleChange {

        void change(double newValue);

    }

    public void disabled(boolean disable) {

        mantissa.setDisable(disable);
        exponent.setDisable(disable);
        root.setDisable(disable);

    }

    public boolean disabled() {
        return mantissa.isDisable();
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
