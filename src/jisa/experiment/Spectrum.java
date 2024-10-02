package jisa.experiment;

import jisa.maths.functions.Function;
import jisa.maths.interpolation.Interpolation;
import jisa.maths.matrices.RealMatrix;
import jisa.results.DataList;
import jisa.results.DoubleColumn;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Spectrum implements Iterable<Spectrum.DataPoint> {

    private final List<DataPoint> list;

    public Spectrum(List<DataPoint> dataPoints) {
        this.list = dataPoints;
    }

    public DataList toResultList() {

        DataList list = new DataList(
            new DoubleColumn("Wavelength", "m"),
            new DoubleColumn("Value")
        );

        forEach(p -> list.addData(p.frequency, p.value));
        return list;

    }

    public RealMatrix toColumnMatrix() {

        RealMatrix matrix = new RealMatrix(list.size(), 2);

        for (int i = 0; i < list.size(); i++) {
            matrix.set(i, 0, list.get(i).frequency);
            matrix.set(i, 1, list.get(i).value);
        }

        return matrix;

    }

    public List<DataPoint> toList() {
        return new ArrayList<>(list);
    }

    public DataPoint[] toArray() {
        return list.toArray(new DataPoint[0]);
    }

    public Function interpolate() {
        return Interpolation.interpolate1D(toColumnMatrix());
    }

    public DataPoint get(int index) {
        return list.get(index);
    }

    public int size() {
        return list.size();
    }

    public DataPoint getNearestPoint(double frequency) {

        double    minDistance = Double.POSITIVE_INFINITY;
        DataPoint minPoint    = null;

        for (DataPoint point : this) {

            double distance = Math.abs(point.frequency - frequency);

            if (distance < minDistance) {
                minDistance = distance;
                minPoint    = point;
            }

        }

        return minPoint;

    }

    @Override
    public Iterator<DataPoint> iterator() {
        return list.iterator();
    }

    public static class DataPoint {

        private final double frequency;
        private final double value;

        public DataPoint(double frequency, double value) {

            this.frequency = frequency;
            this.value     = value;

        }

        public double getFrequency() {
            return frequency;
        }

        public double getValue() {
            return value;
        }

    }
}
