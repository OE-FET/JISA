package jisa.gui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.util.LinkedHashMap;
import java.util.Map;


public class JISALegend extends GridPane {

    private int                                        cols      = 0;
    private int                                        rows      = 10;
    private int                                        r         = 0;
    private int                                        c         = 0;
    private Map<XYChart.Series<Double, Double>, Label> seriesMap = new LinkedHashMap<>();

    public JISALegend() {
        getStyleClass().setAll("chart-legend");
        setAlignment(Pos.CENTER_LEFT);
        setHgap(5.0);
        setVgap(5.0);
    }

    public void setMaxColumns(int columns) {
        rows = 0;
        cols = columns;
        updateGridding();
    }

    public void setMaxRows(int rows) {
        this.rows = rows;
        cols      = 0;
        updateGridding();
    }

    public int getMaxColumns() {
        return cols;
    }

    public int getMaxRows() {
        return rows;
    }

    public void addItem(XYChart.Series<Double, Double> series, Node node) {

        if (series == null || node == null) {
            return;
        }

        Label name = new Label(series.getName());

        name.setGraphic(node);

        if (seriesMap.containsKey(series)) {

            Label old = seriesMap.get(series);
            seriesMap.put(series, name);
            int r = GridPane.getRowIndex(old);
            int c = GridPane.getColumnIndex(old);

            GUI.runNow(() -> {
                getChildren().remove(old);
                add(name, c, r);
            });

        } else {

            GUI.runNow(() -> add(name, c, r));

            if (rows == 0) {

                c++;

                if (c >= cols) {
                    c = 0;
                    r++;
                }

            } else {

                r++;

                if (r >= rows) {
                    r = 0;
                    c++;
                }

            }

        }

    }

    public void removeItem(XYChart.Series<Double, Double> toRemove) {
        GUI.runNow(() -> getChildren().remove(seriesMap.get(toRemove)));
        updateGridding();
    }

    public void clear() {
        GUI.runNow(() -> getChildren().clear());
        r = 0;
        c = 0;
    }

    private void updateGridding() {

        r = 0;
        c = 0;

        if (rows == 0) {

            GUI.runNow(() -> {

                for (Node node : getChildren()) {

                    GridPane.setRowIndex(node, r);
                    GridPane.setColumnIndex(node, c);

                    c++;

                    if (c >= cols) {

                        c = 0;
                        r++;

                    }

                }

            });

        } else {

            GUI.runNow(() -> {

                for (Node node : getChildren()) {

                    GridPane.setRowIndex(node, r);
                    GridPane.setColumnIndex(node, c);

                    r++;

                    if (r >= rows) {

                        r = 0;
                        c++;

                    }

                }

            });

        }

    }

}
