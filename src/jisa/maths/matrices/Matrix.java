package jisa.maths.matrices;

import jisa.maths.functions.GFunction;
import jisa.maths.matrices.exceptions.*;

import java.util.Iterator;
import java.util.function.Consumer;

public interface Matrix<T> extends Iterable<T> {

    /**
     * Checks if the two matrices, A and B, have the same dimensions.
     *
     * @param a Matrix A
     * @param b Matrix B
     *
     * @return Same dimensions?
     */
    public static boolean dimensionsMatch(Matrix a, Matrix b) {
        return (a.rows() == b.rows()) && (a.cols() == b.cols());
    }

    /**
     * Checks if the two matrices, A and B, can be multiplied (A*B)
     *
     * @param a Matrix A
     * @param b Matrix B
     *
     * @return Can they multiply?
     */
    public static boolean canMultiply(Matrix a, Matrix b) {
        return a.cols() == b.rows();
    }

    /**
     * Returns the number of rows in the matrix.
     *
     * @return Number of rows
     */
    int rows();

    /**
     * Returns the number of columns in the matrix.
     *
     * @return Number of columns
     */
    int cols();

    /**
     * Returns whether this matrix is the same as the given matrix. To qualify as being equal the two matrices must be
     * of the same dimensions and each element must be the same.
     *
     * @param compareTo Matrix to compare to
     *
     * @return Are they equal?
     */
    default boolean equals(Matrix<T> compareTo) {

        if (compareTo == null) {
            return false;
        }

        if (compareTo.rows() != rows() || compareTo.cols() != cols()) {
            return false;
        }

        for (int r = 0; r < rows(); r++) {

            for (int c = 0; c < cols(); c++) {

                if (!get(r, c).equals(compareTo.get(r, c))) {
                    return false;
                }

            }

        }

        return true;

    }


    /**
     * Returns the total number of elements in the matrix.
     *
     * @return Number of elements
     */
    default int size() {
        return rows() * cols();
    }

    /**
     * Returns whether the matrix is square or not (no. rows = no. columns).
     *
     * @return Is it square?
     */
    default boolean isSquare() {
        return rows() == cols();
    }

    /**
     * Returns the element at the given indices.
     *
     * @param row Row index
     * @param col Column index
     *
     * @return Element at (row,col)
     */
    T get(int row, int col);

    /**
     * Returns all elements as a 2-dimensional array.
     *
     * @return All elements
     */
    T[][] getData();

    /**
     * Returns all elements in a 1-dimensional array.
     *
     * @return All elements
     */
    T[] getFlatData();

    Matrix<T> getDiagonal();

    /**
     * Sets the elements on the leading diagonal of the matrix. Matrix must be square.
     *
     * @param values Diagonal elements
     *
     * @throws NonSquareException if the matrix is not square.
     */
    void setDiagonal(T... values);

    /**
     * Sets the values along the leading diagonal to the same value. Matrix must be square.
     *
     * @param value Element value
     */
    void setDiagonal(T value);

    /**
     * Sets the matrix element at the given (row, col) position.
     *
     * @param row   Row index
     * @param col   Column index
     * @param value Element value to set
     */
    void set(int row, int col, T value);

    /**
     * Sets all elements in the matrix.
     *
     * @param values Elements
     */
    void setAll(T... values);

    /**
     * Sets all elements in the specified row.
     *
     * @param row    Row index
     * @param values Elements
     */
    default void setRow(int row, T... values) {

        if (values.length != cols()) {
            throw new DimensionException(1, values.length, 1, cols());
        }

        for (int i = 0; i < cols(); i++) {
            set(row, i, values[i]);
        }

    }

    /**
     * Sets all elements in the specified row from a row matrix.
     *
     * @param row    Row index
     * @param values Row matrix of elements
     */
    default void setRow(int row, Matrix<T> values) {

        if (values.cols() != cols()) {
            throw new DimensionException(values, -1, cols());
        }

        if (values.rows() != 1) {
            throw new NonRowException();
        }

        for (int i = 0; i < cols(); i++) {
            set(row, i, values.get(0, i));
        }

    }

