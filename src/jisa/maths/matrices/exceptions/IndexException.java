package jisa.maths.matrices.exceptions;

import jisa.maths.matrices.Matrix;

public class IndexException extends MatrixException {
    public IndexException(int row, int col, Matrix accessed) {
        super(String.format("Element index (%d,%d) is out of bounds in a %dx%d matrix.", row, col, accessed.rows(), accessed.cols()));
    }
}
