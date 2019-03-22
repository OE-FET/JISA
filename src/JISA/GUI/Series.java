package JISA.GUI;

import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;

import java.util.List;

public interface Series extends Iterable<XYChart.Data<Double, Double>> {

    void addPoint(double x, double y);

    List<XYChart.Data<Double, Double>> getPoints();

    void clear();

    void showMarkers(boolean show);

    void setMarkerShape(Shape shape, double size);

    default void setMarkerShape(Shape shape) {
        setMarkerShape(shape, 5.0);
    }

    void setName(String name);

    String getName();

    void setColour(Color colour);

    Color getColour();

    void setLineWidth(double width);

    void setAutoReduction(int reduceTo, int limit);

    default void disableAutoReduction() {
        setAutoReduction(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    void reduceNow();

    void setXAutoRemove(double range);

    void setYAutoRemove(double range);

    default void disableXAutoRemove() {
        setXAutoRemove(Double.POSITIVE_INFINITY);
    }

    default void disableYAutoRemove() {
        setYAutoRemove(Double.POSITIVE_INFINITY);
    }

    void remove();

    void updateLimits();

    void restore();

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