    /**
     * Sets all elements in the specified column.
     *
     * @param col    Column index
     * @param values Elements
     */
    default void setCol(int col, T... values) {

        if (values.length != rows()) {
            throw new DimensionException(values.length, 1, rows(), 1);
        }

        for (int i = 0; i < cols(); i++) {
            set(i, col, values[i]);
        }

    }

    /**
     * Sets all elements in the specified column from a column matrix.
     *
     * @param col    Column index
     * @param values Column matrix of elements
     */
    default void setCol(int col, Matrix<T> values) {

        if (values.rows() != rows()) {
            throw new DimensionException(values, rows(), -1);
        }

        if (values.cols() != 1) {
            throw new NonColException();
        }

        for (int i = 0; i < cols(); i++) {
            set(i, col, values.get(i, 0));
        }

    }

    /**
     * Sets all elements to a single value.
     *
     * @param value Value to set
     */
    void setAll(T value);

    /**
     * Replace an element with the value of a function of itself.
     *
     * @param row    Row index
     * @param col    Column index
     * @param mapper Function for mapping
     */
    void mapElement(int row, int col, GFunction<T, T> mapper);

    /**
     * Replace the value of an element with itself multiplied by a given value.
     *
     * @param row   Row index
     * @param col   Column index
     * @param value Factor to multiply by
     */
    void multiplyElement(int row, int col, T value);

    /**
     * Replace the value of an element with itself divided by a given value.
     *
     * @param row   Row index
     * @param col   Column index
     * @param value Factor to divide by
     */
    void divideElement(int row, int col, T value);

    /**
     * Replace the value of an element with itself plus a given value.
     *
     * @param row   Row index
     * @param col   Column index
     * @param value Value to add
     */
    void addToElement(int row, int col, T value);

    /**
     * Replace the value of an element with itself minus a given value.
     *
     * @param row   Row index
     * @param col   Column index
     * @param value Value to subtract
     */
    void subtractFromElement(int row, int col, T value);

    /**
     * Map all elements of this matrix into a new matrix according to the provided mapping function.
     *
     * @param mapper Mapping function
     *
     * @return Mapped matrix
     */
    Matrix<T> map(EntryMapper<T, T> mapper);

    /**
     * Map all elements of this matrix into a new matrix according to the provided mapping function.
     *
     * @param mapper Mapping function
     *
     * @return Mapped matrix
     */
    Matrix<T> map(GFunction<T, T> mapper);

    /**
     * Map all elements of this matrix onto themselves according to the provided mapping function.
     *
     * @param mapper Mapping function
     */
    default void mapSelf(EntryMapper<T, T> mapper) {
        forEach((r, c, v) -> set(r, c, mapper.map(r, c, v)));
    }

    /**
     * Map all elements of this matrix onto themselves according to the provided mapping function.
     *
     * @param mapper Mapping function
     */
    default void mapSelf(GFunction<T, T> mapper) {
        mapSelf((r, c, v) -> mapper.value(v));
    }

    default <U> void map(Matrix<U> to, EntryMapper<U, T> mapper) {

        if (to.rows() != rows() || to.cols() != cols()) {
            throw new DimensionException(to, this);
        }

        forEach((r, c, v) -> to.set(r, c, mapper.map(r, c, v)));
    }

    default <U> void map(Matrix<U> to, GFunction<U, T> mapper) {
        map(to, (r, c, v) -> mapper.value(v));
    }

    void mapRow(int row, LinearMapper<T> mapper);

    default void mapRow(int row, GFunction<T, T> mapper) {
        mapRow(row, (i, v) -> mapper.value(v));
    }

    void mapCol(int col, LinearMapper<T> mapper);

    default void mapCol(int row, GFunction<T, T> mapper) {
        mapCol(row, (i, v) -> mapper.value(v));
    }

    void mapRowToRow(int source, int dest, LinearMapper<T> mapper);

    default void mapRowToRow(int source, int dest, GFunction<T, T> mapper) {
        mapRowToRow(source, dest, (i, v) -> mapper.value(v));
    }

    void mapColToCol(int source, int dest, LinearMapper<T> mapper);

    default void mapColToCol(int source, int dest, GFunction<T, T> mapper) {
        mapColToCol(source, dest, (i, v) -> mapper.value(v));
    }

    T[] getRowArray(int row);

    T[] getColArray(int col);

    Matrix<T> getRowMatrix(int row);

