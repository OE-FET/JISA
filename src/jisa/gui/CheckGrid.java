package jisa.gui;

import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.GridPane;

import java.util.Arrays;

public class CheckGrid extends JFXElement {

    private final GridPane     gridPane;
    private       CheckBox[][] boxes = new CheckBox[0][0];

    public CheckGrid(String title, int columns, int rows) {

        super(title, new GridPane());
        gridPane = (GridPane) getNode().getCenter();
        gridPane.setVgap(5.0);
        gridPane.setHgap(5.0);
        layoutGrid(columns, rows);

    }

    protected synchronized void layoutGrid(int columns, int rows) {

        boolean[][] values = getValues();
        boxes = new CheckBox[columns][rows];

        GUI.runNow(() -> {

            gridPane.getChildren().clear();

            Button all = new Button("X");
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

        setValues(values);

    }

    public synchronized void setSize(int columns, int rows) {
        layoutGrid(columns, rows);
    }

    public synchronized boolean isChecked(int column, int row) {

        try {
            return boxes[column][row].isSelected();
        } catch (IndexOutOfBoundsException e) {
            return false;
        }

    }

    public synchronized void setChecked(int column, int row, boolean checked) {
        GUI.runNow(() -> boxes[column][row].setSelected(checked));
    }

    public synchronized void setAll(boolean checked) {

        GUI.runNow(() -> {

            for (CheckBox[] column : boxes) {

                for (CheckBox box : column) {
                    box.setSelected(checked);
                }

            }

        });

    }

    public synchronized void setColumn(int column, boolean checked) {

        for (int y = 0; y < getRowCount(); y++) {
            setChecked(column, y, checked);
        }

    }

    public synchronized void setRow(int row, boolean checked) {

        for (int x = 0; x < getColumnCount(); x++) {
            setChecked(x, row, checked);
        }

    }

    public synchronized int getRowCount() {

        try {
            return boxes[0].length;
        } catch (IndexOutOfBoundsException e) {
            return 0;
        }

    }

    public synchronized int getColumnCount() {
        return boxes.length;
    }

    public synchronized boolean[][] getValues() {

        boolean[][] values = new boolean[getColumnCount()][getRowCount()];

        for (int x = 0; x < values.length; x++) {
            for (int y = 0; y < values[0].length; y++) {
                values[x][y] = boxes[x][y] == null || boxes[x][y].isSelected();
            }
        }

        return values;

    }

    public synchronized void setValues(boolean[][] values) {

        GUI.runNow(() -> {

            for (int x = 0; x < Math.min(boxes.length, values.length); x++) {
                for (int y = 0; y < Math.min(boxes[0].length, values[0].length); y++) {
                    boxes[x][y].setSelected(values[x][y]);
                }
            }

        });

    }

}
