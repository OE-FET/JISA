package JISA.Maths;

import JISA.Util;
import org.apache.commons.math.linear.MatrixUtils;
import org.apache.commons.math.linear.RealMatrix;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;

public class Matrix implements Iterable<Double> {

    private       double[][] data;
    private final int        rows;
    private final int        cols;

    /**
     * Creates an n x m matrix.
     *
     * @param rows Number of rows (n)
     * @param cols Number of columns (m)
     */
    public Matrix(int rows, int cols) {
        data      = new double[rows][cols];
        this.rows = rows;
        this.cols = cols;

        for (int i = 0; i < rows; i++) {

            for (int j = 0; j < cols; j++) {
                data[i][j] = 0.0;
            }

        }

    }

    public Matrix(double[][] data) {
        this.data = data;
        rows      = data.length;
        cols      = data[0].length;
    }

    public Matrix(RealMatrix matrix) {
        this(matrix.getData());
    }

    public RealMatrix toRealMatrix() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(rows(), columns());
        forEach(matrix::setEntry);
        return matrix;
    }

    public Matrix asColumn() {

        if (columns() == 1) {
            return this;
        }

        Matrix result = new Matrix(size(), 1);

        int i = 0;
        for (double d : this) {
            result.set(i, 0, d);
            i++;
        }

        return result;

    }

    public Matrix inverse() {
        return new Matrix(toRealMatrix().inverse());
    }

    public Matrix subMatrix(int startRow, int startCol, int n, int m) {

        Matrix sub = new Matrix(n, m);

        sub.forEach((i, j, v) -> sub.set(i, j, get(i + startRow, j + startCol)));

        return sub;

    }

    private void checkElement(int row, int col) {

        if (!Util.isBetween(row, 0, rows - 1) || !Util.isBetween(col, 0, cols - 1)) {
            throw new IndexOutOfBoundsException(String.format("Matrix does not contain element (%d, %d) %dx%d", row, col, rows, cols));
        }

    }

    /**
     * Returns the value of the element with the specified indices.
     *
     * @param row Row index
     * @param col Columns index
     *
     * @return Element value
     *
     * @throws IndexOutOfBoundsException If row/column does not exist
     */
    public double get(int row, int col) {

        checkElement(row, col);
        return data[row][col];

    }

    public double[][] getData() {
        return data.clone();
    }

    public double[] toArray() {

        double[] array = new double[size()];

        int i = 0;
        for (double value : this) {
            array[i++] = value;
        }

        return array;

    }

    /**
     * Sets the value of the element with the specified indices.
     *
     * @param row   Row index
     * @param col   Column index
     * @param value Element value
     *
     * @throws IndexOutOfBoundsException If row/column does not exist
     */
    public void set(int row, int col, Number value) {

        checkElement(row, col);
        data[row][col] = value.doubleValue();

    }

    public void setAll(Number... values) {

        if (values.length != (rows() * columns())) {
            throw new IllegalArgumentException("Number of elements does not match!");
        }

        int k = 0;
        for (int i = 0; i < rows(); i++) {
            for (int j = 0; j < columns(); j++) {
                set(i, j, values[k++]);
            }
        }

    }

    /**
     * Returns the number of columns in the matrix.
     *
     * @return Number of columns
     */
    public int columns() {
        return cols;
    }


    /**
     * Returns the number of rows in the matrix.
     *
     * @return Number of rows
     */
    public int rows() {
        return rows;
    }

    public int size() {
        return rows * cols;
    }

    /**
     * Adds another matrix to this one, returning the result as another Matrix object.
     *
     * @param toAdd Matrix to add
     *
     * @return Result of addition
     *
     * @throws IllegalArgumentException If matrix dimensions do not match
     */
    public Matrix add(Matrix toAdd) {

        if (columns() == toAdd.columns() && rows() == toAdd.rows()) {

            Matrix added = new Matrix(rows(), columns());

            forEach((i, j, v) -> added.set(i, j, v + toAdd.get(i, j)));

            return added;

        } else {

            throw new IllegalArgumentException("Matrix dimensions must match!");

        }

    }

    /**
     * Subtracts another matrix from this one, returning the result as another Matrix object.
     *
     * @param toSub Matrix to subtract
     *
     * @return Result of subtraction
     *
     * @throws IllegalArgumentException If matrix dimensions do not match
     */
    public Matrix subtract(Matrix toSub) {

        if (columns() == toSub.columns() && rows() == toSub.rows()) {

            Matrix added = new Matrix(rows(), columns());

            forEach((i, j, v) -> added.set(i, j, v - toSub.get(i, j)));

            return added;

        } else {

            throw new IllegalArgumentException("Matrix dimensions must match!");

        }

    }

    public Matrix add(Number scalar) {

        Matrix result = new Matrix(rows(), columns());

        forEach((i, j, v) -> result.set(i, j, v + scalar.doubleValue()));

        return result;

    }

    public Matrix subtract(Number scalar) {

        Matrix result = new Matrix(rows(), columns());

        forEach((i, j, v) -> result.set(i, j, v - scalar.doubleValue()));

        return result;

    }

    public double maxElement() {

        AtomicReference<Double> max = new AtomicReference<>(Double.NEGATIVE_INFINITY);
        forEach(v -> max.set(Math.max(v, max.get())));
        return max.get();

    }

    public double minElement() {

        AtomicReference<Double> min = new AtomicReference<>(Double.POSITIVE_INFINITY);
        forEach(v -> min.set(Math.min(v, min.get())));
        return min.get();

    }

    /**
     * Multiplies this matrix with another, returning the result as another Matrix object.
     *
     * @param toMult Matrix to multiply with
     *
     * @return Result of multiplication
     *
     * @throws IllegalArgumentException If matrix inner dimensions do not match
     */
    public Matrix multiply(Matrix toMult) {

        if (columns() != toMult.rows()) {
            throw new IllegalArgumentException(String.format("Matrix inner dimensions must match! %dx%d X %dx%d", rows(), columns(), toMult.rows(), toMult.columns()));
        }

        Matrix result = new Matrix(rows(), toMult.columns());

        result.forEach((i, j, v) -> {

            double total = 0;

            for (int k = 0; k < columns(); k++) {

                total += get(i, k) * toMult.get(k, j);

            }

            result.set(i, j, total);

        });

        return result;

    }

    public Matrix multiply(Number scalar) {

        final double scale  = scalar.doubleValue();
        final Matrix result = new Matrix(rows(), columns());

        forEach((i, j, v) -> result.set(i, j, v * scale));

        return result;

    }

    public Matrix divide(Number scalar) {

        final double scale  = scalar.doubleValue();
        final Matrix result = new Matrix(rows(), columns());

        forEach((i, j, v) -> result.set(i, j, v / scale));

        return result;

    }

    public Matrix transpose() {

        Matrix transposed = new Matrix(columns(), rows());

        forEach((i, j, v) -> transposed.set(j, i, v));

        return transposed;

    }

    public Matrix getRow(int i) {

        checkElement(i, 0);

        Matrix result = new Matrix(1, columns());

        result.forEach((n, j, v) -> result.set(n, j, get(i, j)));

        return result;

    }

    public double[] getRowArray(int i) {

        double[] row = new double[columns()];

        for (int j = 0; j < columns(); j++) {
            row[j] = get(i, j);
        }

        return row;

    }

    public Matrix getColumn(int j) {

        checkElement(0, j);

        Matrix result = new Matrix(rows(), 1);

        result.forEach((i, n, v) -> result.set(i, j, get(i, j)));

        return result;

    }

    public Matrix appendColumns(Matrix columns) {

        Matrix result = new Matrix(rows(), columns() + columns.columns());

        forEach(result::set);
        columns.forEach((i, j, v) -> result.set(i, j + columns(), v));

        return result;

    }

    public Matrix appendRows(Matrix rows) {

        Matrix result = new Matrix(rows() + rows.rows(), columns());

        forEach(result::set);
        rows.forEach((i, j, v) -> result.set(i + rows(), j, v));

        return result;

    }

    public boolean equals(Object compare) {

        if (compare instanceof Matrix) {

            Matrix mat = (Matrix) compare;

            for (int i = 0; i < rows(); i++) {

                for (int j = 0; j < columns(); j++) {

                    if (get(i, j) != mat.get(i, j)) {

                        return false;

                    }

                }

            }

            return true;

        } else {

            return false;

        }

    }

    public void forEach(ElementConsumer forEach) {

        for (int i = 0; i < rows(); i++) {

            for (int j = 0; j < columns(); j++) {

                forEach.accept(i, j, get(i, j));

            }

        }

    }

    public Matrix operate(ElementOperator forEach) {

        Matrix result = new Matrix(rows(), columns());

        forEach((i, j, v) -> result.set(i, j, forEach.operate(i, j, v)));

        return result;

    }

    public Matrix operate(ElementValueOperator forEach) {

        Matrix result = new Matrix(rows(), columns());

        forEach((i, j, v) -> result.set(i, j, forEach.operate(v)));

        return result;

    }

    @Override
    public Iterator<Double> iterator() {

        return new Iterator<Double>() {

            private int i = 0;
            private int j = 0;
            private boolean more = rows() > 0 && columns() > 0;

            @Override
            public boolean hasNext() {
                return more;
            }

            @Override
            public Double next() {

                double value = get(i, j);

                j++;

                if (j >= columns()) {
                    j = 0;
                    i++;
                }

                if (i >= rows()) {
                    more = false;
                }

                return value;

            }

        };

    }

    public interface ElementConsumer {

        void accept(int i, int j, double v);

    }

    public interface ElementOperator {

        double operate(int i, int j, double v);

    }

    public interface ElementValueOperator {

        double operate(double v);

    }

    public static class Rot2D extends Matrix {


        public Rot2D(double theta) {

            super(2, 2);

            set(0, 0, Math.cos(theta));
            set(0, 1, -Math.sin(theta));
            set(1, 0, Math.sin(theta));
            set(1, 1, Math.cos(theta));

        }
    }

}
