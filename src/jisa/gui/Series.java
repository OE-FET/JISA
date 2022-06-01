package jisa.gui;

import de.gsi.chart.renderer.Renderer;
import de.gsi.dataset.DataSet;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;
import jisa.Util;
import jisa.gui.plotting.JISAMarker;
import jisa.maths.matrices.Matrix;
import jisa.maths.fits.Fit;
import jisa.maths.fits.Fitting;
import jisa.results.Column;
import jisa.results.ResultTable;
import jisa.results.Row;
import jisa.results.RowEvaluable;
import org.python.antlr.ast.Num;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public interface Series {

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

    // == Watch ========================================================================================================

    Series watch(ResultTable table, RowEvaluable<? extends Number> xData, RowEvaluable<? extends Number> yData, RowEvaluable<? extends Number> eXData, RowEvaluable<? extends Number> eYData);

    default Series watch(ResultTable table, RowEvaluable<? extends Number> xData, RowEvaluable<? extends Number> yData, RowEvaluable<? extends Number> eYData) {
        return watch(table, xData, yData, r -> 0, eYData);
    }


    /**
     * Watch the specified ResultTable object, plotting points without error-bars based on the specified x and y values.
     *
     * @param table ResultTable to watch
     * @param xData Lambda representing the x-data to plot
     * @param yData Lambda representing the y-data to plot
     *
     * @return Self-reference
     */
    default Series watch(ResultTable table, RowEvaluable xData, RowEvaluable yData) {
        return watch(table, xData, yData, r -> 0, r -> 0);
    }

    /**
     * Watch the specified ResultTable object, plotting points with error-bars based on the specified x,y and error values.
     *
     * @param table ResultTable to watch
     * @param xData Index representing the x-data to plot
     * @param yData Index representing the y-data to plot
     * @param eData Index representing the error-bar data
     *
     * @return Self-reference
     */
    default Series watch(ResultTable table, Column<? extends Number> xData, Column<? extends Number> yData, Column<? extends Number> eYData) {
        return watch(table, r -> r.get(xData), r -> r.get(yData), r -> r.get(eYData));
    }

    default Series watch(ResultTable table, Column<? extends Number> xData, Column<? extends Number> yData, Column<? extends Number> eXData, Column<? extends Number> eYData) {
        return watch(table, r -> r.get(xData), r -> r.get(yData), r -> r.get(eXData), r -> r.get(eYData));
    }

    /**
     * Watch the specified ResultTable object, plotting points without error-bars based on the specified x and y values.
     *
     * @param table ResultTable to watch
     * @param xData Index representing the x-data to plot
     * @param yData Index representing the y-data to plot
     *
     * @return Self-reference
     */
    default Series watch(ResultTable table, Column<? extends Number> xData, Column<? extends Number> yData) {
        return watch(table, r -> r.get(xData), r -> r.get(yData), r -> 0);
    }

    // == Split ========================================================================================================

    /**
     * Cause the series to automatically split into a set of sub-series based on a value in each result.
     *
     * @param splitBy Lambda representing the value to split by
     * @param pattern Lambda that returns the name of each series
     *
     * @return Self-reference
     */
    Series split(RowEvaluable<?> splitBy, SeriesFormatter pattern);

    /**
     * Cause the series to automatically split into a set of sub-series based on a value in each result.
     *
     * @param splitBy Lambda representing the value to split by
     * @param pattern String formatting pattern for the split value to use as the name of each series
     *
     * @return Self-reference
     */
    default Series split(RowEvaluable<?> splitBy, String pattern) {
        return split(splitBy, r -> String.format(pattern, splitBy.evaluate(r)));
    }

    /**
     * Cause the series to automatically split into a set of sub-series based on a value in each result.
     *
     * @param splitBy Lambda representing the value to split by
     *
     * @return Self-reference
     */
    default Series split(RowEvaluable<?> splitBy) {
        return split(splitBy, "%s");
    }

    /**
     * Cause the series to automatically split into a set of sub-series based on a value in each result.
     *
     * @param column  Column to use for the splitting
     * @param pattern String formatting pattern for the split value to use as the name of each series
     *
     * @return Self-reference
     */
    default Series split(Column<?> column, String pattern) {
        return split(r -> r.get(column), pattern);
    }

    /**
     * Cause the series to automatically split into a set of sub-series based on a value in each result.
     *
     * @param column Column to use for the splitting
     *
     * @return Self-reference
     */
    default Series split(Column<?> column) {
        return split(column, column.hasUnits() ? "%s " + column.getUnits() : "%s");
    }

    // == Watch All ====================================================================================================

    /**
     * Returns which, if any, ResultTable is currently being watched by this series.
     *
     * @return ResultTable, null if none
     */
    ResultTable getWatched();


    /**
     * Set a true/false test to filter data by, only plotting rows from the ResultTable that return true when tested.
     *
     * @param filter true/false test
     *
     * @return Self-reference
     */
    Series filter(Predicate<Row> filter);

    Series addPoint(double x, double y, double errorX, double errorY);

    default Series addPoint(double x, double y, double errorY) {
        return addPoint(x, y, 0.0, errorY);
    }

    /**
     * Manually add a data-point, without an error-bar, to the series.
     *
     * @param x x-value
     * @param y y-value
     *
     * @return Self-reference
     */
    default Series addPoint(double x, double y) {
        return addPoint(x, y, 0, 0);
    }

    default Series addPoints(Iterable<Double> x, Iterable<Double> y) {

        Util.iterateCombined(x, y, this::addPoint);
        return this;

    }

    default Series addPoints(Iterable<? extends Number> x, Iterable<? extends Number> y, Iterable<? extends Number> eX, Iterable<? extends Number> eY) {

        Iterator<? extends Number> xI  = x.iterator();
        Iterator<? extends Number> yI  = y.iterator();
        Iterator<? extends Number> eXI = eY.iterator();
        Iterator<? extends Number> eYI = eY.iterator();

        while (xI.hasNext() && yI.hasNext() && eXI.hasNext() && eYI.hasNext()) {
            addPoint(xI.next().doubleValue(), yI.next().doubleValue(), eXI.next().doubleValue(), eYI.next().doubleValue());
        }

        return this;

    }

    default Series addPoints(Iterable<? extends Number> x, Iterable<? extends Number> y, Iterable<? extends Number> eY) {

        Iterator<? extends Number> xI  = x.iterator();
        Iterator<? extends Number> yI  = y.iterator();
        Iterator<? extends Number> eYI = eY.iterator();

        while (xI.hasNext() && yI.hasNext() && eYI.hasNext()) {
            addPoint(xI.next().doubleValue(), yI.next().doubleValue(), eYI.next().doubleValue());
        }

        return this;
    }

    default Series addPoints(Matrix<Double> data) {

        if (data.cols() == 2) {

            for (int i = 0; i < data.rows(); i++) {
                addPoint(data.get(i, 0), data.get(i, 1));
            }

        } else if (data.cols() == 3) {

            for (int i = 0; i < data.rows(); i++) {
                addPoint(data.get(i, 0), data.get(i, 1), data.get(i, 2));
            }

        } else {
            throw new IllegalArgumentException("Matrix must be nx2 or nx3!");
        }

        return this;

    }

    /**
     * Remove all points from this series.
     *
     * @return Self-reference
     */
    Series clear();

    /**
     * Sets whether a marker should be placed at each data point in this series or not.
     *
     * @param show Show markers?
     *
     * @return Self-reference
     */
    Series setMarkerVisible(boolean show);

    /**
     * Returns whether markers are being shown at each data point or not in this series.
     *
     * @return Markers showing?
     */
    boolean isMarkerVisible();

    /**
     * Returns the marker shape currently being used by this series.
     *
     * @return Maker shape being used
     */
    Shape getMarkerShape();

    /**
     * Sets the marker shape to use for this series.
     *
     * @param shape Marker shape to use
     *
     * @return Self-reference
     */
    Series setMarkerShape(Shape shape);

    /**
     * Returns the marker size being used for this series.
     *
     * @return Size
     */
    double getMarkerSize();

    /**
     * Sets the marker size to use for this series.
     *
     * @param size Size
     *
     * @return Self-reference
     */
    Series setMarkerSize(double size);

    /**
     * Returns the name of the series.
     *
     * @return Name
     */
    String getName();

    /**
     * Sets the name of the series.
     *
     * @param name Name
     *
     * @return Self-reference
     */
    Series setName(String name);

    /**
     * Returns the colour being used to represent this series.
     *
     * @return Colour of series
     */
    Color getColour();

    /**
     * Sets the colour used to represent this series.
     *
     * @param colour Colour to use
     *
     * @return Self-reference
     */
    Series setColour(Color colour);

    /**
     * Returns the colour being used to represent errors in this series.
     *
     * @return Colour of series
     */
    Color getErrorColour();

    /**
     * Sets the colour used to represent errors in this series.
     *
     * @param colour Colour to use
     *
     * @return Self-reference
     */
    Series setErrorColour(Color colour);

    /**
     * Sets the sequence of colours to use when auto-generating sub-series (for example, when split() is called).
     *
     * @param colours The colours in order that they should be used.
     *
     * @return Self-reference.
     */
    Series setColourSequence(Color... colours);

    /**
     * Returns line-width used when drawing the line for this series.
     *
     * @return Line width, in pixels
     */
    double getLineWidth();

    /**
     * Sets the line-width used for drawing the line for this series.
     *
     * @param width Line width to use, in pixels
     *
     * @return Self-reference
     */
    Series setLineWidth(double width);

    /**
     * Returns line-width used when drawing lines for error bars on this series.
     *
     * @return Line width, in pixels
     */
    double getErrorLineWidth();

    /**
     * Sets the line-width used for drawing lines for error bars on this series.
     *
     * @param width Line width to use, in pixels
     *
     * @return Self-reference
     */
    Series setErrorLineWidth(double width);

    /**
     * Returns the dash type used for drawing the line for this series.
     *
     * @return Dash type
     */
    Dash getLineDash();

    /**
     * Sets the dash type for the line of this series.
     *
     * @param dash Dash type
     *
     * @return Self-reference
     */
    Series setLineDash(Dash dash);

    /**
     * Sets whether to show the line of this series.
     *
     * @param show Line visible?
     *
     * @return Self-reference
     */
    Series setLineVisible(boolean show);

    /**
     * Returns whether the series line is visible or not.
     *
     * @return Line visible?
     */
    boolean isLineVisible();

    /**
     * Sets the series to automatically reduce the number of data points to the specified number when exceeding the specified limit.
     *
     * @param reduceTo Number of points to reduce to
     * @param limit    Limit at which reducing should occur
     *
     * @return Self-reference
     */
    Series setAutoReduction(int reduceTo, int limit);

    /**
     * Disables any automatic point-reduction set using {@link #setAutoReduction(int, int) setAutoReduction()}.
     *
     * @return Self-reference
     */
    default Series disableAutoReduction() {
        return setAutoReduction(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    /**
     * Forces the automatic point-reduction to happen now.
     *
     * @return Self-reference
     */
    Series reduceNow();

    /**
     * Sets the range below the greatest x-value that outside of which points are automatically removed.
     *
     * @param range Range to use
     *
     * @return Self-reference
     */
    Series setXAutoRemove(double range);

    /**
     * Sets the range below the greatest y-value that outside of which points are automatically removed.
     *
     * @param range Range to use
     *
     * @return Self-reference
     */
    Series setYAutoRemove(double range);

    /**
     * Disables any automatic point reduction set by {@link #setXAutoRemove(double) setXAutoRemove()}.
     *
     * @return Self-reference
     */
    default Series disableXAutoRemove() {
        return setXAutoRemove(Double.POSITIVE_INFINITY);
    }

    /**
     * Disables any automatic point reduction set by {@link #setYAutoRemove(double) setYAutoRemove()}.
     *
     * @return Self-reference
     */
    default Series disableYAutoRemove() {
        return setYAutoRemove(Double.POSITIVE_INFINITY);
    }

    /**
     * Removes this series from its plot.
     *
     * @return Self-reference
     */
    Series remove();

    Series fit(SeriesFitter fitter);

    default Series polyFit(int order) {
        return fit((x, y) -> Fitting.polyFit(x, y, order));
    }

    Series removeFit();

    /**
     * Returns the fit being used to draw the fitted curve to the data currently.
     *
     * @return Fit being used currently
     */
    Fit getFit();

    /**
     * Returns whether the data is currently being fitted.
     *
     * @return Fitted?
     */
    boolean isFitted();

    Series setPointOrder(Ordering order);

    Ordering getPointOrdering();

    /**
     * Returns the unboxed JavaFx representation of this series.
     *
     * @return JavaFx XYChart.Series object
     */
    ObservableList<? extends DataSet> getDatasets();

    interface SeriesFitter {

        Fit fit(Iterable<Double> x, Iterable<Double> y);

        default Fit fit(double[] x, double[] y) {
            return fit(Arrays.stream(x).boxed().collect(Collectors.toList()), Arrays.stream(y).boxed().collect(Collectors.toList()));
        }

    }

    enum Shape {

        CIRCLE(JISAMarker.CIRCLE),
        DOT(JISAMarker.DOT),
        SQUARE(JISAMarker.RECTANGLE),
        TRIANGLE(JISAMarker.TRIANGLE),
        STAR(JISAMarker.STAR),
        DIAMOND(JISAMarker.DIAMOND),
        DASH(JISAMarker.DASH),
        PLUS(JISAMarker.PLUS),
        CROSS(JISAMarker.CROSS);

        private final JISAMarker marker;

        Shape(JISAMarker marker) {
            this.marker = marker;
        }

        public JISAMarker getMarker() {
            return marker;
        }

    }

    enum Ordering {
        NONE,
        X_AXIS,
        Y_AXIS
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

        public double[] getDoubleArray() {
            return Arrays.stream(array).mapToDouble(v -> v).toArray();
        }

    }

    interface SeriesFormatter {

        String getName(Row row);

    }

}
