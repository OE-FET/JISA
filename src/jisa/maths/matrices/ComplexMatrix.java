package jisa.maths.matrices;

import jisa.Util;
import jisa.maths.functions.GFunction;
import jisa.maths.matrices.exceptions.DimensionException;
import jisa.maths.matrices.exceptions.NonSquareException;
import org.apache.commons.math.complex.Complex;
import org.apache.commons.math.complex.ComplexField;
import org.apache.commons.math.linear.FieldLUDecompositionImpl;
import org.apache.commons.math.linear.FieldMatrix;
import org.apache.commons.math.linear.MatrixUtils;

public class ComplexMatrix implements Matrix<Complex> {

    private final FieldMatrix<Complex> backing;

    public ComplexMatrix(int rows, int cols) {
        backing = MatrixUtils.createFieldMatrix(ComplexField.getInstance(), rows, cols);
        setAll(Complex.ZERO);
    }

    public ComplexMatrix(int rows, int cols, Complex... values) {
        this(rows, cols);
        setAll(values);
    }

    public ComplexMatrix(Complex[][] data) {
        backing = MatrixUtils.createFieldMatrix(data);
    }

    public ComplexMatrix(FieldMatrix<Complex> matrix) {
        this(matrix.getData());
    }

    public ComplexMatrix(Matrix<Complex> matrix) {
        this(matrix.getData());
    }

    public String toString() {

        String[] rows = new String[rows()];

        for (int r = 0; r < rows(); r++) {

            String[] cols = new String[cols()];

            for (int c = 0; c < cols(); c++) {
                cols[c] = String.format("%s + i%s", get(r,c).getReal(), get(r,c).getImaginary());
            }

            rows[r] = String.join(", ", cols);

        }

        return "[ " + String.join("; ", rows) + " ]";

    }

    public FieldMatrix<Complex> fieldMatrix() {
        return backing;
    }

    @Override
    public int rows() {
        return backing.getRowDimension();
    }

    @Override
    public int cols() {
        return backing.getColumnDimension();
    }

    @Override
    public Complex get(int row, int col) {
        checkIndices(row, col);
        return backing.getEntry(row, col);
    }

    @Override
    public Complex[][] getData() {
        return backing.getData();
    }

    @Override
    public Complex[] getFlatData() {

        Complex[] data = new Complex[size()];

        int i = 0;
        for (Complex v : this) {
            data[i++] = v;
        }

        return data;

    }

    @Override
    public ComplexMatrix getDiagonal() {

        if (!isSquare()) {
            throw new NonSquareException();
        }

        ComplexMatrix diag = new ComplexMatrix(rows(), 1);

        for (int i = 0; i < rows(); i++) {
            diag.set(i, 0, get(i,i));
        }

        return diag;

    }

    @Override
    public void set(int row, int col, Complex value) {
        checkIndices(row, col);
        backing.setEntry(row, col, value);
    }

    @Override
    public void setAll(Complex... values) {

        if (values.length != size()) {
            throw new DimensionException(values.length, size());
        }

        int i = 0;
        for (int r = 0; r < rows(); r++) {

            for (int c = 0; c < cols(); c++) {
                set(r, c, values[i++]);
            }

        }

    }

    @Override
    public void setDiagonal(Complex... values) {

        if (!isSquare()) {
            throw new NonSquareException();
        }

        if (values.length != rows()) {
            throw new DimensionException(values.length, rows());
        }

        for (int i = 0; i < rows(); i++) {
            set(i, i, values[i]);
        }

    }

    @Override
    public void setDiagonal(Complex value) {

        if (!isSquare()) {
            throw new NonSquareException();
        }

        for (int i = 0; i < rows(); i++) {
            set(i, i, value);
        }

    }

    @Override
    public void setAll(Complex value) {
        forEach((r, c, v) -> set(r, c, value));
    }

    @Override
    public void mapElement(int row, int col, GFunction<Complex, Complex> mapper) {
        set(row, col, mapper.value(get(row, col)));
    }

    @Override
    public void multiplyElement(int row, int col, Complex value) {
        set(row, col, get(row, col).multiply(value));
    }

    @Override
    public void divideElement(int row, int col, Complex value) {
        set(row, col, get(row, col).divide(value));
    }

    @Override
    public void addToElement(int row, int col, Complex value) {
        set(row, col, get(row, col).add(value));
    }

    @Override
    public void subtractFromElement(int row, int col, Complex value) {
        set(row, col, get(row, col).subtract(value));
    }

