package jisa.maths.matrices;

import jisa.maths.functions.GFunction;
import jisa.maths.matrices.exceptions.*;

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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

    public static boolean rowMatrixMatch(Matrix a, Matrix b) {
        return (a.cols() == b.cols() && b.rows() == 1);
    }

    public static boolean colMatrixMatch(Matrix a, Matrix b) {
        return (a.rows() == b.rows() && b.cols() == 1);
    }

    public static boolean isScalar(Matrix a) {
        return a.rows() == 1 && a.cols() == 1;
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

    /**
     * Returns the diagonal values of the square matrix as a column matrix.
     *
     * @return Diagonal values
     */
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

    /**
     * Map all elements onto another matrix of a different type, U.
     *
     * @param to     Matrix to map onto
     * @param mapper Function that defines mapping
     * @param <U>    Type to map to
     */
    default <U> void map(Matrix<U> to, EntryMapper<U, T> mapper) {

        if (to.rows() != rows() || to.cols() != cols()) {
            throw new DimensionException(to, this);
        }

        forEach((r, c, v) -> to.set(r, c, mapper.map(r, c, v)));
    }

    /**
     * Map all elements onto another matrix of a different type, U.
     *
     * @param to     Matrix to map onto
     * @param mapper Function that defines mapping
     * @param <U>    Type to map to
     */
    default <U> void map(Matrix<U> to, GFunction<U, T> mapper) {
        map(to, (r, c, v) -> mapper.value(v));
    }

    /**
     * Maps the values of a specified row onto themselves.
     *
     * @param row    Row index to map
     * @param mapper Function the defines mapping
     */
    void mapRow(int row, LinearMapper<T> mapper);

    /**
     * Maps the values of a specified row onto themselves.
     *
     * @param row    Row index to map
     * @param mapper Function the defines mapping
     */
    default void mapRow(int row, GFunction<T, T> mapper) {
        mapRow(row, (i, v) -> mapper.value(v));
    }

    /**
     * Maps the values of a specified column onto themselves.
     *
     * @param col    Column index to map
     * @param mapper Function the defines mapping
     */
    void mapCol(int col, LinearMapper<T> mapper);

    /**
     * Maps the values of a specified column onto themselves.
     *
     * @param col    Column index to map
     * @param mapper Function the defines mapping
     */
    default void mapCol(int col, GFunction<T, T> mapper) {
        mapCol(col, (i, v) -> mapper.value(v));
    }

    /**
     * Maps one row onto another.
     *
     * @param source Source row index
     * @param dest   Destination row index
     * @param mapper Function that defines mapping
     */
    void mapRowToRow(int source, int dest, LinearMapper<T> mapper);

    /**
     * Maps one row onto another.
     *
     * @param source Source row index
     * @param dest   Destination row index
     * @param mapper Function that defines mapping
     */
    default void mapRowToRow(int source, int dest, GFunction<T, T> mapper) {
        mapRowToRow(source, dest, (i, v) -> mapper.value(v));
    }

    /**
     * Maps one column onto another.
     *
     * @param source Source column index
     * @param dest   Destination column index
     * @param mapper Function that defines mapping
     */
    void mapColToCol(int source, int dest, LinearMapper<T> mapper);

    /**
     * Maps one column onto another.
     *
     * @param source Source column index
     * @param dest   Destination column index
     * @param mapper Function that defines mapping
     */
    default void mapColToCol(int source, int dest, GFunction<T, T> mapper) {
        mapColToCol(source, dest, (i, v) -> mapper.value(v));
    }

    default Stream<T> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    /**
     * Returns the values in a single row as an array.
     *
     * @param row Row index
     *
     * @return Array of values
     */
    T[] getRowArray(int row);

    /**
     * Returns the values in a single column as an array.
     *
     * @param col Column index
     *
     * @return Array of values
     */
    T[] getColArray(int col);

    /**
     * Returns a single row as a row matrix.
     *
     * @param row Row index
     *
     * @return Row matrix
     */
    Matrix<T> getRowMatrix(int row);

    /**
     * Returns a single column as a column matrix.
     *
     * @param col Column index
     *
     * @return Column matrix
     */
    Matrix<T> getColMatrix(int col);

    /**
     * Multiplies this matrix by another (this * other).
     *
     * @param rhs Right-hand side of multiplication
     *
     * @return Result of multiplication
     */
    Matrix<T> multiply(Matrix<T> rhs);

    /**
     * Multiplies this matrix by another (this * other).
     *
     * @param rhs Matrix to multiply by (right-hand side)
     *
     * @return Result of multiplication
     */
    default Matrix<T> times(Matrix<T> rhs) {
        return multiply(rhs);
    }

    /**
     * Multiplies another matrix by this matrix (other * this).
     *
     * @param lhs Matrix to multiply (left-hand side)
     *
     * @return Result of multiplication
     */
    Matrix<T> leftMultiply(Matrix<T> lhs);

    /**
     * Multiplies each element in this matrix by a scalar value.
     *
     * @param rhs Scalar value.
     *
     * @return Result of multiplication
     */
    Matrix<T> multiply(T rhs);

    /**
     * Multiplies each element in this matrix by a scalar value. (Kotlin operator overload)
     *
     * @param rhs Scalar value.
     *
     * @return Result of multiplication
     */
    default Matrix<T> times(T rhs) {
        return multiply(rhs);
    }

    /**
     * Pre-multiplies each element in this matrix by a scalar value.
     *
     * @param lhs Scalar value.
     *
     * @return Result of multiplication
     */
    Matrix<T> leftMultiply(T lhs);

    /**
     * Computes the element-wise product of this matrix with another (this .* other).
     *
     * @param rhs Matrix to multiply by (right-hand side)
     *
     * @return Result of multiplication
     */
    Matrix<T> elementMultiply(Matrix<T> rhs);

    /**
     * Computes the element-wise product of another matrix with this matrix (other * this).
     *
     * @param lhs Matrix to multiply (left-hand side)
     *
     * @return Result of multiplication
     */
    Matrix<T> leftElementMultiply(Matrix<T> lhs);

    /**
     * Multiplies this matrix by the inverse of another.
     *
     * @param rhs Matrix to "divide" by (left-hand side)
     *
     * @return Result of "division"
     */
    default Matrix<T> divide(Matrix<T> rhs) {
        return multiply(rhs.invert());
    }

    /**
     * Multiplies this matrix by the inverse of another.
     *
     * @param rhs Matrix to "divide" by (left-hand side)
     *
     * @return Result of "division"
     */
    default Matrix<T> div(Matrix<T> rhs) {
        return divide(rhs);
    }

    /**
     * Multiplies the supplied matrix by the inverse of this matrix.
     *
     * @param rhs Matrix to be "divided"
     *
     * @return Result of division
     */
    Matrix<T> leftDivide(Matrix<T> rhs);

    /**
     * Performs element-wise division of this matrix by the specified matrix (divides each element in this matrix by its
     * corresponding element in the other).
     *
     * @param rhs Matrix to divide by
     *
     * @return Result of division
     */
    Matrix<T> elementDivide(Matrix<T> rhs);

    /**
     * Performs element-wise division of the specified matrix by this matrix (divides each element in the supplied matrix
     * by its corresponding element in this matrix).
     *
     * @param lhs Matrix to divide
     *
     * @return Result of division
     */
    Matrix<T> leftElementDivide(Matrix<T> lhs);

    /**
     * Divides all elements in this matrix by the given value.
     *
     * @param rhs Value to divide by
     *
     * @return Result of division
     */
    Matrix<T> divide(T rhs);

    /**
     * Divides all elements in this matrix by the given value.
     *
     * @param rhs Value to divide by
     *
     * @return Result of division
     */
    default Matrix<T> div(T rhs) {
        return divide(rhs);
    }

    /**
     * Divides the given value by each element in the matrix returning a matrix of the results.
     *
     * @param lhs Value to be divided
     *
     * @return Result of division
     */
    Matrix<T> leftDivide(T lhs);

    /**
     * Adds the given matrix to this one. (Adds each element with its corresponding element).
     *
     * @param rhs Matrix to add
     *
     * @return Result of addition
     */
    Matrix<T> add(Matrix<T> rhs);

    /**
     * Adds the given matrix to this one. (Adds each element with its corresponding element).
     *
     * @param rhs Matrix to add
     *
     * @return Result of addition
     */
    default Matrix<T> plus(Matrix<T> rhs) {
        return add(rhs);
    }

    /**
     * Add the given value to each element in this matrix, returning the result.
     *
     * @param rhs Value to add
     *
     * @return Result of addition
     */
    Matrix<T> add(T rhs);

    /**
     * Add the given value to each element in this matrix, returning the result.
     *
     * @param rhs Value to add
     *
     * @return Result of addition
     */
    default Matrix<T> plus(T rhs) {
        return add(rhs);
    }

    /**
     * Subtracts the given matrix from this one, returning the result.
     *
     * @param rhs Matrix to subtract
     *
     * @return Result of subtraction
     */
    Matrix<T> subtract(Matrix<T> rhs);

    /**
     * Subtracts the given matrix from this one, returning the result.
     *
     * @param rhs Matrix to subtract
     *
     * @return Result of subtraction
     */
    default Matrix<T> minus(Matrix<T> rhs) {
        return subtract(rhs);
    }

    /**
     * Subtracts the given value from each element in this matrix, returning the result.
     *
     * @param rhs Value to subtract
     *
     * @return Result of subtraction
     */
    Matrix<T> subtract(T rhs);

    /**
     * Subtracts the given value from each element in this matrix, returning the result.
     *
     * @param rhs Value to subtract
     *
     * @return Result of subtraction
     */
    default Matrix<T> minus(T rhs) {
        return subtract(rhs);
    }

    /**
     * Returns a deep copy of the matrix.
     *
     * @return Copy of matrix
     */
    Matrix<T> copy();

    /**
     * Returns a sub-matrix containing only the specified rows and columns from this matrix.
     *
     * @param rows Array of row indices
     * @param cols Array of column indices
     *
     * @return Sub-matrix
     */
    Matrix<T> getSubMatrix(int[] rows, int[] cols);

    /**
     * Returns a sub-matrix containing only the rows and columns specified in the given ranges (inclusive).
     *
     * @param startRow Start of row range
     * @param endRow   End of row range
     * @param startCol Start of column range
     * @param endCol   End of column range
     *
     * @return Sub-matrix
     */
    Matrix<T> getSubMatrix(int startRow, int endRow, int startCol, int endCol);

    /**
     * Creates a new matrix consisting of the given rows appended to the bottom of this matrix.
     *
     * @param rows Rows to append (must match column dimension)
     *
     * @return Combined matrix
     */
    Matrix<T> appendRows(Matrix<T> rows);

    /**
     * Creates a new matrix consisting of the given columns appended to the right of this matrix.
     *
     * @param cols Columns to append (must match row dimension)
     *
     * @return Combined matrix
     */
    Matrix<T> appendCols(Matrix<T> cols);

    /**
     * Sets the value of elements within the specified sub-matrix.
     *
     * @param startRow  Starting row index of sub-matrix within matrix
     * @param startCol  Starting column index of sub-matrix within matrix
     * @param subMatrix Sub-matrix to set
     */
    default void setSubMatrix(int startRow, int startCol, Matrix<T> subMatrix) {

        if (startRow + subMatrix.rows() > rows() || startCol + subMatrix.cols() > cols()) {
            throw new SubMatrixException(this, subMatrix, startRow, startCol);
        }

        for (int r = startRow; r < startRow + subMatrix.rows(); r++) {

            for (int c = startCol; c < startCol + subMatrix.cols(); c++) {

                set(r, c, subMatrix.get(r - startRow, c - startCol));

            }

        }

    }

    /**
     * Returns whether the matrix is singular (has no inverse).
     *
     * @return Singular?
     */
    boolean isSingular();

    /**
     * Returns whether this matrix is a row matrix (rows() == 1).
     *
     * @return Row matrix?
     */
    default boolean isRow() {
        return rows() == 1;
    }

    /**
     * Returns whether this matrix is a column matrix (cols() == 1).
     *
     * @return Column matrix?
     */
    default boolean isCol() {
        return cols() == 1;
    }

    /**
     * Returns the determinant of the matrix (only for square matrices).
     *
     * @return Determinant value
     */
    T getDeterminant();

    /**
     * Returns the trace of the matrix (sum of diagonal elements).
     *
     * @return Trace value
     */
    T getTrace();

    /**
     * Inverts the matrix and returns the result (only for square matrices).
     *
     * @return Inverted matrix
     */
    Matrix<T> invert();

    /**
     * Transposes the matrix and returns the result.
     *
     * @return Transposed matrix
     */
    Matrix<T> transpose();

    /**
     * Reshapes the elements in the matrix into a matrix of different dimensions (but same total number of elements),
     * returning the result.
     * <p>
     * rows * cols must equal this.size().
     *
     * @param rows New number of rows
     * @param cols New number of columns
     *
     * @return Reshaped matrix
     */
    Matrix<T> reshape(int rows, int cols);

    /**
     * Computes and returns the QR decomposition of this matrix.
     *
     * @return QR Decomposition
     */
    QR<T> getQR();

    /**
     * Computes and returns the LU decomposition of this matrix (only for square matrices).
     *
     * @return LU Decomposition
     */
    LU<T> getLU();

    /**
     * Returns a column matrix of the sums of each row in this matrix.
     *
     * @return Column matrix of sums
     */
    Matrix<T> getRowSums();

    /**
     * Returns a row matrix of the sums of each column in this matrix.
     *
     * @return Row matrix of sums
     */
    Matrix<T> getColSums();

    /**
     * Loops over each element, going along each row in turn.
     *
     * @param forEach Action to perform for each value.
     */
    default void forEach(EntryConsumer<T> forEach) {

        for (int r = 0; r < rows(); r++) {

            for (int c = 0; c < cols(); c++) {
                forEach.accept(r, c, get(r, c));
            }

        }

    }

    /**
     * Loops over each element, going along each row in turn.
     *
     * @param forEach Action to perform for each value.
     */
    default void forEach(Consumer<? super T> forEach) {
        forEach((r, c, v) -> forEach.accept(v));
    }

    /**
     * Loops over all rows, providing each row as an array.
     *
     * @param forEach Action to perform for each row.
     */
    default void forEachRow(Consumer<T[]> forEach) {
        for (int r = 0; r < rows(); r++) {
            forEach.accept(getRowArray(r));
        }
    }

    /**
     * Loops over all columns, providing each column as an array.
     *
     * @param forEach Action to perform for each column.
     */
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

    /**
     * Checks if the provided row and column indices are within bounds for this matrix. Throws an IndexException if not.
     *
     * @param row Row index
     * @param col Column index
     *
     * @throws IndexException If indices are out of bounds.
     */
    default void checkIndices(int row, int col) throws IndexException {
        if (row >= rows() || col >= cols()) {
            throw new IndexException(row, col, this);
        }
    }

    /**
     * Returns a textual representation of this matrix.
     *
     * @return String representation
     */
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
