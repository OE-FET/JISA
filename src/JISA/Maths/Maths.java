package JISA.Maths;

import JISA.Experiment.Function;
import JISA.Experiment.PFunction;
import JISA.Experiment.XYPoint;
import org.apache.commons.math.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math.linear.MatrixUtils;
import org.apache.commons.math.linear.QRDecomposition;
import org.apache.commons.math.linear.QRDecompositionImpl;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.optimization.fitting.CurveFitter;
import org.apache.commons.math.optimization.fitting.ParametricRealFunction;
import org.apache.commons.math.optimization.fitting.PolynomialFitter;
import org.apache.commons.math.optimization.general.GaussNewtonOptimizer;
import org.apache.commons.math.stat.correlation.PearsonsCorrelation;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Maths {

    public static Function polyFit(Matrix x, Matrix y, final int degree) {

        if (x.size() != y.size()) {
            throw new IllegalArgumentException("Matrices much match in size!");
        }

        x = x.asColumn();
        y = y.asColumn();

        Matrix V = new Matrix(x.size(), degree + 1);

        for (int i = 0; i < x.size(); i++) {
            V.set(i, degree, 1);
        }

        for (int j = degree - 1; j >= 0; j--) {

            Iterator<Double> ittr = x.iterator();

            for (int i = 0; i < x.size(); i++) {
                V.set(i, j, ittr.next() * V.get(i, j + 1));
            }

        }

        QRDecomposition decomp = new QRDecompositionImpl(V.toRealMatrix());
        Matrix          Q      = new Matrix(decomp.getQ());
        Matrix          R      = new Matrix(decomp.getR());
        R = R.subMatrix(0, 0, R.columns(), R.columns());
        Matrix denom = Q.transpose().multiply(y).subMatrix(0, 0, R.columns(), 1);

        Matrix p = new Matrix(R.toRealMatrix().solve(denom.toRealMatrix()));

        double[] c = new double[p.size()];

        int i = c.length - 1;
        for (double v : p) {
            c[i] = v;
            i--;
        }

        return new Function.PolyFunction(new PolynomialFunction(c));

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

    public static double pearsonsCorrelation(Matrix x, Matrix y) {

        PearsonsCorrelation correlation = new PearsonsCorrelation();
        return correlation.correlation(x.toArray(), y.toArray());

    }

}
