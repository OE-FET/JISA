package jisa.maths.matrices;

public class MatrixFactory<T> {

    private final AbstractMatrix<T> source;
    private final T                 unity;
    private final Copier<T>         copier;

    public MatrixFactory(T zero, T unity, Operation<T> add, Operation<T> subtract, Operation<T> multiply, Operation<T> divide, Copier<T> copier) {

        this.unity  = unity;
        this.copier = copier;

        source = new AbstractMatrix<T>(zero, unity, 1, 1) {
            @Override
            protected T add(T a, T b) {
                return add.operate(a, b);
            }

            @Override
            protected T subtract(T a, T b) {
                return subtract.operate(a, b);
            }

            @Override
            protected T multiply(T a, T b) {
                return multiply.operate(a, b);
            }

            @Override
            protected T divide(T a, T b) {
                return divide.operate(a, b);
            }

            @Override
            protected T copy(T a) {
                return copier.copy(a);
            }
        };

    }

    public Matrix<T> create(int rows, int cols) {
        return source.create(rows, cols);
    }

    public Matrix<T> create(int rows, int cols, T... values) {
        Matrix<T> matrix = create(rows, cols);
        matrix.setAll(values);
        return matrix;
    }

    public Matrix<T> createIdentity(int size) {

        Matrix<T> matrix = create(size, size);

        for (int i = 0; i < size; i++) {
            matrix.set(i, i, copier.copy(unity));
        }

        return matrix;

    }

    public Matrix<T> createRow(T... values) {
        return create(1, values.length, values);
    }

    public Matrix<T> createCol(T... values) {
        return create(values.length, 1, values);
    }

    public interface Operation<T> {
        T operate(T a, T b);
    }

    public interface Copier<T> {
        T copy(T a);
    }

}
