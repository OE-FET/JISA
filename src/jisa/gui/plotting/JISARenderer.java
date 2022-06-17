package jisa.gui.plotting;

import de.gsi.chart.Chart;
import de.gsi.chart.XYChart;
import de.gsi.chart.axes.Axis;
import de.gsi.chart.axes.spi.CategoryAxis;
import de.gsi.chart.renderer.Renderer;
import de.gsi.chart.renderer.spi.AbstractErrorDataSetRendererParameter;
import de.gsi.dataset.DataSet;
import de.gsi.dataset.spi.DoubleDataSet;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import jisa.gui.Colour;
import jisa.gui.Series;
import jisa.maths.Range;

import java.security.InvalidParameterException;
import java.util.*;

/**
 * Modified copy of the ErrorDataSetRenderer
 */
public class JISARenderer extends AbstractErrorDataSetRendererParameter<JISARenderer> implements Renderer {

    /**
     * Creates new <code>ErrorDataSetRenderer</code>.
     */
    public JISARenderer() {
        this(3);
    }

    /**
     * Creates new <code>ErrorDataSetRenderer</code>.
     *
     * @param dashSize initial size (top/bottom cap) on top of the error bars
     */
    public JISARenderer(final int dashSize) {
        setDashSize(dashSize);
    }

    protected static void setLineGC(GraphicsContext gc, JISAErrorDataSet dataSet) {

        gc.setStroke(dataSet.getColour());
        gc.setLineWidth(dataSet.getThickness());
        gc.setLineDashes(dataSet.getDash().getDoubleArray());

    }

    protected static void setErrorBarGC(GraphicsContext gc, JISAErrorDataSet dataSet) {

        gc.setStroke(dataSet.getErrorColour());
        gc.setLineWidth(dataSet.getErrorThickness());

    }

    /**
     * @param dataSet for which the representative icon should be generated
     * @param dsIndex index within renderer set
     * @param width   requested width of the returning Canvas
     * @param height  requested height of the returning Canvas
     *
     * @return a graphical icon representation of the given data sets
     */
    @Override
    public Canvas drawLegendSymbol(final DataSet dataSet, final int dsIndex, final int width, final int height) {

        if (!(dataSet instanceof JISAErrorDataSet)) {
            return null;
        }

        final JISAErrorDataSet set    = (JISAErrorDataSet) dataSet;
        final Canvas           canvas = new Canvas(width, height);
        final GraphicsContext  gc     = canvas.getGraphicsContext2D();
        final String           style  = dataSet.getStyle();
        final double           x      = width / 2.0;
        final double           y      = height / 2.0;

        if (set.isLineVisible()) {
            gc.save();
            setLineGC(gc, set);
            gc.strokeLine(1, y, width - 2.0, y);
            gc.restore();
        }

        if (set.isMarkerVisible()) {
            gc.save();
            drawMarker(set, gc, (1.0 + width - 2.0) / 2.0, y);
            gc.restore();
        }

        return canvas;

    }

