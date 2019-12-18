package jisa.maths;

import javafx.scene.chart.XYChart;
import jisa.Util;
import jisa.experiment.Function;
import jisa.experiment.PFunction;
import jisa.maths.fits.*;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.fitting.*;
import org.apache.commons.math.optimization.general.GaussNewtonOptimizer;
import org.apache.commons.math.stat.correlation.PearsonsCorrelation;

import java.util.Iterator;
import java.util.List;

public class Fitting {

    public static Matrix getXYMatrix(List<XYChart.Data<Double, Double>> data) {

        Matrix xy = new Matrix(data.size(), 2);

        for (int i = 0; i < data.size(); i++) {
            xy.setEntry(i, 0, data.get(i).getXValue());
            xy.setEntry(i, 1, data.get(i).getYValue());
        }

        return xy;

    }

    public static LinearFit linearFit(Matrix x, Matrix y) {

        PolyFit fit = polyFit(x, y, 1);
        return fit == null ? null : new LinearFit(fit);

    }

    public static LinearFit linearFit(List<XYChart.Data<Double, Double>> data) {

        PolyFit fit = polyFit(data, 1);
        return fit == null ? null : new LinearFit(fit);

    }

    public static PolyFit polyFit(List<XYChart.Data<Double, Double>> data, final int degree) {

        Matrix xy = getXYMatrix(data);
        return polyFit(xy.getColumnMatrix(0), xy.getColumnMatrix(1), degree);

    }

    /**
     * Fit a polynomial of given degree to the data provided as two column matrices x and y.
     *
     * @param xData  X-Data
     * @param yData  Y-Data
     * @param degree Degree of polynomial to fit
     *
     * @return Polynomial function representing the fit
     */
    public static PolyFit polyFit(RealMatrix xData, RealMatrix yData, final int degree) {

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
            PolynomialFunction     fitted = (new PolynomialFunction(p));
            double                 norm   = y.subtract(x.map(fitted::value)).getNorm();
            Matrix                 covb   = R.transpose().multiply(R).solve(new Matrix.Identity(R.getColumnDimension(), R.getColumnDimension())).multiply(norm * norm / (x.getSize() - degree));
            Matrix                 se     = covb.getDiagonals().map(Math::sqrt);
            double[]               errors = Util.reverseArray(se.to1DArray());

            return new PolyFit(p, errors);

        } catch (Throwable e) {
            return null;
        }

    }

    public static PolyFit polyFit(double[] x, double[] y, int degree) {
        return polyFit(new Matrix(x), new Matrix(y), 1);
    }

    public static GaussianFit gaussianFit(List<XYChart.Data<Double, Double>> data) {

        Matrix xy = getXYMatrix(data);
        return gaussianFit(xy.getColumnMatrix(0), xy.getColumnMatrix(1));

    }

    public static GaussianFit gaussianFit(Matrix x, Matrix y) {

        GaussianFitter fit = new GaussianFitter(new GaussNewtonOptimizer(true));

        Iterator<Double> xIttr = x.iterator();
        Iterator<Double> yIttr = y.iterator();

        while (xIttr.hasNext() && yIttr.hasNext()) {

            fit.addObservedPoint(xIttr.next(), yIttr.next());

        }

        try {

            return new GaussianFit(fit.fit());

        } catch (FunctionEvaluationException | OptimizationException e) {
            return null;
        }

    }

    public static Fit fit(List<XYChart.Data<Double, Double>> data, PFunction toFit, double[] initial) {
        Matrix xy = getXYMatrix(data);
        return fit(xy.getColumnMatrix(0), xy.getColumnMatrix(1), toFit, initial);
    }

    public static Fit fit(Matrix x, Matrix y, PFunction toFit, double[] initial) {

        ParametricRealFunction func = new ParametricRealFunction() {
            @Override
            public double value(double v, double[] doubles) {
                return toFit.calculate(v, doubles);
            }

            @Override
            public double[] gradient(double v, double[] doubles) {

                double[] gradients = new double[doubles.length];

                for (int i = 0; i < doubles.length; i++) {

                    double[] tmpPos = doubles.clone();
                    double[] tmpNeg = doubles.clone();

                    if (doubles[i] == 0) {
                        tmpPos[i] += 1e-5;
                        tmpNeg[i] -= 1e-5;
                    } else {
                        tmpPos[i] *= 1.1;
                        tmpNeg[i] *= 0.9;
                    }

                    gradients[i] = (toFit.calculate(v, tmpPos) - toFit.calculate(v, tmpNeg)) / (tmpPos[i] - tmpNeg[i]);

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

            return new Fit() {
                @Override
                public double getParameter(int order) {
                    return params[order];
                }

                @Override
                public double[] getParameters() {
                    return params.clone();
                }

                @Override
                public double getError(int order) {
                    return 0;
                }

                @Override
                public double[] getErrors() {
                    return new double[params.length];
                }

                @Override
                public Function getFunction() {
                    return x1 -> toFit.calculate(x1, params);
                }
            };


        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public static CosFit cosFit(List<XYChart.Data<Double, Double>> data) {
        Matrix xy = getXYMatrix(data);
        return cosFit(xy.getColumnMatrix(0), xy.getColumnMatrix(1));
    }

    public static CosFit cosFit(Matrix x, Matrix y) {

        HarmonicFitter fitter = new HarmonicFitter(new GaussNewtonOptimizer(true));

        Iterator<Double> xIttr = x.iterator();
        Iterator<Double> yIttr = y.iterator();

        while (xIttr.hasNext() && yIttr.hasNext()) {

            fitter.addObservedPoint(1.0, xIttr.next(), yIttr.next());

        }

        try {
            return new CosFit(fitter.fit());
        } catch (OptimizationException e) {
            return null;
        }

    }

    public static double pearsonsCorrelation(Matrix x, Matrix y) {

        PearsonsCorrelation correlation = new PearsonsCorrelation();
        return correlation.correlation(x.to1DArray(), y.to1DArray());

    }

}
