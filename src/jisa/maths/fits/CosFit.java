package jisa.maths.fits;

import jisa.maths.functions.Function;
import org.apache.commons.math.optimization.fitting.HarmonicFunction;

public class CosFit implements Fit {

    private final HarmonicFunction function;
    private final Function         wrapped;

    public CosFit(HarmonicFunction function) {
        this.function = function;
        this.wrapped  = new Function.WrappedFunction(function);
    }

    @Override
    public double getParameter(int order) {

        switch (order) {

            case 0:
                return function.getAmplitude();

            case 1:
                return function.getPulsation();

            case 2:
                return function.getPhase();

            default:
                throw new IndexOutOfBoundsException();

        }

    }

    public double getAmplitude() {
        return function.getAmplitude();
    }

    public double getAngFrequency() {
        return function.getPulsation();
    }

    public double getPhase() {
        return function.getPhase();
    }

    @Override
    public double[] getParameters() {
        return new double[]{function.getAmplitude(), function.getPulsation(), function.getPhase()};
    }

    @Override
    public double getError(int order) {
        return 0;
    }

    @Override
    public double[] getErrors() {
        return new double[]{0, 0, 0};
    }

    @Override
    public Function getFunction() {
        return wrapped;
    }
}
