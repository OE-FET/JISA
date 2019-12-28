package jisa.maths.matrices.exceptions;

public class SingularException extends MatrixException {

    public SingularException() {
        super("Matrix is singular.");
    }

}
