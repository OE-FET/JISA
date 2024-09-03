package jisa.gui.controls;


import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;
import jisa.gui.GUI;

import java.util.function.UnaryOperator;

public class DoubleInput extends HBox {

    public TextField mantissa;
    public TextField exponent;
    public int       leading = 3;
    public HBox      root;

    private final Property<Double> valueProperty = new SimpleObjectProperty<>(0.0);

    public DoubleInput() {

        try {

            FXMLLoader loader = new FXMLLoader(GUI.getFXML("DoubleInput"));

            loader.setController(this);
            root = loader.load();

            GUI.runNow(() -> getChildren().add(root));
            HBox.setHgrow(root, Priority.ALWAYS);

            mantissa.setTextFormatter(new TextFormatter<Double>(new DoubleStringConverter(), 0.0, DBL_FILTER));
            exponent.setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(), 0, INT_FILTER));

            root.setPadding(Insets.EMPTY);

            mantissa.textProperty().addListener((observable, oldValue, newValue) -> {

                double value = getValue();

                if (value != valueProperty.getValue()) {
                    valueProperty.setValue(value);
                }

            });

            exponent.textProperty().addListener((observable, oldValue, newValue) -> {

                double value = getValue();

                if (value != valueProperty.getValue()) {
                    valueProperty.setValue(value);
                }

            });

            valueProperty.addListener((observable, oldValue, newValue) -> {

                double value = getValue();

                if (value != newValue) {
                    setValue(newValue);
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
            return Double.parseDouble(mantissa.getText());
        } catch (Exception e) {
            return 0.0;
        }
    }

    public int getExponent() {
        try {
            return Integer.parseInt(exponent.getText());
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

    public void disabled(boolean disable) {

        mantissa.setDisable(disable);
        exponent.setDisable(disable);
        root.setDisable(disable);

    }

    public boolean disabled() {
        return mantissa.isDisable();
    }

    public Property<Double> valueProperty() {
        return valueProperty;
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
