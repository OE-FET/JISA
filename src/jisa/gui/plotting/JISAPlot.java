package jisa.gui.plotting;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;
import jisa.Util;
import jisa.gui.GUI;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class JISAPlot extends BorderPane {

    private final GridPane                       axesContainer = new GridPane();
    private final Pane                           lineArea      = new Pane();
    private final Pane                           errorArea     = new Pane();
    private final Pane                           markerArea    = new Pane();
    private final StackPane                      plotArea      = new StackPane(lineArea, errorArea, markerArea);
    private final AnchorPane                     xAxis         = new AnchorPane();
    private final AnchorPane                     yAxis         = new AnchorPane();
    private final ObservableList<JISAPlotSeries> series        = FXCollections.observableArrayList();
    private       double                         minX          = -100.0;
    private       double                         maxX          = +100.0;
    private       double                         minY          = -100.0;
    private       double                         maxY          = +100.0;
    private       double                         trackX        = 0.0;
    private       double                         trackY        = 0.0;
    private       AxisMode                       xMode         = AxisMode.AUTO;
    private       AxisMode                       yMode         = AxisMode.AUTO;
    private       AxisType                       xType         = AxisType.LINEAR;
    private       AxisType                       yType         = AxisType.LINEAR;

    public JISAPlot() {

        setCenter(axesContainer);

    }

    public void seriesAdded(JISAPlotSeries series) {
        lineArea.getChildren().add(series.getLine());
        pointsAdded(series, series.getPoints());
    }

    public void seriesRemoved(JISAPlotSeries series) {
        lineArea.getChildren().remove(series.getLine());
        pointsRemoved(series, series.getPoints());
    }

    public void pointAdded(JISAPlotSeries series, JISAPlotPoint point) {

        GUI.runNow(() -> {

            markerArea.getChildren().add(point.getMarker());
            errorArea.getChildren().add(point.getXErrorBar());
            errorArea.getChildren().add(point.getYErrorBar());
            point.reposition();

            if (autoLimit()) {
                drawAxes();
                updatePositions();
                updateBinningSimple();
                updateLines();
            } else {
                series.updateBinningSimple();
                series.updateLine();
            }

        });

    }

    public void pointsAdded(JISAPlotSeries series, Collection<JISAPlotPoint> points) {

        GUI.runNow(() -> {

            markerArea.getChildren().addAll(points.stream().map(JISAPlotPoint::getMarker).collect(Collectors.toList()));
            errorArea.getChildren().addAll(points.stream().map(JISAPlotPoint::getXErrorBar).collect(Collectors.toList()));
            errorArea.getChildren().addAll(points.stream().map(JISAPlotPoint::getYErrorBar).collect(Collectors.toList()));
            points.forEach(JISAPlotPoint::reposition);

            if (autoLimit()) {
                drawAxes();
                updatePositions();
                updateBinningSimple();
                updateLines();
            } else {
                series.updateBinningSimple();
                series.updateLine();
            }

        });

    }

    public void pointRemoved(JISAPlotSeries series, JISAPlotPoint point) {

        GUI.runNow(() -> {

            markerArea.getChildren().remove(point.getMarker());
            errorArea.getChildren().remove(point.getXErrorBar());
            errorArea.getChildren().remove(point.getYErrorBar());

            if (autoLimit()) {
                drawAxes();
                updatePositions();
                updateLines();
            } else {
                series.updateLine();
            }

        });

    }

    public void pointsRemoved(JISAPlotSeries series, Collection<JISAPlotPoint> points) {

        GUI.runNow(() -> {

            markerArea.getChildren().removeAll(points.stream().map(JISAPlotPoint::getMarker).collect(Collectors.toList()));
            errorArea.getChildren().removeAll(points.stream().map(JISAPlotPoint::getXErrorBar).collect(Collectors.toList()));
            errorArea.getChildren().removeAll(points.stream().map(JISAPlotPoint::getYErrorBar).collect(Collectors.toList()));

            if (autoLimit()) {
                drawAxes();
                updatePositions();
                updateLines();
            } else {
                series.updateLine();
            }

        });

    }

    public void pointsChanged(JISAPlotSeries series, Collection<JISAPlotPoint> added, Collection<JISAPlotPoint> removed) {

        GUI.runNow(() -> {

            markerArea.getChildren().removeAll(removed.stream().map(JISAPlotPoint::getMarker).collect(Collectors.toList()));
            errorArea.getChildren().removeAll(removed.stream().map(JISAPlotPoint::getXErrorBar).collect(Collectors.toList()));
            errorArea.getChildren().removeAll(removed.stream().map(JISAPlotPoint::getYErrorBar).collect(Collectors.toList()));

            markerArea.getChildren().addAll(added.stream().map(JISAPlotPoint::getMarker).collect(Collectors.toList()));
            errorArea.getChildren().addAll(added.stream().map(JISAPlotPoint::getXErrorBar).collect(Collectors.toList()));
            errorArea.getChildren().addAll(added.stream().map(JISAPlotPoint::getYErrorBar).collect(Collectors.toList()));

            added.forEach(JISAPlotPoint::reposition);

            series.updateLine();

        });

    }

    public List<Double> calculateTicks(AxisType type, double min, double max) {

        List<Double> ticks = new LinkedList<>();

        switch (type) {

            case LINEAR:

                double range = Util.getNiceValue(max - min, false);
                double tickSpacing = Util.getNiceValue(range / (10 - 1), true);
                double niceMin = Math.floor(min / tickSpacing) * tickSpacing;
                double niceMax = Math.ceil(max / tickSpacing) * tickSpacing;


                for (double v = niceMax; v <= niceMax; v += tickSpacing) {
                    ticks.add(v);
                }

                break;

            case LOGARITHMIC:

                int logStart = (int) Math.floor(Math.log10(min));
                int logStop = (int) Math.ceil(Math.log10(max));

                for (int i = logStart; i <= logStop; i++) {
                    ticks.add(Math.pow(10, i));
                }

                break;

        }

        return ticks;

    }

    public void drawAxes() {

        List<Line> oldX = xAxis.getChildren().stream()
                               .filter(e -> e instanceof Line)
                               .map(e -> (Line) e)
                               .collect(Collectors.toList());

        List<Line> oldY = yAxis.getChildren().stream()
                               .filter(e -> e instanceof Line)
                               .map(e -> (Line) e)
                               .collect(Collectors.toList());

        xAxis.getChildren().clear();
        yAxis.getChildren().clear();

        for (double x : calculateTicks(xType, minX, maxX)) {

            double position = getXPosition(x);

            Line line;

            if (oldX.isEmpty()) {
                line = new Line();
            } else {
                line = oldX.remove(0);
            }

            line.setStartX(position);
            line.setEndX(position);
            line.setStartY(0.0);
            line.setEndY(10.0);

            xAxis.getChildren().add(line);
            AnchorPane.setTopAnchor(line, 0.0);
            AnchorPane.setLeftAnchor(line, 0.0);

        }

        for (double y : calculateTicks(yType, minY, maxY)) {

            double position = getYPosition(y);

            Line line;

            if (oldY.isEmpty()) {
                line = new Line();
            } else {
                line = oldY.remove(0);
            }

            line.setStartY(position);
            line.setEndY(position);
            line.setStartX(0.0);
            line.setEndX(10.0);

            yAxis.getChildren().add(line);
            AnchorPane.setTopAnchor(line, 0.0);
            AnchorPane.setLeftAnchor(line, 0.0);

        }


    }

    public boolean autoLimitX() {

        double minX = this.minX;
        double maxX = this.maxX;

        switch (xMode) {

            case AUTO:

                minX = series.stream().mapToDouble(JISAPlotSeries::getMinX).min().orElse(this.minX);
                maxX = series.stream().mapToDouble(JISAPlotSeries::getMaxX).max().orElse(this.maxX);

                break;

            case TRACK:

                maxX = series.stream().mapToDouble(JISAPlotSeries::getMaxX).max().orElse(this.maxX);
                minX = maxX - trackX;

                break;

        }


        if (minX != this.minX || maxX != this.maxX) {
            setXLimits(minX, maxX);
            return true;
        } else {
            return false;
        }

    }

    public boolean autoLimitY() {

        double minY = this.minY;
        double maxY = this.maxY;

        switch (xMode) {

            case AUTO:

                minY = series.stream().mapToDouble(JISAPlotSeries::getMinY).min().orElse(this.minY);
                maxY = series.stream().mapToDouble(JISAPlotSeries::getMaxY).max().orElse(this.maxY);

                break;

            case TRACK:

                maxY = series.stream().mapToDouble(JISAPlotSeries::getMaxY).max().orElse(this.maxY);
                minY = maxY - trackX;

                break;

        }

        if (minY != this.minY || maxY != this.maxY) {
            setYLimits(minY, maxY);
            return true;
        } else {
            return false;
        }

    }

    public boolean autoLimit() {
        boolean x = autoLimitX();
        boolean y = autoLimitY();
        return x || y;
    }

    public synchronized void updatePositions() {
        series.forEach(s -> s.getPoints().forEach(JISAPlotPoint::reposition));
    }

    public synchronized void updateBinningSimple() {
        series.forEach(JISAPlotSeries::updateBinningSimple);
    }

    public synchronized void updateBinningFull() {
        series.forEach(JISAPlotSeries::updateBinningFull);
    }

    public synchronized void updateLines() {
        series.forEach(JISAPlotSeries::updateLine);
    }

    public double getXPosition(double xValue) {
        return plotArea.getWidth() * ((xValue - minX) / (maxX - minX));
    }

    public double getYPosition(double yValue) {
        return plotArea.getHeight() * (1 - ((yValue - minY) / (maxY - minY)));
    }

    public double getMinX() {
        return minX;
    }

    public void setMinX(double minX) {
        this.minX = minX;
    }

    public double getMaxX() {
        return maxX;
    }

    public void setMaxX(double maxX) {
        this.maxX = maxX;
    }

    public double getAreaWidth() {
        return plotArea.getWidth();
    }

    public double getAreaHeight() {
        return plotArea.getHeight();
    }

    public void setXLimits(double minX, double maxX) {
        this.minX = minX;
        this.maxX = maxX;
    }

    public double getMinY() {
        return minY;
    }

    public void setMinY(double minY) {
        this.minY = minY;
    }

    public double getMaxY() {
        return maxY;
    }

    public void setMaxY(double maxY) {
        this.maxY = maxY;
    }

    public void setYLimits(double minY, double maxY) {

        double oldMin = this.minY;
        double oldMax = this.maxY;

        this.minY = minY;
        this.maxY = maxY;

    }

    public void startXAutoTracking(double range) {
        this.trackX = range;
    }

    public void startYAutoTracking(double range) {
        this.trackY = range;
    }

    public void stopXAutoTracking() {
        startXAutoTracking(0.0);
    }

    public void stopYAutoTracking() {
        startYAutoTracking(0.0);
    }

    public boolean isInRange(double x, double y) {

        boolean xInRange;
        boolean yInRange;

        switch (xMode) {

            case TRACK:
                xInRange = x > (maxX - trackX);
                break;

            case MANUAL:
                xInRange = Util.isBetween(x, minX, maxX);
                break;

            default:
            case AUTO:
                xInRange = true;
                break;

        }

        switch (yMode) {

            case TRACK:
                yInRange = y > (maxY - trackY);
                break;

            case MANUAL:
                yInRange = Util.isBetween(y, minY, maxY);
                break;

            default:
            case AUTO:
                yInRange = true;
                break;

        }

        return xInRange && yInRange;

    }

    public enum AxisMode {
        AUTO,
        TRACK,
        MANUAL
    }

    public enum AxisType {
        LINEAR,
        LOGARITHMIC
    }

}
