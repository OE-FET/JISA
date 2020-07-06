package jisa.maths.functions;

public interface XYFunction extends MultiFunction {

    double value(double x, double y);

    default double value(double[] parameters) {
        return value(parameters[0], parameters[1]);
    }

}
