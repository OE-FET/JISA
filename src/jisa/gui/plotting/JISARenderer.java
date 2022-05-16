package jisa.gui.plotting;

import de.gsi.chart.Chart;
import de.gsi.chart.XYChart;
import de.gsi.chart.XYChartCss;
import de.gsi.chart.axes.Axis;
import de.gsi.chart.axes.spi.CategoryAxis;
import de.gsi.chart.renderer.Renderer;
import de.gsi.chart.renderer.spi.AbstractErrorDataSetRendererParameter;
import de.gsi.chart.renderer.spi.utils.DefaultRenderColorScheme;
import de.gsi.chart.utils.StyleParser;
import de.gsi.dataset.DataSet;
import de.gsi.dataset.spi.DoubleDataSet;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
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

        final Canvas          canvas = new Canvas(width, height);
        final GraphicsContext gc     = canvas.getGraphicsContext2D();

        final String  style        = dataSet.getStyle();
        final Integer layoutOffset = StyleParser.getIntegerPropertyValue(style, XYChartCss.DATASET_LAYOUT_OFFSET);
        final Integer dsIndexLocal = StyleParser.getIntegerPropertyValue(style, XYChartCss.DATASET_INDEX);

        final int dsLayoutIndexOffset = layoutOffset == null ? 0 : layoutOffset; // TODO: rationalise

        final int plottingIndex = dsLayoutIndexOffset + (dsIndexLocal == null ? dsIndex : dsIndexLocal);

        gc.save();

        DefaultRenderColorScheme.setLineScheme(gc, dataSet.getStyle(), plottingIndex);
        DefaultRenderColorScheme.setGraphicsContextAttributes(gc, dataSet.getStyle());
        DefaultRenderColorScheme.setFillScheme(gc, dataSet.getStyle(), plottingIndex);

        final double x = width / 2.0;
        final double y = height / 2.0;

        if (!(dataSet instanceof JISAErrorDataSet && !((JISAErrorDataSet) dataSet).isLineVisible())) {
            gc.strokeLine(1, y, width - 2.0, y);
        }

        if (!(dataSet instanceof JISAErrorDataSet && !((JISAErrorDataSet) dataSet).isMarkerVisible())) {
            drawMarker(dataSet.getStyle(), gc, (1.0 + width - 2.0) / 2.0, y);
        }

        gc.restore();
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

            final int     ldataSetIndex = dataSetIndex;
            final DataSet dataSet       = localDataSetList.get(dataSetIndex);

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

                final CachedDataPoints localCachedPoints = new CachedDataPoints(indexMin, indexMax, dataSet.getDataCount(), true);
                final boolean          isPolarPlot       = ((XYChart) chart).isPolarPlot();

                if (isParallelImplementation()) {

                    localCachedPoints.computeScreenCoordinatesInParallel(
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

                    localCachedPoints.computeScreenCoordinates(
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

                return Optional.of(localCachedPoints);

            });

            cachedPoints.ifPresent(value -> {

                // invoke data reduction algorithm
                value.reduce(rendererDataReducerProperty().get(), isReducePoints(), getMinRequiredReductionSize());

                // draw individual plot components
                drawChartComponents(gc, value, dataSet);
                value.release();

            });

        }

        return localDataSetList;

    }

    protected void drawMarker(final String style, final GraphicsContext gc, final double x, final double y) {

        JISAMarker marker;

        try {
            marker = Series.Shape.valueOf(StyleParser.getPropertyValue(style, XYChartCss.MARKER_TYPE).toUpperCase()).getMarker();
        } catch (IllegalArgumentException e) {
            marker = JISAMarker.CIRCLE;
        }

        Color  stroke    = StyleParser.getColorPropertyValue(style, XYChartCss.STROKE_COLOR);
        Color  fill      = Colour.WHITE;
        double size      = StyleParser.getFloatingDecimalPropertyValue(style, XYChartCss.MARKER_SIZE);
        double thickness = StyleParser.getFloatingDecimalPropertyValue(style, XYChartCss.STROKE_WIDTH);

        marker.draw(gc, x, y, size, thickness, stroke, fill);

    }

    protected void drawErrorBars(final GraphicsContext gc, final CachedDataPoints lCacheP, DataSet dataSet) {

        if (!(dataSet instanceof JISAErrorDataSet && !((JISAErrorDataSet) dataSet).isMarkerVisible())) {

            gc.save();

            final int dashHalf;
            if (dataSet instanceof JISAErrorDataSet) {
                JISAErrorDataSet set = (JISAErrorDataSet) dataSet;
                gc.setStroke(set.getErrorColour());
                gc.setLineWidth(set.getErrorThickness());
                dashHalf = set.getSize().intValue();
            } else {
                dashHalf = getDashSize() / 2;
                DefaultRenderColorScheme.setLineScheme(gc, lCacheP.defaultStyle, lCacheP.dataSetIndex);
            }


            for (int i = 0; i < lCacheP.actualDataCount; i++) {

                if (lCacheP.errorXNeg != lCacheP.errorXPos) {

                    gc.strokeLine(lCacheP.errorXNeg[i], lCacheP.yValues[i], lCacheP.errorXPos[i], lCacheP.yValues[i]);
                    gc.strokeLine(lCacheP.errorXNeg[i], lCacheP.yValues[i] - dashHalf, lCacheP.errorXNeg[i], lCacheP.yValues[i] + dashHalf);
                    gc.strokeLine(lCacheP.errorXPos[i], lCacheP.yValues[i] - dashHalf, lCacheP.errorXPos[i], lCacheP.yValues[i] + dashHalf);

                }

                if (lCacheP.errorYNeg != lCacheP.errorYPos) {

                    gc.strokeLine(lCacheP.xValues[i], lCacheP.errorYNeg[i], lCacheP.xValues[i], lCacheP.errorYPos[i]);
                    gc.strokeLine(lCacheP.xValues[i] - dashHalf, lCacheP.errorYNeg[i], lCacheP.xValues[i] + dashHalf, lCacheP.errorYNeg[i]);
                    gc.strokeLine(lCacheP.xValues[i] - dashHalf, lCacheP.errorYPos[i], lCacheP.xValues[i] + dashHalf, lCacheP.errorYPos[i]);

                }

            }

            gc.restore();

        }

        drawPolyLine(gc, lCacheP, dataSet);
        drawMarker(gc, lCacheP, dataSet);

    }

    /**
     * @param gc                the graphics context from the Canvas parent
     * @param localCachedPoints reference to local cached data point object
     */
    protected void drawMarker(final GraphicsContext gc, final CachedDataPoints localCachedPoints, DataSet dataSet) {

        if (!isDrawMarker() || (dataSet instanceof JISAErrorDataSet && !((JISAErrorDataSet) dataSet).isMarkerVisible())) {
            return;
        }

        gc.save();

        DefaultRenderColorScheme.setMarkerScheme(gc, localCachedPoints.defaultStyle, localCachedPoints.dataSetIndex + localCachedPoints.dataSetStyleIndex);

        for (int i = 0; i < localCachedPoints.actualDataCount; i++) {

            final double x = localCachedPoints.xValues[i];
            final double y = localCachedPoints.yValues[i];
            drawMarker(localCachedPoints.defaultStyle, gc, x, y);

        }

        gc.restore();
    }

    protected void drawPolyLine(final GraphicsContext gc, final CachedDataPoints localCachedPoints, DataSet dataSet) {

        if ((dataSet instanceof JISAErrorDataSet && !((JISAErrorDataSet) dataSet).isLineVisible())) {
            return;
        }

        if (dataSet instanceof JISAErrorDataSet && ((JISAErrorDataSet) dataSet).isFitted()) {

            Axis          xAxis  = getFirstAxis(Orientation.HORIZONTAL);
            Axis          yAxis  = getFirstAxis(Orientation.VERTICAL);
            DoubleDataSet fitted = ((JISAErrorDataSet) dataSet).getFittedPoints(xAxis.getMin(), xAxis.getMax());

            if (fitted == null) {

                drawPolyLineLine(gc, localCachedPoints, dataSet);

            } else {

                gc.save();

                gc.setStroke(((JISAErrorDataSet) dataSet).getColour());
                gc.setLineWidth(((JISAErrorDataSet) dataSet).getThickness());
                gc.setLineDashes(((JISAErrorDataSet) dataSet).getDash().getDoubleArray());

                gc.strokePolyline(Arrays.stream(fitted.getXValues()).map(xAxis::getDisplayPosition).toArray(), Arrays.stream(fitted.getYValues()).map(yAxis::getDisplayPosition).toArray(), fitted.getDataCount());

                gc.restore();

            }

        } else {

            drawPolyLineLine(gc, localCachedPoints, dataSet);

        }

    }

    protected JISARenderer getThis() {
        return this;
    }

    private void drawChartComponents(final GraphicsContext gc, final CachedDataPoints localCachedPoints, final DataSet dataSet) {
        drawErrorBars(gc, localCachedPoints, dataSet);
    }

    protected static void drawPolyLineLine(final GraphicsContext gc, final CachedDataPoints localCachedPoints, DataSet dataSet) {


        gc.save();

        DefaultRenderColorScheme.setLineScheme(gc, localCachedPoints.defaultStyle, localCachedPoints.dataSetIndex + localCachedPoints.dataSetStyleIndex);
        DefaultRenderColorScheme.setGraphicsContextAttributes(gc, localCachedPoints.defaultStyle);

        if (dataSet instanceof JISAErrorDataSet && ((JISAErrorDataSet) dataSet).getOrdering() != Series.Ordering.NONE) {

            Integer[]        indices = Range.count(0, localCachedPoints.actualDataCount - 1).array();
            JISAErrorDataSet set     = (JISAErrorDataSet) dataSet;

            switch (set.getOrdering()) {

                case X_AXIS:
                    Arrays.sort(indices, Comparator.comparingDouble(i -> localCachedPoints.xValues[i]));
                    break;

                case Y_AXIS:
                    Arrays.sort(indices, Comparator.comparingDouble(i -> localCachedPoints.yValues[i]));
                    break;

            }

            gc.beginPath();
            gc.moveTo(localCachedPoints.xValues[indices[0]], localCachedPoints.yValues[indices[0]]);

            for (int i = 1; i < indices.length; i++) {
                gc.lineTo(localCachedPoints.xValues[indices[i]], localCachedPoints.yValues[indices[i]]);
            }

            gc.stroke();


        } else {
            gc.strokePolyline(localCachedPoints.xValues, localCachedPoints.yValues, localCachedPoints.actualDataCount);
        }

        gc.restore();

    }

    private static void compactVector(final double[] input, final int stopIndex) {

        if (stopIndex >= 0) {
            System.arraycopy(input, input.length - stopIndex, input, stopIndex, stopIndex);
        }

    }
}
