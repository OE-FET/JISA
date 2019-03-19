package JISA.Experiment;

import java.util.Comparator;
import java.util.List;

public class DataFunction implements Function {

    private List<XYPoint>  points;

    public DataFunction(List<XYPoint> points) {

        this.points = points;
        this.points.sort(Comparator.comparingDouble(o -> o.x));

    }

    @Override
    public double value(double x) {

        int index = 0;

        if (x < points.get(0).x) {
            index = 1;
        } else if (x > points.get(points.size() - 1).x) {
            index = points.size() - 1;
        } else {

            for (int i = 1; i < points.size(); i++) {

                if (x < points.get(i).x) {
                    index = i;
                    break;
                }

            }
        }

        double diffY = points.get(index).y - points.get(index - 1).y;
        double diffX = points.get(index).x - points.get(index - 1).x;
        double diff  = x - points.get(index - 1).x;

        return points.get(index - 1).y + ((diff / diffX) * diffY);

    }
}
