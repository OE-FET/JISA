package jisa.maths.matrices.exceptions;

import jisa.maths.matrices.Matrix;

public class DimensionException extends MatrixException {

    public DimensionException(int rowGiven, int colGiven, int rowNeeded, int colNeeded) {
        super(String.format("A %sx%s matrix was given when a %sx%s matrix was expected",
            rowGiven  >= 0 ? rowGiven  : "n",
            colGiven  >= 0 ? colGiven  : "n",
            rowNeeded >= 0 ? rowNeeded : "n",
            colNeeded >= 0 ? colNeeded : "n"
        ));
    }

    public DimensionException(Matrix given, int rowNeeded, int colNeeded) {
        this(given.rows(), given.cols(), rowNeeded, colNeeded);
    }

    public DimensionException(Matrix given, Matrix needed) {
        this(given, needed.rows(), needed.cols());
    }

    public DimensionException(int numGiven, int numNeeded) {
        super(String.format("Expecting %d elements, %d were given.", numNeeded, numGiven));
    }

}
