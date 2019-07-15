package jisa.experiment;

import org.apache.commons.math.analysis.DifferentiableUnivariateRealFunction;
import org.apache.commons.math.analysis.polynomials.PolynomialFunction;

public interface Function extends DifferentiableUnivariateRealFunction {


    @Override
    default Function derivative() {
        return x -> {
            return (Function.this.value(x*1.01) - Function.this.value(x)) / (0.01*x);
        };
    }

    @Override
    double value(double x);


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

    class PolyFunction implements Function {

        PolynomialFunction func;

        public PolyFunction(PolynomialFunction f) {
            func = f;
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

    }

}
