package jisa.maths.matrices.exceptions;

public class NonRowException extends MatrixException {
    public NonRowException() {
        super("Matrix is not a row matrix.");
    }
}
