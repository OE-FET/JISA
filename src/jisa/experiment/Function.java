package jisa.experiment;

import jisa.maths.Matrix;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.DifferentiableUnivariateRealFunction;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.analysis.polynomials.PolynomialFunction;

import java.util.Arrays;
import java.util.Iterator;

public interface Function extends DifferentiableUnivariateRealFunction {

    @Override
    default Function derivative() {
        return x -> (Function.this.value(x * 1.01) - Function.this.value(x)) / (0.01 * x);
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

    class WrappedFunction implements Function {

        private final DifferentiableUnivariateRealFunction function;

        public WrappedFunction(DifferentiableUnivariateRealFunction function) {
            this.function = function;
        }

        @Override
        public double value(double x) {

            try {
                return function.value(x);
            } catch (FunctionEvaluationException e) {
                return Double.NaN;
            }

        }

        public Function derivative() {

            UnivariateRealFunction deriv = function.derivative();

            if (deriv instanceof DifferentiableUnivariateRealFunction) {
                return new WrappedFunction((DifferentiableUnivariateRealFunction) function.derivative());
            } else {
                return x -> {
                    try {
                        return deriv.value(x);
                    } catch (FunctionEvaluationException e) {
                        return Double.NaN;
                    }
                };
            }
        }

    }

}
