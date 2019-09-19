package jisa.maths;

import jisa.Util;
import jisa.experiment.Function;
import jisa.experiment.PFunction;
import org.apache.commons.math.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math.linear.QRDecomposition;
import org.apache.commons.math.linear.QRDecompositionImpl;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.optimization.fitting.CurveFitter;
import org.apache.commons.math.optimization.fitting.ParametricRealFunction;
import org.apache.commons.math.optimization.general.GaussNewtonOptimizer;
import org.apache.commons.math.stat.correlation.PearsonsCorrelation;

import java.util.Iterator;

public class Maths {

    /**
     * Fit a polynomial of given degree to the data provided as two column matrices x and y.
     *
     * @param xData  X-Data
     * @param yData  Y-Data
     * @param degree Degree of polynomial to fit
     *
     * @return Polynomial function representing the fit
     */
    public static Fit polyFit(RealMatrix xData, RealMatrix yData, final int degree) {

        Matrix x = Matrix.toMatrix(xData);
        Matrix y = Matrix.toMatrix(yData);

        if (x.getSize() != y.getSize()) {
            throw new IllegalArgumentException("Matrices much match in size!");
        }

        try {

            x = x.asColumn();
            y = y.asColumn();

            Matrix V = new Matrix(x.getSize(), degree + 1);

            for (int i = 0; i < x.getSize(); i++) {
                V.setEntry(i, degree, 1);
            }

            for (int j = degree - 1; j >= 0; j--) {

                Iterator<Double> ittr = x.iterator();

                for (int i = 0; ittr.hasNext(); i++) {
                    V.setEntry(i, j, ittr.next() * V.getEntry(i, j + 1));
                }

            }

            Matrix.QRDecomposition decomp = V.getQRDecomposition();
            Matrix                 Q      = decomp.getQ();
            Matrix                 R      = decomp.getR();
            Matrix                 subR   = R.getSubMatrix(0, R.getColumnDimension() - 1, 0, R.getColumnDimension() - 1);
            Matrix                 denom  = Q.transpose().multiply(y).getSubMatrix(0, subR.getColumnDimension() - 1, 0, 0);
            double[]               p      = Util.reverseArray(subR.solve(denom).to1DArray());
            Function.PolyFunction  fitted = new Function.PolyFunction(new PolynomialFunction(p));
            double                 norm   = y.subtract(x.map(fitted::value)).getNorm();
            Matrix                 covb   = R.transpose().multiply(R).solve(new Matrix.Identity(R.getColumnDimension(), R.getColumnDimension())).multiply(norm * norm / (x.getSize() - degree));
            Matrix                 se     = covb.getDiagonals().map(Math::sqrt);
            double[]               errors = Util.reverseArray(se.to1DArray());

            return new Fit() {

                @Override
                public double getCoefficient(int order) {
                    return p[order];
                }

                @Override
                public double[] getCoefficients() {
                    return p.clone();
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
                    return fitted;
                }

            };

        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }

    }

    public static Fit polyFit(double[] x, double[] y, int degree) {
        return polyFit(new Matrix(x), new Matrix(y), 1);
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
                    gradients[i] = (toFit.calculate(v, tmpParams) - toFit.calculate(
                        v,
                        doubles
                    )) / (tmpParams[i] - doubles[i]);

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
        return correlation.correlation(x.to1DArray(), y.to1DArray());

    }

}
