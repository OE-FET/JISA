package JISA.GUI;

import javafx.application.Platform;
import javafx.scene.chart.Chart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.util.Pair;

import java.awt.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;

public class SmartChart {

    private final LineChart<Double, Double>                              chart;
    private final NumberAxis                                             xAxis;
    private final NumberAxis                                             yAxis;
    private       LinkedHashMap<Integer, XYChart.Series<Double, Double>> fullData;
    private       LinkedHashMap<Integer, XYChart.Series<Double, Double>> showData;
    private       Double                                                 minX      = null;
    private       Double                                                 maxX      = null;
    private       Double                                                 minY      = null;
    private       Double                                                 maxY      = null;
    private       XMode                                                  xMode     = XMode.SHOW_ALL;
    private       YMode                                                  yMode     = YMode.AUTO_LIMIT;
    private       int                                                    counter   = 0;
    private       HashMap<Integer, String>                               styles    = new HashMap<>();
    private       String                                                 baseStyle = "";

    public SmartChart(LineChart<Double, Double> chart, NumberAxis xAxis, NumberAxis yAxis) {

        this.chart = chart;
        this.xAxis = xAxis;
        this.yAxis = yAxis;

    }

    public int createSeries(String name, Color colour) {

        XYChart.Series<Double, Double> series = new XYChart.Series<>();
        chart.getData().add(series);
        fullData.put(counter++, series);

        Platform.runLater(() -> {
            chart.getData().add(series);
        });

        if (colour != null) {
            setSeriesColour(counter, colour);
        }

        return counter;
    }

    public void setSeriesColour(int series, Color colour) {

        styles.put(
                series,
                String.format("CHART_COLOR_%d: rgba(%f, %f, %f);", series, colour.getRed() * 255D, colour.getGreen() * 255D, colour.getBlue() * 255D)
        );

        Platform.runLater(() -> {
            chart.setStyle(baseStyle + " " + String.join(" ", styles.values()));
        });

    }

    public enum XMode {
        SHOW_ALL,
        TRACK_LATEST,
        TRACK_LATEST_DISCARD,
        MANUAL_LIMIT
    }

    public enum YMode {
        AUTO_LIMIT,
        MANUAL_LIMIT
    }

    private void onUpdate() {

    }

}
