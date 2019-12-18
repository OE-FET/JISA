package jisa.maths.fits;

public class LinearFit extends PolyFit {

    public LinearFit(PolyFit fit) {
        super(fit.getParameters(), fit.getErrors());
    }

    public double getIntercept() {
        return getParameter(0);
    }

    public double getGradient() {
        return getParameter(1);
    }

}
