package jisa.maths;

import jisa.experiment.Function;
import org.apache.commons.math.linear.*;

import java.util.Arrays;
import java.util.Iterator;

public class Matrix implements RealMatrix, Iterable<Double> {

    private final RealMatrix backingMatrix;

    public static Matrix toMatrix(RealMatrix m) {

        if (m instanceof Matrix) {
            return (Matrix) m;
        } else {
            return new Matrix(m);
        }

    }

    public Matrix(int rows, int cols) {
        backingMatrix = MatrixUtils.createRealMatrix(rows, cols);
    }

    public Matrix(int rows, int cols, double... values) {
        this(rows, cols);
        setAllEntries(values);
    }

    public Matrix(RealMatrix matrix) {
        backingMatrix = matrix;
    }

    public Matrix(double[][] data) {
        backingMatrix = MatrixUtils.createRealMatrix(data);
    }

    public Matrix(double[] data) {
        backingMatrix = MatrixUtils.createRowRealMatrix(data);
    }

    public double getSum() {

        double total = 0;

        for (double v : this) {
            total += v;
        }

        return total;

    }

    public Matrix getRowSums() {

        Matrix result = new Matrix(getRowDimension(), 1);
        result.setAllEntries(0.0);
        forEach((r, c, v) -> result.addToEntry(r, 0, v));
        return result;

    }

    public Matrix getColumnSums() {

        Matrix result = new Matrix(1, getColumnDimension());
        result.setAllEntries(0.0);
        forEach((r, c, v) -> result.addToEntry(0, c, v));
        return result;

    }

    public Matrix map(EntryMapper mapper) {

        Matrix result = new Matrix(getRowDimension(), getColumnDimension());
        forEach((r, c, v) -> result.setEntry(r, c, mapper.map(r, c, v)));
        return result;

    }

    public Matrix map(Mapper mapper) {
        return map((r, c, v) -> mapper.map((v)));
    }

    public Matrix elementMultiply(Matrix matrix) {

        if (matrix.getRowDimension() != getRowDimension() || matrix.getColumnDimension() != getColumnDimension()) {
            throw new IllegalArgumentException("Matrices must have the same dimensions");
        }

        Matrix result = new Matrix(getRowDimension(), getColumnDimension());

        forEach((r, c, v) -> result.setEntry(r, c, v * matrix.getEntry(r, c)));

        return result;

    }


    public void setAllEntries(double... values) {

        if (values.length == 1) {
            forEach((r, c, v) -> setEntry(r, c, values[0]));

        } else if (values.length == getSize()) {

            int k = 0;

            for (int i = 0; i < getRowDimension(); i++) {

                for (int j = 0; j < getColumnDimension(); j++) {

                    setEntry(i, j, values[k++]);

                }

            }
        } else {
            throw new IllegalArgumentException("Number of elements does not match!");
        }

    }

    public int getSize() {
        return getRowDimension() * getColumnDimension();
    }

    @Override
    public Matrix createMatrix(int i, int i1) {
        return new Matrix(i, i1);
    }

    @Override
    public Matrix copy() {
        return new Matrix(backingMatrix.copy());
    }

    @Override
    public Matrix add(RealMatrix realMatrix) throws IllegalArgumentException {
        return new Matrix(backingMatrix.add(realMatrix));
    }

    @Override
    public Matrix subtract(RealMatrix realMatrix) throws IllegalArgumentException {
        return new Matrix(backingMatrix.subtract(realMatrix));
    }

    public Matrix add(Number scalar) {
        return scalarAdd(scalar.doubleValue());
    }

    public Matrix subtract(Number scalar) {
        return scalarSubtract(scalar.doubleValue());
    }

    public Matrix multiply(Number scalar) {
        return scalarMultiply(scalar.doubleValue());
    }

    public Matrix divide(Number scalar) {
        return scalarMultiply(1D / scalar.doubleValue());
    }

    @Override
    public Matrix scalarAdd(double v) {
        return new Matrix(backingMatrix.scalarAdd(v));
    }

    public Matrix scalarSubtract(double v) {
        return scalarAdd(-v);
    }

    @Override
    public Matrix scalarMultiply(double v) {
        return new Matrix(backingMatrix.scalarMultiply(v));
    }

    @Override
    public Matrix multiply(RealMatrix realMatrix) throws IllegalArgumentException {
        return new Matrix(backingMatrix.multiply(realMatrix));
    }

    @Override
    public Matrix preMultiply(RealMatrix realMatrix) throws IllegalArgumentException {
        return new Matrix(backingMatrix.preMultiply(realMatrix));
    }

    @Override
    public double[][] getData() {
        return backingMatrix.getData();
    }

    @Override
    public double getNorm() {
        return backingMatrix.getNorm();
    }

    @Override
    public double getFrobeniusNorm() {
        return backingMatrix.getFrobeniusNorm();
    }

