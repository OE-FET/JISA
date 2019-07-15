package jisa.gui;

import jisa.experiment.Result;
import jisa.experiment.ResultTable;
import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;

import java.util.*;
import java.util.function.Predicate;

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
    public SeriesGroup watch(ResultTable list, SmartChart.Evaluable xData, SmartChart.Evaluable yData) {
        series.replaceAll(s -> s.watch(list, xData, yData));
        return this;
    }

    @Override
    public SeriesGroup split(SmartChart.Evaluable splitBy, String pattern) {
        series.replaceAll(s -> s.split(splitBy, pattern));
        return this;
    }

    @Override
    public SeriesGroup watchAll(ResultTable list, int xData) {
        return this;
    }

    @Override
    public ResultTable getWatched() {
        return null;
    }

    @Override
    public SeriesGroup filter(Predicate<Result> filter) {
        series.replaceAll(s -> s.filter(filter));
        return this;
    }

    @Override
    public SeriesGroup addPoint(double x, double y) {
        return this;
    }

    @Override
    public List<XYChart.Data<Double, Double>> getPoints() {
        return null;
    }

    @Override
    public SeriesGroup clear() {
        for (Series s : series) {
            s.clear();
        }
        return this;
    }

    @Override
    public SeriesGroup showMarkers(boolean show) {
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
    public SeriesGroup setMarkerShape(Shape shape, double size) {
        for (Series s : series) {
            s.setMarkerShape(shape, size);
        }
        return this;
    }

    @Override
    public SeriesGroup setName(String name) {
        return this;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public SeriesGroup setColour(Color colour) {
        return this;
    }

    @Override
    public Color getColour() {
        return null;
    }

    @Override
    public SeriesGroup setLineWidth(double width) {
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
    public Series showLine(boolean show) {

        for (Series s : series) {
            s.showLine(show);
        }

        return this;

    }

    @Override
    public boolean isShowingLine() {
        return series.get(0).isShowingLine();
    }

    @Override
    public SeriesGroup setAutoReduction(int reduceTo, int limit) {
        for (Series s : series) {
            s.setAutoReduction(reduceTo, limit);
        }
        return this;
    }

    @Override
    public SeriesGroup reduceNow() {
        for (Series s : series) {
            s.reduceNow();
        }
        return this;
    }

    @Override
    public SeriesGroup setXAutoRemove(double range) {
        for (Series s : series) {
            s.setXAutoRemove(range);
        }
        return this;
    }

    @Override
    public SeriesGroup setYAutoRemove(double range) {
        for (Series s : series) {
            s.setYAutoRemove(range);
        }
        return this;
    }

    @Override
    public SeriesGroup remove() {
        for (Series s : series) {
            s.remove();
        }
        return this;
    }

    @Override
    public SeriesGroup updateLimits() {
        for (Series s : series) {
            s.updateLimits();
        }
        return this;
    }

    @Override
    public SeriesGroup restore() {
        for (Series s : series) {
            s.restore();
        }
        return this;
    }

    @Override
    public SeriesGroup polyFit(int degree) {
        for (Series s : series) {
            s.polyFit(degree);
        }
        return this;
    }

    @Override
    public void updateStyles() {
        for (Series s : series) {
            s.updateStyles();
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
