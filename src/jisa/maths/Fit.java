package jisa.maths;

import jisa.experiment.Function;

public interface Fit {

    double getCoefficient(int order);

    double[] getCoefficients();

    double getError(int order);

    double[] getErrors();

    Function getFunction();

}
