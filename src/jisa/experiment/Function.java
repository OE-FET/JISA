package jisa.experiment;

import jisa.maths.Matrix;
import org.apache.commons.math.analysis.DifferentiableUnivariateRealFunction;
import org.apache.commons.math.analysis.polynomials.PolynomialFunction;

import java.util.Arrays;
import java.util.Iterator;

public interface Function extends DifferentiableUnivariateRealFunction {


    @Override
    default Function derivative() {
        return x -> {
            return (Function.this.value(x * 1.01) - Function.this.value(x)) / (0.01 * x);
        };
    }

    @Override
    double value(double x);

    default double getNormalisedChiSquared(Matrix x, Matrix y) {

        if (x.getSize() != y.getSize()) {
            throw new IllegalArgumentException("Matrices must have the same size!");
        }

        Iterator<Double> xIttr = x.iterator();
        Iterator<Double> yIttr = y.iterator();

        double sum = 0;

        while (xIttr.hasNext() && yIttr.hasNext()) {

            sum += Math.pow(yIttr.next() - value(xIttr.next()), 2);

        }

        return sum / x.getSize();

    }


    default Function add(Function toAdd) {
        return x -> Function.this.value(x) + toAdd.value(x);
    }

    default Function subtract(Function toAdd) {
        return x -> Function.this.value(x) - toAdd.value(x);
    }

    default Function multiply(Function toAdd) {
        return x -> Function.this.value(x) * toAdd.value(x);
    }

    default Function divide(Function toAdd) {
        return x -> Function.this.value(x) / toAdd.value(x);
    }

    default double[] getCoefficients() {
        return new double[0];
    }

    default double[] getCoefficientErrors() {
        return new double[0];
    }

    class PolyFunction implements Function {

        private PolynomialFunction func;
        private double[]           errors;

        public PolyFunction(PolynomialFunction f) {
            func   = f;
            errors = new double[func.getCoefficients().length];
            Arrays.fill(errors, 0.0);
        }

        @Override
        public double value(double x) {
            return func.value(x);
        }

        public Function derivative() {
            return new PolyFunction(func.polynomialDerivative());
        }

        public double[] getCoefficients() {
            return func.getCoefficients();
        }

        public void setCoefficientErrors(double[] errors) {
            this.errors = errors;
        }

        public double[] getCoefficientErrors() {
            return errors;
        }

    }

}
