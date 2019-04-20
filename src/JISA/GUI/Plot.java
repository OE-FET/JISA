package JISA.GUI;

import JISA.Experiment.Function;
import JISA.Experiment.Result;
import JISA.Experiment.ResultTable;
import JISA.Util;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.function.Predicate;

public class Plot extends JFXWindow implements Element, Clearable {

    public  BorderPane                pane;
    public  ToolBar                   toolbar;
    public  Pane                      stack;
    public  LineChart<Double, Double> chart;
    public  SmartAxis                 xAxis;
    public  SmartAxis                 yAxis;
    public  ToggleButton              autoButton;
    public  ToggleButton              zoomButton;
    public  ToggleButton              dragButton;
    private SmartChart                controller;
    private Rectangle                 rect;
    private Series                    autoSeries = null;

    /**
     * Creates an empty plot from the given title, x-axis label, and y-axis label.
     *
     * @param title  Title of the plot
     * @param xLabel X-Axis Label
     * @param yLabel Y-Axis Lable
     */
    public Plot(String title, String xLabel, String yLabel) {

        super(title, Plot.class.getResource("FXML/PlotWindow.fxml"));

        xAxis = new SmartAxis();
        xAxis.setSide(Side.BOTTOM);

        yAxis = new SmartAxis();
        yAxis.setSide(Side.LEFT);

        chart = new LineChart<>(xAxis, yAxis);
        chart.setMinHeight(400);

        AnchorPane.setBottomAnchor(chart, 0.0);
        AnchorPane.setTopAnchor(chart, 0.0);
        AnchorPane.setLeftAnchor(chart, 0.0);
        AnchorPane.setRightAnchor(chart, 0.0);
        BorderPane.setAlignment(chart, Pos.CENTER);

        chart.setLegendSide(Side.RIGHT);
        chart.setAnimated(true);

        stack.getChildren().add(chart);

        GUI.runNow(() -> {
            chart.setStyle("-fx-background-color: white;");
            xAxis.setLabelText(xLabel);
            yAxis.setLabelText(yLabel);
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
        autoSeries = watchList(list, xData, yData, seriesName, colour);
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
        this(title, list, xData, yData, list.getTitle(yData), null);
    }

    public Plot(String title, ResultTable list, int xData, int yData, int sData) {
        this(title, list.getTitle(xData), list.getTitle(yData));
        autoSeries = watchListSplit(list, xData, yData, sData);
    }

    public Plot(String title, String xLabel) {
        this(title, xLabel, "");
    }

    public Plot(String title) {
        this(title, "", "");
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

    public void setYAxisType(AxisType type) {

        switch (type) {

            case LINEAR:
                yAxis.setMode(SmartAxis.Mode.LINEAR);
                break;

            case LOGARITHMIC:
                yAxis.setMode(SmartAxis.Mode.LOGARITHMIC);
                break;

        }

    }

    public void setXAxisType(AxisType type) {

        switch (type) {

            case LINEAR:
                xAxis.setMode(SmartAxis.Mode.LINEAR);
                break;

            case LOGARITHMIC:
                xAxis.setMode(SmartAxis.Mode.LOGARITHMIC);
                break;

        }

    }

    public void setPointOrdering(Sort ordering) {

        switch (ordering) {

            case X_AXIS:
                chart.setAxisSortingPolicy(LineChart.SortingPolicy.X_AXIS);
                break;

            case Y_AXIS:
                chart.setAxisSortingPolicy(LineChart.SortingPolicy.Y_AXIS);
                break;

            case ORDER_ADDED:
                chart.setAxisSortingPolicy(LineChart.SortingPolicy.NONE);
                break;

        }

    }

    /**
     * Returns the series object of any automatically generated series from the Plot constructor
     *
     * @return Automatically generated series, null if there is none
     */
    public Series getAutoSeries() {
        return autoSeries;
    }

    public Series createSeries(String name, Color colour) {
        return controller.createSeries(name, colour);
    }

    public Series createSeries(String name) {
        return createSeries(name, null);
    }

    public Series createSeries() {
        return createSeries(String.format("Series %d", chart.getData().size() + 1));
    }

    public Series watchList(ResultTable list, SmartChart.Evaluable xData, SmartChart.Evaluable yData, Predicate<Result> filter, String name, Color colour) {
        return controller.createWatchSeries(name, colour, list, xData, yData, filter);
    }

    public Series watchList(ResultTable list, SmartChart.Evaluable xData, SmartChart.Evaluable yData, String name, Color colour) {
        return watchList(list, xData, yData, null, name, colour);
    }

    public Series watchList(ResultTable list, SmartChart.Evaluable xData, SmartChart.Evaluable yData, String name) {
        return watchList(list, xData, yData, null, name, null);
    }

    public Series watchList(ResultTable list, SmartChart.Evaluable xData, SmartChart.Evaluable yData, Predicate<Result> filter) {
        return watchList(list, xData, yData, filter, String.format("Series %d", chart.getData().size() + 1), null);
    }

    public Series watchList(ResultTable list, SmartChart.Evaluable xData, SmartChart.Evaluable yData) {
        return watchList(list, xData, yData, (Predicate<Result>) null);
    }

    public Series watchList(ResultTable list, int xData, int yData, Predicate<Result> filter, String name, Color colour) {

        if (getXLabel().equals("")) {
            setXLabel(list.getTitle(xData));
        }

        if (getYLabel().equals("")) {
            setYLabel(list.getTitle(yData));
        }

        return watchList(list, (r) -> r.get(xData), (r) -> r.get(yData), filter, name, colour);
    }

    public Series watchList(ResultTable list, int xData, int yData, String name, Color colour) {
        return watchList(list, xData, yData, null, name, colour);
    }

    public Series watchList(ResultTable list, int xData, int yData, String name) {
        return watchList(list, xData, yData, name, null);
    }

    public Series watchList(ResultTable list, int xData, int yData, Predicate<Result> filter, String name) {
        return watchList(list, xData, yData, filter, name, null);
    }

    public Series watchList(ResultTable list, int xData, int yData, Predicate<Result> filter) {
        return watchList(list, xData, yData, filter, list.getTitle(yData));
    }

    public Series watchList(ResultTable list, int xData, int yData) {
        return watchList(list, xData, yData, (Predicate<Result>) null);
    }

    public Series watchList(ResultTable list, String name, Color colour) {
        return watchList(list, 0, 1, name, colour);
    }

    public Series watchList(ResultTable list, String name) {
        return watchList(list, name, null);
    }

    public Series watchList(ResultTable list) {
        return watchList(list, list.getTitle(1), null);
    }

    public SeriesGroup watchListSplit(ResultTable list, SmartChart.Evaluable xData, SmartChart.Evaluable yData, SmartChart.Evaluable sData, Predicate<Result> filter, String pattern) {
        return controller.createAutoSeries(list, xData, yData, sData, pattern, filter);
    }

    public SeriesGroup watchListSplit(ResultTable list, SmartChart.Evaluable xData, SmartChart.Evaluable yData, SmartChart.Evaluable sData, String pattern) {
        return watchListSplit(list, xData, yData, sData, null, pattern);
    }

    public SeriesGroup watchListSplit(ResultTable list, SmartChart.Evaluable xData, SmartChart.Evaluable yData, SmartChart.Evaluable sData) {
        return watchListSplit(list, xData, yData, sData, null, "%s");
    }

    public SeriesGroup watchListSplit(ResultTable list, int xData, int yData, int sData, Predicate<Result> filter, String pattern) {
        return watchListSplit(list, (r) -> r.get(xData), (r) -> r.get(yData), (r) -> r.get(sData), filter, pattern);
    }

    public SeriesGroup watchListSplit(ResultTable list, int xData, int yData, int sData, Predicate<Result> filter) {
        return watchListSplit(list, xData, yData, sData, filter, list.getColumn(yData).hasUnit() ? "%s " + list.getUnits(sData) : "%s");
    }

    public SeriesGroup watchListSplit(ResultTable list, int xData, int yData, int sData, String pattern) {
        return watchListSplit(list, xData, yData, sData, null, pattern);
    }

    public SeriesGroup watchListSplit(ResultTable list, int xData, int yData, int sData) {
        return watchListSplit(list, xData, yData, sData, (Predicate<Result>) null);
    }

    public Series plotFunction(Function toPlot, String name, Color colour) {
        return controller.createFunctionSeries(name, colour, toPlot);
    }

    public Series plotFunction(Function toPlot, double minX, double maxX, int steps, String name, Color colour) {
        Series s = createSeries(name, colour);
        s.showMarkers(false);

        for (double x : Util.makeLinearArray(minX, maxX, steps)) {
            s.addPoint(x, toPlot.value(x));
        }

        return s;

    }

    public void showToolbar(boolean flag) {
        toolbar.setManaged(flag);
        toolbar.setVisible(flag);
        adjustSize();
    }

    public String getXLabel() {
        return controller.getXLabel();
    }

    public void setXLabel(String label) {
        controller.setXLabel(label);
    }

    public String getYLabel() {
        return controller.getYLabel();
    }

    public void setYLabel(String label) {
        controller.setYLabel(label);
    }

    public void setZoomMode() {

        zoomButton.setSelected(true);
        autoButton.setSelected(false);
        dragButton.setSelected(false);

        XYChart.Series<Double, Double> series = new XYChart.Series<>();

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

    public void setXAutoTrack(double range) {
        controller.setTrackingX(range);
    }

    public void setYAutoTrack(double range) {
        controller.setTrackingY(range);
    }

    public void show() {
        super.show();
        adjustSize();
    }

    private void adjustSize() {

        GUI.runNow(() -> {

            double width  = stage.getWidth();
            double height = stage.getHeight();

            stage.setMinWidth(0.0);
            stage.setMinHeight(0.0);

            stage.sizeToScene();
            stage.setMinHeight(stage.getHeight());
            stage.setMinWidth(stage.getWidth());
            stage.setHeight(height);
            stage.setWidth(width);

        });

    }

    @Override
    public Pane getPane() {
        return pane;
    }

    @Override
    public synchronized void clear() {
        controller.clear();
    }

    public enum AxisType {
        LINEAR,
        LOGARITHMIC
    }

    public enum Sort {
        X_AXIS,
        Y_AXIS,
        ORDER_ADDED
    }

}