    @Override
    public List<DataSet> render(final GraphicsContext gc, final Chart chart, final int dataSetOffset, final ObservableList<DataSet> datasets) {

        if (!(chart instanceof XYChart)) {
            throw new InvalidParameterException("must be derivative of XYChart for renderer - " + this.getClass().getSimpleName());
        }

        // make local copy and add renderer specific data sets
        final List<DataSet> localDataSetList = isDrawChartDataSets() ? new ArrayList<>(datasets) : new ArrayList<>();
        localDataSetList.addAll(super.getDatasets());

        // If there are no data sets
        if (localDataSetList.isEmpty()) {
            return Collections.emptyList();
        }

        Axis xAxisTemp = getFirstAxis(Orientation.HORIZONTAL);

        if (xAxisTemp == null) {
            xAxisTemp = chart.getFirstAxis(Orientation.HORIZONTAL);
        }

        final Axis xAxis     = xAxisTemp;
        Axis       yAxisTemp = getFirstAxis(Orientation.VERTICAL);

        if (yAxisTemp == null) {
            yAxisTemp = chart.getFirstAxis(Orientation.VERTICAL);
        }

        final Axis    yAxis         = yAxisTemp;
        final double  xAxisWidth    = xAxis.getWidth();
        final boolean xAxisInverted = xAxis.isInvertedAxis();
        final double  xMin          = xAxis.getValueForDisplay(xAxisInverted ? xAxisWidth : 0.0);
        final double  xMax          = xAxis.getValueForDisplay(xAxisInverted ? 0.0 : xAxisWidth);

        for (int dataSetIndex = localDataSetList.size() - 1; dataSetIndex >= 0; dataSetIndex--) {

            if (!(localDataSetList.get(dataSetIndex) instanceof JISAErrorDataSet)) {
                continue;
            }

            final int              ldataSetIndex = dataSetIndex;
            final JISAErrorDataSet dataSet       = (JISAErrorDataSet) localDataSetList.get(dataSetIndex);

            if (xAxis instanceof JISADefaultAxis) {
                ((JISADefaultAxis) xAxis).recordLogValues(dataSet.xValues.toArray(new double[0]));
            }

            if (yAxis instanceof JISADefaultAxis) {
                ((JISADefaultAxis) yAxis).recordLogValues(dataSet.yValues.toArray(new double[0]));
            }

            if (!dataSet.isVisible()) {
                continue;
            }

            // update categories in case of category axes for the first (index == '0') indexed data set
            if (dataSetIndex == 0) {

                if (getFirstAxis(Orientation.HORIZONTAL) instanceof CategoryAxis) {
                    final CategoryAxis axis = (CategoryAxis) getFirstAxis(Orientation.HORIZONTAL);
                    dataSet.lock().readLockGuard(() -> axis.updateCategories(dataSet));
                }

                if (getFirstAxis(Orientation.VERTICAL) instanceof CategoryAxis) {
                    final CategoryAxis axis = (CategoryAxis) getFirstAxis(Orientation.VERTICAL);
                    dataSet.lock().readLockGuard(() -> axis.updateCategories(dataSet));
                }

            }

            // check for potentially reduced data range we are supposed to plot
            final Optional<CachedDataPoints> cachedPoints = dataSet.lock().readLockGuard(() -> {

                int indexMin;
                int indexMax; /* indexMax is excluded in the drawing */

                if (isAssumeSortedData()) {
                    indexMin = Math.max(0, dataSet.getIndex(DataSet.DIM_X, xMin) - 1);
                    indexMax = Math.min(dataSet.getIndex(DataSet.DIM_X, xMax) + 2, dataSet.getDataCount());
                } else {
                    indexMin = 0;
                    indexMax = dataSet.getDataCount();
                }

                // zero length/range data set -> nothing to be drawn
                if (indexMax - indexMin <= 0) {
                    return Optional.empty();
                }

                final CachedDataPoints cache       = new CachedDataPoints(indexMin, indexMax, dataSet.getDataCount(), true);
                final boolean          isPolarPlot = ((XYChart) chart).isPolarPlot();

                if (isParallelImplementation()) {

                    cache.computeScreenCoordinatesInParallel(
                        xAxis,
                        yAxis,
                        dataSet,
                        dataSetOffset + ldataSetIndex,
                        indexMin,
                        indexMax,
                        getErrorType(),
                        isPolarPlot,
                        isallowNaNs()
                    );

                } else {

                    cache.computeScreenCoordinates(
                        xAxis,
                        yAxis,
                        dataSet,
                        dataSetOffset + ldataSetIndex,
                        indexMin,
                        indexMax,
                        getErrorType(),
                        isPolarPlot,
                        isallowNaNs()
                    );

                }

                return Optional.of(cache);

            });

            cachedPoints.ifPresentOrElse(value -> {

                // invoke data reduction algorithm
                value.reduce(rendererDataReducerProperty().get(), isReducePoints(), getMinRequiredReductionSize());

                // draw individual plot components
                drawChartComponents(gc, value, dataSet);
                value.release();

            }, () -> {

                if (dataSet.isFitted()) {
                    drawFittedLine(gc, dataSet, dataSet.getFittedPoints(xMin, xMax), xAxis, yAxis);
                }

            });

        }

        return localDataSetList;

    }

    protected void drawMarker(JISAErrorDataSet dataSet, final GraphicsContext gc, final double x, final double y) {

        JISAMarker marker;

        try {
            marker = dataSet.getShape().getMarker();
        } catch (Exception e) {
            marker = JISAMarker.CIRCLE;
        }

        marker.draw(gc, x, y, dataSet.getSize(), dataSet.getThickness(), dataSet.getColour(), Colour.WHITE);

    }

