package JISA.GUI;

import javafx.scene.chart.XYChart;

import java.io.Serializable;

public class ChartData implements Serializable, Comparable<ChartData>, Chartable {

    private double X;
    private double Y;

    public ChartData(double X, double Y) {
        this.X = X;
        this.Y = Y;
    }

    public double getXValue() {
        return X;
    }

    public double getYValue() {
        return Y;
    }

    @Override
    public int compareTo(ChartData o) {

        double dist1 = Math.sqrt(Math.pow(X,2) + Math.pow(Y,2));
        double dist2 = Math.sqrt(Math.pow(o.X, 2) + Math.pow(o.Y,2));

        if (dist2 > dist1) {
            return +1;
        } else if (dist2 < dist1) {
            return -1;
        } else {
            return 0;
        }

    }

    @Override
    public XYChart.Data getData() {
        return new XYChart.Data<Double, Double>(X,Y);
    }
}

interface Chartable {

    XYChart.Data getData();

}
