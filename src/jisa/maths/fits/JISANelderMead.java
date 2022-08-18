package jisa.maths.fits;

import jisa.maths.matrices.RealMatrix;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.MultivariateRealFunction;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.apache.commons.math.optimization.direct.NelderMead;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JISANelderMead extends NelderMead {

    protected MultivariateRealFunction f;

    public RealPointValuePair optimize(final MultivariateRealFunction function, final MultivariateRealFunction realFunction, final GoalType goalType, final double[] startPoint) throws FunctionEvaluationException, OptimizationException, IllegalArgumentException {
        this.f = realFunction;
        return super.optimize(function, goalType, startPoint);
    }

    public double[] estimateErrors(int num) throws FunctionEvaluationException {

        RealPointValuePair[] simplex = this.simplex.clone();

        int        n  = simplex.length;
        RealMatrix y  = new RealMatrix(n, n);
        RealMatrix B  = new RealMatrix(n - 1, n - 1);
        RealMatrix Q  = new RealMatrix(n - 1, n - 1);
        RealMatrix p0 = RealMatrix.asColumn(simplex[0].getPoint());

        for (int i = 1; i < n; i++) {

            RealMatrix pi = RealMatrix.asColumn(simplex[i].getPoint());
            Q.setCol(i - 1, pi.subtract(p0));

        }

        for (int i = 0; i < n; i++) {

            for (int j = 0; j < n; j++) {

                RealMatrix pi  = RealMatrix.asColumn(simplex[i].getPoint());
                RealMatrix pj  = RealMatrix.asColumn(simplex[j].getPoint());

                y.set(i, j, 0.5 * (f.value(pi.getCol(0)) + f.value(pj.getCol(0))));

            }

        }


        for (int i = 1; i < n; i++) {

            for (int j = 1; j < n; j++) {

                B.set(i - 1, j - 1, 2.0 * (y.get(i, j) + y.get(0, 0) - y.get(i, 0) - y.get(0, j)));

            }

        }

        RealMatrix cVar = Q.multiply(B.invert()).multiply(Q.transpose());


        return cVar.getDiagonal().map(Math::abs).map(Math::sqrt).getCol(0);

    }

}