    Matrix<T> getColMatrix(int col);

    Matrix<T> multiply(Matrix<T> rhs);

    default Matrix<T> times(Matrix<T> rhs) {
        return multiply(rhs);
    }

    Matrix<T> leftMultiply(Matrix<T> lhs);

    Matrix<T> multiply(T rhs);

    default Matrix<T> times(T rhs) {
        return multiply(rhs);
    }

    Matrix<T> leftMultiply(T lhs);

    Matrix<T> elementMultiply(Matrix<T> rhs);

    Matrix<T> leftElementMultiply(Matrix<T> lhs);

    default Matrix<T> divide(Matrix<T> rhs) {
        return multiply(rhs.invert());
    }

    default Matrix<T> div(Matrix<T> rhs) {
        return divide(rhs);
    }

    Matrix<T> elementDivide(Matrix<T> rhs);

    Matrix<T> leftElementDivide(Matrix<T> lhs);

    Matrix<T> divide(T rhs);

    default Matrix<T> div(T rhs) {
        return divide(rhs);
    }

    Matrix<T> leftDivide(T lhs);

    Matrix<T> add(Matrix<T> rhs);

    default Matrix<T> plus(Matrix<T> rhs) {
        return add(rhs);
    }

    Matrix<T> add(T rhs);

    default Matrix<T> plus(T rhs) {
        return add(rhs);
    }

    Matrix<T> subtract(Matrix<T> rhs);

    default Matrix<T> minus(Matrix<T> rhs) {
        return subtract(rhs);
    }

    Matrix<T> subtract(T rhs);

    default Matrix<T> minus(T rhs) {
        return subtract(rhs);
    }

    Matrix<T> copy();

    Matrix<T> getSubMatrix(int[] rows, int[] cols);

    Matrix<T> getSubMatrix(int startRow, int endRow, int startCol, int endCol);

    Matrix<T> appendRows(Matrix<T> rows);

    Matrix<T> appendCols(Matrix<T> cols);

    default void setSubMatrix(int startRow, int startCol, Matrix<T> subMatrix) {

        if (startRow + subMatrix.rows() >= rows() || startCol + subMatrix.cols() >= cols()) {
            throw new SubMatrixException(this, subMatrix, startRow, startCol);
        }

        for (int r = startRow; r < startRow + subMatrix.rows(); r++) {

            for (int c = startCol; c < startCol + subMatrix.cols(); c++) {

                set(r, c, subMatrix.get(r - startRow, c - startCol));

            }

        }

    }

    boolean isSingular();

    T getDeterminant();

    T getTrace();

    Matrix<T> invert();

    Matrix<T> transpose();

    Matrix<T> leftDivide(Matrix<T> rhs);

    QR<T> getQR();

    LU<T> getLU();

    Matrix<T> getRowSums();

    Matrix<T> getColSums();

    default void forEach(EntryConsumer<T> forEach) {

        for (int r = 0; r < rows(); r++) {

            for (int c = 0; c < cols(); c++) {
                forEach.accept(r, c, get(r, c));
            }

        }

    }

    default void forEach(Consumer<? super T> forEach) {
        forEach((r, c, v) -> forEach.accept(v));
    }

    default void forEachRow(Consumer<T[]> forEach) {
        for (int r = 0; r < rows(); r++) {
            forEach.accept(getRowArray(r));
        }
    }

    default void forEachCol(Consumer<T[]> forEach) {
        for (int c = 0; c < cols(); c++) {
            forEach.accept(getColArray(c));
        }
    }

    default Iterator<T> iterator() {

        return new Iterator<>() {

            private int columns = cols();
            private int size = size();
            private int i = 0;

            @Override
            public boolean hasNext() {
                return i < size;
            }

            @Override
            public T next() {
                return get(i / columns, i++ % columns);
            }

        };

    }

    default void checkIndices(int row, int col) throws IndexException {
        if (row >= rows() || col >= cols()) {
            throw new IndexException(row, col, this);
        }
    }

    String toString();

    interface EntryConsumer<T> {

        void accept(int row, int col, T value);

    }

    interface EntryMapper<U, T> {

        U map(int row, int col, T value);

    }

    interface LinearMapper<T> {

        T map(int index, T value);

    }

}
