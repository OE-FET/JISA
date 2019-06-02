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
    public Series addPoint(double x, double y) {
        return this;
    }

    @Override
    public List<XYChart.Data<Double, Double>> getPoints() {
        return null;
    }

    @Override
    public Series clear() {
        for (Series s : series) {
            s.clear();
        }
        return this;
    }

    @Override
    public Series showMarkers(boolean show) {
        for (Series s : series) {
            s.showMarkers(show);
        }
        return this;
    }

    @Override
    public boolean isShowingMarkers() {
        return series.get(0).isShowingMarkers();
    }

    @Override
    public Series setMarkerShape(Shape shape, double size) {
        for (Series s : series) {
            s.setMarkerShape(shape, size);
        }
        return this;
    }

    @Override
    public Series setName(String name) {
        return this;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Series setColour(Color colour) {
        return this;
    }

    @Override
    public Color getColour() {
        return null;
    }

    @Override
    public Series setLineWidth(double width) {
        for (Series s : series) {
            s.setLineWidth(width);
        }
        return this;
    }

    @Override
    public double getLineWidth() {
        return series.get(0).getLineWidth();
    }

    @Override
    public Series setAutoReduction(int reduceTo, int limit) {
        for (Series s : series) {
            s.setAutoReduction(reduceTo, limit);
        }
        return this;
    }

    @Override
    public Series reduceNow() {
        for (Series s : series) {
            s.reduceNow();
        }
        return this;
    }

    @Override
    public Series setXAutoRemove(double range) {
        for (Series s : series) {
            s.setXAutoRemove(range);
        }
        return this;
    }

    @Override
    public Series setYAutoRemove(double range) {
        for (Series s : series) {
            s.setYAutoRemove(range);
        }
        return this;
    }

    @Override
    public Series remove() {
        for (Series s : series) {
            s.remove();
        }
        return this;
    }

    @Override
    public Series updateLimits() {
        for (Series s : series) {
            s.updateLimits();
        }
        return this;
    }

    @Override
    public Series restore() {
        for (Series s : series) {
            s.restore();
        }
        return this;
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
