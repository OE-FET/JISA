package JISA.GUI;

import JISA.Experiment.Result;
import JISA.Experiment.ResultTable;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class Plot extends JFXWindow implements Gridable, Clearable {

    public  BorderPane                                             pane;
    public  ToolBar                                                toolbar;
    public  Pane                                                   stack;
    public  LineChart<Double, Double>                              chart;
    public  NumberAxis                                             xAxis;
    public  NumberAxis                                             yAxis;
    public  ToggleButton                                           autoButton;
    public  ToggleButton                                           zoomButton;
    public  ToggleButton                                           dragButton;
    private LinkedHashMap<Integer, XYChart.Series<Double, Double>> data     = new LinkedHashMap<>();
    private HashMap<Integer, Boolean>                              auto     = new HashMap<>();
    private ArrayList<HashMap<Double, Integer>>                    maps     = new ArrayList<>();
    private SmartChart                                             controller;
    private double                                                 xRange   = 0;
    private double                                                 maxX     = Double.NEGATIVE_INFINITY;
    private double                                                 minX     = Double.POSITIVE_INFINITY;
    private double                                                 maxY     = Double.NEGATIVE_INFINITY;
    private double                                                 minY     = Double.POSITIVE_INFINITY;
    private double                                                 maxRange = -1;
    private Rectangle                                              rect;


    /**
     * Creates an empty plot from the given title, x-axis label, and y-axis label.
     *
     * @param title  Title of the plot
     * @param xLabel X-Axis Label
     * @param yLabel Y-Axis Lable
     */
    public Plot(String title, String xLabel, String yLabel) {

        super(title, Plot.class.getResource("FXML/PlotWindow.fxml"));

        GUI.runNow(() -> {
            chart.setStyle("-fx-background-color: white;");
            xAxis.setLabel(xLabel);
            yAxis.setLabel(yLabel);
            chart.setTitle(title);
            xAxis.setForceZeroInRange(false);
            yAxis.setForceZeroInRange(false);
            xAxis.setAnimated(false);
            yAxis.setAnimated(false);
            xAxis.setAutoRanging(true);
            yAxis.setAutoRanging(true);

            rect = new Rectangle();
            rect.setFill(Color.CORNFLOWERBLUE.deriveColor(0, 1, 1, 0.5));

            rect.setVisible(false);
            rect.setManaged(false);

            stack.getChildren().add(rect);
            toolbar.setVisible(false);
            toolbar.setManaged(false);

        });

        controller = new SmartChart(chart, xAxis, yAxis);
    }

    public void showToolbar(boolean flag) {
        toolbar.setManaged(flag);
        toolbar.setVisible(flag);
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

    public void reduce() {
        GUI.runNow(controller::reduce);
    }

    public void setZoomMode() {

        zoomButton.setSelected(true);
        autoButton.setSelected(false);
        dragButton.setSelected(false);

        final Node                          background = chart.lookup(".chart-plot-background");
        final SimpleObjectProperty<Point2D> start      = new SimpleObjectProperty<>();
        final SimpleObjectProperty<Point2D> first      = new SimpleObjectProperty<>();

        chart.setOnMousePressed(event -> {
            Point2D pointX = xAxis.sceneToLocal(event.getSceneX(), event.getSceneY());
            Point2D pointY = yAxis.sceneToLocal(event.getSceneX(), event.getSceneY());
            start.set(new Point2D(event.getX(), event.getY()));
            first.set(new Point2D(pointX.getX(), pointY.getY()));
            rect.setVisible(true);
            rect.setManaged(true);
        });

        chart.setOnMouseDragged(event -> {

            final double x = Math.max(0, Math.min(stack.getWidth(), event.getX()));
            final double y = Math.max(0, Math.min(stack.getHeight(), event.getY()));
            rect.setX(Math.min(x, start.get().getX()));
            rect.setY(Math.min(y, start.get().getY()));
            rect.setWidth(Math.abs(x - start.get().getX()));
            rect.setHeight(Math.abs(y - start.get().getY()));

        });

        chart.setOnMouseReleased(event -> {

            final Point2D pointX = xAxis.sceneToLocal(event.getSceneX(), event.getSceneY());
            final Point2D pointY = yAxis.sceneToLocal(event.getSceneX(), event.getSceneY());

            final double minX = xAxis.getValueForDisplay(Math.min(first.get().getX(), pointX.getX())).doubleValue();
            final double maxX = xAxis.getValueForDisplay(Math.max(first.get().getX(), pointX.getX())).doubleValue();
            final double minY = yAxis.getValueForDisplay(Math.min(first.get().getY(), pointY.getY())).doubleValue();
            final double maxY = yAxis.getValueForDisplay(Math.max(first.get().getY(), pointY.getY())).doubleValue();

            controller.setLimits(Math.min(minX, maxX), Math.max(minX, maxX), Math.min(minY, maxY), Math.max(minY, maxY));

            rect.setWidth(0);
            rect.setHeight(0);
            rect.setVisible(false);
            rect.setManaged(false);

        });

    }

    public void setDragMode() {

        zoomButton.setSelected(false);
        autoButton.setSelected(false);
        dragButton.setSelected(true);

        final Node                          background = chart.lookup(".chart-plot-background");
        final SimpleObjectProperty<Point2D> start      = new SimpleObjectProperty<>();
        final SimpleObjectProperty<Point2D> startMax   = new SimpleObjectProperty<>();
        final SimpleObjectProperty<Point2D> startMin   = new SimpleObjectProperty<>();


        chart.setOnMousePressed(event -> {
            Point2D pointX = xAxis.sceneToLocal(event.getSceneX(), event.getSceneY());
            Point2D pointY = yAxis.sceneToLocal(event.getSceneX(), event.getSceneY());
            startMin.set(new Point2D(xAxis.getLowerBound(), yAxis.getLowerBound()));
            startMax.set(new Point2D(xAxis.getUpperBound(), yAxis.getUpperBound()));
            start.set(new Point2D(pointX.getX(), pointY.getY()));
        });

        chart.setOnMouseDragged(event -> {

            Point2D pointX = xAxis.sceneToLocal(event.getSceneX(), event.getSceneY());
            Point2D pointY = yAxis.sceneToLocal(event.getSceneX(), event.getSceneY());

            final double diffX = xAxis.getValueForDisplay(pointX.getX()).doubleValue() - xAxis.getValueForDisplay(start.get().getX()).doubleValue();
            final double diffY = yAxis.getValueForDisplay(pointY.getY()).doubleValue() - yAxis.getValueForDisplay(start.get().getY()).doubleValue();

            final double minX = startMin.get().getX() - diffX;
            final double minY = startMin.get().getY() - diffY;

            final double maxX = startMax.get().getX() - diffX;
            final double maxY = startMax.get().getY() - diffY;

            controller.setLimits(Math.min(minX, maxX), Math.max(minX, maxX), Math.min(minY, maxY), Math.max(minY, maxY));

        });

        chart.setOnMouseReleased(null);

    }

    public void setAutoMode() {

        zoomButton.setSelected(false);
        autoButton.setSelected(true);
        dragButton.setSelected(false);

        chart.setOnMousePressed(null);
        chart.setOnMouseDragged(null);
        chart.setOnMouseReleased(null);

        controller.autoLimits();

    }

    /**
     * Sets the bounds on the x-axis.
     *
     * @param min Minimum value to show on x-axis
     * @param max Maximum value to show on x-axis
     */
    public void setXLimits(final double min, final double max) {
        controller.setXLimits(min, max);
    }

    /**
     * Sets the bounds on the y-axis.
     *
     * @param min Minimum value to show on y-axis
     * @param max Maximum value to show on y-axis
     */
    public void setYLimits(final double min, final double max) {
        controller.setYLimits(min, max);
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
        GUI.runNow(() -> chart.setLegendVisible(show));
    }

    /**
     * Sets the x-axis to automatically choose its bounds.
     */
    public void autoXLimits() {
        controller.autoXLimits();
    }

    /**
     * Sets the y-axis to automatically choose its bounds.
     */
    public void autoYLimits() {
        Platform.runLater(() -> yAxis.setAutoRanging(true));
        controller.autoYLimits();
    }

    public void setXAutoRemove(double range) {
        controller.setXAutoRemove(range);
    }

    public void setYAutoRemove(double range) {
        controller.setYAutoRemove(range);
    }

    public void stopXAutoRemove() {
        controller.stopXAutoRemove();
    }

    public void stopYAutoRemove() {
        controller.stopYAutoRemove();
    }

    public void setXAutoTrack(double range) {
        controller.setTrackingX(range);
    }

    public void setYAutoTrack(double range) {
        controller.setTrackingY(range);
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
    public synchronized void watchList(final ResultTable list, final int xData, final int yData, String seriesName, Color colour) {

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
    public synchronized void watchList(final ResultTable list, final int xData, final int yData, final int sData) {

        final HashMap<Double, Integer> map = new HashMap<>();
        maps.add(map);
        for (Result row : list) {

            int series;
            if (map.containsKey(row.get(sData))) {
                series = map.get(row.get(sData));
            } else {
                series = controller.createSeriesAuto(String.format("%s %s", row.get(sData), list.getUnits(sData)));
            }

            map.put(row.get(sData), series);
            addPoint(series, row.get(xData), row.get(yData));

        }

        list.addOnUpdate((row) -> {

            int series;
            if (map.containsKey(row.get(sData))) {
                series = map.get(row.get(sData));
            } else {
                series = controller.createSeriesAuto(String.format("%s %s", row.get(sData), list.getUnits(sData)));
            }

            map.put(row.get(sData), series);
            addPoint(series, row.get(xData), row.get(yData));

        });

        list.addClearable(this);

    }

    public synchronized void watchList(final ResultTable list, final int xData, final int yData, final int fData, final double fValue, String seriesName, Color colour) {

        final int series = createSeries(seriesName, colour);

        for (Result row : list) {
            if (row.get(fData) == fValue) {
                addPoint(series, row.get(xData), row.get(yData));
            }
        }

        list.addOnUpdate((r) -> {
            if (r.get(fData) == fValue) {
                addPoint(series, r.get(xData), r.get(yData));
            }
        });

        list.addClearable(this);

    }

    public synchronized void watchList(final ResultTable list, final int xData, final int yData, final int sData, final int fData, final double fValue) {

        final HashMap<Double, Integer> map = new HashMap<>();
        maps.add(map);
        for (Result row : list) {

            if (row.get(fData) != fValue) {
                continue;
            }

            int series;
            if (map.containsKey(row.get(sData))) {
                series = map.get(row.get(sData));
            } else {
                series = controller.createSeriesAuto(String.format("%s %s", row.get(sData), list.getUnits(sData)));
            }

            map.put(row.get(sData), series);
            addPoint(series, row.get(xData), row.get(yData));

        }

        list.addOnUpdate((row) -> {

            if (row.get(fData) != fValue) {
                return;
            }

            int series;
            if (map.containsKey(row.get(sData))) {
                series = map.get(row.get(sData));
            } else {
                series = controller.createSeriesAuto(String.format("%s %s", row.get(sData), list.getUnits(sData)));
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
        return controller.createSeries(name, colour);
    }

    /**
     * Add a point to the plot (in the last series created).
     *
     * @param x X-Value
     * @param y Y-Value
     */
    public void addPoint(double x, double y) {
        controller.addPoint(x, y);
    }

    /**
     * Add a point to the plot in the specified series.
     *
     * @param series The series id number, returned by createSeries(...)
     * @param x      X-Value
     * @param y      Y-Value
     */
    public void addPoint(int series, double x, double y) {
        controller.addPoint(series, x, y);
    }

    @Override
    public Pane getPane() {
        return pane;
    }

    @Override
    public synchronized void clear() {
        controller.clear();
    }

    public synchronized void fullClear() {
        controller.fullClear();
    }

}
