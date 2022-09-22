package jisa.maths.fits;

import jisa.Util;
import jisa.maths.functions.Function;
import jisa.maths.functions.PFunction;
import jisa.maths.matrices.RealMatrix;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.MultivariateRealFunction;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.apache.commons.math.optimization.SimpleRealPointChecker;
import org.apache.commons.math.optimization.direct.NelderMead;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class Fitter {

    private final PFunction                function;
    private final MultivariateRealFunction minFunction;
    private final MultivariateRealFunction leastSquares;
    private final List<Point>              points            = new LinkedList<>();
    private       double[]                 start;
    private       double[]                 maxLimits;
    private       double[]                 minLimits;
    private       int                      maxIterations;
    private       int                      maxEvaluations;
    private       double                   relativeTolerance = 1e-50;
    private       double                   absoluteTolerance = 1e-50;

    public static double[] estimateErrors(MultivariateRealFunction leastSquares, double[] solution, int n) throws FunctionEvaluationException {

        final double                   minLS      = leastSquares.value(solution);
        final double                   sig2       = minLS / (n - 1);
        final MultivariateRealFunction likelihood = p -> n * 0.5 * Math.log(2.0 * Math.PI * sig2) + (leastSquares.value(p) / (2.0 * sig2));

        RealMatrix hessian = new RealMatrix(
            solution.length,
            solution.length,
            (r, c) -> secondDerivative(likelihood, solution, r, c)
        );

        return hessian.invert().getDiagonal().map(Math::abs).map(Math::sqrt).divide(Math.sqrt(n)).getCol(0);

    }

    private static double secondDerivative(MultivariateRealFunction function, double[] point, int dimX, int dimY) {

        try {

            final double   diffX = Math.abs(point[dimX]) * 0.02;
            final double   diffY = Math.abs(point[dimY]) * 0.02;
            final double[] pnt   = point.clone();

            pnt[dimX]  = point[dimX];
            pnt[dimY]  = point[dimY] - (diffY / 2.0);
            pnt[dimX] -= diffX / 2.0;
            final double val1 = function.value(pnt);
            pnt[dimX] += diffX;
            final double val2  = function.value(pnt);
            final double grad1 = (val2 - val1) / diffX;

            pnt[dimX]  = point[dimX];
            pnt[dimY]  = point[dimY] + (diffY / 2.0);
            pnt[dimX] -= diffX / 2.0;
            final double val3 = function.value(pnt);
            pnt[dimX] += diffX;
            final double val4  = function.value(pnt);
            final double grad2 = (val4 - val3) / diffX;

            return (grad2 - grad1) / diffY;

        } catch (Exception e) {
            return Double.NaN;
        }


    }

    public Fitter(PFunction function) {

        this.function    = function;
        this.minFunction = parameters -> {

            for (int i = 0; i < parameters.length; i++) {

                if (!Util.isBetween(parameters[i], minLimits[i], maxLimits[i])) {
                    return Double.POSITIVE_INFINITY;
                }

            }

            return points
                .stream()
                .mapToDouble(p -> p.w * Math.pow((p.y - function.calculate(p.x, parameters)) / p.y, 2))
                .sum() / points.stream().mapToDouble(p -> p.w).sum();

        };

        this.leastSquares = parameters -> points
            .stream()
            .mapToDouble(p -> Math.pow((p.y - function.calculate(p.x, parameters)), 2))
            .sum();

    }


    public void addPoint(double x, double y, double w) {
        points.add(new Point(x, y, w));
    }

    public void addPoint(double x, double y) {
        addPoint(x, y, 1.0);
    }

    public List<Point> getPoints() {
        return List.copyOf(points);
    }

    public void addPoints(Collection<Point> points) {
        this.points.addAll(points);
    }

    public void addPoints(Point... points) {
        addPoints(List.of(points));
    }

    public void setPoints(Collection<Point> points) {
        clearPoints();
        addPoints(points);
    }

    public void setPoints(Point... points) {
        setPoints(List.of(points));
    }

    public void clearPoints() {
        points.clear();
    }

    public PFunction getFunction() {
        return function;
    }

    public double[] getStart() {
        return start;
    }

    public void setStart(double[] start) {
        this.start = start;
    }

    public double[] getMaxLimits() {
        return maxLimits;
    }

    public void setMaxLimits(double[] maxLimits) {
        this.maxLimits = maxLimits;
    }

    public double[] getMinLimits() {
        return minLimits;
    }

    public void setMinLimits(double[] minLimits) {
        this.minLimits = minLimits;
    }

    public void setLimits(double[] minLimits, double[] maxLimits) {
        setMinLimits(minLimits);
        setMaxLimits(maxLimits);
    }

    public int getMaxIterations() {
        return maxIterations;
    }

    public void setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
    }

    public int getMaxEvaluations() {
        return maxEvaluations;
    }

    public void setMaxEvaluations(int maxEvaluations) {
        this.maxEvaluations = maxEvaluations;
    }

    public double getRelativeTolerance() {
        return relativeTolerance;
    }

    public void setRelativeTolerance(double relativeTolerance) {
        this.relativeTolerance = relativeTolerance;
    }

    public double getAbsoluteTolerance() {
        return absoluteTolerance;
    }

    public void setAbsoluteTolerance(double absoluteTolerance) {
        this.absoluteTolerance = absoluteTolerance;
    }

    public Fit fit() {

        double[]   position = start.clone();
        NelderMead opt      = new NelderMead();

        opt.setMaxEvaluations(maxEvaluations);
        opt.setMaxIterations(maxIterations);

        opt.setConvergenceChecker(new SimpleRealPointChecker(relativeTolerance, absoluteTolerance));

        try {

            RealPointValuePair pair = opt.optimize(minFunction, GoalType.MINIMIZE, start);
            Function           func = x -> function.calculate(x, pair.getPoint());

            return new Fit() {

                private double[] errors = null;

                @Override
                public double getParameter(int order) {
                    return pair.getPoint()[order];
                }

                @Override
                public double[] getParameters() {
                    return pair.getPoint();
                }

                @Override
                public double getError(int order) {
                    return getErrors()[order];
                }

                @Override
                public double[] getErrors() {

                    if (errors == null) {

                        try {
                            errors = estimateErrors(leastSquares, pair.getPoint(), points.size());
                        } catch (Throwable e) {
                            errors = new double[pair.getPoint().length];
                            Arrays.fill(errors, Double.NaN);
                        }

                    }

                    return errors.clone();

                }

                @Override
                public Function getFunction() {
                    return func;
                }

            };

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }


    }

    public static class Point {

        public final double x;
        public final double y;
        public final double w;

        public Point(double x, double y, double w) {
            this.x = x;
            this.y = y;
            this.w = w;
        }

    }

}