    @Override
    public void mapRow(int row, LinearMapper<Complex> mapper) {

        for (int i = 0; i < cols(); i++) {
            set(row, i, mapper.map(i, get(row, i)));
        }

    }

    @Override
    public void mapCol(int col, LinearMapper<Complex> mapper) {

        for (int i = 0; i < rows(); i++) {
            set(i, col, mapper.map(i, get(i, col)));
        }

    }

    @Override
    public void mapRowToRow(int source, int dest, LinearMapper<Complex> mapper) {
        setRow(dest, getRowMatrix(source).map((r, c, v) -> mapper.map(c, v)));
    }

    @Override
    public void mapColToCol(int source, int dest, LinearMapper<Complex> mapper) {
        setCol(dest, getColMatrix(source).map((r, c, v) -> mapper.map(r, v)));
    }

    @Override
    public Complex[] getRowArray(int row) {
        return backing.getRow(row);
    }

    @Override
    public Complex[] getColArray(int col) {
        return backing.getColumn(col);
    }

    @Override
    public ComplexMatrix getRowMatrix(int row) {
        return getSubMatrix(row, row, 0, cols() - 1);
    }

    @Override
    public ComplexMatrix getColMatrix(int col) {
        return getSubMatrix(0, rows() - 1, col, col);
    }

    @Override
    public ComplexMatrix multiply(Matrix<Complex> rhs) {

        if (!Matrix.canMultiply(this, rhs)) {
            throw new DimensionException(rhs, cols(), -1);
        }

        ComplexMatrix result = new ComplexMatrix(rows(), rhs.cols());

        for (int r = 0; r < rows(); r++) {

            for (int c = 0; c < rhs.cols(); c++) {

                for (int k = 0; k < cols(); k++) {
                    result.addToElement(r, c, get(r, k).multiply(rhs.get(k, c)));
                }

            }

        }

        return result;

    }

    @Override
    public ComplexMatrix leftMultiply(Matrix<Complex> lhs) {

        if (lhs instanceof ComplexMatrix) {
            return ((ComplexMatrix) lhs).multiply(this);
        } else {
            return new ComplexMatrix(lhs.multiply(this));
        }

    }

    @Override
    public ComplexMatrix multiply(Complex rhs) {
        return map(v -> v.multiply(rhs));
    }

    @Override
    public ComplexMatrix leftMultiply(Complex lhs) {
        return map(v -> lhs.multiply(v));
    }

    @Override
    public ComplexMatrix elementMultiply(Matrix<Complex> rhs) {
        return map((r, c, v) -> v.multiply(rhs.get(r, c)));
    }

    @Override
    public ComplexMatrix leftElementMultiply(Matrix<Complex> lhs) {
        return map((r, c, v) -> lhs.get(r, c).multiply(v));
    }

    @Override
    public ComplexMatrix elementDivide(Matrix<Complex> rhs) {
        return map((r, c, v) -> v.divide(rhs.get(r, c)));
    }

    @Override
    public ComplexMatrix leftElementDivide(Matrix<Complex> lhs) {
        return map((r, c, v) -> lhs.get(r, c).divide(v));
    }

    @Override
    public ComplexMatrix divide(Complex rhs) {
        return map(v -> v.divide(rhs));
    }

    @Override
    public ComplexMatrix leftDivide(Complex lhs) {
        return map(v -> lhs.divide(v));
    }

    @Override
    public ComplexMatrix add(Matrix<Complex> rhs) {
        return map((r, c, v) -> v.add(rhs.get(r, c)));
    }

    @Override
    public ComplexMatrix add(Complex rhs) {
        return map(v -> v.add(rhs));
    }

    @Override
    public ComplexMatrix subtract(Matrix<Complex> rhs) {
        return map((r, c, v) -> v.subtract(rhs.get(r, c)));
    }

    @Override
    public ComplexMatrix subtract(Complex rhs) {
        return map(v -> v.subtract(rhs));
    }

    @Override
    public ComplexMatrix map(EntryMapper<Complex, Complex> mapper) {
        ComplexMatrix result = new ComplexMatrix(rows(), cols());
        forEach((r, c, v) -> result.set(r, c, mapper.map(r, c, v)));
        return result;
    }

    @Override
    public ComplexMatrix map(GFunction<Complex, Complex> mapper) {
        return map((r, c, v) -> mapper.value(v));
    }

