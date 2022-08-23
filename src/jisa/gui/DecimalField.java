package jisa.gui;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.util.converter.DoubleStringConverter;

import java.util.function.UnaryOperator;

public class DecimalField extends TextField
{

    private static final UnaryOperator<TextFormatter.Change> DBL_FILTER = change -> {
        String newText = change.getControlNewText();
        if (newText.matches("[-+]?([0-9]*)?(\\.[0-9]*)?")) {
            return change;
        }
        return null;
    };

    public DecimalField() {
        super();
        setTextFormatter(new TextFormatter<Double>(new DoubleStringConverter(), 0.0, DBL_FILTER));
    }

    public Double getDecimalValue() {
        return Double.parseDouble(getText());
    }

}

