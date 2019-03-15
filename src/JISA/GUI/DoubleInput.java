package JISA.GUI;


import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
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
    public HBox         root;
    public DoubleChange onChange = null;

    public DoubleInput() {

        try {
            FXMLLoader loader = new FXMLLoader(DoubleInput.class.getResource("FXML/DoubleInput.fxml"));
            loader.setController(this);
            root = loader.load();
            GUI.runNow(() -> getChildren().add(root));
            HBox.setHgrow(root, Priority.ALWAYS);
            mantissa.setTextFormatter(new TextFormatter<Double>(new DoubleStringConverter(), 0.0, DBL_FILTER));
            exponent.setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(), 0, INT_FILTER));

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
                    root.setEffect(new DropShadow(3, Color.valueOf("#039ED3")));
                } else {
                    root.getStyleClass().remove("focused");
                    root.setEffect(null);
                }

            });

            exponent.focusedProperty().addListener((observable, oldValue, newValue) -> {

                if (exponent.isFocused()) {
                    root.getStyleClass().add("focused");
                    root.setEffect(new DropShadow(3, Color.valueOf("#039ED3")));
                } else {
                    root.getStyleClass().remove("focused");
                    root.setEffect(null);
                }

            });

        } catch (Exception ignored) {
        }

    }

    public double getValue() {
        return getMantissa() * Math.pow(10.0, getExponent());
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

        int    exponent = value == 0 ? 0 : (int) (3 * Math.floor(Math.log10(Math.abs(value)) / 3));
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
