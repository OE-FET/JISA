package jisa.gui;

import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import jisa.devices.interfaces.*;

import java.util.Arrays;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

public class CheckGrid extends JFXElement {

    private final GridPane     gridPane;
    private       CheckBox[][] boxes;

    public CheckGrid(String title, int columns, int rows) {

        super(title, new GridPane());
        gridPane = (GridPane) getNode().getCenter();
        gridPane.setVgap(5.0);
        gridPane.setHgap(5.0);
        layoutGrid(columns, rows);

    }

    protected void layoutGrid(int columns, int rows) {

        boxes = new CheckBox[columns][rows];

        GUI.runNow(() -> {

            gridPane.getChildren().clear();

            Button    all = new Button("X");
            all.setMinWidth(30);
            all.setMinHeight(30);
            all.setOnAction(e -> Arrays.stream(boxes).flatMap(Arrays::stream).forEach(c -> c.setSelected(!c.isSelected())));
            gridPane.add(all, 0, 0);

            for (int x = 0; x < columns; x++) {
                final int fX     = x;
                Button    button = new Button(String.format("%d", x + 1));
                button.setMinWidth(30);
                button.setMinHeight(30);
                button.setOnAction(e -> setColumn(fX, Arrays.stream(boxes[fX]).filter(CheckBox::isSelected).count() < getRowCount() / 2));
                gridPane.add(button, x + 1, 0);
            }

            for (int y = 0; y < rows; y++) {
                final int fY     = y;
                Button    button = new Button(String.format("%d", y + 1));
                button.setMinWidth(30);
                button.setMinHeight(30);
                button.setOnAction(e -> setRow(fY, Arrays.stream(boxes).map(c -> c[fY]).filter(CheckBox::isSelected).count() < getColumnCount() / 2));
                gridPane.add(button, 0, y + 1);
            }

            for (int x = 0; x < columns; x++) {

                for (int y = 0; y < rows; y++) {

                    CheckBox box = new CheckBox();
                    boxes[x][y] = box;
                    box.setSelected(true);
                    GridPane.setHalignment(box, HPos.CENTER);
                    GridPane.setValignment(box, VPos.CENTER);
                    gridPane.add(box, x + 1, y + 1);

                }

            }

        });

    }

    public void setSize(int columns, int rows) {
        layoutGrid(columns, rows);
    }

    public boolean isChecked(int column, int row) {

        try {
            return boxes[column][row].isSelected();
        } catch (IndexOutOfBoundsException e) {
            return false;
        }

    }

    public void setChecked(int column, int row, boolean checked) {
        GUI.runNow(() -> boxes[column][row].setSelected(checked));
    }

    public void setAll(boolean checked) {

        GUI.runNow(() -> {

            for (CheckBox[] column : boxes) {

                for (CheckBox box : column) {
                    box.setSelected(checked);
                }

            }

        });

    }

    public void setColumn(int column, boolean checked) {

        for (int y = 0; y < getRowCount(); y++) {
            setChecked(column, y, checked);
        }

    }

    public void setRow(int row, boolean checked) {

        for (int x = 0; x < getColumnCount(); x++) {
            setChecked(x, row, checked);
        }

    }

    public int getRowCount() {

        try {
            return boxes[0].length;
        } catch (IndexOutOfBoundsException e) {
            return 0;
        }

    }

    public int getColumnCount() {
        return boxes.length;
    }

    public boolean[][] getValues() {

        boolean[][] values = new boolean[boxes.length][boxes[0].length];

        for (int x = 0; x < boxes.length; x++) {

            for (int y = 0; y < boxes[0].length; y++) {

                values[x][y] = boxes[x][y].isSelected();

            }

        }

        return values;

    }

}
