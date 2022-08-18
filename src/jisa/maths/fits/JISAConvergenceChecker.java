package jisa.maths.fits;

import org.apache.commons.math.optimization.RealPointValuePair;
import org.apache.commons.math.optimization.SimpleRealPointChecker;

public class JISAConvergenceChecker extends SimpleRealPointChecker {

    public boolean converged(final int iteration, final RealPointValuePair previous, final RealPointValuePair current) {

        if (Double.isInfinite(previous.getValue()) || Double.isInfinite(current.getValue()) ) {
            return false;
        } else {
            return super.converged(iteration, previous, current);
        }

    }

}
