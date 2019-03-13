package JISA.GUI;

import JISA.Experiment.Result;
import JISA.Experiment.ResultList;
import JISA.Experiment.ResultStream;
import JISA.Experiment.ResultTable;
import JISA.Util;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;

public class SmartChart {

    private final LineChart<Double, Double>          chart;
    private final NumberAxis                         xAxis;
    private final NumberAxis                         yAxis;
    private       String                             xLabel;
    private       String                             yLabel;
    private       LinkedList<Series>                 data          = new LinkedList<>();
    private       double                             minX          = Double.POSITIVE_INFINITY;
    private       double                             maxX          = Double.NEGATIVE_INFINITY;
    private       double                             minY          = Double.POSITIVE_INFINITY;
    private       double                             maxY          = Double.NEGATIVE_INFINITY;
    private       double                             limMaxX;
    private       double                             limMaxY;
    private       double                             limMinX;
    private       double                             limMinY;
    private       AMode                              xMode         = AMode.SHOW_ALL;
    private       AMode                              yMode         = AMode.SHOW_ALL;
    private       double                             autoXRange    = 0;
    private       double                             autoYRange    = 0;
    private       boolean                            autoRemoveX   = false;
    private       boolean                            autoRemoveY   = false;
    private       double                             removeXRange  = -1.0;
    private       double                             removeYRange  = -1.0;
    private       int                                nTicksX       = 10;
    private       int                                nTicksY       = 10;
    private       int                                counter       = 0;
    private       HashMap<String, String>            styles        = new HashMap<>();
    private       String                             baseStyle     = "-fx-background-color: white;";
    private       List<XYChart.Data<Double, Double>> reduced       = new ArrayList<>();
    private       List<Runnable>                     onLimitChange = new LinkedList<>();
    private       double                             zoomFactor    = 1;

    public SmartChart(LineChart<Double, Double> chart, NumberAxis xAxis, NumberAxis yAxis) {

        this.chart = chart;
        this.xAxis = xAxis;
        this.yAxis = yAxis;
        xLabel = xAxis.getLabel();
        yLabel = yAxis.getLabel();

        xAxis.setAnimated(false);
        yAxis.setAnimated(false);

    }

    private static List<XYChart.Data<Double, Double>> reducePoints(List<XYChart.Data<Double, Double>> points, double epsilon) {

        double dmax  = 0;
        int    index = 0;
        int    end   = points.size() - 1;
        Line   line  = new Line(points.get(0), points.get(end));

        for (int i = 1; i < end; i++) {
            double d = line.getDistance(points.get(i));
            if (d > dmax) {
                dmax = d;
                index = i;
            }
        }

        List<XYChart.Data<Double, Double>> results = new LinkedList<>();

        if (dmax > epsilon) {
            List<XYChart.Data<Double, Double>> list1 = reducePoints(points.subList(0, index + 1), epsilon);
            List<XYChart.Data<Double, Double>> list2 = reducePoints(points.subList(index, points.size()), epsilon);
            results.addAll(list1);
            results.addAll(list2.subList(1, list2.size()));
        } else {
            results.add(points.get(0));
            results.add(points.get(end));
        }

        return results;

    }

    private static Limits getEpsilonBounds(List<XYChart.Data<Double, Double>> list) {

        double[] deviations = new double[Math.max(0, list.size() - 2)];
        for (int i = 2; i < list.size(); i++) {
            XYChart.Data<Double, Double> p1  = list.get(i - 2);
            XYChart.Data<Double, Double> p2  = list.get(i - 1);
            XYChart.Data<Double, Double> p3  = list.get(i);
            double                       dev = new Line(p1, p3).getDistance(p2);
            deviations[i - 2] = dev;
        }

        double max = Double.NEGATIVE_INFINITY;
        double min = Double.POSITIVE_INFINITY;

        for (double d : deviations) {
            max = Math.max(d, max);
            min = Math.min(d, min);
        }

        return new Limits(min, max);

    }

    public synchronized Series createSeries(String name, Color colour) {
        return new NormalSeries(name, colour);
    }

    public synchronized Series createWatchSeries(String name, Color colour, ResultTable results, int xData, int yData, Predicate<Result> filter) {
        return new NormalSeries(results, xData, yData, filter, name, colour);
    }

