package jisa.maths.matrices;

public class MMatrix<U> extends AbstractMatrix<Matrix<U>> {

    public MMatrix(Matrix<U> zero, Matrix<U> unity, int rows, int cols) {
        super(zero, unity, rows, cols);
    }

    @Override
    protected Matrix<U> add(Matrix<U> a, Matrix<U> b) {
        return a.add(b);
    }

    @Override
    protected Matrix<U> subtract(Matrix<U> a, Matrix<U> b) {
        return a.subtract(b);
    }

    @Override
    protected Matrix<U> multiply(Matrix<U> a, Matrix<U> b) {
        return a.multiply(b);
    }

    @Override
    protected Matrix<U> divide(Matrix<U> a, Matrix<U> b) {
        return a.divide(b);
    }

    @Override
    protected Matrix<U> copy(Matrix<U> a) {
        return a.copy();
    }

}
