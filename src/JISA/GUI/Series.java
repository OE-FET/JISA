package JISA.GUI;

import JISA.Experiment.Col;
import JISA.Experiment.Result;
import JISA.Experiment.ResultTable;
import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.function.Predicate;

public interface Series extends Iterable<XYChart.Data<Double, Double>> {

    Color[] defaultColours = {
            Color.web("#f3622d"),
            Color.web("#fba71b"),
            Color.web("#57b757"),
            Color.web("#41a9c9"),
            Color.web("#4258c9"),
            Color.web("#9a42c8"),
            Color.web("#c84164"),
            Color.web("#888888")
    };

    Series watch(ResultTable list, SmartChart.Evaluable xData, SmartChart.Evaluable yData);

    default Series watch(ResultTable list, int xData, int yData) {
        return watch(list, r -> r.get(xData), r -> r.get(yData));
    }

    default Series watch(ResultTable list) {
        return watch(list, 0, 1);
    }

    SeriesGroup split(SmartChart.Evaluable splitBy, String pattern);

    default SeriesGroup split(SmartChart.Evaluable splitBy) {
        return split(splitBy, "%s");
    }

    default SeriesGroup split(int colNum, String pattern) {
        return split(r -> r.get(colNum), pattern);
    }

    default SeriesGroup split(int colNum) {
        Col column = getWatched().getColumn(colNum);
        return split(colNum, column.hasUnit() ? "%s " + column.getUnit() : "%s");
    }

    SeriesGroup watchAll(ResultTable list, int xData);

    default SeriesGroup watchAll(ResultTable list) {
        return watchAll(list, 0);
    }

    ResultTable getWatched();

    Series filter(Predicate<Result> filter);

    Series addPoint(double x, double y);

    List<XYChart.Data<Double, Double>> getPoints();

    Series clear();

    Series showMarkers(boolean show);

    boolean isShowingMarkers();

    Series setMarkerShape(Shape shape, double size);

    default Series setMarkerShape(Shape shape) {
        return setMarkerShape(shape, 5.0);
    }

    Series setName(String name);

    String getName();

    Series setColour(Color colour);

    Color getColour();

    Series setLineWidth(double width);

    double getLineWidth();

    Series setAutoReduction(int reduceTo, int limit);

    default Series disableAutoReduction() {
        return setAutoReduction(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    Series reduceNow();

    Series setXAutoRemove(double range);

    Series setYAutoRemove(double range);

    default Series disableXAutoRemove() {
        return setXAutoRemove(Double.POSITIVE_INFINITY);
    }

    default Series disableYAutoRemove() {
        return setYAutoRemove(Double.POSITIVE_INFINITY);
    }

    Series remove();

    Series updateLimits();

    Series restore();

    Series polyFit(int degree);

    void updateStyles();

    XYChart.Series<Double, Double> getXYChartSeries();

    enum Shape {
        CIRCLE,
        DOT,
        SQUARE,
        DIAMOND,
        CROSS,
        TRIANGLE,
        STAR
    }

}