    public synchronized SeriesGroup createAutoSeries(ResultTable results, int xData, int yData, int sData, Predicate<Result> filter) {
        return new AutoSeries(results, xData, yData, sData, filter);
    }

    public void setSeriesColour(int series, Color colour) {

        setStyle(
                String.format("CHART_COLOR_%d", series + 1),
                String.format("rgba(%f,%f,%f,%f)", colour.getRed() * 255D, colour.getGreen() * 255D, colour.getBlue() * 255D, colour.getOpacity())
        );

    }

    public void setLineWidth(int series, double width) {

        for (Node node : chart.lookupAll(String.format(".default-color%d.chart-series-line", series))) {
            node.setStyle(String.format("-fx-stroke-width: %f;", width));
        }

    }

    private String setSymbol(int series, Series.Shape shape, double size) {

        String style;
        switch (shape) {

            case CIRCLE:
                style = String.format("-fx-background-radius: %fpx; -fx-padding: %fpx;", size, size);
                break;

            case DOT:
                style = String.format("-fx-background-radius: %fpx; -fx-padding: %fpx; -fx-background-insets: 0, %fpx;", size, size, 2.0 * size);
                break;

            case SQUARE:
                style = String.format("-fx-background-radius: 0px; -fx-padding: %fpx;", size);
                break;

            case DIAMOND:
                style = String.format(
                        "-fx-background-radius: 0;\n" +
                                "-fx-padding: %fpx;\n" +
                                "-fx-shape: \"M5,0 L10,9 L5,18 L0,9 Z\";",
                        size
                );

                break;

            case CROSS:
                style = String.format("-fx-background-radius: 0;\n" +
                        "-fx-padding: %fpx;\n" +
                        "-fx-shape: \"M2,0 L5,4 L8,0 L10,0 L10,2 L6,5 L10,8 L10,10 L8,10 L5,6 L2,10 L0,10 L0,8 L4,5 L0,2 L0,0 Z\";", size);
                break;

            case TRIANGLE:
                style = String.format("-fx-background-radius: 0;\n" +
                        "-fx-padding: %fpx;\n" +
                        "-fx-shape: \"M5,0 L10,8 L0,8 Z\";", size);
                break;

            case STAR:
                style = String.format("-fx-background-radius: 0;\n" +
                        "-fx-background-insets: 0, 3px;\n" +
                        "-fx-padding: %fpx;\n" +
                        "-fx-shape: \"M20,2 L8,36 L38,12 L2,12 L32,36 Z\";", size);
                break;

            default:
                style = String.format("-fx-background-radius: %fpx; -fx-padding: %fpx;", size, size);
                break;
        }

        for (Node node : chart.lookupAll(String.format(".default-color%d.chart-line-symbol", series))) {
            node.setStyle(style);
        }

        return style;

    }

    public void setStyle(String key, String value) {
        styles.put(key, value);
        GUI.runNow(this::updateStyle);
    }

    private void updateStyle() {

        StringBuilder builder = new StringBuilder(baseStyle);

        styles.forEach((k, v) -> {
            builder.append(String.format("%s: %s; ", k, v));
        });

        chart.setStyle(baseStyle + " " + builder.toString());
    }

    public void setXLimits(double minX, double maxX) {

        xMode = AMode.MANUAL;
        limMinX = minX;
        limMaxX = maxX;
        GUI.runNow(this::update);

    }

    public void setYLimits(double minY, double maxY) {

        yMode = AMode.MANUAL;
        limMinY = minY;
        limMaxY = maxY;
        GUI.runNow(this::update);

    }

    public synchronized void setLimits(double minX, double maxX, double minY, double maxY) {

        xMode = AMode.MANUAL;
        yMode = AMode.MANUAL;

        limMinX = minX;
        limMinY = minY;
        limMaxX = maxX;
        limMaxY = maxY;

        GUI.runNow(this::update);

    }

    public void setTrackingX(double range) {
        autoXRange = range;
        xMode = AMode.TRACK;
        GUI.runNow(this::update);


    }

    public void setTrackingY(double range) {
        autoYRange = range;
        yMode = AMode.TRACK;
        GUI.runNow(this::update);


    }

    public void autoXLimits() {
        xMode = AMode.SHOW_ALL;
        GUI.runNow(this::update);


    }

    public void autoYLimits() {
        yMode = AMode.SHOW_ALL;
        GUI.runNow(this::update);


    }