    @Override
    public Matrix getSubMatrix(int startRow, int stopRow, int startCol, int stopCol) throws MatrixIndexException {
        return new Matrix(backingMatrix.getSubMatrix(startRow, stopRow, startCol, stopCol));
    }

    @Override
    public Matrix getSubMatrix(int[] ints, int[] ints1) throws MatrixIndexException {
        return new Matrix(backingMatrix.getSubMatrix(ints, ints1));
    }

    @Override
    public void copySubMatrix(int i, int i1, int i2, int i3, double[][] doubles) throws
                                                                                 MatrixIndexException, IllegalArgumentException {
        backingMatrix.copySubMatrix(i, i1, i2, i3, doubles);
    }

    @Override
    public void copySubMatrix(int[] ints, int[] ints1, double[][] doubles) throws
                                                                           MatrixIndexException, IllegalArgumentException {
        backingMatrix.copySubMatrix(ints, ints1, doubles);
    }

    @Override
    public void setSubMatrix(double[][] doubles, int i, int i1) throws MatrixIndexException {
        backingMatrix.setSubMatrix(doubles, i, i1);
    }

    @Override
    public Matrix getRowMatrix(int i) throws MatrixIndexException {
        return new Matrix(backingMatrix.getRowMatrix(i));
    }

    @Override
    public void setRowMatrix(int i, RealMatrix realMatrix) throws MatrixIndexException, InvalidMatrixException {
        backingMatrix.setRowMatrix(i, realMatrix);
    }

    @Override
    public Matrix getColumnMatrix(int i) throws MatrixIndexException {
        return new Matrix(backingMatrix.getColumnMatrix(i));
    }

    @Override
    public void setColumnMatrix(int i, RealMatrix realMatrix) throws MatrixIndexException, InvalidMatrixException {
        backingMatrix.setColumnMatrix(i, realMatrix);
    }

    @Override
    public RealVector getRowVector(int i) throws MatrixIndexException {
        return backingMatrix.getRowVector(i);
    }

    @Override
    public void setRowVector(int i, RealVector realVector) throws MatrixIndexException, InvalidMatrixException {
        backingMatrix.setRowVector(i, realVector);
    }

    @Override
    public RealVector getColumnVector(int i) throws MatrixIndexException {
        return backingMatrix.getColumnVector(i);
    }

    @Override
    public void setColumnVector(int i, RealVector realVector) throws MatrixIndexException, InvalidMatrixException {
        backingMatrix.setColumnVector(i, realVector);
    }

    @Override
    public double[] getRow(int i) throws MatrixIndexException {
        return backingMatrix.getRow(i);
    }

    @Override
    public void setRow(int i, double[] doubles) throws MatrixIndexException, InvalidMatrixException {
        backingMatrix.setRow(i, doubles);
    }

    @Override
    public double[] getColumn(int i) throws MatrixIndexException {
        return backingMatrix.getColumn(i);
    }

    @Override
    public void setColumn(int i, double[] doubles) throws MatrixIndexException, InvalidMatrixException {
        backingMatrix.setColumn(i, doubles);
    }

    @Override
    public double getEntry(int i, int i1) throws MatrixIndexException {
        return backingMatrix.getEntry(i, i1);
    }

    @Override
    public void setEntry(int i, int i1, double v) throws MatrixIndexException {
        backingMatrix.setEntry(i, i1, v);
    }

    @Override
    public void addToEntry(int i, int i1, double v) throws MatrixIndexException {
        backingMatrix.addToEntry(i, i1, v);
    }

    @Override
    public void multiplyEntry(int i, int i1, double v) throws MatrixIndexException {
        backingMatrix.multiplyEntry(i, i1, v);
    }

    @Override
    public Matrix transpose() {
        return new Matrix(backingMatrix.transpose());
    }

    @Override
    public Matrix inverse() throws InvalidMatrixException {
        return new Matrix(backingMatrix.inverse());
    }

    @Override
    public double getDeterminant() {
        return backingMatrix.getDeterminant();
    }

    @Override
    public boolean isSingular() {
        return backingMatrix.isSingular();
    }

    @Override
    public double getTrace() throws NonSquareMatrixException {
        return backingMatrix.getTrace();
    }

    @Override
    public double[] operate(double[] doubles) throws IllegalArgumentException {
        return backingMatrix.operate(doubles);
    }

    @Override
    public RealVector operate(RealVector realVector) throws IllegalArgumentException {
        return backingMatrix.operate(realVector);
    }

    @Override
    public double[] preMultiply(double[] doubles) throws IllegalArgumentException {
        return backingMatrix.preMultiply(doubles);
    }

    @Override
    public RealVector preMultiply(RealVector realVector) throws IllegalArgumentException {
        return backingMatrix.preMultiply(realVector);
    }

