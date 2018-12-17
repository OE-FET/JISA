package JISA.GUI;

import JISA.Experiment.Result;
import JISA.Experiment.ResultTable;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Plot extends JFXWindow implements Gridable, Clearable {

    public  BorderPane                                             pane;
    private LinkedHashMap<Integer, XYChart.Series<Double, Double>> data     = new LinkedHashMap<>();
    private HashMap<Integer, Boolean>                              auto     = new HashMap<>();
    private ArrayList<HashMap<Double, Integer>>                    maps     = new ArrayList<>();
    public  LineChart                                              chart;
    public  NumberAxis                                             xAxis;
    public  NumberAxis                                             yAxis;
    private double                                                 xRange   = 0;
    private double                                                 maxX     = Double.NEGATIVE_INFINITY;
    private double                                                 minX     = Double.POSITIVE_INFINITY;
    private double                                                 maxY     = Double.NEGATIVE_INFINITY;
    private double                                                 minY     = Double.POSITIVE_INFINITY;
    private double                                                 maxRange = -1;

    /**
     * Creates an empty plot from the given title, x-axis label, and y-axis label.
     *
     * @param title  Title of the plot
     * @param xLabel X-Axis Label
     * @param yLabel Y-Axis Lable
     */
    public Plot(String title, String xLabel, String yLabel) {

        super(title, "FXML/PlotWindow.fxml", true);

        GUI.runNow(() -> {
            chart.setStyle("-fx-background-color: white;");
            xAxis.setLabel(xLabel);
            yAxis.setLabel(yLabel);
            chart.setTitle(title);
            xAxis.setForceZeroInRange(false);
            yAxis.setForceZeroInRange(false);
            xAxis.setAnimated(false);
            yAxis.setAnimated(false);
        });

    }

    /**
     * Creates a plot that automatically tracks and plots a ResultTable (ResultList or ResultStream) object, specifying
     * which columns to plot, the name of the data series and its colour.
     *
     * @param title      Title for the plot
     * @param list       The ResultTable (ResultList/ResultStream) to track
     * @param xData      Column number to plot on the x-axis
     * @param yData      Column number to plot on the y-axis
     * @param seriesName The name of the data series to create
     * @param colour     The colour to use for the plotted points and line
     */
    public Plot(String title, ResultTable list, int xData, int yData, String seriesName, Color colour) {
        this(title, list.getTitle(xData), list.getTitle(yData));
        watchList(list, xData, yData, seriesName, colour);
    }

    /**
     * Creates a plot that automatically tracks and plots a ResultTable (ResultList or ResultStream) object, specifying
     * which columns to plot but also specifying which column to use to sort the data into separate series.
     *
     * @param title Title for the plot
     * @param list  The ResultTable to track
     * @param xData Column number to plot on the x-axis
     * @param yData Column number to plot on the y-axis
     * @param sData Column number to use for sorting into series
     */
    public Plot(String title, ResultTable list, int xData, int yData, int sData) {
        this(title, list.getTitle(xData), list.getTitle(yData));
        watchList(list, xData, yData, sData);
    }

    /**
     * Creates a plot that automatically tracks and plots a ResultTable (ResultList or ResultStream) object, specifying
     * which two columns to plot. Colour and series name automatically chosen.
     *
     * @param title Title for the plot
     * @param list  The ResultTable to track
     * @param xData Column number to plot on the x-axis
     * @param yData Column number to plot on the y-axis
     */
    public Plot(String title, ResultTable list, int xData, int yData) {
        this(title, list, xData, yData, "Data", Color.RED);
    }

    /**
     * Creates a plot that automatically tracks and plots the first two columns of a ResultTable. Column 0 on the x-axis
     * and column 1 on the y-axis. Series name and colour automatically chosen.
     *
     * @param title The title of the plot
     * @param list  The ResultTable to track
     */
    public Plot(String title, ResultTable list) {
        this(title, list, 0, 1);
    }

    /**
     * Sets the bounds on the x-axis.
     *
     * @param min Minimum value to show on x-axis
     * @param max Maximum value to show on x-axis
     */
    public void setXLimit(final double min, final double max) {
        GUI.runNow(() -> {
            xAxis.setAutoRanging(false);
            xAxis.setForceZeroInRange(false);
            xAxis.setLowerBound(min);
            xAxis.setUpperBound(max);
        });
    }

    /**
     * Sets the bounds on the y-axis.
     *
     * @param min Minimum value to show on y-axis
     * @param max Maximum value to show on y-axis
     */
    public void setYLimit(final double min, final double max) {
        GUI.runNow(() -> {
            yAxis.setAutoRanging(false);
            yAxis.setForceZeroInRange(false);
            yAxis.setLowerBound(min);
            yAxis.setUpperBound(max);
        });
    }

    /**
     * Sets whether or not to show markers on the plot.
     *
     * @param show Show markers?
     */
    public void showMarkers(boolean show) {
        GUI.runNow(() -> chart.setCreateSymbols(show));
    }

    public void showLegend(boolean show) {
        GUI.runNow(()-> chart.setLegendVisible(show));
    }

    /**
     * Sets the x-axis to automatically choose its bounds.
     */
    public void autoXLimit() {
        Platform.runLater(() -> xAxis.setAutoRanging(true));
    }

    /**
     * Sets the y-axis to automatically choose its bounds.
     */
    public void autoYLimit() {
        Platform.runLater(() -> yAxis.setAutoRanging(true));
    }

    /**
     * Sets the max x-axis range to show on the plot. Points that are outside this range from the maximum x-axis value
     * will be removed from the plot automatically.
     *
     * @param range The range, set to -1 to disable
     */
    public void setMaxRange(double range) {
        maxRange = range;
    }

    /**
     * Set the plot to automatically track and plot a ResultTable object, specifying which columns to plot, the name of
     * the series and its colour.
     *
     * @param list       The ResultTable to track
     * @param xData      Column number to plot on the x-axis
     * @param yData      Column number to plot on the y-axis
     * @param seriesName Name of the data series
     * @param colour     Colour of the series (eg try: Color.RED or Color.GREEN, etc)
     */
    public void watchList(final ResultTable list, final int xData, final int yData, String seriesName, Color colour) {

        final int series = createSeries(seriesName, colour);

        for (Result row : list) {
            addPoint(series, row.get(xData), row.get(yData));
        }

        list.addOnUpdate((r) -> addPoint(series, r.get(xData), r.get(yData)));

        list.addClearable(this);

    }

    /**
     * Set the plot to automatically track and plot a ResultTable object, split into multiple data series, specifying
     * which columns to plot and which column to use to sort into series.
     *
     * @param list  The ResultTable to track
     * @param xData Column number to plot on the x-axis
     * @param yData Column number to plot on the y-axis
     * @param sData Column number to use for series sorting
     */
    public void watchList(final ResultTable list, final int xData, final int yData, final int sData) {

        final HashMap<Double, Integer> map = new HashMap<>();
        maps.add(map);
        for (Result row : list) {

            int series;
            if (map.containsKey(row.get(sData))) {
                series = map.get(row.get(sData));
            } else {
                series = createSeries(String.format("%s %s", row.get(sData), list.getUnits(sData)), null, true);
            }

            map.put(row.get(sData), series);
            addPoint(series, row.get(xData), row.get(yData));

        }

        list.addOnUpdate((row) -> {

            int series;
            if (map.containsKey(row.get(sData))) {
                series = map.get(row.get(sData));
            } else {
                series = createSeries(String.format("%s %s", row.get(sData), list.getUnits(sData)), null, true);
            }

            map.put(row.get(sData), series);
            addPoint(series, row.get(xData), row.get(yData));

        });

        list.addClearable(this);

    }

    /**
     * Creates a new data series, returning its number.
     *
     * @param name   The series name
     * @param colour The series colour (eg Color.RED, Color.GREEN, Color.BLUE etc)
     *
     * @return The id of the new series
     */
    public int createSeries(String name, Color colour) {
        return createSeries(name, colour, false);
    }

    public int createSeries(String name, Color colour, boolean a) {

        XYChart.Series<Double, Double> series = new XYChart.Series<>();

        series.setName(name);

        int index = data.size();
        data.put(index, series);
        auto.put(index, a);
        Platform.runLater(() -> {
            chart.getData().add(series);

            if (colour != null) {
                chart.setStyle(chart.getStyle().concat(
                        String.format("CHART_COLOR_%d: rgba(%f, %f, %f);", index + 1, colour.getRed() * 255, colour.getGreen() * 255, colour.getBlue() * 255)

                ));
            }
        });

        return index;

    }

    /**
     * Add a point to the plot (in the last series created).
     *
     * @param x X-Value
     * @param y Y-Value
     */
    public void addPoint(double x, double y) {

        if (data.size() == 0) {
            createSeries("Data", Color.RED);
        }

        addPoint(data.size() - 1, x, y);

    }

    /**
     * Add a point to the plot in the specified series.
     *
     * @param series The series id number, returned by createSeries(...)
     * @param x      X-Value
     * @param y      Y-Value
     */
    public void addPoint(int series, double x, double y) {

        Platform.runLater(() -> {
            data.get(series).getData().add(
                    new XYChart.Data<>(x, y)
            );

            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);

            if (maxRange > 0) {
                xAxis.setAutoRanging(false);
                xAxis.setAnimated(false);
                yAxis.setAnimated(false);

                data.get(series).getData().removeIf(data -> data.getXValue() < (maxX - maxRange));

                xAxis.setLowerBound(Math.max(minX, maxX - maxRange));
                xAxis.setUpperBound(maxX);
            }

        });

    }

    @Override
    public Pane getPane() {
        return pane;
    }

    @Override
    public void clear() {


        for (HashMap<Double, Integer> map : maps) {
            map.clear();
        }

        GUI.runNow(() -> {

            chart.getData().removeAll(chart.getData());

            for (Integer i : data.keySet()) {

                if (!auto.get(i)) {
                    data.get(i).getData().clear();
                    XYChart.Series<Double, Double> series = new XYChart.Series<>();
                    series.setName(data.get(i).getName());
                    chart.getData().add(series);
                    data.put(i, series);
                } else {
                    data.get(i).getData().clear();
                    data.remove(i);
                    auto.remove(i);
                }

            }


        });

    }

}
