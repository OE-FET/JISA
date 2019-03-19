package JISA.Experiment;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.DifferentiableUnivariateRealFunction;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.analysis.polynomials.PolynomialFunction;

public interface Function extends DifferentiableUnivariateRealFunction {


    @Override
    default Function derivative() {
        return x -> (Function.this.value(x + Float.MIN_VALUE) - Function.this.value(x)) / Float.MIN_VALUE;
    }

    @Override
     double value(double x);

    default double[] getCoefficients() {
        return new double[]{};
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