    @Override
    public ComplexMatrix copy() {
        return map(v -> new Complex(v.getReal(), v.getImaginary()));
    }

    @Override
    public ComplexMatrix getSubMatrix(int[] rows, int[] cols) {

        ComplexMatrix subMatrix = new ComplexMatrix(rows.length, cols.length);

        for (int r = 0; r < rows.length; r++) {

            for (int c = 0; c < cols.length; c++) {

                subMatrix.set(r, c, get(rows[r], cols[c]));

            }

        }

        return subMatrix;

    }

    @Override
    public ComplexMatrix getSubMatrix(int startRow, int endRow, int startCol, int endCol) {
        return getSubMatrix(Util.makeCountingArray(startRow, endRow), Util.makeCountingArray(startCol, endCol));
    }

    @Override
    public ComplexMatrix appendRows(Matrix<Complex> rows) {

        if (rows.cols() != cols()) {
            throw new DimensionException(rows, -1, cols());
        }

        ComplexMatrix newMatrix = new ComplexMatrix(rows() + rows.rows(), cols());

        newMatrix.setSubMatrix(0, 0, this);
        newMatrix.setSubMatrix(rows(), 0, rows);

        return newMatrix;

    }

    @Override
    public ComplexMatrix appendCols(Matrix<Complex> cols) {

        if (cols.rows() != rows()) {
            throw new DimensionException(cols, cols(), -1);
        }

        ComplexMatrix newMatrix = new ComplexMatrix(rows(), cols() + cols.cols());

        newMatrix.setSubMatrix(0 ,0, this);
        newMatrix.setSubMatrix(0, cols(), cols);

        return newMatrix;

    }

    @Override
    public boolean isSingular() {
        return !(new FieldLUDecompositionImpl<>(backing)).getSolver().isNonSingular();
    }

    @Override
    public Complex getDeterminant() {
        return (new FieldLUDecompositionImpl<>(backing)).getDeterminant();
    }

    @Override
    public Complex getTrace() {

        if (!isSquare()) {
            throw new NonSquareException();
        }

        Complex value = Complex.ZERO;

        for (int i = 0; i < rows(); i++) {
            value = value.add(get(i, i));
        }

        return value;

    }

    @Override
    public ComplexMatrix invert() {
        return new ComplexMatrix((new FieldLUDecompositionImpl<>(backing)).getSolver().getInverse());
    }

    @Override
    public ComplexMatrix transpose() {

        ComplexMatrix result = new ComplexMatrix(cols(), rows());
        forEach((r, c, v) -> result.set(c, r, v));
        return result;

    }

    @Override
    public ComplexMatrix reshape(int rows, int cols) {

        if (rows * cols != size()) {
            throw new DimensionException(rows * cols, size());
        }

        return new ComplexMatrix(rows, cols, getFlatData());

    }

    @Override
    public ComplexMatrix leftDivide(Matrix<Complex> rhs) {

        FieldMatrix<Complex> field;

        if (rhs instanceof ComplexMatrix) {
            field = ((ComplexMatrix) rhs).fieldMatrix();
        } else {
            field = MatrixUtils.createFieldMatrix(rhs.getData());
        }

        return new ComplexMatrix((new FieldLUDecompositionImpl<>(backing)).getSolver().solve(field));

    }

    @Override
    public QR<Complex> getQR() {
        return null;
    }

    @Override
    public LU<Complex> getLU() {

        FieldLUDecompositionImpl<Complex> lu = new FieldLUDecompositionImpl<>(backing);

        return new LU<>() {
            @Override
            public ComplexMatrix getL() {
                return new ComplexMatrix(lu.getL());
            }

            @Override
            public ComplexMatrix getU() {
                return new ComplexMatrix(lu.getU());
            }

            @Override
            public ComplexMatrix getP() {
                return new ComplexMatrix(lu.getP());
            }
        };

    }

    @Override
    public ComplexMatrix getRowSums() {
        return null;
    }

    @Override
    public ComplexMatrix getColSums() {
        return null;
    }

    public static class Identity extends ComplexMatrix {

        public Identity(int size) {
            super(size, size);
            for (int i = 0; i < size; i++) {
                set(i, i, Complex.ONE);
            }
        }

    }

    public static class Row extends ComplexMatrix {

        public Row(Complex... values) {
            super(1, values.length, values);
        }

    }

    public static class Col extends ComplexMatrix {

        public Col(Complex... values) {
            super(values.length, 1, values);
        }

    }

}
