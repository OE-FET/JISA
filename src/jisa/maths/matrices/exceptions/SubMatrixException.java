package jisa.maths.matrices.exceptions;

import jisa.Util;
import jisa.maths.matrices.Matrix;

public class SubMatrixException extends MatrixException {

    public SubMatrixException(Matrix attempted, Matrix sub, int row, int col) {
        super(String.format("A %dx%d matrix is too small to fit a %dx%d sub-matrix at (%d,%d).", attempted.rows(), attempted.cols(), sub.rows(), sub.cols(), row, col));
    }

    public SubMatrixException(Matrix attempted, int[] rows, int[] cols) {
        super(String.format("Cannot extract rows [%s] and columns [%s] from a %dx%d matrix.", Util.joinInts(",", rows), Util.joinInts(",", cols), attempted.rows(), attempted.cols()));
    }

}