    public void autoLimits() {
        xMode = AMode.SHOW_ALL;
        yMode = AMode.SHOW_ALL;
        GUI.runNow(this::update);
    }

    public void setXAutoRemove(double range) {
        autoRemoveX = true;
        removeXRange = range;
    }

    public void setYAutoRemove(double range) {
        autoRemoveY = true;
        removeYRange = range;
    }

    public void stopXAutoRemove() {
        autoRemoveX = false;
    }

    public void stopYAutoRemove() {
        autoRemoveY = false;
    }

    private void refreshLimits() {

        minX = Double.POSITIVE_INFINITY;
        maxX = Double.NEGATIVE_INFINITY;
        minY = Double.POSITIVE_INFINITY;
        maxY = Double.NEGATIVE_INFINITY;

        for (Series s : data) {
            s.updateLimits();
        }

    }

    private synchronized void update() {

        switch (xMode) {

            case SHOW_ALL:
                limMinX = minX - 0.025 * (maxX - minX);
                limMaxX = maxX + 0.025 * (maxX - minX);
                break;

            case TRACK:
                limMaxX = maxX;
                limMinX = Math.max(maxX - autoXRange, minX);
                break;

        }

        switch (yMode) {

            case SHOW_ALL:
                limMinY = minY - 0.025 * (maxY - minY);
                limMaxY = maxY + 0.025 * (maxY - minY);
                break;

            case TRACK:
                limMaxY = maxY;
                limMinY = Math.max(maxY - autoYRange, minY);
                break;

        }

        if (limMinX == Double.POSITIVE_INFINITY) {
            limMinX = -100;
            limMaxX = +100;
        }

        if (limMinY == Double.POSITIVE_INFINITY) {
            limMinY = -100;
            limMaxY = +100;
        }

        double xUnit = Util.roundSigFig((limMaxX - limMinX) / nTicksX, 1, 0);
        double yUnit = Util.roundSigFig((limMaxY - limMinY) / nTicksY, 1, 0);

        int xMag = 0;
        if (Math.max(Math.abs(limMaxX), Math.abs(limMinX)) != 0) {
            xMag = (int) Math.floor(Math.floor(Math.log10(Math.max(Math.abs(limMaxX), Math.abs(limMinX)))) / 3) * 3;
        }
        int yMag = 0;
        if (Math.max(Math.abs(limMaxY), Math.abs(limMinY)) != 0) {
            yMag = (int) Math.floor(Math.floor(Math.log10(Math.max(Math.abs(limMaxY), Math.abs(limMinY)))) / 3) * 3;
        }

        double xMagnitude = Math.pow(10.0, xMag);
        double yMagnitude = Math.pow(10.0, yMag);

        if (limMinX == limMaxX) {
            limMinX -= xMagnitude;
            limMaxX += xMagnitude;
        }

        if (limMinY == limMaxY) {
            limMinY -= yMagnitude;
            limMaxY += yMagnitude;
        }

        xAxis.setTickUnit(xUnit);
        yAxis.setTickUnit(yUnit);

        xAxis.setTickLabelFormatter(new StringConverter<Number>() {
            @Override
            public String toString(Number number) {
                return String.format("%.02f", number.doubleValue() / xMagnitude);
            }

            @Override
            public Number fromString(String s) {
                return Double.valueOf(s) * xMagnitude;
            }
        });

        yAxis.setTickLabelFormatter(new StringConverter<Number>() {
            @Override
            public String toString(Number number) {
                return String.format("%.02f", number.doubleValue() / yMagnitude);
            }

            @Override
            public Number fromString(String s) {
                return Double.valueOf(s) * yMagnitude;
            }
        });

        if (xMag != 0) {
            xAxis.setLabel(String.format("%s (E%+d)", xLabel, xMag));
        } else {
            xAxis.setLabel(xLabel);
        }

        if (yMag != 0) {
            yAxis.setLabel(String.format("%s (E%+d)", yLabel, yMag));
        } else {
            yAxis.setLabel(yLabel);
        }

        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(limMinX);
        xAxis.setUpperBound(limMaxX);
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(limMinY);
        yAxis.setUpperBound(limMaxY);

        if ((Math.abs(maxX - minX) * Math.abs(maxY - minY)) == 0) {
            zoomFactor = 1;
        } else {
            zoomFactor = Math.min(1, (Math.abs(limMaxX - limMinX) * Math.abs(limMaxY - limMinY)) / (Math.abs(maxX - minX) * Math.abs(maxY - minY)));
        }

        if (zoomFactor == 0) {
            zoomFactor = 1;
        }

    }

