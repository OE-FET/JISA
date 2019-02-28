package JISA.GUI;

import JISA.Util;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;

import java.io.Serializable;
import java.util.*;

public class SmartChart {

    private final LineChart<Double, Double>          chart;
    private final NumberAxis                         xAxis;
    private final NumberAxis                         yAxis;
    private       String                             xLabel;
    private       String                             yLabel;
    private       LinkedHashMap<Integer, Series>     data         = new LinkedHashMap<>();
    private       double                             minX         = Double.POSITIVE_INFINITY;
    private       double                             maxX         = Double.NEGATIVE_INFINITY;
    private       double                             minY         = Double.POSITIVE_INFINITY;
    private       double                             maxY         = Double.NEGATIVE_INFINITY;
    private       double                             limMaxX;
    private       double                             limMaxY;
    private       double                             limMinX;
    private       double                             limMinY;
    private       AMode                              xMode        = AMode.SHOW_ALL;
    private       AMode                              yMode        = AMode.SHOW_ALL;
    private       double                             autoXRange   = 0;
    private       double                             autoYRange   = 0;
    private       boolean                            autoRemoveX  = false;
    private       boolean                            autoRemoveY  = false;
    private       double                             removeXRange = -1.0;
    private       double                             removeYRange = -1.0;
    private       int                                nTicksX      = 10;
    private       int                                nTicksY      = 10;
    private       int                                counter      = 0;
    private       HashMap<Integer, String>           styles       = new HashMap<>();
    private       String                             baseStyle    = "";
    private       List<XYChart.Data<Double, Double>> reduced      = new ArrayList<>();

    public SmartChart(LineChart<Double, Double> chart, NumberAxis xAxis, NumberAxis yAxis) {

        this.chart = chart;
        this.xAxis = xAxis;
        this.yAxis = yAxis;
        xLabel = xAxis.getLabel();
        yLabel = yAxis.getLabel();

    }

    public synchronized int createSeries(String name, Color colour) {

        int key = counter++;

        XYChart.Series<Double, Double> show = new XYChart.Series<Double, Double>(new HidingList<>());

        show.setName(name);

        data.put(key, new Series(key, name, false, colour, show));

        GUI.runNow(() -> chart.getData().add(show));

        if (colour != null) {
            setSeriesColour(chart.getData().indexOf(show) + 1, colour);
        }

        return key;
    }

    public synchronized int createSeriesAuto(String name) {

        int key = counter++;

        XYChart.Series<Double, Double> show = new XYChart.Series<Double, Double>(new HidingList<>());

        show.setName(name);

        data.put(key, new Series(key, name, true, null, show));

        Platform.runLater(() -> chart.getData().add(show));

        return key;
    }

    public void setSeriesColour(int series, Color colour) {

        styles.put(
                series,
                String.format("CHART_COLOR_%d: rgba(%f, %f, %f);", series, colour.getRed() * 255D, colour.getGreen() * 255D, colour.getBlue() * 255D)
        );

        GUI.runNow(this::updateStyle);

    }

    private void updateStyle() {
        chart.setStyle(baseStyle + " " + String.join(" ", styles.values()));
    }

    public void addPoint(final int series, final double x, final double y) {

        maxX = Math.max(maxX, x);
        maxY = Math.max(maxY, y);
        minX = Math.min(minX, x);
        minY = Math.min(minY, y);

        Platform.runLater(() -> {

            Series s = data.get(series);

            XYChart.Data<Double, Double> data = new XYChart.Data<>(x, y);

            s.data.add(data);

            update();

            if (autoRemoveX || autoRemoveY) {
                updateRemove();
            }

        });

    }

    public void addPoint(final double x, final double y) {

        if (data.isEmpty()) {
            createSeries("Data", Color.RED);
        }

        int key = counter;
        if (!data.containsKey(key)) {
            key = data.keySet().iterator().next();
        }

        addPoint(key, x, y);
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

    private synchronized void update() {

        switch (xMode) {

            case SHOW_ALL:
                limMinX = minX - 0.025 * (maxX - minX);
                limMaxX = maxX + 0.025 * (maxX - minX);
                break;

            case TRACK:
                limMaxX = maxX;
                limMinX = maxX - autoXRange;
                break;

        }

        switch (yMode) {

            case SHOW_ALL:
                limMinY = minY - 0.025 * (maxY - minY);
                limMaxY = maxY + 0.025 * (maxY - minY);
                ;
                break;

            case TRACK:
                limMaxY = maxY;
                limMinY = maxY - autoYRange;
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

    }

    public void reduce() {
        data.forEach((i, s) -> {
            s.reduce();
        });
    }

    private synchronized void updateRemove() {
        for (Series s : data.values()) {
            s.data.removeIf(this::removePoint);
        }
    }

    private boolean removePoint(XYChart.Data<Double, Double> data) {

        if (autoRemoveX && (data.getXValue() < (maxX - removeXRange))) {
            return true;
        }

        if (autoRemoveY && (data.getYValue() < (maxY - removeYRange))) {
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

    public void clear() {

        GUI.runNow(() -> {
            Integer[] keys = data.keySet().toArray(new Integer[0]);

            for (Integer i : keys) {

                if (data.get(i).auto) {
                    chart.getData().remove(data.get(i).series);
                    data.remove(i);
                } else {
                    data.get(i).data.clear();
                }

            }

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

    public void fullClear() {
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

    public enum AMode {
        SHOW_ALL,
        TRACK,
        MANUAL;
    }

    private class Series {

        final int                                      key;
        final String                                   name;
        final boolean                                  auto;
        final Color                                    colour;
        final XYChart.Series<Double, Double>           series;
        final HidingList<XYChart.Data<Double, Double>> data;

        public Series(int key, String name, boolean auto, Color colour, XYChart.Series<Double, Double> data) {
            this.name = name;
            this.key = key;
            this.auto = auto;
            this.colour = colour;
            this.series = data;
            this.data = (HidingList<XYChart.Data<Double, Double>>) this.series.getData();
        }

        public void reduce() {

            HashMap<String, List<XYChart.Data<Double, Double>>> map = new HashMap<>();

            for (XYChart.Data<Double, Double> d : data.fullList()) {

                int    x   = 500 * (int) (xAxis.getDisplayPosition(d.getXValue()) / 500);
                int    y   = 500 * (int) (yAxis.getDisplayPosition(d.getYValue()) / 500);
                String key = String.format("%d-%d", x, y);

                if (!map.containsKey(key)) {
                    map.put(key, new ArrayList<>());
                }

                map.get(key).add(d);

            }

            for (List<XYChart.Data<Double, Double>> pixel : map.values()) {
                reduced.add(pixel.get(0));
            }

            data.setShowCondition(reduced::contains);

        }

    }

}
