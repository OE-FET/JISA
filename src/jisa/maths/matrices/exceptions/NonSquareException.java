package jisa.maths.matrices.exceptions;

public class NonSquareException extends MatrixException {

    public NonSquareException() {
        super("Matrix is not square.");
    }

}