    private boolean removePoint(XYChart.Data<Double, Double> data) {

        if (autoRemoveX && (data.getXValue() < (maxX - removeXRange))) {
            minX = maxX - removeXRange;
            return true;
        }

        if (autoRemoveY && (data.getYValue() < (maxY - removeYRange))) {
            minY = maxY - removeYRange;
            return true;
        }

        return false;

    }

    private boolean showPoint(XYChart.Data<Double, Double> data) {

        if (xMode == AMode.SHOW_ALL && yMode == AMode.SHOW_ALL) {
            return true;
        } else {
            return Util.isBetween(data.getXValue(), limMinX, limMaxX)
                    && Util.isBetween(data.getYValue(), limMinY, limMaxY);
        }


    }

    public List<Series> getSeries() {
        return new LinkedList<>(data);
    }

    public void clear() {
        GUI.runNow(() -> {
            chart.getData().clear();
            data.clear();
            styles.clear();
            updateStyle();

            maxX = Double.NEGATIVE_INFINITY;
            minX = Double.POSITIVE_INFINITY;
            maxY = Double.NEGATIVE_INFINITY;
            minY = Double.POSITIVE_INFINITY;

            limMaxX = 0;
            limMinX = 0;
            limMaxY = 0;
            limMinY = 0;
            update();
        });
    }

    private void updateLimits(double newMinX, double newMaxX, double newMinY, double newMaxY) {

        minX = Math.min(minX, newMinX);
        maxX = Math.max(maxX, newMaxX);
        minY = Math.min(minY, newMinY);
        maxY = Math.max(maxY, newMaxY);
        update();

    }

    public enum AMode {
        SHOW_ALL,
        TRACK,
        MANUAL
    }

    public interface BiPredicate<A, B> {

        boolean test(A a, B b);

    }

    public interface TriConsumer<A, B, C> {
        void accept(A a, B b, C c);
    }

    public interface LimitChange {

        void change(double minX, double maxX, double minY, double maxY);

    }

    private static class Limits {

        private double min;
        private double max;

        public Limits(double minValue, double maxValue) {
            min = minValue;
            max = maxValue;
        }

        public double getMin() {
            return min;
        }

        public double getMax() {
            return max;
        }

    }

    private static class Line {

        private double x1;
        private double x2;
        private double y1;
        private double y2;
        private double dx;
        private double dy;
        private double x1y2;
        private double x2y1;
        private double length;

        public Line(XYChart.Data<Double, Double> start, XYChart.Data<Double, Double> end) {
            x1 = start.getXValue();
            x2 = end.getXValue();
            y1 = start.getYValue();
            y2 = end.getYValue();
            dx = x1 - x2;
            dy = y1 - y2;
            x1y2 = x1 * y2;
            x2y1 = x2 * y1;
            length = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
        }

        public double getDistance(XYChart.Data<Double, Double> p) {

            if (length == 0) {
                return 0;
            }

            return Math.abs(dy * p.getXValue() - dx * p.getYValue() + x1y2 - x2y1) / length;
        }

    }

    private class NormalSeries implements Series {

        private Color                          colour;
        private XYChart.Series<Double, Double> series;
        private DataList                       data;
        private int                            reduceLimit = Integer.MAX_VALUE;
        private int                            reduceValue = Integer.MAX_VALUE;
        private ResultTable                    list;
        private double                         xRange      = Double.POSITIVE_INFINITY;
        private double                         yRange      = Double.POSITIVE_INFINITY;
        private double                         minX        = Double.POSITIVE_INFINITY;
        private double                         maxX        = Double.NEGATIVE_INFINITY;
        private double                         minY        = Double.POSITIVE_INFINITY;
        private double                         maxY        = Double.NEGATIVE_INFINITY;

