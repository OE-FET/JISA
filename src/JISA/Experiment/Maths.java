package JISA.Experiment;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.fitting.CurveFitter;
import org.apache.commons.math.optimization.fitting.ParametricRealFunction;
import org.apache.commons.math.optimization.fitting.PolynomialFitter;
import org.apache.commons.math.optimization.general.GaussNewtonOptimizer;
import org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizer;

import java.util.Collection;

public class Maths {

    public static Function polyFit(Collection<XYPoint> points, final int degree) {

        GaussNewtonOptimizer optimiser = new GaussNewtonOptimizer(true);
        PolynomialFitter fitter = new PolynomialFitter(degree, optimiser);
        optimiser.setMaxEvaluations(1000);
        optimiser.setMaxIterations(1000);

        for (XYPoint p : points) {
            fitter.addObservedPoint(1.0, p.x, p.y);
        }

        PolynomialFunction poly = null;
        try {
            poly = fitter.fit();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return new Function.PolyFunction(poly);

    }

    public static Function fit(Collection<XYPoint> points, PFunction toFit, double[] initial) {

        ParametricRealFunction func = new ParametricRealFunction() {
            @Override
            public double value(double v, double[] doubles) {
                return toFit.calculate(v, doubles);
            }

            @Override
            public double[] gradient(double v, double[] doubles) {

                double[] gradients = new double[doubles.length];

                for (int i = 0; i < doubles.length; i++) {

                    double[] tmpParams = new double[doubles.length];
                    System.arraycopy(doubles, 0, tmpParams, 0, doubles.length);
                    tmpParams[i] *= 1.01;
                    gradients[i] = (toFit.calculate(v, tmpParams) - toFit.calculate(v, doubles)) / (tmpParams[i] - doubles[i]);

                }

                return gradients;

            }
        };

        GaussNewtonOptimizer optimiser = new GaussNewtonOptimizer(true);
        optimiser.setMaxIterations(1000);
        optimiser.setMaxEvaluations(1000);

        CurveFitter fitter = new CurveFitter(optimiser);

        for (XYPoint p : points) {
            fitter.addObservedPoint(p.x, p.y);
        }

        try {
            double[] params = fitter.fit(func, initial);
            return new Function() {
                @Override
                public double value(double x) {
                    return toFit.calculate(x, params);
                }

                @Override
                public double[] getCoefficients() {
                    return params;
                }
            };
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

}
