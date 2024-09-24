package jisa.maths.interpolation;

import jisa.maths.functions.Function;
import jisa.maths.functions.MultiFunction;
import jisa.maths.functions.XYFunction;
import jisa.maths.functions.XYZFunction;
import jisa.maths.matrices.Matrix;
import jisa.maths.matrices.RealMatrix;
import jisa.maths.matrices.exceptions.DimensionException;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MathException;
import org.apache.commons.math.analysis.MultivariateRealFunction;
import org.apache.commons.math.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math.analysis.interpolation.MicrosphereInterpolator;
import org.apache.commons.math.analysis.interpolation.MultivariateRealInterpolator;
import org.apache.commons.math.analysis.interpolation.SplineInterpolator;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Interpolation {

    /**
     * Returns a function representing the interpolation between the given set of points.
     *
     * @param v         Values of the function to interpolate
     * @param arguments The arguments of the function to interpolate.
     *
     * @return Multivariate function the returns a value for a given set of arguments by interpolating between the supplied points.
     */
    public static MultiFunction interpolateND(Iterable<Double> v, Iterable<Double>... arguments) {

        MultivariateRealInterpolator interpolator = new MicrosphereInterpolator();
        List<double[]>               paramList    = new LinkedList<>();
        List<Double>                 valueList    = new LinkedList<>();

        Iterator<double[]> pi = new MultiIterable(arguments).iterator();
        Iterator<Double>   vi = v.iterator();

        while (pi.hasNext() && vi.hasNext()) {

            paramList.add(pi.next());
            valueList.add(vi.next());

        }

        double[][] params = new double[paramList.size()][arguments.length];
        double[]   values = new double[paramList.size()];

        for (int i = 0; i < values.length; i++) {
            params[i] = paramList.get(i);
            values[i] = valueList.get(i);
        }

        try {

            MultivariateRealFunction interpolated = interpolator.interpolate(params, values);

            return (p) -> {
                try {
                    return interpolated.value(p);
                } catch (FunctionEvaluationException e) {
                    return Double.NaN;
                }
            };

        } catch (MathException e) {
            return null;
        }

    }

    /**
     * Returns a function representing the interpolation between the given set of points.
     *
     * @param colData the data to interpolate with each argument as a separate column and the final column being the values of each point.
     *
     * @return Multivariate function the returns a value for a given set of arguments by interpolating between the supplied points.
     */
    public static MultiFunction interpolateND(Matrix<Double> colData) {

        RealMatrix   matrix    = RealMatrix.asRealMatrix(colData);
        RealMatrix[] arguments = new RealMatrix[colData.cols() - 1];
        RealMatrix   values    = matrix.getColMatrix(colData.cols() - 1);

        for (int i = 0; i < colData.cols() - 1; i++) {
            arguments[i] = matrix.getColMatrix(i);
        }

        return interpolateND(values, arguments);

    }

    /**
     * Returns a function representing the 3-dimensional interpolation of the supplied data-points.
     *
     * @param x X-Values
     * @param y Y-Values
     * @param z Z-Values
     * @param v Value to interpolate
     *
     * @return Interpolated 3D function (XYZFunction)
     */
    public static XYZFunction interpolate3D(Iterable<Double> x, Iterable<Double> y, Iterable<Double> z, Iterable<Double> v) {

        MultivariateRealInterpolator interpolator = new MicrosphereInterpolator();
        List<double[]>               paramList    = new LinkedList<>();
        List<Double>                 valueList    = new LinkedList<>();

        Iterator<Double> xi = x.iterator();
        Iterator<Double> yi = y.iterator();
        Iterator<Double> zi = z.iterator();
        Iterator<Double> vi = v.iterator();

        while (xi.hasNext() && yi.hasNext() && zi.hasNext() && vi.hasNext()) {

            paramList.add(new double[]{xi.next(), yi.next(), zi.next()});
            valueList.add(vi.next());

        }

        double[][] params = new double[paramList.size()][3];
        double[]   values = new double[paramList.size()];

        for (int i = 0; i < values.length; i++) {
            params[i] = paramList.get(i);
            values[i] = valueList.get(i);
        }

        try {

            MultivariateRealFunction interpolated = interpolator.interpolate(params, values);

            return (xv, yv, zv) -> {
                try {
                    return interpolated.value(new double[]{xv, yv, zv});
                } catch (FunctionEvaluationException e) {
                    return Double.NaN;
                }
            };

        } catch (MathException e) {
            return null;
        }

    }

    /**
     * Returns a function representing the 3-dimensional interpolation of the supplied data-points.
     *
     * @param colData Matrix with the following 4 columns: x-values, y-values, z-values and values of each point
     *
     * @return Interpolated 3D function (XYZFunction)
     */
    public static XYZFunction interpolate3D(Matrix<Double> colData) {

        if (colData.cols() < 4) {
            throw new DimensionException(colData, -1, 4);
        }

        return interpolate3D(
            colData.getColMatrix(0),
            colData.getColMatrix(1),
            colData.getColMatrix(2),
            colData.getColMatrix(3)
        );

    }

    public static XYFunction interpolate2D(Iterable<? extends Number> x, Iterable<? extends Number> y, Iterable<? extends Number> v) {

        MultivariateRealInterpolator interpolator = new MicrosphereInterpolator();
        List<double[]>               paramList    = new LinkedList<>();
        List<Double>                 valueList    = new LinkedList<>();

        Iterator<? extends Number> xi = x.iterator();
        Iterator<? extends Number> yi = y.iterator();
        Iterator<? extends Number> vi = v.iterator();

        while (xi.hasNext() && yi.hasNext() && vi.hasNext()) {

            paramList.add(new double[]{xi.next().doubleValue(), yi.next().doubleValue()});
            valueList.add(vi.next().doubleValue());

        }

        double[][] params = new double[paramList.size()][2];
        double[]   values = new double[paramList.size()];

        for (int i = 0; i < values.length; i++) {
            params[i] = paramList.get(i);
            values[i] = valueList.get(i);
        }

        try {

            MultivariateRealFunction interpolated = interpolator.interpolate(params, values);

            return (xv, yv) -> {
                try {
                    return interpolated.value(new double[]{xv, yv});
                } catch (FunctionEvaluationException e) {
                    return Double.NaN;
                }
            };

        } catch (MathException e) {
            return null;
        }

    }

    public static XYFunction interpolate2D(Matrix<Double> colData) {

        if (colData.cols() < 3) {
            throw new DimensionException(colData, -1, 3);
        }

        return interpolate2D(
            colData.getColMatrix(0),
            colData.getColMatrix(1),
            colData.getColMatrix(2)
        );

    }

    public static Function interpolate1D(Iterable<Double> x, Iterable<Double> v) {

        LinearInterpolator              interpolator = new LinearInterpolator();
        List<Map.Entry<Double, Double>> list         = new LinkedList<>();

        Iterator<Double> xi = x.iterator();
        Iterator<Double> vi = v.iterator();

        while (xi.hasNext() && vi.hasNext()) {
            list.add(Map.entry(xi.next(), vi.next()));
        }

        list.sort(Map.Entry.comparingByKey());

        Iterator<Map.Entry<Double, Double>> iterator = list.iterator();

        // Remove any entries that are not strictly increasing
        Double last = null;
        while (iterator.hasNext()) {

            Map.Entry<Double, Double> entry = iterator.next();

            if (last != null && entry.getKey() <= last) {
                iterator.remove();
            }

            last = entry.getKey();

        }

        double[] params = new double[list.size()];
        double[] values = new double[list.size()];

        for (int i = 0; i < values.length; i++) {
            params[i] = list.get(i).getKey();
            values[i] = list.get(i).getValue();
        }

        return new Function.WrappedFunction(interpolator.interpolate(params, values));

    }

    public static Function interpolate1D(Matrix<Double> colData) {

        if (colData.cols() < 2) {
            throw new DimensionException(colData, -1, 2);
        }

        return interpolate1D(
            colData.getColMatrix(0),
            colData.getColMatrix(1)
        );

    }

    public static Function interpolateSmooth(Iterable<Double> x, Iterable<Double> v) {

        SplineInterpolator              interpolator = new SplineInterpolator();
        List<Map.Entry<Double, Double>> list         = new LinkedList<>();

        Iterator<Double> xi = x.iterator();
        Iterator<Double> vi = v.iterator();

        while (xi.hasNext() && vi.hasNext()) {
            list.add(Map.entry(xi.next(), vi.next()));
        }

        list.sort(Map.Entry.comparingByKey());

        Iterator<Map.Entry<Double, Double>> iterator = list.iterator();

        // Remove any entries that are not strictly increasing
        Double last = null;
        while (iterator.hasNext()) {

            Map.Entry<Double, Double> entry = iterator.next();

            if (last != null && entry.getKey() <= last) {
                iterator.remove();
            }

            last = entry.getKey();

        }

        double[] params = new double[list.size()];
        double[] values = new double[list.size()];

        for (int i = 0; i < values.length; i++) {
            params[i] = list.get(i).getKey();
            values[i] = list.get(i).getValue();
        }

        return new Function.WrappedFunction(interpolator.interpolate(params, values));

    }

    public static class MultiIterable implements Iterable<double[]> {

        private final Iterable<Double>[] iterables;

        public MultiIterable(Iterable<Double>... iterables) {
            this.iterables = iterables;
        }

        @Override
        public Iterator<double[]> iterator() {

            return new Iterator<>() {

                private final Iterator<Double>[] iterators = new Iterator[iterables.length];

                {
                    for (int i = 0; i < iterables.length; i++) {
                        iterators[i] = iterables[i].iterator();
                    }
                }

                @Override
                public boolean hasNext() {

                    for (Iterator<Double> i : iterators) {
                        if (!i.hasNext()) {return false;}
                    }

                    return true;

                }

                @Override
                public double[] next() {

                    double[] values = new double[iterators.length];

                    for (int i = 0; i < iterators.length; i++) {
                        values[i] = iterators[i].next();
                    }

                    return values;

                }

            };
        }

    }

}