        public NormalSeries(String name, Color colour) {

            try {
                list = new ResultStream(File.createTempFile("JISA-TEMP", ".tmp").getAbsolutePath(), "X", "Y");
            } catch (IOException e) {
                list = new ResultList("X", "Y");
            }
            data = new DataList(list, 0, 1);
            series = new XYChart.Series<>(data);
            GUI.runNow(() -> chart.getData().add(series));
            setName(name);
            setColour(colour);

            data.onChange = () -> {
                if (data.size() > reduceLimit) {
                    data.reduce(reduceValue);
                }
            };

            data.limitChange = (xMin, xMax, yMin, yMax) -> {
                minX = xMin;
                maxX = xMax;
                minY = yMin;
                maxY = yMax;
                SmartChart.this.updateLimits(minX, maxX, minY, maxY);
            };

            data.setShowCondition((d) -> d.getXValue() >= (maxX - xRange) && d.getYValue() >= (maxY - yRange));

            SmartChart.this.data.add(this);

        }

        public NormalSeries(ResultTable results, int xData, int yData, Predicate<Result> filter, String name, Color colour) {

            list = results;
            data = new DataList(list, xData, yData);
            data.setFilter(filter == null ? (r) -> true : filter);
            series = new XYChart.Series<>(data);

            GUI.runNow(() -> chart.getData().add(series));
            setName(name);

            if (colour != null) {
                setColour(colour);
            }

            data.onChange = () -> {
                if (data.size() > reduceLimit) {
                    data.reduce(reduceValue);
                }
            };

            data.limitChange = (xMin, xMax, yMin, yMax) -> {
                minX = xMin;
                maxX = xMax;
                minY = yMin;
                maxY = yMax;
                SmartChart.this.updateLimits(minX, maxX, minY, maxY);
            };

            data.setShowCondition((d) -> d.getXValue() >= (maxX - xRange) && d.getYValue() >= (maxY - yRange));

            SmartChart.this.data.add(this);

        }

        @Override
        public void addPoint(double x, double y) {
            data.add(new XYChart.Data<>(x, y));
        }

        @Override
        public List<XYChart.Data<Double, Double>> getPoints() {
            return data.list;
        }

        @Override
        public void clear() {
            list.clear();
            data.clear();
            refreshLimits();
        }

        @Override
        public void showMarkers(boolean show) {
            data.showMarkers(show);
        }

        @Override
        public void setMarkerShape(Shape shape, double size) {
            data.setMarkerStyle(SmartChart.this.setSymbol(chart.getData().indexOf(series), shape, size));
        }

        @Override
        public String getName() {
            return series.getName();
        }

        @Override
        public void setName(String name) {
            GUI.runNow(() -> series.setName(name));
        }

        @Override
        public Color getColour() {
            return colour;
        }

        @Override
        public void setColour(Color colour) {
            this.colour = colour;
            setSeriesColour(chart.getData().indexOf(series), colour);
        }

        @Override
        public void setLineWidth(double width) {
            SmartChart.this.setLineWidth(chart.getData().indexOf(series), width);
        }

        @Override
        public void setAutoReduction(int reduceTo, int limit) {
            reduceValue = reduceTo;
            reduceLimit = limit;
            reduceNow();
        }

        @Override
        public void reduceNow() {
            GUI.runNow(() -> data.reduce(reduceValue));
        }

        @Override
        public void setXAutoRemove(double range) {
            xRange = range;
        }

        @Override
        public void setYAutoRemove(double range) {
            yRange = range;
        }

        @Override
        public void updateLimits() {
            data.updateLimits();
        }

        @Override
        public void restore() {
            disableAutoReduction();
            disableXAutoRemove();
            disableYAutoRemove();
            data.update();
        }

        @Override
        public Iterator<XYChart.Data<Double, Double>> iterator() {
            return data.iterator();
        }
    }

    private class AutoSeries implements SeriesGroup {

        private Map<Double, Series> map         = new LinkedHashMap<>();
        private boolean             showMarkers = true;
        private int                 reduceLimit = 2000;
        private int                 reduceValue = 1000;
        private double              lineWidth   = 2;
        private Shape               shape       = Shape.CIRCLE;
        private double              size        = 5;
        private double              xRange      = Double.POSITIVE_INFINITY;
        private double              yRange      = Double.POSITIVE_INFINITY;

        public AutoSeries(ResultTable results, int xData, int yData, int sData, Predicate<Result> filter) {

            if (filter == null) {
                filter = (r) -> true;
            }

            final Predicate<Result> finalFilter = filter;

            results.addOnUpdate((r) -> {

                if (finalFilter.test(r)) {

                    final double key = r.get(sData);

                    if (!map.containsKey(key)) {
                        Series s = new NormalSeries(
                                results, xData, yData,
                                (v) -> ((v.get(sData) == key) && finalFilter.test(v)),
                                results.hasUnits() ? String.format("%s %s", key, results.getUnits(sData)) : String.valueOf(key),
                                null
                        );

                        s.showMarkers(showMarkers);
                        s.setAutoReduction(reduceValue, reduceLimit);
                        s.setLineWidth(lineWidth);
                        s.setMarkerShape(shape, size);

                        map.put(key, s);
                    }

                }

            });

        }

