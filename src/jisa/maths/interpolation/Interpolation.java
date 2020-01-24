package jisa.maths.interpolation;

import jisa.maths.functions.MultiFunction;
import jisa.maths.functions.XYFunction;
import jisa.maths.functions.XYZFunction;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MathException;
import org.apache.commons.math.analysis.MultivariateRealFunction;
import org.apache.commons.math.analysis.interpolation.MicrosphereInterpolator;
import org.apache.commons.math.analysis.interpolation.MultivariateRealInterpolator;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Interpolation {

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

        double[][] params = new double[paramList.size()][3];
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

    public static XYFunction interpolate2D(Iterable<Double> x, Iterable<Double> y, Iterable<Double> v) {

        MultivariateRealInterpolator interpolator = new MicrosphereInterpolator();
        List<double[]>               paramList    = new LinkedList<>();
        List<Double>                 valueList    = new LinkedList<>();

        Iterator<Double> xi = x.iterator();
        Iterator<Double> yi = y.iterator();
        Iterator<Double> vi = v.iterator();

        while (xi.hasNext() && yi.hasNext() && vi.hasNext()) {

            paramList.add(new double[]{xi.next(), yi.next()});
            valueList.add(vi.next());

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
                        if (!i.hasNext()) { return false; }
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
