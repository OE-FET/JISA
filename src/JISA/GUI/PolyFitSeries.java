package JISA.GUI;

import JISA.Experiment.Function;
import JISA.Maths.Maths;
import JISA.Maths.Matrix;
import JISA.Util;
import javafx.collections.ListChangeListener;
import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;

import java.util.Iterator;
import java.util.List;

public class PolyFitSeries implements Series {

    private final Series dataSeries;
    private final Series fitSeries;
    private final int    degree;

    public PolyFitSeries(Series data, Series fit, int degree) {

        data.setLineWidth(0.0)
            .showMarkers(true);

        dataSeries = data;

        fit.showMarkers(false)
           .setLineWidth(2.0);

        fitSeries = fit;

        this.degree = degree;

    }

    public void updateFit() {

        List<XYChart.Data<Double, Double>> data = dataSeries.getPoints();

        if (data.size() <= degree) {
            return;
        }

        Matrix x = new Matrix(data.size(), 1);
        Matrix y = new Matrix(data.size(), 1);

        for (int i = 0; i < data.size(); i++) {
            x.set(i, 0, data.get(i).getXValue());
            y.set(i, 0, data.get(i).getYValue());
        }

        Function fitted = Maths.polyFit(x, y, degree);

        if (fitted == null) {
            return;
        }

        fitSeries.getXYChartSeries().getData().clear();

        for (double xv : Util.makeLinearArray(x.minElement(), x.maxElement(), 100)) {
            fitSeries.addPoint(xv, fitted.value(xv));
        }


        updateLimits();

    }

    @Override
    public Series addPoint(double x, double y) {
        dataSeries.addPoint(x, y);
        updateFit();
        return this;
    }

    @Override
    public List<XYChart.Data<Double, Double>> getPoints() {
        return dataSeries.getPoints();
    }

    @Override
    public Series clear() {
        dataSeries.clear();
        return this;
    }

    @Override
    public Series showMarkers(boolean show) {
        dataSeries.showMarkers(show);
        return this;
    }

    @Override
    public boolean isShowingMarkers() {
        return dataSeries.isShowingMarkers();
    }

    @Override
    public Series setMarkerShape(Shape shape, double size) {
        dataSeries.setMarkerShape(shape, size);
        return this;
    }

    @Override
    public Series setName(String name) {
        dataSeries.setName(name);
        fitSeries.setName(String.format("%s (fit)", name));
        return this;
    }

    @Override
    public String getName() {
        return dataSeries.getName();
    }

    @Override
    public Series setColour(Color colour) {
        dataSeries.setColour(colour);
        fitSeries.setColour(colour);
        return this;
    }

    @Override
    public Color getColour() {
        return dataSeries.getColour();
    }

    @Override
    public Series setLineWidth(double width) {
        fitSeries.setLineWidth(width);
        return this;
    }

    @Override
    public double getLineWidth() {
        return fitSeries.getLineWidth();
    }

    @Override
    public Series setAutoReduction(int reduceTo, int limit) {
        dataSeries.setAutoReduction(reduceTo, limit);
        return this;
    }

    @Override
    public Series reduceNow() {
        dataSeries.reduceNow();
        return this;
    }

    @Override
    public Series setXAutoRemove(double range) {
        dataSeries.setXAutoRemove(range);
        return this;
    }

    @Override
    public Series setYAutoRemove(double range) {
        dataSeries.setYAutoRemove(range);
        return this;
    }

    @Override
    public Series remove() {
        dataSeries.remove();
        fitSeries.remove();
        return this;
    }

    @Override
    public Series updateLimits() {
        dataSeries.updateLimits();
        fitSeries.updateLimits();
        return this;
    }

    @Override
    public Series restore() {
        dataSeries.restore();
        return this;
    }

    @Override
    public Series polyFit(int degree) {
        return this;
    }

    @Override
    public void updateStyles() {
        dataSeries.updateStyles();
        fitSeries.updateStyles();
    }

    @Override
    public XYChart.Series<Double, Double> getXYChartSeries() {
        return dataSeries.getXYChartSeries();
    }

    @Override
    public Iterator<XYChart.Data<Double, Double>> iterator() {
        return dataSeries.iterator();
    }
}