        @Override
        public Collection<Series> getSeries() {
            return map.values();
        }

        @Override
        public Series getSeriesFor(double value) {
            return map.getOrDefault(value, null);
        }

        @Override
        public void addPoint(double x, double y) {
            for (Series s : map.values()) {
                s.addPoint(x, y);
            }
        }

        @Override
        public List<XYChart.Data<Double, Double>> getPoints() {
            return new LinkedList<>();
        }

        @Override
        public void clear() {
            for (Series s : map.values()) {
                s.clear();
            }
        }

        @Override
        public void showMarkers(boolean show) {
            showMarkers = show;
            for (Series s : map.values()) {
                s.showMarkers(show);
            }
        }

        @Override
        public void setMarkerShape(Shape shape, double size) {
            this.shape = shape;
            this.size = size;
            for (Series s : map.values()) {
                s.setMarkerShape(shape, size);
            }
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public void setName(String name) {

        }

        @Override
        public Color getColour() {
            return null;
        }

        @Override
        public void setColour(Color colour) {

        }

        @Override
        public void setLineWidth(double width) {
            lineWidth = width;
            for (Series s : map.values()) {
                s.setLineWidth(width);
            }
        }

        @Override
        public void setAutoReduction(int reduceTo, int limit) {
            reduceValue = reduceTo;
            reduceLimit = limit;
            for (Series s : map.values()) {
                s.setAutoReduction(reduceTo, limit);
            }
        }

        @Override
        public void reduceNow() {
            for (Series s : map.values()) {
                s.reduceNow();
            }
        }

        @Override
        public void setXAutoRemove(double range) {
            xRange = range;
            for (Series s : map.values()) {
                s.setXAutoRemove(range);
            }
        }

        @Override
        public void setYAutoRemove(double range) {
            yRange = range;
            for (Series s : map.values()) {
                s.setYAutoRemove(range);
            }
        }

        @Override
        public void updateLimits() {
            for (Series s : map.values()) {
                s.updateLimits();
            }
        }

        @Override
        public void restore() {
            disableAutoReduction();
            disableXAutoRemove();
            disableYAutoRemove();
            for (Series s : map.values()) {
                s.restore();
            }
        }

        @Override
        public Iterator<XYChart.Data<Double, Double>> iterator() {
            return new Iterator<XYChart.Data<Double, Double>>() {
                @Override
                public boolean hasNext() {
                    return false;
                }

                @Override
                public XYChart.Data<Double, Double> next() {
                    return null;
                }
            };
        }
    }

    public class DataList implements ObservableList<XYChart.Data<Double, Double>> {

        private ResultTable data;

        private ObservableList<XYChart.Data<Double, Double>>       list        = FXCollections.observableArrayList();
        private List<Integer>                                      shown       = new LinkedList<>();
        private Predicate<Result>                                  filter      = (r) -> true;
        private BiPredicate<Integer, XYChart.Data<Double, Double>> show        = (i, r) -> true;
        private Runnable                                           onChange    = () -> {
        };
        private int                                                xData;
        private int                                                yData;
        private boolean                                            showMarkers = true;
        private String                                             markerStyle = "";
        private LimitChange                                        limitChange = (a, b, c, d) -> {
        };
        private double                                             minX        = Double.POSITIVE_INFINITY;
        private double                                             maxX        = Double.NEGATIVE_INFINITY;
        private double                                             minY        = Double.POSITIVE_INFINITY;
        private double                                             maxY        = Double.NEGATIVE_INFINITY;


        public DataList(ResultTable results, int xData, int yData) {
            this(results, xData, yData, null);
        }

