package jisa.gui;

import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

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

            for (int x = 0; x < columns; x++) {
                gridPane.add(new Label(String.format("%d", x + 1)), x + 1, 0);
            }

            for (int y = 0; y < rows; y++) {
                gridPane.add(new Label(String.format("%d", y + 1)), 0, y + 1);
            }

            for (int x = 0; x < columns; x++) {

                for (int y = 0; y < rows; y++) {

                    CheckBox box = new CheckBox();
                    boxes[x][y] = box;
                    box.setSelected(true);
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
