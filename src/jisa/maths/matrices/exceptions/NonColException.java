package jisa.maths.matrices.exceptions;

public class NonColException extends MatrixException {
    public NonColException() {
        super("Matrix is not a column matrix.");
    }
}