        public DataList(ResultTable results, int xData, int yData, Predicate<Result> filter) {


            this.data = results;
            this.xData = xData;
            this.yData = yData;
            this.filter = (filter == null) ? (r) -> true : filter;

            data.addOnUpdate((r) -> {

                if (this.filter.test(r)) {
                    int                          index = data.getNumRows() - 1;
                    XYChart.Data<Double, Double> d     = new XYChart.Data<>(r.get(xData), r.get(yData), index);
                    if (show.test(index, d)) {
                        GUI.runNow(() -> {
                            list.add(d);
                            shown.add(index);
                            if (d.getNode() != null) {
                                d.getNode().setVisible(showMarkers);
                                d.getNode().lookup(".chart-line-symbol").setStyle(markerStyle);
                            }
                            onChange.run();
                            minX = Math.min(minX, d.getXValue());
                            maxX = Math.max(maxX, d.getXValue());
                            minY = Math.min(minY, d.getYValue());
                            maxY = Math.max(maxY, d.getYValue());
                            limitChange.change(minX, maxX, minY, maxY);
                        });
                        updateRemove();
                    }
                }

            });

            update();

        }

        public void updateLimits() {

            minX = Double.POSITIVE_INFINITY;
            maxX = Double.NEGATIVE_INFINITY;
            minY = Double.POSITIVE_INFINITY;
            maxY = Double.NEGATIVE_INFINITY;

            for (XYChart.Data<Double, Double> d : list) {
                minX = Math.min(minX, d.getXValue());
                maxX = Math.max(maxX, d.getXValue());
                minY = Math.min(minY, d.getYValue());
                maxY = Math.max(maxY, d.getYValue());
            }

            limitChange.change(minX, maxX, minY, maxY);

        }

        public synchronized void reduce(int target) {

            list.sort((o1, o2) -> {

                if (o1.getXValue() < o2.getXValue()) {
                    return -1;
                } else if (o1.getXValue() > o2.getXValue()) {
                    return +1;
                } else {
                    return 0;
                }

            });

            reduce(list, target);
        }

        public synchronized void reduce(List<XYChart.Data<Double, Double>> list, int target) {

            Limits limits = getEpsilonBounds(list);

            double step = (limits.getMax() - limits.getMin()) / 10.0;

            double epsilon = limits.getMin();
            while (list.size() > target) {
                List<XYChart.Data<Double, Double>> toKeep   = reducePoints(list, epsilon);
                List<XYChart.Data<Double, Double>> toRemove = this.list.filtered((p) -> list.contains(p) && !toKeep.contains(p));
                for (XYChart.Data<Double, Double> d : toRemove) {
                    shown.remove((Integer) d.getExtraValue());
                }
                this.list.removeAll(toRemove);
                epsilon += step;
            }

        }

        public void showMarkers(boolean show) {
            showMarkers = show;

            GUI.runNow(() -> {
                for (XYChart.Data<Double, Double> point : list) {
                    if (point.getNode() != null) {
                        point.getNode().setVisible(showMarkers);
                    }
                }
            });

        }

        public void setMarkerStyle(String style) {

            markerStyle = style;

            GUI.runNow(() -> {
                for (XYChart.Data<Double, Double> point : list) {
                    if (point.getNode() != null) {
                        point.getNode().lookup(".chart-line-symbol").setStyle(markerStyle);
                    }
                }
            });

        }

        public void setFilter(Predicate<Result> filter) {
            this.filter = filter;
        }

        public void forEach(TriConsumer<Integer, Result, XYChart.Data<Double, Double>> forEach) {

            int                          i     = 0;
            XYChart.Data<Double, Double> point = new XYChart.Data<>();
            for (Result r : data) {

                if (filter.test(r)) {
                    point.setXValue(r.get(xData));
                    point.setYValue(r.get(yData));
                    forEach.accept(i, r, point);
                    i++;
                }

            }

        }

        public synchronized void updateRemove() {

            final List<XYChart.Data<Double, Double>> toRemoveData   = new LinkedList<>();
            final List<Integer>                      toRemoveResult = new LinkedList<>();

            boolean removed = false;

            for (XYChart.Data<Double, Double> d : list) {
                if (!show.test((Integer) d.getExtraValue(), d)) {
                    toRemoveData.add(d);
                    toRemoveResult.add((Integer) d.getExtraValue());
                    removed = true;
                }
            }

            GUI.runNow(() -> list.removeAll(toRemoveData));
            shown.removeAll(toRemoveResult);
            if (removed) {
                refreshLimits();
            }

        }

