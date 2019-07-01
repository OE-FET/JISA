package JISA.GUI;

import JISA.Experiment.Function;
import JISA.Experiment.Result;
import JISA.Experiment.ResultList;
import JISA.Experiment.ResultTable;
import JISA.GUI.SVG.*;
import JISA.Maths.Maths;
import JISA.Maths.Matrix;
import JISA.Util;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class SmartChart {

    private final LineChart<Double, Double>          chart;
    private final SmartAxis                          xAxis;
    private final SmartAxis                          yAxis;
    private       String                             xLabel;
    private       String                             yLabel;
    private       LinkedList<Series>                 data          = new LinkedList<>();
    private       Map<Series, Integer>               map           = new HashMap<>();
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

    public SmartChart(LineChart<Double, Double> chart, SmartAxis xAxis, SmartAxis yAxis) {

        this.chart = chart;
        this.xAxis = xAxis;
        this.yAxis = yAxis;
        xLabel     = xAxis.getLabel();
        yLabel     = yAxis.getLabel();

        xAxis.setAutoRanging(true);
        yAxis.setAutoRanging(true);

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
                dmax  = d;
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

    public synchronized void updateSeries() {

        for (Series s : data) {
            s.updateStyles();
        }

    }

    public synchronized Series createSeries(String name, Color colour) {

        return new NormalSeries(name, colour);
    }

    public synchronized Series createWatchSeries(String name, Color colour, ResultTable results, Evaluable xData, Evaluable yData, Predicate<Result> filter) {

        return new NormalSeries(results, xData, yData, filter, name, colour);
    }

    public synchronized SeriesGroup createAutoSeries(ResultTable results, Evaluable xData, Evaluable yData, Evaluable sData, String pattern, Predicate<Result> filter) {

        return new AutoSeries(results, xData, yData, sData, pattern, filter);
    }

    public synchronized Series createFunctionSeries(String name, Color colour, Function f) {

        return new FunctionSeries(f, name, colour);
    }

    public void setSeriesColour(int series, Color colour) {

        setStyle(
                String.format("CHART_COLOR_%d", series + 1),
                String.format(
                        "rgba(%f,%f,%f,%f)",
                        colour.getRed() * 255D,
                        colour.getGreen() * 255D,
                        colour.getBlue() * 255D,
                        colour.getOpacity()
                )
        );


    }

    public void setLineWidth(int series, double width) {

        for (Node node : chart.lookupAll(String.format(".default-color%d.chart-series-line", series))) {
            node.setStyle(String.format("-fx-stroke-width: %f;", width));
        }

    }


    public SVG getSVG(double width, double height) {

        SVG svg = new SVG(width, height);

        double scaleW = chart.getWidth();
        double scaleH = chart.getHeight();


        return svg;

    }

    private String getSymbolCSS(Series.Shape shape, double size) {

        String style;
        switch (shape) {

            case CIRCLE:
                style = String.format("-fx-background-radius: %fpx; -fx-padding: %fpx;", size, size);
                break;

            case DOT:
                style = String.format(
                        "-fx-background-radius: %fpx; -fx-padding: %fpx; -fx-background-insets: 0, %fpx;",
                        size,
                        size,
                        2.0 * size
                );
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
                style = String.format(
                        "-fx-background-radius: 0;\n" +
                                "-fx-padding: %fpx;\n" +
                                "-fx-shape: \"M2,0 L5,4 L8,0 L10,0 L10,2 L6,5 L10,8 L10,10 L8,10 L5,6 L2,10 L0,10 L0,8 L4,5 L0,2 L0,0 Z\";",
                        size
                );
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

        return style;

    }

    public void writeSVG(double width, double height, String fileName) throws IOException {

        SVGElement main = new SVGElement("g");

        main.setAttribute("font-family", "sans-serif")
            .setAttribute("font-size", 12);

        double aStartX = 100.0;
        double aStartY = height + 65.0;
        double aEndX   = 100.0 + width;
        double aEndY   = 65.0;

        SVGLine xAxis = new SVGLine(aStartX - 0.5, aStartY, aEndX, aStartY);
        SVGLine yAxis = new SVGLine(aStartX, aStartY + 0.5, aStartX, aEndY);

        SVGText title = new SVGText((aStartX + aEndX) / 2, 50.0, "middle", chart.getTitle());
        title.setAttribute("font-size", "20px");
        main.add(title);

        xAxis.setStrokeWidth(1)
             .setStrokeColour(Color.GREY);

        yAxis.setStrokeWidth(1)
             .setStrokeColour(Color.GREY);

        List<Double> xTicks = this.xAxis.getMajorTicks();
        List<Double> yTicks = this.yAxis.getMajorTicks();

        double xScale = (aEndX - aStartX) / this.xAxis.getWidth();
        double yScale = (aEndY - aStartY) / this.yAxis.getHeight();

        StringConverter<Double> formatterX = this.xAxis.getTickLabelFormatter();
        StringConverter<Double> formatterY = this.yAxis.getTickLabelFormatter();

        for (Double x : xTicks) {

            double pos = xScale * this.xAxis.getDisplayPosition(x) + aStartX;

            if (!Util.isBetween(pos, aStartX, aEndX)) {
                continue;
            }

            SVGLine tick = new SVGLine(pos, aStartY, pos, aStartY + 10);

            tick.setStrokeWidth(1)
                .setStrokeColour(Colour.GREY);

            SVGLine grid = new SVGLine(pos, aStartY, pos, aEndY);

            grid.setStrokeWidth(0.5)
                .setStrokeColour(Colour.SILVER)
                .setDash("5", "5");

            main.add(tick);
            main.add(grid);

            SVGText label = new SVGText(pos, aStartY + 26.0, "middle", formatterX.toString(x));
            main.add(label);

        }

        SVGText xLabel = new SVGText((aEndX + aStartX) / 2, aStartY + 75.0, "middle", this.xAxis.getLabel());
        xLabel.setAttribute("font-size", "16px");
        main.add(xLabel);

        for (Double y : yTicks) {

            double pos = aEndY - yScale * this.yAxis.getDisplayPosition(y);

            if (!Util.isBetween(pos, aEndY, aStartY)) {
                continue;
            }

            SVGLine tick = new SVGLine(aStartX, pos, aStartX - 10, pos);

            tick.setStrokeWidth(1)
                .setStrokeColour(Colour.GREY);
            SVGLine grid = new SVGLine(aStartX, pos, aEndX, pos);

            grid.setStrokeWidth(0.5)
                .setStrokeColour(Colour.SILVER)
                .setDash("5", "5");
            main.add(tick);
            main.add(grid);

            SVGText label = new SVGText(aStartX - 12.0, pos + 4.0, "end", formatterY.toString(y));
            main.add(label);

        }

        SVGText yLabel = new SVGText(aStartX - 75.0, (aEndY + aStartY) / 2, "middle", this.yAxis.getLabel());
        yLabel.setAttribute("transform", String.format("rotate(-90 %s %s)", aStartX - 75.0, (aEndY + aStartY) / 2))
              .setAttribute("font-size", "16px");
        main.add(yLabel);

        main.add(xAxis);
        main.add(yAxis);

        SVGElement legend = new SVGElement("rect");

        legend.setStrokeWidth(1.0)
              .setStrokeColour(Color.SILVER)
              .setFillColour(Color.web("#f5f5f5"));


        double legendH = (data.size() * 25) + 5.0;
        double legendX = aEndX + 25.0;
        double legendY = ((aEndY + aStartY) / 2) - (legendH / 2);

        double legendW = 0.0;

        for (Series s : data) {
            legendW = Math.max(legendW, (10.0 * s.getName().length()) + 15.0 + 5 + 3 + 20.0);
        }

        legend.setAttribute("x", legendX)
              .setAttribute("y", legendY)
              .setAttribute("width", legendW)
              .setAttribute("height", legendH)
              .setAttribute("rx", 5)
              .setAttribute("ry", 5);

        if (chart.isLegendVisible()) {
            main.add(legend);
        } else {
            legendW = 0;
        }

        int i = 0;
        for (Series s : data) {

            Color  c = s.getColour();
            double w = s.getLineWidth();

            List<String> terms = new LinkedList<>();

            SVGCircle legendCircle = new SVGCircle(legendX + 15.0, legendY + (25 * i) + 15.0, 5);

            legendCircle.setStrokeColour(c)
                        .setFillColour(Color.WHITE)
                        .setStrokeWidth(3);


            SVGText legendText = new SVGText(
                    legendX + 15.0 + 5 + 3 + 10,
                    legendY + (25 * i) + 15.0 + 5,
                    "beginning",
                    s.getName()
            );

            legendText.setAttribute("font-size", "16px");

            main.add(legendCircle);
            main.add(legendText);

            boolean first = true;

            List<SVGElement> list = new LinkedList<>();

            for (XYChart.Data<Double, Double> point : s.getXYChartSeries().getData()) {

                double x = aStartX + xScale * this.xAxis.getDisplayPosition(point.getXValue());
                double y = aEndY - yScale * this.yAxis.getDisplayPosition(point.getYValue());

                terms.add(String.format("%s%s %s", first ? "M" : "L", x, y));

                if (s.isShowingMarkers()) {

                    SVGCircle circle = new SVGCircle(x, y, 5);

                    circle.setStrokeColour(c)
                          .setFillColour(Color.WHITE)
                          .setStrokeWidth(3);

                    list.add(circle);

                }

                first = false;

            }

            SVGPath path = new SVGPath(String.join(" ", terms));

            path.setStrokeColour(c)
                .setStrokeWidth(w)
                .setStyle("fill", "none");

            main.add(path);

            list.forEach(main::add);

            i++;

        }


        SVG svg = new SVG(width + legendW + 50.0 + 100.0, height + 60.0 + 100.0);
        svg.add(main);

        svg.output(fileName);

    }

    public String getXLabel() {

        return xLabel;
    }

    public void setXLabel(String label) {

        xLabel = label;
        GUI.runNow(() -> xAxis.setLabelText(label));
    }

    public String getYLabel() {

        return yLabel;
    }

    public void setYLabel(String label) {

        yLabel = label;
        GUI.runNow(() -> yAxis.setLabelText(label));
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

        xMode   = AMode.MANUAL;
        limMinX = minX;
        limMaxX = maxX;
        GUI.runNow(this::update);

    }

    public void setYLimits(double minY, double maxY) {

        yMode   = AMode.MANUAL;
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
        xMode      = AMode.TRACK;
        GUI.runNow(this::update);

    }

    public void setTrackingY(double range) {

        autoYRange = range;
        yMode      = AMode.TRACK;
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

        autoRemoveX  = true;
        removeXRange = range;
    }

    public void setYAutoRemove(double range) {

        autoRemoveY  = true;
        removeYRange = range;
    }

    public void stopXAutoRemove() {

        autoRemoveX = false;
    }

    public void stopYAutoRemove() {

        autoRemoveY = false;
    }

    private synchronized void update() {

        switch (xMode) {

            case SHOW_ALL:
                xAxis.setMaxRange(Double.POSITIVE_INFINITY);
                xAxis.setAutoRanging(true);
                break;

            case TRACK:
                xAxis.setMaxRange(autoXRange);
                xAxis.setAutoRanging(true);
                break;

            case MANUAL:
                xAxis.setAutoRanging(false);
                xAxis.setLowerBound(limMinX);
                xAxis.setUpperBound(limMaxX);
                break;

        }

        switch (yMode) {

            case SHOW_ALL:
                yAxis.setMaxRange(Double.POSITIVE_INFINITY);
                yAxis.setAutoRanging(true);
                break;

            case TRACK:
                yAxis.setMaxRange(autoYRange);
                yAxis.setAutoRanging(true);
                break;

            case MANUAL:
                yAxis.setAutoRanging(false);
                yAxis.setLowerBound(limMinY);
                yAxis.setUpperBound(limMaxY);
                break;

        }

        if (zoomFactor == 0) {
            zoomFactor = 1;
        }

        for (Series s : data) {

            if (s instanceof FunctionSeries) {
                ((FunctionSeries) s).update();
            }

        }

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
            map.clear();
            styles.clear();
            updateStyle();

            limMaxX = 0;
            limMinX = 0;
            limMaxY = 0;
            limMinY = 0;


            if (xAxis.isAutoRanging()) {
                setXLimits(xAxis.getMode() != SmartAxis.Mode.LOGARITHMIC ? 0 : 1, 100);
                autoXLimits();
            }

            if (yAxis.isAutoRanging() && yAxis.getMode() != SmartAxis.Mode.LOGARITHMIC) {
                setYLimits(yAxis.getMode() != SmartAxis.Mode.LOGARITHMIC ? 0 : 1, 100);
                autoYLimits();
            }

        });
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

    public interface Evaluable {

        double evaluate(Result r);

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

            x1     = start.getXValue();
            x2     = end.getXValue();
            y1     = start.getYValue();
            y2     = end.getYValue();
            dx     = x1 - x2;
            dy     = y1 - y2;
            x1y2   = x1 * y2;
            x2y1   = x2 * y1;
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

        protected Color                          colour;
        protected String[]                       lineStyle    = {"-fx-stroke: orange;", "-fx-stroke-width: 1.5;"};
        protected String[]                       symbolStyle  = {"", ""};
        protected XYChart.Series<Double, Double> series;
        protected DataList                       data;
        protected int                            reduceLimit  = Integer.MAX_VALUE;
        protected int                            reduceValue  = Integer.MAX_VALUE;
        protected ResultTable                    list;
        protected double                         xRange       = Double.POSITIVE_INFINITY;
        protected double                         yRange       = Double.POSITIVE_INFINITY;
        protected double                         minX         = Double.POSITIVE_INFINITY;
        protected double                         maxX         = Double.NEGATIVE_INFINITY;
        protected double                         minY         = Double.POSITIVE_INFINITY;
        protected double                         maxY         = Double.NEGATIVE_INFINITY;
        protected double                         lineWidth    = 3;
        protected boolean                        externalList = false;
        protected String                         className;

        public NormalSeries(String name, Color colour) {

            SmartChart.this.data.add(this);

            int index = 0;

            for (int i = 0; i < map.size() + 1; i++) {
                if (!map.containsValue(i)) {
                    map.put(this, i);
                    index = i;
                    break;
                }
            }

            className = String.format("series-%d", index);


            list   = new ResultList("X", "Y");
            data   = new DataList(list, (r) -> r.get(0), (r) -> r.get(1));
            series = new XYChart.Series<>(data);


            GUI.runNow(() -> {
                chart.getData().add(series);
                series.getNode().getStyleClass().add(className);
            });

            setName(name);

            if (colour != null) {
                setColour(colour);
            } else {
                setColour(defaultColours[index % defaultColours.length]);
            }

            data.onChange = () -> {
                if (data.size() > reduceLimit) {
                    data.reduce(reduceValue);
                }
                updateStyles();
            };

            data.limitChange = (xMin, xMax, yMin, yMax) -> {
                minX = xMin;
                maxX = xMax;
                minY = yMin;
                maxY = yMax;
            };

            data.setShowCondition((d) -> d.getXValue() >= (maxX - xRange) && d.getYValue() >= (maxY - yRange));

            updateSeries();

        }

        public NormalSeries(ResultTable results, Evaluable xData, Evaluable yData, Predicate<Result> filter, String name, Color colour) {

            externalList = true;

            SmartChart.this.data.add(this);
            int index = 0;
            for (int i = 0; i < map.size() + 1; i++) {
                if (!map.containsValue(i)) {
                    index = i;
                    map.put(this, i);
                    break;
                }
            }

            className = String.format("series-%d", index);

            list   = results;
            data   = new DataList(list, xData, yData, filter == null ? (r) -> true : filter);
            series = new XYChart.Series<>(data);

            GUI.runNow(() -> {
                chart.getData().add(series);
                series.getNode().getStyleClass().add(className);
            });

            setName(name);

            if (colour != null) {
                setColour(colour);
            } else {
                setColour(defaultColours[index % defaultColours.length]);
            }

            data.onChange = () -> {
                if (data.size() > reduceLimit) {
                    data.reduce(reduceValue);
                }
                updateStyles();
            };

            data.limitChange = (xMin, xMax, yMin, yMax) -> {
                minX = xMin;
                maxX = xMax;
                minY = yMin;
                maxY = yMax;
            };

            data.setShowCondition((d) -> d.getXValue() >= (maxX - xRange) && d.getYValue() >= (maxY - yRange));

            updateSeries();

        }

        @Override
        public Series addPoint(double x, double y) {

            data.add(new XYChart.Data<>(x, y));
            return this;
        }

        public List<XYChart.Data<Double, Double>> getPoints() {

            return data.list;
        }

        @Override
        public Series clear() {

            GUI.runNow(() -> {

                data.clear();

                if (!externalList) {
                    list.clear();
                }

                chart.getData().set(chart.getData().indexOf(series), series);
                updateSeries();

            });
            return this;
        }

        @Override
        public Series showMarkers(boolean show) {

            data.showMarkers(show);

            if (!show) {
                symbolStyle[1] = "-fx-background-radius: 0px; -fx-padding: 0; -fx-padding: 1 5 1 5;";
            }

            updateStyles();

            return this;
        }

        @Override
        public boolean isShowingMarkers() {

            return data.showMarkers;
        }

        @Override
        public Series setMarkerShape(Shape shape, double size) {

            symbolStyle[1] = getSymbolCSS(shape, size);
            data.setMarkerStyle(String.join(" ", symbolStyle));
            updateStyles();
            return this;
        }

        @Override
        public String getName() {

            return series.getName();
        }

        @Override
        public Series setName(String name) {

            GUI.runNow(() -> series.setName(name));
            return this;
        }

        @Override
        public Color getColour() {

            return colour;
        }

        @Override
        public Series setColour(Color c) {

            GUI.runNow(() -> {
                colour         = c;
                lineStyle[0]   = String.format(
                        "-fx-stroke: rgba(%f,%f,%f,%f);",
                        colour.getRed() * 255,
                        colour.getGreen() * 255,
                        colour.getBlue() * 255,
                        colour.getOpacity()
                );
                symbolStyle[0] = String.format(
                        "-fx-background-color: rgba(%f,%f,%f,%f), white;",
                        colour.getRed() * 255,
                        colour.getGreen() * 255,
                        colour.getBlue() * 255,
                        colour.getOpacity()
                );
                updateStyles();
            });

            return this;

        }

        public synchronized void updateStyles() {

            GUI.runNow(() -> {

                int index = chart.getData().indexOf(series);
                series.getNode().lookupAll(".chart-series-line").forEach(n -> n.setStyle(String.join(" ", lineStyle)));

                series.getData().forEach((d) -> {
                    if (d.getNode() != null) {
                        d.getNode().lookupAll(".chart-line-symbol").forEach(s -> s.setStyle(String.join(
                                " ",
                                symbolStyle
                        )));
                    }
                });

                for (Node node : chart.lookupAll(".chart-legend-item-symbol")) {
                    for (String styleClass : node.getStyleClass()) {
                        if (styleClass.equals(String.format("series%d", index))) {
                            node.setStyle(String.join(" ", symbolStyle));
                            break;
                        }
                    }
                }

            });

        }

        @Override
        public double getLineWidth() {

            return lineWidth;
        }

        @Override
        public Series setLineWidth(double width) {

            lineWidth    = width;
            lineStyle[1] = String.format("-fx-stroke-width: %s;", width);
            updateStyles();
            return this;

        }

        @Override
        public Series setAutoReduction(int reduceTo, int limit) {

            reduceValue = reduceTo;
            reduceLimit = limit;
            reduceNow();
            return this;
        }

        @Override
        public Series reduceNow() {

            GUI.runNow(() -> data.reduce(reduceValue));
            return this;
        }

        @Override
        public Series setXAutoRemove(double range) {

            xRange = range;
            return this;
        }

        @Override
        public Series setYAutoRemove(double range) {

            yRange = range;
            return this;
        }

        @Override
        public Series remove() {

            GUI.runNow(() -> chart.getData().remove(series));
            SmartChart.this.data.remove(this);
            map.remove(this);
            updateSeries();
            return this;
        }

        @Override
        public Series updateLimits() {

            data.updateLimits();
            return this;
        }

        @Override
        public Series restore() {

            disableAutoReduction();
            disableXAutoRemove();
            disableYAutoRemove();
            data.update();
            return this;
        }

        @Override
        public Series polyFit(int degree) {

            Series fitted = createSeries(getName() + " (fit)", getColour());
            return new PolyFitSeries(this, fitted, degree);
        }

        @Override
        public XYChart.Series<Double, Double> getXYChartSeries() {

            return series;
        }

        @Override
        public Iterator<XYChart.Data<Double, Double>> iterator() {

            return data.iterator();
        }

    }

    private class FunctionSeries implements Series {

        private Function                                     function;
        private ObservableList<XYChart.Data<Double, Double>> data;
        private XYChart.Series<Double, Double>               series;
        private Color                                        colour;
        private double                                       lineWidth = 3;

        public FunctionSeries(Function f, String name, Color colour) {

            function = f;
            series   = new XYChart.Series<>();
            data     = series.getData();
            SmartChart.this.data.add(this);

            int index = 0;

            for (int i = 0; i < map.size() + 1; i++) {
                if (!map.containsValue(i)) {
                    index = i;
                    map.put(this, i);
                    break;
                }
            }

            GUI.runNow(() -> chart.getData().add(series));
            setName(name);


            if (colour != null) {
                setColour(colour);
            } else {
                setColour(defaultColours[index % defaultColours.length]);
            }

            update();
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

            return this;
        }

        @Override
        public Series showMarkers(boolean show) {

            return this;
        }

        @Override
        public boolean isShowingMarkers() {

            return false;
        }

        @Override
        public Series setMarkerShape(Shape shape, double size) {

            return this;
        }

        @Override
        public String getName() {

            return series.getName();
        }

        @Override
        public Series setName(String name) {

            GUI.runNow(() -> series.setName(name));
            return this;
        }

        @Override
        public Color getColour() {

            return colour;
        }

        @Override
        public Series setColour(Color colour) {

            this.colour = colour;
            setSeriesColour(map.get(this), colour);
            return this;
        }

        @Override
        public double getLineWidth() {

            return lineWidth;
        }

        @Override
        public Series setLineWidth(double width) {

            lineWidth = width;
            SmartChart.this.setLineWidth(map.get(this), width);
            return this;
        }

        @Override
        public Series setAutoReduction(int reduceTo, int limit) {

            return this;
        }

        @Override
        public Series reduceNow() {

            return this;
        }

        @Override
        public Series setXAutoRemove(double range) {

            return this;
        }

        @Override
        public Series setYAutoRemove(double range) {

            return this;
        }

        @Override
        public Series remove() {

            GUI.runNow(() -> chart.getData().remove(series));
            SmartChart.this.data.remove(this);
            map.remove(this);
            updateSeries();
            return this;
        }

        public Series update() {

            GUI.runNow(() -> {
                series.getData().clear();

                for (double x : Util.makeLinearArray(limMinX, limMaxX, (int) chart.getWidth())) {
                    XYChart.Data<Double, Double> d = new XYChart.Data<>(x, function.value(x));
                    series.getData().add(d);
                    if (d.getNode() != null) {
                        d.getNode().setVisible(false);
                    }
                }
            });
            return this;
        }

        @Override
        public Series updateLimits() {

            return this;
        }

        @Override
        public Series restore() {

            return this;
        }

        @Override
        public Series polyFit(int degree) {

            return null;
        }

        @Override
        public void updateStyles() {

        }

        @Override
        public XYChart.Series<Double, Double> getXYChartSeries() {

            return series;
        }

        @Override
        public Iterator<XYChart.Data<Double, Double>> iterator() {

            return series.getData().iterator();
        }

    }

    private class AutoSeries implements SeriesGroup {

        private Map<Double, Series> map         = new LinkedHashMap<>();
        private boolean             showMarkers = true;
        private int                 reduceLimit = 2000;
        private int                 reduceValue = 1000;
        private double              lineWidth   = 3;
        private Shape               shape       = Shape.CIRCLE;
        private double              size        = 5;
        private double              xRange      = Double.POSITIVE_INFINITY;
        private double              yRange      = Double.POSITIVE_INFINITY;
        private int                 degree      = -1;

        public AutoSeries(ResultTable results, Evaluable xData, Evaluable yData, Evaluable sData, String pattern, Predicate<Result> filter) {

            this(results, xData, yData, sData, pattern, filter, -1);
        }

        public AutoSeries(ResultTable results, Evaluable xData, Evaluable yData, Evaluable sData, String pattern, Predicate<Result> filter, int degree) {

            if (filter == null) {
                filter = r -> true;
            }

            final Predicate<Result> finalFilter = filter;

            ResultTable.OnUpdate onUpdate = (r) -> {

                if (finalFilter.test(r)) {

                    final double key = sData.evaluate(r);

                    if (!map.containsKey(key)) {

                        Series data = new NormalSeries(
                                results, xData, yData,
                                (v) -> ((sData.evaluate(v) == key) && finalFilter.test(v)),
                                String.format(pattern, key),
                                null
                        );

                        Series s;

                        if (this.degree > 0) {
                            s = data.polyFit(this.degree);
                            ((PolyFitSeries) s).updateFit();
                        } else {
                            s = data;
                        }

                        s.showMarkers(showMarkers);
                        s.setAutoReduction(reduceValue, reduceLimit);
                        s.setLineWidth(lineWidth);
                        s.setMarkerShape(shape, size);

                        map.put(key, s);
                    }

                }

            };

            for (Result r : results) {
                onUpdate.run(r);
            }

            results.addOnUpdate(onUpdate);

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
        public SeriesGroup addPoint(double x, double y) {

            for (Series s : map.values()) {
                s.addPoint(x, y);
            }
            return this;
        }

        @Override
        public List<XYChart.Data<Double, Double>> getPoints() {

            return new LinkedList<>();
        }

        @Override
        public SeriesGroup clear() {

            GUI.runNow(() -> {
                for (Series s : map.values()) {
                    s.clear();
                    chart.getData().remove(s.getXYChartSeries());
                }
                map.clear();
            });
            return this;
        }

        @Override
        public SeriesGroup showMarkers(boolean show) {

            showMarkers = show;
            for (Series s : map.values()) {
                s.showMarkers(show);
            }
            return this;
        }

        @Override
        public boolean isShowingMarkers() {

            return showMarkers;
        }

        @Override
        public SeriesGroup setMarkerShape(Shape shape, double size) {

            this.shape = shape;
            this.size  = size;
            for (Series s : map.values()) {
                s.setMarkerShape(shape, size);
            }
            return this;
        }

        @Override
        public String getName() {

            return null;
        }

        @Override
        public SeriesGroup setName(String name) {

            return this;
        }

        @Override
        public Color getColour() {

            return null;
        }

        @Override
        public SeriesGroup setColour(Color colour) {

            return this;
        }

        @Override
        public double getLineWidth() {

            return lineWidth;
        }

        @Override
        public SeriesGroup setLineWidth(double width) {

            lineWidth = width;
            for (Series s : map.values()) {
                s.setLineWidth(width);
            }
            return this;
        }

        @Override
        public SeriesGroup setAutoReduction(int reduceTo, int limit) {

            reduceValue = reduceTo;
            reduceLimit = limit;
            for (Series s : map.values()) {
                s.setAutoReduction(reduceTo, limit);
            }
            return this;
        }

        @Override
        public SeriesGroup reduceNow() {

            for (Series s : map.values()) {
                s.reduceNow();
            }
            return this;
        }

        @Override
        public SeriesGroup setXAutoRemove(double range) {

            xRange = range;
            for (Series s : map.values()) {
                s.setXAutoRemove(range);
            }
            return this;
        }

        @Override
        public SeriesGroup setYAutoRemove(double range) {

            yRange = range;
            for (Series s : map.values()) {
                s.setYAutoRemove(range);
            }
            return this;
        }

        @Override
        public SeriesGroup remove() {

            for (Series s : map.values()) {
                s.remove();
            }
            map.clear();
            updateSeries();
            return this;
        }

        @Override
        public SeriesGroup updateLimits() {

            for (Series s : map.values()) {
                s.updateLimits();
            }
            return this;
        }

        @Override
        public SeriesGroup restore() {

            disableAutoReduction();
            disableXAutoRemove();
            disableYAutoRemove();
            for (Series s : map.values()) {
                s.restore();
            }
            return this;
        }

        @Override
        public Series polyFit(int degree) {

            this.degree = degree;

            map.replaceAll((k, series) -> series.polyFit(degree));
            map.forEach((k, series) -> ((PolyFitSeries) series).updateFit());

            return this;
        }

        @Override
        public void updateStyles() {

            for (Series s : map.values()) {
                s.updateLimits();
            }
        }

        @Override
        public XYChart.Series<Double, Double> getXYChartSeries() {

            return null;
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
        private Evaluable                                          xData;
        private Evaluable                                          yData;
        private boolean                                            showMarkers = true;
        private String                                             markerStyle = "";
        private LimitChange                                        limitChange = (a, b, c, d) -> {
        };
        private double                                             minX        = Double.POSITIVE_INFINITY;
        private double                                             maxX        = Double.NEGATIVE_INFINITY;
        private double                                             minY        = Double.POSITIVE_INFINITY;
        private double                                             maxY        = Double.NEGATIVE_INFINITY;


        public DataList(ResultTable results, Evaluable xData, Evaluable yData) {

            this(results, xData, yData, null);
        }

        public DataList(ResultTable results, Evaluable xData, Evaluable yData, Predicate<Result> filter) {


            this.data   = results;
            this.xData  = xData;
            this.yData  = yData;
            this.filter = (filter == null) ? (r) -> true : filter;

            data.addOnUpdate((r) -> {

                if (this.filter.test(r)) {
                    int index = data.getNumRows() - 1;
                    XYChart.Data<Double, Double> d = new XYChart.Data<>(
                            xData.evaluate(r),
                            yData.evaluate(r),
                            index
                    );
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

            data.addClearable(() -> GUI.runNow(() -> list.clear()));

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
                List<XYChart.Data<Double, Double>> toKeep = reducePoints(list, epsilon);
                List<XYChart.Data<Double, Double>> toRemove = this.list.filtered((p) -> list.contains(p) && !toKeep.contains(
                        p));
                for (XYChart.Data<Double, Double> d : toRemove) {
                    shown.remove(d.getExtraValue());
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
                    point.setXValue(xData.evaluate(r));
                    point.setYValue(yData.evaluate(r));
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

        }

        public synchronized void update() {

            updateRemove();

            final List<XYChart.Data<Double, Double>> toAdd = new LinkedList<>();
            forEach((i, r, d) -> {

                if (!shown.contains(i) && show.test(i, d)) {
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

            data.addData(doubleDoubleData.getXValue(), doubleDoubleData.getYValue());
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