    @Override
    public double walkInRowOrder(RealMatrixChangingVisitor realMatrixChangingVisitor) throws MatrixVisitorException {
        return backingMatrix.walkInRowOrder(realMatrixChangingVisitor);
    }

    @Override
    public double walkInRowOrder(RealMatrixPreservingVisitor realMatrixPreservingVisitor) throws
                                                                                          MatrixVisitorException {
        return backingMatrix.walkInRowOrder(realMatrixPreservingVisitor);
    }

    @Override
    public double walkInRowOrder(RealMatrixChangingVisitor realMatrixChangingVisitor, int i, int i1, int i2, int i3) throws
                                                                                                                     MatrixIndexException, MatrixVisitorException {
        return backingMatrix.walkInRowOrder(realMatrixChangingVisitor, i, i1, i2, i3);
    }

    @Override
    public double walkInRowOrder(RealMatrixPreservingVisitor realMatrixPreservingVisitor, int i, int i1, int i2,
                                 int i3) throws
                                         MatrixIndexException, MatrixVisitorException {
        return backingMatrix.walkInRowOrder(realMatrixPreservingVisitor, i, i1, i2, i3);
    }

    @Override
    public double walkInColumnOrder(RealMatrixChangingVisitor realMatrixChangingVisitor) throws
                                                                                         MatrixVisitorException {
        return backingMatrix.walkInColumnOrder(realMatrixChangingVisitor);
    }

    @Override
    public double walkInColumnOrder(RealMatrixPreservingVisitor realMatrixPreservingVisitor) throws
                                                                                             MatrixVisitorException {
        return backingMatrix.walkInColumnOrder(realMatrixPreservingVisitor);
    }

    @Override
    public double walkInColumnOrder(RealMatrixChangingVisitor realMatrixChangingVisitor, int i, int i1, int i2,
                                    int i3) throws
                                            MatrixIndexException, MatrixVisitorException {
        return backingMatrix.walkInColumnOrder(realMatrixChangingVisitor, i, i1, i2, i3);
    }

    @Override
    public double walkInColumnOrder(RealMatrixPreservingVisitor realMatrixPreservingVisitor, int i, int i1, int i2,
                                    int i3) throws
                                            MatrixIndexException, MatrixVisitorException {
        return backingMatrix.walkInColumnOrder(realMatrixPreservingVisitor, i, i1, i2, i3);
    }

    @Override
    public double walkInOptimizedOrder(RealMatrixChangingVisitor realMatrixChangingVisitor) throws
                                                                                            MatrixVisitorException {
        return backingMatrix.walkInOptimizedOrder(realMatrixChangingVisitor);
    }

    @Override
    public double walkInOptimizedOrder(RealMatrixPreservingVisitor realMatrixPreservingVisitor) throws
                                                                                                MatrixVisitorException {
        return backingMatrix.walkInOptimizedOrder(realMatrixPreservingVisitor);
    }

    @Override
    public double walkInOptimizedOrder(RealMatrixChangingVisitor realMatrixChangingVisitor, int i, int i1, int i2,
                                       int i3) throws
                                               MatrixIndexException, MatrixVisitorException {
        return backingMatrix.walkInOptimizedOrder(realMatrixChangingVisitor, i, i1, i2, i3);
    }

    @Override
    public double walkInOptimizedOrder(RealMatrixPreservingVisitor realMatrixPreservingVisitor, int i, int i1,
                                       int i2, int i3) throws
                                                       MatrixIndexException, MatrixVisitorException {
        return backingMatrix.walkInOptimizedOrder(realMatrixPreservingVisitor, i, i1, i2, i3);
    }

    @Override
    public double[] solve(double[] doubles) throws IllegalArgumentException, InvalidMatrixException {
        return backingMatrix.solve(doubles);
    }

    @Override
    public Matrix solve(RealMatrix realMatrix) throws IllegalArgumentException, InvalidMatrixException {
        return new Matrix(backingMatrix.solve(realMatrix));
    }

    @Override
    public boolean isSquare() {
        return backingMatrix.isSquare();
    }

    @Override
    public int getRowDimension() {
        return backingMatrix.getRowDimension();
    }

    @Override
    public int getColumnDimension() {
        return backingMatrix.getColumnDimension();
    }

    public Matrix asColumn() {

        Matrix column = new Matrix(getSize(), 1);

        int i = 0;
        for (double v : this) {
            column.setEntry(i++, 0, v);
        }

        return column;

    }

    public Matrix asRow() {

        Matrix row = new Matrix(1, getSize());

        int i = 0;
        for (double v : this) {
            row.setEntry(0, i++, v);
        }

        return row;

    }

