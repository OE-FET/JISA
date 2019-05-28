package JISA.Maths;

import JISA.Experiment.Function;
import JISA.Experiment.PFunction;
import JISA.Experiment.XYPoint;
import org.apache.commons.math.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math.optimization.fitting.CurveFitter;
import org.apache.commons.math.optimization.fitting.ParametricRealFunction;
import org.apache.commons.math.optimization.fitting.PolynomialFitter;
import org.apache.commons.math.optimization.general.GaussNewtonOptimizer;

import java.util.Collection;
import java.util.Iterator;

public class Maths {

    public static Function polyFit(Matrix x, Matrix y, final int degree) {

        if (x.size() != y.size()) {
            throw new IllegalArgumentException("Matrices much match in size!");
        }

        GaussNewtonOptimizer optimiser = new GaussNewtonOptimizer(true);
        PolynomialFitter fitter = new PolynomialFitter(degree, optimiser);
        optimiser.setMaxEvaluations(1000);
        optimiser.setMaxIterations(1000);

        Iterator<Double> xIttr = x.iterator();
        Iterator<Double> yIttr = y.iterator();

        while (xIttr.hasNext() && yIttr.hasNext()) {
            fitter.addObservedPoint(1.0, xIttr.next(), yIttr.next());
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

    public static Function fit(Matrix x, Matrix y, PFunction toFit, double[] initial) {

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

        Iterator<Double> xIttr = x.iterator();
        Iterator<Double> yIttr = y.iterator();

        while (xIttr.hasNext() && yIttr.hasNext()) {
            fitter.addObservedPoint(1.0, xIttr.next(), yIttr.next());
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
