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

public class CMatrix implements Matrix<Complex> {

    private final FieldMatrix<Complex> backing;

    public CMatrix(int rows, int cols) {
        backing = MatrixUtils.createFieldMatrix(ComplexField.getInstance(), rows, cols);
        setAll(Complex.ZERO);
    }

    public CMatrix(int rows, int cols, Complex... values) {
        this(rows, cols);
        setAll(values);
    }

    public CMatrix(Complex[][] data) {
        backing = MatrixUtils.createFieldMatrix(data);
    }

    public CMatrix(FieldMatrix<Complex> matrix) {
        this(matrix.getData());
    }

    public CMatrix(Matrix<Complex> matrix) {
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
    public CMatrix getDiagonal() {

        if (!isSquare()) {
            throw new NonSquareException();
        }

        CMatrix diag = new CMatrix(rows(), 1);

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
    public CMatrix getRowMatrix(int row) {
        return getSubMatrix(row, row, 0, cols() - 1);
    }

    @Override
    public CMatrix getColMatrix(int col) {
        return getSubMatrix(0, rows() - 1, col, col);
    }

    @Override
    public CMatrix multiply(Matrix<Complex> rhs) {

        if (!Matrix.canMultiply(this, rhs)) {
            throw new DimensionException(rhs, cols(), -1);
        }

        CMatrix result = new CMatrix(rows(), rhs.cols());

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
    public CMatrix leftMultiply(Matrix<Complex> lhs) {

        if (lhs instanceof CMatrix) {
            return ((CMatrix) lhs).multiply(this);
        } else {
            return new CMatrix(lhs.multiply(this));
        }

    }

    @Override
    public CMatrix multiply(Complex rhs) {
        return map(v -> v.multiply(rhs));
    }

    @Override
    public CMatrix leftMultiply(Complex lhs) {
        return map(v -> lhs.multiply(v));
    }

    @Override
    public CMatrix elementMultiply(Matrix<Complex> rhs) {
        return map((r, c, v) -> v.multiply(rhs.get(r, c)));
    }

    @Override
    public CMatrix leftElementMultiply(Matrix<Complex> lhs) {
        return map((r, c, v) -> lhs.get(r, c).multiply(v));
    }

    @Override
    public CMatrix elementDivide(Matrix<Complex> rhs) {
        return map((r, c, v) -> v.divide(rhs.get(r, c)));
    }

    @Override
    public CMatrix leftElementDivide(Matrix<Complex> lhs) {
        return map((r, c, v) -> lhs.get(r, c).divide(v));
    }

    @Override
    public CMatrix divide(Complex rhs) {
        return map(v -> v.divide(rhs));
    }

    @Override
    public CMatrix leftDivide(Complex lhs) {
        return map(v -> lhs.divide(v));
    }

    @Override
    public CMatrix add(Matrix<Complex> rhs) {
        return map((r, c, v) -> v.add(rhs.get(r, c)));
    }

    @Override
    public CMatrix add(Complex rhs) {
        return map(v -> v.add(rhs));
    }

    @Override
    public CMatrix subtract(Matrix<Complex> rhs) {
        return map((r, c, v) -> v.subtract(rhs.get(r, c)));
    }

    @Override
    public CMatrix subtract(Complex rhs) {
        return map(v -> v.subtract(rhs));
    }

    @Override
    public CMatrix map(EntryMapper<Complex, Complex> mapper) {
        CMatrix result = new CMatrix(rows(), cols());
        forEach((r, c, v) -> result.set(r, c, mapper.map(r, c, v)));
        return result;
    }

    @Override
    public CMatrix map(GFunction<Complex, Complex> mapper) {
        return map((r, c, v) -> mapper.value(v));
    }

    @Override
    public CMatrix copy() {
        return map(v -> new Complex(v.getReal(), v.getImaginary()));
    }

    @Override
    public CMatrix getSubMatrix(int[] rows, int[] cols) {

        CMatrix subMatrix = new CMatrix(rows.length, cols.length);

        for (int r = 0; r < rows.length; r++) {

            for (int c = 0; c < cols.length; c++) {

                subMatrix.set(r, c, get(rows[r], cols[c]));

            }

        }

        return subMatrix;

    }

    @Override
    public CMatrix getSubMatrix(int startRow, int endRow, int startCol, int endCol) {
        return getSubMatrix(Util.makeCountingArray(startRow, endRow), Util.makeCountingArray(startCol, endCol));
    }

    @Override
    public CMatrix appendRows(Matrix<Complex> rows) {

        if (rows.cols() != cols()) {
            throw new DimensionException(rows, -1, cols());
        }

        CMatrix newMatrix = new CMatrix(rows() + rows.rows(), cols());

        newMatrix.setSubMatrix(0, 0, this);
        newMatrix.setSubMatrix(rows(), 0, rows);

        return newMatrix;

    }

    @Override
    public CMatrix appendCols(Matrix<Complex> cols) {

        if (cols.rows() != rows()) {
            throw new DimensionException(cols, cols(), -1);
        }

        CMatrix newMatrix = new CMatrix(rows(), cols() + cols.cols());

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
    public CMatrix invert() {
        return new CMatrix((new FieldLUDecompositionImpl<>(backing)).getSolver().getInverse());
    }

    @Override
    public CMatrix transpose() {

        CMatrix result = new CMatrix(cols(), rows());
        forEach((r, c, v) -> result.set(c, r, v));
        return result;

    }

    @Override
    public CMatrix reshape(int rows, int cols) {

        if (rows * cols != size()) {
            throw new DimensionException(rows * cols, size());
        }

        return new CMatrix(rows, cols, getFlatData());

    }

    @Override
    public CMatrix leftDivide(Matrix<Complex> rhs) {

        FieldMatrix<Complex> field;

        if (rhs instanceof CMatrix) {
            field = ((CMatrix) rhs).fieldMatrix();
        } else {
            field = MatrixUtils.createFieldMatrix(rhs.getData());
        }

        return new CMatrix((new FieldLUDecompositionImpl<>(backing)).getSolver().solve(field));

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
            public CMatrix getL() {
                return new CMatrix(lu.getL());
            }

            @Override
            public CMatrix getU() {
                return new CMatrix(lu.getU());
            }

            @Override
            public CMatrix getP() {
                return new CMatrix(lu.getP());
            }
        };

    }

    @Override
    public CMatrix getRowSums() {
        return null;
    }

    @Override
    public CMatrix getColSums() {
        return null;
    }

    public static class Identity extends CMatrix {

        public Identity(int size) {
            super(size, size);
            for (int i = 0; i < size; i++) {
                set(i, i, Complex.ONE);
            }
        }

    }

    public static class Row extends CMatrix {

        public Row(Complex... values) {
            super(1, values.length, values);
        }

    }

    public static class Col extends CMatrix {

        public Col(Complex... values) {
            super(values.length, 1, values);
        }

    }

}
