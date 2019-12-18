package jisa.maths.fits;

import jisa.experiment.Function;
import org.apache.commons.math.analysis.UnivariateRealFunction;

public interface Fit {

    /**
     * Returns the specified fitted co-efficient.
     *
     * @param order Which co-efficient
     * @return Fitted value
     */
    double getParameter(int order);

    /**
     * Returns an array of all fitted co-efficients
     *
     * @return Array of co-efficients
     */
    double[] getParameters();

    /**
     * Returns the uncertainty on the specified fitted co-efficient.
     *
     * @param order Which co-efficient
     * @return Error
     */
    double getError(int order);

    /**
     * Returns an array of all fitted co-efficient uncertainties.
     *
     * @return Errors
     */
    double[] getErrors();

    /**
     * Returns a Function object that represents the fitted function.
     *
     * @return Fitted function
     */
    Function getFunction();

}
