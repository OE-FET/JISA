package jisa.maths.fits;

import javafx.scene.chart.XYChart;
import jisa.Util;
import jisa.experiment.ResultTable;
import jisa.maths.functions.Function;
import jisa.maths.functions.PFunction;
import jisa.maths.matrices.Matrix;
import jisa.maths.matrices.RMatrix;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.fitting.CurveFitter;
import org.apache.commons.math.optimization.fitting.GaussianFitter;
import org.apache.commons.math.optimization.fitting.HarmonicFitter;
import org.apache.commons.math.optimization.fitting.ParametricRealFunction;
import org.apache.commons.math.optimization.general.GaussNewtonOptimizer;

import java.util.Iterator;
import java.util.List;

public class Fitting {

    private static Iterable<Double> separateX(List<XYChart.Data<Double, Double>> data) {

        return () -> new Iterator<>() {

            int i = 0;

            @Override
            public boolean hasNext() {
                return i < data.size();
            }

            @Override
            public Double next() {
                return data.get(i++).getXValue();
            }

        };

    }

    private static Iterable<Double> separateY(List<XYChart.Data<Double, Double>> data) {

        return () -> new Iterator<>() {

            int i = 0;

            @Override
            public boolean hasNext() {
                return i < data.size();
            }

            @Override
            public Double next() {
                return data.get(i++).getYValue();
            }

        };

    }


    public static LinearFit linearFit(Iterable<Double> x, Iterable<Double> y) {

        PolyFit fit = polyFit(x, y, 1);
        return fit == null ? null : new LinearFit(fit);

    }

    public static LinearFit linearFit(List<XYChart.Data<Double, Double>> data) {

        PolyFit fit = polyFit(data, 1);
        return fit == null ? null : new LinearFit(fit);

    }

    public static LinearFit linearFit(ResultTable data, int xCol, int yCol) {
        return linearFit(data.getColumns(xCol), data.getColumns(yCol));
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
    public static PolyFit polyFit(Iterable<Double> xData, Iterable<Double> yData, final int degree) {

        RMatrix x = RMatrix.iterableToCol(xData);
        RMatrix y = RMatrix.iterableToCol(yData);

        try {

            RMatrix V = new RMatrix(x.size(), degree + 1);

            for (int i = 0; i < x.size(); i++) {
                V.set(i, degree, 1.0);
            }

            for (int j = degree - 1; j >= 0; j--) {

                Iterator<Double> ittr = x.iterator();

                for (int i = 0; ittr.hasNext(); i++) {
                    V.set(i, j, ittr.next() * V.get(i, j + 1));
                }

            }

            RMatrix.QR decomp = V.getQR();
            RMatrix    Q      = decomp.getQ();
            RMatrix    R      = decomp.getR();
            RMatrix    subR   = R.getSubMatrix(0, R.cols() - 1, 0, R.cols() - 1);
            RMatrix    denom  = Q.transpose().multiply(y).getSubMatrix(0, subR.cols() - 1, 0, 0);
            double[]   p      = Util.reverseArray(subR.leftDivide(denom).getCol(0));
            Function   fitted = new Function.WrappedFunction(new PolynomialFunction(p));
            double     norm   = y.subtract(fitted.value(x)).getNorm();
            RMatrix    covb   = R.transpose().multiply(R).leftDivide(new RMatrix.Identity(R.cols())).multiply(norm * norm / (x.size() - degree));
            RMatrix    se     = covb.getDiagonal().map(Math::sqrt);
            double[]   errors = Util.reverseArray(se.getCol(0));

            return new PolyFit(p, errors);

        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }

    }

    public static PolyFit polyFit(List<XYChart.Data<Double, Double>> data, final int degree) {
        return polyFit(separateX(data), separateY(data), degree);
    }

    public static PolyFit polyFit(ResultTable data, int xCol, int yCol, int degree) {
        return polyFit(data.getColumns(xCol), data.getColumns(yCol), degree);
    }

    public static GaussianFit gaussianFit(Iterable<Double> x, Iterable<Double> y) {

        GaussianFitter fit = new GaussianFitter(new GaussNewtonOptimizer(true));

        Util.iterateCombined(x, y, fit::addObservedPoint);

        try {

            return new GaussianFit(fit.fit());

        } catch (FunctionEvaluationException | OptimizationException e) {
            return null;
        }

    }

    public static GaussianFit gaussianFit(List<XYChart.Data<Double, Double>> data) {
        return gaussianFit(separateX(data), separateY(data));
    }

    public static GaussianFit gaussianFit(ResultTable data, int xCol, int yCol) {
        return gaussianFit(data.getColumns(xCol), data.getColumns(yCol));
    }

    public static Fit fit(Iterable<Double> x, Iterable<Double> y, PFunction toFit, double... initial) {

        ParametricRealFunction func = new ParametricRealFunction() {
            @Override
            public double value(double v, double[] doubles) {
                return toFit.calculate(v, doubles);
            }

            @Override
            public double[] gradient(double x, double[] parameters) {

                double[] gradients = new double[parameters.length];

                for (int i = 0; i < parameters.length; i++) {

                    final int finalI = i;
                    Function toDeriv = p -> {
                        double[] temp = parameters.clone();
                        temp[finalI] = p;
                        return toFit.calculate(x, temp);
                    };

                    gradients[i] = toDeriv.derivative().value(parameters[i]);

                }

                return gradients;

            }
        };

        GaussNewtonOptimizer optimiser = new GaussNewtonOptimizer(false);
        optimiser.setMaxIterations(1000);
        optimiser.setMaxEvaluations(1000);

        CurveFitter fitter = new CurveFitter(optimiser);

        Util.iterateCombined(x, y, (xp, yp) -> {
            if (Double.isFinite(xp) && Double.isFinite(yp) && Double.isFinite(toFit.calculate(xp, initial))) {
                fitter.addObservedPoint(xp, yp);
            }
        });

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

    public static Fit fit(List<XYChart.Data<Double, Double>> data, PFunction toFit, double... initial) {
        return fit(separateX(data), separateY(data), toFit, initial);
    }

    public static Fit fit(ResultTable data, int xCol, int yCol, PFunction toFit, double... initial) {
        return fit(data.getColumns(xCol), data.getColumns(yCol), toFit, initial);
    }

    public static CosFit cosFit(Iterable<Double> x, Iterable<Double> y) {

        HarmonicFitter fitter = new HarmonicFitter(new GaussNewtonOptimizer(true));

        Util.iterateCombined(x, y, (xp, yp) -> fitter.addObservedPoint(1.0, xp, yp));

        try {
            return new CosFit(fitter.fit());
        } catch (Throwable e) {
            return null;
        }

    }

    public static CosFit cosFit(List<XYChart.Data<Double, Double>> data) {
        return cosFit(separateX(data), separateY(data));
    }

    public static CosFit cosFit(ResultTable data, int xCol, int yCol) {
        return cosFit(data.getColumns(xCol), data.getColumns(yCol));
    }

}
