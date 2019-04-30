package JISA.GUI;

import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;

import java.util.List;

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

    void addPoint(double x, double y);

    List<XYChart.Data<Double, Double>> getPoints();

    void clear();

    void showMarkers(boolean show);

    boolean isShowingMarkers();

    void setMarkerShape(Shape shape, double size);

    default void setMarkerShape(Shape shape) {
        setMarkerShape(shape, 5.0);
    }

    void setName(String name);

    String getName();

    void setColour(Color colour);

    Color getColour();

    void setLineWidth(double width);

    double getLineWidth();

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