    protected void drawErrorBars(final GraphicsContext gc, final CachedDataPoints cache, JISAErrorDataSet dataSet) {

        if (dataSet.isMarkerVisible()) {

            gc.save();

            setErrorBarGC(gc, dataSet);

            final int dashHalf = dataSet.getSize().intValue();

            for (int i = 0; i < cache.actualDataCount; i++) {

                if (Math.abs(cache.errorXPos[i] - cache.errorXNeg[i]) > 1.0) {

                    gc.strokeLine(cache.errorXNeg[i], cache.yValues[i], cache.errorXPos[i], cache.yValues[i]);
                    gc.strokeLine(cache.errorXNeg[i], cache.yValues[i] - dashHalf, cache.errorXNeg[i], cache.yValues[i] + dashHalf);
                    gc.strokeLine(cache.errorXPos[i], cache.yValues[i] - dashHalf, cache.errorXPos[i], cache.yValues[i] + dashHalf);

                }

                if (Math.abs(cache.errorYPos[i] - cache.errorYNeg[i]) > 1.0) {

                    gc.strokeLine(cache.xValues[i], cache.errorYNeg[i], cache.xValues[i], cache.errorYPos[i]);
                    gc.strokeLine(cache.xValues[i] - dashHalf, cache.errorYNeg[i], cache.xValues[i] + dashHalf, cache.errorYNeg[i]);
                    gc.strokeLine(cache.xValues[i] - dashHalf, cache.errorYPos[i], cache.xValues[i] + dashHalf, cache.errorYPos[i]);

                }

            }

            gc.restore();

        }

        drawPolyLine(gc, cache, dataSet);
        drawMarkers(gc, cache, dataSet);

    }

    /**
     * @param gc    the graphics context from the Canvas parent
     * @param cache reference to local cached data point object
     */
    protected void drawMarkers(final GraphicsContext gc, final CachedDataPoints cache, JISAErrorDataSet dataSet) {

        if (!isDrawMarker() || !dataSet.isMarkerVisible()) {
            return;
        }

        gc.save();

        for (int i = 0; i < cache.actualDataCount; i++) {

            final double x = cache.xValues[i];
            final double y = cache.yValues[i];
            drawMarker(dataSet, gc, x, y);

        }

        gc.restore();
    }

    protected void drawPolyLine(final GraphicsContext gc, final CachedDataPoints cache, JISAErrorDataSet dataSet) {

        if (!dataSet.isLineVisible()) {
            return;
        }

        if (dataSet.isFitted()) {

            Axis          xAxis  = getFirstAxis(Orientation.HORIZONTAL);
            Axis          yAxis  = getFirstAxis(Orientation.VERTICAL);
            DoubleDataSet fitted = dataSet.getFittedPoints(xAxis.getMin(), xAxis.getMax());

            if (fitted == null) {
                drawDataLine(gc, cache, dataSet);
            } else {
                drawFittedLine(gc, dataSet, fitted, xAxis, yAxis);
            }

        } else {

            drawDataLine(gc, cache, dataSet);

        }

    }

    protected void drawFittedLine(GraphicsContext gc, JISAErrorDataSet dataSet, DoubleDataSet fitted, Axis xAxis, Axis yAxis) {

        if (fitted == null) {
            return;
        }

        gc.save();

        gc.setStroke(dataSet.getColour());
        gc.setLineWidth(dataSet.getThickness());
        gc.setLineDashes(dataSet.getDash().getDoubleArray());

        gc.strokePolyline(Arrays.stream(fitted.getXValues()).map(xAxis::getDisplayPosition).toArray(), Arrays.stream(fitted.getYValues()).map(yAxis::getDisplayPosition).toArray(), fitted.getDataCount());

        gc.restore();

    }

    protected JISARenderer getThis() {
        return this;
    }

    private void drawChartComponents(final GraphicsContext gc, final CachedDataPoints cache, final JISAErrorDataSet dataSet) {
        drawErrorBars(gc, cache, dataSet);
    }

    protected static void drawDataLine(final GraphicsContext gc, final CachedDataPoints cache, JISAErrorDataSet dataSet) {

        gc.save();

        setLineGC(gc, dataSet);

        if (dataSet.getOrdering() != Series.Ordering.NONE) {

            Integer[] indices = Range.count(0, cache.actualDataCount - 1).array();

            switch (dataSet.getOrdering()) {

                case X_AXIS:
                    Arrays.sort(indices, Comparator.comparingDouble(i -> cache.xValues[i]));
                    break;

                case Y_AXIS:
                    Arrays.sort(indices, Comparator.comparingDouble(i -> cache.yValues[i]));
                    break;

            }

            gc.beginPath();
            gc.moveTo(cache.xValues[indices[0]], cache.yValues[indices[0]]);

            for (int i = 1; i < indices.length; i++) {
                gc.lineTo(cache.xValues[indices[i]], cache.yValues[indices[i]]);
            }

            gc.stroke();


        } else {

            gc.strokePolyline(cache.xValues, cache.yValues, cache.actualDataCount);

        }

        gc.restore();

    }

    private static void compactVector(final double[] input, final int stopIndex) {

        if (stopIndex >= 0) {
            System.arraycopy(input, input.length - stopIndex, input, stopIndex, stopIndex);
        }

    }
}
