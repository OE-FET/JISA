package jisa.gui.controls;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.util.converter.IntegerStringConverter;

import java.util.function.UnaryOperator;

public class IntegerField extends TextField
{

    private static final UnaryOperator<TextFormatter.Change> INT_FILTER = change -> {
        String newText = change.getControlNewText();
        if (newText.matches("[\\-\\+]?([0-9]*)?")) {
            return change;
        }
        return null;
    };

    public IntegerField() {
        super();
        setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(), 0, INT_FILTER));
    }


    public int getIntValue() {
        try {
            return Integer.valueOf(getText());
        } catch (Exception e) {
            return 0;
        }
    }

}

