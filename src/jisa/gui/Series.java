package jisa.gui;

import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;
import jisa.experiment.Col;
import jisa.experiment.Result;
import jisa.experiment.ResultTable;
import jisa.maths.Fit;
import jisa.maths.Maths;

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

    Series watch(ResultTable list, ResultTable.Evaluable xData, ResultTable.Evaluable yData, ResultTable.Evaluable eData);


    default Series watch(ResultTable list, ResultTable.Evaluable xData, ResultTable.Evaluable yData) {
        return watch(list, xData, yData, r -> 0);
    }

    default Series watch(ResultTable list, int xData, int yData, int eData) {
        return watch(list, r -> r.get(xData), r -> r.get(yData), r -> r.get(eData));
    }

    default Series watch(ResultTable list, int xData, int yData) {
        return watch(list, r -> r.get(xData), r -> r.get(yData), r -> 0);
    }

    default Series watch(ResultTable list) {
        return watch(list, 0, 1);
    }

    Series split(ResultTable.Evaluable splitBy, SeriesFormatter pattern);

    default Series split(ResultTable.Evaluable splitBy, String pattern) { return split(splitBy, r -> String.format(pattern, splitBy.evaluate(r))); }

    default Series split(ResultTable.Evaluable splitBy) {
        return split(splitBy, "%s");
    }

    default Series split(int colNum, String pattern) {
        return split(r -> r.get(colNum), pattern);
    }

    default Series split(int colNum) {
        Col column = getWatched().getColumn(colNum);
        return split(colNum, column.hasUnit() ? "%s " + column.getUnit() : "%s");
    }

    Series watchAll(ResultTable list, int xData);

    Series setOnClick(JISAChart.DataHandler onClick);

    default Series watchAll(ResultTable list) {
        return watchAll(list, 0);
    }

    ResultTable getWatched();

    Series filter(Predicate<Result> filter);

    Series addPoint(double x, double y, double error);

    default Series addPoint(double x, double y) {
        return addPoint(x, y, 0);
    }

    List<XYChart.Data<Double, Double>> getPoints();

    Series clear();

    Series showMarkers(boolean show);

    boolean isShowingMarkers();

    Series setMarkerShape(Shape shape);

    Series setMarkerSize(double size);

    Shape getMarkerShape();

    double getMarkerSize();

    String getName();

    Series setName(String name);

    Color getColour();

    Series setColour(Color colour);

    /**
     * Sets the sequence of colours to use when auto-generating sub-series (for example, when split() is called).
     *
     * @param colours The colours in order that they should be used.
     *
     * @return Self-reference.
     */
    Series setColourSequence(Color... colours);

    double getLineWidth();

    Series setLineWidth(double width);

    Series setLineDash(Dash dash);

    Dash getLineDash();

    Series showLine(boolean show);

    boolean isShowingLine();

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

    Series fit(JISAChart.Fitter fitter);

    default Series polyFit(final int degree) {

        return fit((data) -> {

            if (data.size() < degree) {
                return null;
            }

            return Maths.polyFit(data, degree);

        });

    }

    Fit getFit();

    JISAChart.Fitter getFitter();

    boolean isFitted();

    XYChart.Series<Double, Double> getXYChartSeries();

    enum Shape {
        CIRCLE,
        DOT,
        SQUARE,
        DIAMOND,
        CROSS,
        TRIANGLE,
        STAR,
        DASH
    }

    enum Dash {

        SOLID(),
        DASHED(5.0, 5.0),
        DOTTED(1.0, 5.0),
        TWO_DASH(20.0, 5.0, 10.0, 5.0),
        DOT_DASH(1.0, 5.0, 5.0, 5.0),
        LONG_DASH(40.0, 5.0);

        private Double[] array;

        Dash(Double... array) {
            this.array = array;
        }

        public Double[] getArray() {
            return array;
        }

    }

    interface SeriesFormatter {

        String getName(Result row);

    }

}
