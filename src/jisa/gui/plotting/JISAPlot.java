package jisa.gui.plotting;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import jisa.Util;
import jisa.gui.GUI;

import java.util.Collection;

public class JISAPlot extends BorderPane {

    private final GridPane                       axesContainer = new GridPane();
    private final Pane                           plotArea      = new Pane();
    private final ObservableList<JISAPlotSeries> series        = FXCollections.observableArrayList();
    private       double                         minX          = -100.0;
    private       double                         maxX          = +100.0;
    private       double                         minY          = -100.0;
    private       double                         maxY          = +100.0;
    private       double                         trackX        = 0.0;
    private       double                         trackY        = 0.0;
    private       AxisMode                       xMode         = AxisMode.AUTO;
    private       AxisMode                       yMode         = AxisMode.AUTO;

    public JISAPlot() {

        setCenter(axesContainer);

    }

    public void seriesAdded(JISAPlotSeries series) {


    }

    public void pointAdded(JISAPlotSeries series, JISAPlotPoint point) {
        GUI.runNow(() -> {
            plotArea.getChildren().add(point);
            autoLimit();
            series.updateLine();
        });
    }

    public void pointsAdded(JISAPlotSeries series, Collection<JISAPlotPoint> points) {
        GUI.runNow(() -> {
            plotArea.getChildren().addAll(points);
            autoLimit();
            series.updateLine();
        });
    }

    public void pointRemoved(JISAPlotSeries series, JISAPlotPoint point) {
        GUI.runNow(() -> {
            plotArea.getChildren().remove(point);
            autoLimit();
            series.updateLine();
        });
    }

    public void pointsRemoved(JISAPlotSeries series, Collection<JISAPlotPoint> points) {
        GUI.runNow(() -> {
            plotArea.getChildren().removeAll(points);
            autoLimit();
            series.updateLine();
        });
    }

    public void autoLimitX() {

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

        setXLimits(minX, maxX);

    }

    public void autoLimit() {
        autoLimitX();
        autoLimitY();
    }

    public void autoLimitY() {

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

        setYLimits(minY, maxY);

    }

    public void limitsChanged(double oldMinX, double oldMaxX, double newMinX, double newMaxX) {

        // TODO: update axes

        if ((newMaxX - newMinX) > (oldMaxX - oldMinX) && Util.isBetween(newMinX, oldMinX, oldMaxX) && Util.isBetween(newMaxX, oldMinX, oldMaxX)) {
            series.forEach(JISAPlotSeries::updateBinningSimple);
        } else {
            series.forEach(JISAPlotSeries::updateBinningFull);
        }

    }

    public double getXPosition(double xValue) {
        return 0.0;
    }

    public double getYPosition(double yValue) {
        return 0.0;
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

}
