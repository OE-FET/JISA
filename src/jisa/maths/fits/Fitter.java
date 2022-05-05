package jisa.maths.fits;

import jisa.maths.functions.PFunction;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.MultivariateRealFunction;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class Fitter {

    private final PFunction                function;
    private final MultivariateRealFunction minFunction;
    private final List<Point>              points = new LinkedList<>();
    private       double[]                 start;
    private       double[]                 maxLimits;
    private       double[]                 minLimits;
    private       int                      maxIterations;
    private       int                      maxEvaluations;
    private       double                   maxChange;
    private       double                   minChange;

    public Fitter(PFunction function) {

        this.function    = function;
        this.minFunction = parameters -> points
            .stream()
            .mapToDouble(p -> p.w * Math.pow((p.y - function.calculate(p.x, parameters)), 2))
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

    public double getMaxChange() {
        return maxChange;
    }

    public void setMaxChange(double maxChange) {
        this.maxChange = maxChange;
    }

    public double getMinChange() {
        return minChange;
    }

    public void setMinChange(double minChange) {
        this.minChange = minChange;
    }

    protected double[] gradient(final double[] parameters) {
        return new double[0];
    }

    public Fit fit() throws FunctionEvaluationException {

        double[] position = start.clone();

        return null;

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
