package jisa.maths.functions;

import org.apache.commons.math.analysis.MultivariateRealFunction;

public interface MultiFunction extends MultivariateRealFunction {

    double value(double... arguments);

}
