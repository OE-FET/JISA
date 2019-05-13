package JISA.GUI;

import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;

import java.util.*;

public class SeriesCluster implements SeriesGroup {

    private List<Series> series = new LinkedList<>();

    public SeriesCluster(Series... series) {
        this.series.addAll(Arrays.asList(series));
    }

    @Override
    public Collection<Series> getSeries() {
        return series;
    }

    @Override
    public Series getSeriesFor(double value) {
        return series.get((int) value);
    }

    @Override
    public void addPoint(double x, double y) {

    }

    @Override
    public List<XYChart.Data<Double, Double>> getPoints() {
        return null;
    }

    @Override
    public void clear() {
        for (Series s : series) {
            s.clear();
        }
    }

    @Override
    public void showMarkers(boolean show) {
        for (Series s : series) {
            s.showMarkers(show);
        }
    }

    @Override
    public boolean isShowingMarkers() {
        return series.get(0).isShowingMarkers();
    }

    @Override
    public void setMarkerShape(Shape shape, double size) {
        for (Series s : series) {
            s.setMarkerShape(shape, size);
        }
    }

    @Override
    public void setName(String name) {

    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void setColour(Color colour) {

    }

    @Override
    public Color getColour() {
        return null;
    }

    @Override
    public void setLineWidth(double width) {
        for (Series s : series) {
            s.setLineWidth(width);
        }
    }

    @Override
    public double getLineWidth() {
        return series.get(0).getLineWidth();
    }

    @Override
    public void setAutoReduction(int reduceTo, int limit) {
        for (Series s : series) {
            s.setAutoReduction(reduceTo, limit);
        }
    }

    @Override
    public void reduceNow() {
        for (Series s : series) {
            s.reduceNow();
        }
    }

    @Override
    public void setXAutoRemove(double range) {
        for (Series s : series) {
            s.setXAutoRemove(range);
        }
    }

    @Override
    public void setYAutoRemove(double range) {
        for (Series s : series) {
            s.setYAutoRemove(range);
        }
    }

    @Override
    public void remove() {
        for (Series s : series) {
            s.remove();
        }
    }

    @Override
    public void updateLimits() {
        for (Series s : series) {
            s.updateLimits();
        }
    }

    @Override
    public void restore() {
        for (Series s : series) {
            s.restore();
        }
    }

    @Override
    public XYChart.Series<Double, Double> getXYChartSeries() {
        return null;
    }

    @Override
    public Iterator<XYChart.Data<Double, Double>> iterator() {
        return null;
    }
}