        public synchronized void update() {
            updateRemove();

            final List<XYChart.Data<Double, Double>> toAdd = new LinkedList<>();


            forEach((i, r, d) -> {

                if (filter.test(r) && !shown.contains(i) && show.test(i, d)) {
                    toAdd.add(new XYChart.Data<>(d.getXValue(), d.getYValue()));
                    shown.add(i);
                    minX = Math.min(minX, d.getXValue());
                    maxX = Math.max(maxX, d.getXValue());
                    minY = Math.min(minY, d.getYValue());
                    maxY = Math.max(maxY, d.getYValue());
                    limitChange.change(minX, maxX, minY, maxY);
                }

            });

            GUI.runNow(() -> {
                list.addAll(toAdd);

                for (XYChart.Data d : toAdd) {
                    if (d.getNode() != null) {
                        d.getNode().setVisible(showMarkers);
                    }
                }

            });

        }

        public ResultTable getResults() {
            return data;
        }

        public void setShowCondition(Predicate<XYChart.Data<Double, Double>> condition) {
            setShowCondition((i, d) -> condition.test(d));
        }

        public void setShowCondition(BiPredicate<Integer, XYChart.Data<Double, Double>> condition) {
            show = condition;
            update();
        }

        @Override
        public void addListener(ListChangeListener<? super XYChart.Data<Double, Double>> listener) {
            list.addListener(listener);
        }

        @Override
        public void removeListener(ListChangeListener<? super XYChart.Data<Double, Double>> listener) {
            list.removeListener(listener);
        }

        @Override
        public boolean addAll(XYChart.Data<Double, Double>... elements) {
            return false;
        }

        @Override
        public boolean setAll(XYChart.Data<Double, Double>... elements) {
            return false;
        }

        @Override
        public boolean setAll(Collection<? extends XYChart.Data<Double, Double>> col) {
            return false;
        }

        @Override
        public boolean removeAll(XYChart.Data<Double, Double>... elements) {
            return false;
        }

        @Override
        public boolean retainAll(XYChart.Data<Double, Double>... elements) {
            return false;
        }

        @Override
        public void remove(int from, int to) {

        }

        @Override
        public int size() {
            return list.size();
        }

        @Override
        public boolean isEmpty() {
            return list.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return list.contains(o);
        }

        @Override
        public Iterator<XYChart.Data<Double, Double>> iterator() {
            return list.iterator();
        }

        @Override
        public Object[] toArray() {
            return list.toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return list.toArray(a);
        }

        @Override
        public boolean add(XYChart.Data<Double, Double> doubleDoubleData) {

            Double[] d = new Double[data.getNumCols()];

            for (int i = 0; i < d.length; i++) {
                d[i] = 0.0;
            }

            d[xData] = doubleDoubleData.getXValue();
            d[yData] = doubleDoubleData.getYValue();

            data.addData(d);

            return true;
        }

        @Override
        public boolean remove(Object o) {
            return false;
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return list.containsAll(c);
        }

        @Override
        public boolean addAll(Collection<? extends XYChart.Data<Double, Double>> c) {
            return false;
        }

        @Override
        public boolean addAll(int index, Collection<? extends XYChart.Data<Double, Double>> c) {
            return false;
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return false;
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return false;
        }

        @Override
        public void clear() {
            list.clear();
        }

        @Override
        public XYChart.Data<Double, Double> get(int index) {
            return list.get(index);
        }

        @Override
        public XYChart.Data<Double, Double> set(int index, XYChart.Data<Double, Double> element) {
            return list.get(index);
        }

        @Override
        public void add(int index, XYChart.Data<Double, Double> element) {

        }

        @Override
        public XYChart.Data<Double, Double> remove(int index) {
            return null;
        }

        @Override
        public int indexOf(Object o) {
            return list.indexOf(o);
        }

        @Override
        public int lastIndexOf(Object o) {
            return list.lastIndexOf(o);
        }

        @Override
        public ListIterator<XYChart.Data<Double, Double>> listIterator() {
            return list.listIterator();
        }

        @Override
        public ListIterator<XYChart.Data<Double, Double>> listIterator(int index) {
            return list.listIterator(index);
        }

        @Override
        public List<XYChart.Data<Double, Double>> subList(int fromIndex, int toIndex) {
            return list.subList(fromIndex, toIndex);
        }

        @Override
        public void addListener(InvalidationListener listener) {
            list.addListener(listener);
        }

        @Override
        public void removeListener(InvalidationListener listener) {
            list.removeListener(listener);
        }

    }

}