    public Matrix appendRows(Matrix rows) {

        if (rows.getColumnDimension() != getColumnDimension()) {
            throw new IllegalArgumentException("Number of columns does not match.");
        }

        Matrix result = new Matrix(getRowDimension() + rows.getRowDimension(), getColumnDimension());

        int i = 0;

        for (double[] row : getRows()) {
            result.setRow(i++, row);
        }

        for (double[] row : rows.getRows()) {
            result.setRow(i++, row);
        }

        return result;

    }

    public Matrix appendColumns(Matrix columns) {

        if (columns.getRowDimension() != getRowDimension()) {
            throw new IllegalArgumentException("Number of rows does not match.");
        }

        Matrix result = new Matrix(getRowDimension(), getColumnDimension() + columns.getColumnDimension());

        int i = 0;

        for (double[] col : getColumns()) {
            result.setColumn(i++, col);
        }

        for (double[] col : columns.getColumns()) {
            result.setColumn(i++, col);
        }

        return result;

    }

    public QRDecomposition getQRDecomposition() {
        return new QRDecomposition(this);
    }

    public Fit polyFitAgainst(Matrix x, int degree) {
        return Maths.polyFit(x, this, degree);
    }

    public Matrix asSquareDiagonal() {

        int    size   = getSize();
        Matrix result = new Matrix(size, size);

        int i = 0;

        for (double v : this) {
            result.setEntry(i, i++, v);
        }

        return result;

    }

    public Matrix getDiagonals() {

        int    size   = Math.min(getRowDimension(), getColumnDimension());
        Matrix result = new Matrix(size, 1);

        for (int i = 0; i < size; i++) {
            result.setEntry(i, 0, getEntry(i, i));
        }

        return result;

    }

    @Override
    public Iterator<Double> iterator() {

        return new Iterator<Double>() {

            private int columns = getColumnDimension();
            private int size = getSize();
            private int i = 0;

            @Override
            public boolean hasNext() {
                return i < size;
            }

            @Override
            public Double next() {
                return getEntry(i / columns, i++ % columns);
            }

        };

    }

    public void forEach(EntryConsumer consumer) {

        for (int i = 0; i < getRowDimension(); i++) {

            for (int j = 0; j < getColumnDimension(); j++) {

                consumer.accept(i, j, getEntry(i, j));

            }

        }

    }

    public double[] to1DArray() {

        double[] values = new double[getSize()];

        int i = 0;
        for (double v : this) {
            values[i++] = v;
        }

        return values;

    }

    public double getMinElement() {

        double min = Double.POSITIVE_INFINITY;

        for (double v : this) {
            min = Math.min(min, v);
        }

        return min;

    }

    public double getMaxElement() {

        double max = Double.NEGATIVE_INFINITY;

        for (double v : this) {
            max = Math.max(max, v);
        }

        return max;

    }

    public double getMean() {

        if (getSize() == 0) {
            return 0;
        }

        double total = 0;

        for (double v : this) {
            total += v;
        }

        return total / getSize();

    }

    public interface EntryConsumer {

        void accept(int row, int col, double value);

    }

    public interface EntryMapper {

        double map(int row, int col, double value);

    }

    public interface Mapper {

        double map(double value);

    }

    public Iterable<double[]> getRows() {

        return () -> new Iterator<double[]>() {

            private int i = 0;
            private int rows = getRowDimension();

            @Override
            public boolean hasNext() {
                return i < rows;
            }

            @Override
            public double[] next() {
                return getRow(i++);
            }
        };

    }

    public Iterable<double[]> getColumns() {

        return () -> new Iterator<double[]>() {

            private int i = 0;
            private int cols = getColumnDimension();

            @Override
            public boolean hasNext() {
                return i < cols;
            }

            @Override
            public double[] next() {
                return getColumn(i++);
            }
        };

    }

    public class QRDecomposition implements org.apache.commons.math.linear.QRDecomposition {

        private final QRDecompositionImpl backing;

        public QRDecomposition(Matrix matrix) {
            this.backing = new QRDecompositionImpl(matrix);
        }

        @Override
        public Matrix getR() {
            return new Matrix(backing.getR());
        }

        @Override
        public Matrix getQ() {
            return new Matrix(backing.getQ());
        }

        @Override
        public Matrix getQT() {
            return new Matrix(backing.getQT());
        }

        @Override
        public Matrix getH() {
            return new Matrix(backing.getH());
        }

        @Override
        public DecompositionSolver getSolver() {
            return backing.getSolver();
        }

    }

    public static class Rot2D extends Matrix {


        public Rot2D(double theta) {

            super(
                2, 2,
                +Math.cos(theta), -Math.sin(theta),
                +Math.sin(theta), +Math.cos(theta)
            );

        }

    }

    public static class Identity extends Matrix {

        public Identity(int rows, int columns) {

            super(rows, columns);
            setAllEntries(0.0);

            for (int i = 0; i < Math.min(rows, columns); i++) {

                setEntry(i, i, 1.0);

            }

        }

    }


}


