package jisa.maths.functions;

public interface XYZFunction extends MultiFunction {

    double value(double x, double y, double z);

    default double value(double[] parameters) {
        return value(parameters[0], parameters[1], parameters[2]);
    }

}
