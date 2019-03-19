package JISA.Experiment;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.DifferentiableUnivariateRealFunction;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.analysis.polynomials.PolynomialFunction;

public abstract class Function implements DifferentiableUnivariateRealFunction {

    protected double[] params;

    public Function(double... params) {
        this.params = params;
    }

    @Override
    public Function derivative() {

        return new Function() {
            @Override
            public double value(double x) {
                return (Function.this.value(x + Float.MIN_VALUE) - Function.this.value(x)) / Float.MIN_VALUE;
            }
        };

    }

    @Override
    public abstract double value(double x);

    public double[] getCoefficients() {
        return params;
    }

    public Function add(Function toAdd) {
        return new Function() {
            @Override
            public double value(double x) {
                return Function.this.value(x) + toAdd.value(x);
            }
        };
    }

    public Function subtract(Function toAdd) {
        return new Function() {
            @Override
            public double value(double x) {
                return Function.this.value(x) - toAdd.value(x);
            }
        };
    }

    public Function multiply(Function toAdd) {
        return new Function() {
            @Override
            public double value(double x) {
                return Function.this.value(x) * toAdd.value(x);
            }
        };
    }

    public Function divide(Function toAdd) {
        return new Function() {
            @Override
            public double value(double x) {
                return Function.this.value(x) / toAdd.value(x);
            }
        };
    }

    public static class PolyFunction extends Function {

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
