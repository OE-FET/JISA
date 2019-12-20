package jisa.maths.fits;

import jisa.maths.Function;
import org.apache.commons.math.analysis.polynomials.PolynomialFunction;

public class PolyFit implements Fit {

    private final PolynomialFunction function;
    private final Function           wrapped;
    private final double[]           errors;

    public PolyFit(double[] coeffs, double[] errors) {
        this.function = new PolynomialFunction(coeffs);
        this.wrapped  = new Function.WrappedFunction(function);
        this.errors   = errors;
    }

    @Override
    public double getParameter(int order) {
        return function.getCoefficients()[order];
    }

    @Override
    public double[] getParameters() {
        return function.getCoefficients();
    }

    @Override
    public double getError(int order) {
        return errors[order];
    }

    @Override
    public double[] getErrors() {
        return errors.clone();
    }

    @Override
    public Function getFunction() {
        return wrapped;
    }

}
