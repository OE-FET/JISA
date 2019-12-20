package jisa.maths.fits;

import jisa.maths.Function;
import org.apache.commons.math.optimization.fitting.GaussianFunction;

public class GaussianFit implements Fit {

    private final GaussianFunction function;
    private final Function         wrapped;

    public GaussianFit(GaussianFunction function) {
        this.function = function;
        this.wrapped  = new Function.WrappedFunction(function);
    }

    @Override
    public double getParameter(int order) {

        switch (order) {

            case 0:
                return function.getA();

            case 1:
                return function.getB();

            case 2:
                return function.getC();

            case 3:
                return function.getD();

            default:
                throw new IndexOutOfBoundsException();

        }

    }

    @Override
    public double[] getParameters() {
        return new double[]{function.getA(), function.getB(), function.getC(), function.getD()};
    }

    public double getOffset() {
        return function.getA();
    }

    public double getAmplitude() {
        return function.getB();
    }

    public double getPosition() {
        return function.getC();
    }

    public double getDeviation() {
        return function.getD();
    }

    @Override
    public double getError(int order) {
        return 0;
    }

    @Override
    public double[] getErrors() {
        return new double[]{0, 0, 0, 0};
    }

    @Override
    public Function getFunction() {
        return wrapped;
    }
}
