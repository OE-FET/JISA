package jisa.maths.functions;

import jisa.maths.matrices.Matrix;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.DifferentiableUnivariateRealFunction;
import org.apache.commons.math.analysis.UnivariateRealFunction;

import java.util.Iterator;

public interface Function extends DifferentiableUnivariateRealFunction {

    @Override
    default Function derivative() {

        return x -> {

            double value = value(x);
            double diff  = 1e-6;
            double next;
            double prev;

            int n = 0;

            do {

                diff *= 10;
                next = value(x + (diff * Math.abs(x)));
                prev = value(x - (diff * Math.abs(x)));

            } while (n++ < 5 && (Double.isNaN(next) || Double.isNaN(prev) || (next == value && prev == value)));

            return (next - prev) / (2.0 * diff * Math.abs(x));

        };

    }

    @Override
    double value(double x);

    default Matrix<Double> value(Matrix<Double> x) {
        return x.map(this::value);
    }

    default double getNormalisedChiSquared(Matrix x, Matrix y) {

        if (x.size() != y.size()) {
            throw new IllegalArgumentException("Matrices must have the same size!");
        }

        Iterator<Double> xIttr = x.iterator();
        Iterator<Double> yIttr = y.iterator();

        double sum = 0;

        while (xIttr.hasNext() && yIttr.hasNext()) {

            sum += Math.pow(yIttr.next() - value(xIttr.next()), 2);

        }

        return sum / x.size();

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
