package jisa.maths.matrices;

import jisa.Util;
import jisa.maths.functions.GFunction;
import jisa.maths.matrices.MatrixFactory.Copier;
import jisa.maths.matrices.MatrixFactory.Operation;
import jisa.maths.matrices.exceptions.*;
import org.apache.commons.math.complex.Complex;
import org.apache.commons.math.linear.FieldLUDecomposition;
import org.apache.commons.math.linear.FieldLUDecompositionImpl;
import org.apache.commons.math.linear.FieldMatrix;
import org.apache.commons.math.linear.MatrixUtils;

public abstract class AbstractMatrix<T> implements Matrix<T> {

    private final FieldMatrix<FieldElement> backing;
    private final T                         zero;
    private final T                         unity;

    @Override
    public Matrix<T> getDiagonal() {

        if (!isSquare()) {
            throw new NonSquareException();
        }

        Matrix<T> diag = create(rows(), 1);

        for (int i = 0; i < rows(); i++) {
            diag.set(i,0, get(i,i));
        }

        return diag;

    }

    public AbstractMatrix(T zero, T unity, int rows, int cols) {

        if (rows < 0 || cols < 0) {
            throw new MatrixException("Matrix dimensions cannot be negative.");
        }

        this.zero    = zero;
        this.unity   = unity;
        this.backing = MatrixUtils.createFieldMatrix(new Field(), rows, cols);

        for (int r = 0; r < rows; r++) {

            for (int c = 0; c < cols; c++) {

                backing.setEntry(r, c, new FieldElement(copy(zero)));

            }

        }

    }

    public AbstractMatrix(T zero, T unity, FieldMatrix<FieldElement> matrix) {
        this.zero    = zero;
        this.unity   = unity;
        this.backing = matrix;
    }

    public AbstractMatrix(T zero, T unity, Matrix<T> matrix) {
        this(zero, unity, matrix.rows(), matrix.cols());
        setAll(matrix.getFlatData());
    }

    public String toString() {

        String[] rows = new String[rows()];

        for (int r = 0; r < rows(); r++) {

            String[] cols = new String[cols()];

            for (int c = 0; c < cols(); c++) {
                cols[c] = get(r,c).toString();
            }

            rows[r] = String.join(", ", cols);

        }

        return "[ " + String.join("; ", rows) + " ]";

    }

    protected abstract T add(T a, T b);

    protected abstract T subtract(T a, T b);

    protected abstract T multiply(T a, T b);

    protected abstract T divide(T a, T b);

    protected abstract T copy(T a);

    public AbstractMatrix<T> create(int rows, int cols) {

        return new AbstractMatrix<T>(zero, unity, rows, cols) {
            @Override
            protected T add(T a, T b) {
                return AbstractMatrix.this.add(a, b);
            }

            @Override
            protected T subtract(T a, T b) {
                return AbstractMatrix.this.subtract(a, b);
            }

            @Override
            protected T multiply(T a, T b) {
                return AbstractMatrix.this.multiply(a, b);
            }

            @Override
            protected T divide(T a, T b) {
                return AbstractMatrix.this.divide(a, b);
            }

            @Override
            protected T copy(T a) {
                return AbstractMatrix.this.copy(a);
            }
        };

    }

    public AbstractMatrix<T> create() {
        return create(rows(), cols());
    }

    public AbstractMatrix<T> create(FieldMatrix<FieldElement> matrix) {

        return new AbstractMatrix<T>(zero, unity, matrix) {
            @Override
            protected T add(T a, T b) {
                return AbstractMatrix.this.add(a, b);
            }

            @Override
            protected T subtract(T a, T b) {
                return AbstractMatrix.this.subtract(a, b);
            }

            @Override
            protected T multiply(T a, T b) {
                return AbstractMatrix.this.multiply(a, b);
            }

            @Override
            protected T divide(T a, T b) {
                return AbstractMatrix.this.divide(a, b);
            }

            @Override
            protected T copy(T a) {
                return AbstractMatrix.this.copy(a);
            }
        };

    }

    public AbstractMatrix<T> create(Matrix<T> matrix) {

        return new AbstractMatrix<T>(zero, unity, matrix) {
            @Override
            protected T add(T a, T b) {
                return AbstractMatrix.this.add(a, b);
            }

            @Override
            protected T subtract(T a, T b) {
                return AbstractMatrix.this.subtract(a, b);
            }

            @Override
            protected T multiply(T a, T b) {
                return AbstractMatrix.this.multiply(a, b);
            }

            @Override
            protected T divide(T a, T b) {
                return AbstractMatrix.this.divide(a, b);
            }

            @Override
            protected T copy(T a) {
                return AbstractMatrix.this.copy(a);
            }
        };

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
    public T get(int row, int col) {
        checkIndices(row, col);
        return backing.getEntry(row, col).value;
    }

    @Override
    public T[][] getData() {

        T[][] data = (T[][]) new Object[rows()][cols()];
        forEach((r, c, v) -> data[r][c] = v);
        return data;

    }

    @Override
    public T[] getFlatData() {

        T[] data = (T[]) new Object[size()];

        int i = 0;
        for (T v : this) {
            data[i++] = v;
        }

        return data;

    }

    @Override
    public void set(int row, int col, T value) {
        checkIndices(row, col);
        backing.getEntry(row, col).value = value;
    }

    @Override
    public void setAll(T... values) {

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
    public void setDiagonal(T... values) {

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
    public void setDiagonal(T value) {

        if (!isSquare()) {
            throw new NonSquareException();
        }

        for (int i = 0; i < rows(); i++) {
            set(i, i, copy(value));
        }

    }

    @Override
    public void setAll(T value) {
        forEach((r, c, v) -> set(r, c, copy(value)));
    }

    @Override
    public void mapElement(int row, int col, GFunction<T, T> mapper) {
        set(row, col, mapper.value(get(row, col)));
    }

    @Override
    public void multiplyElement(int row, int col, T value) {
        set(row, col, multiply(get(row, col), value));
    }

    @Override
    public void divideElement(int row, int col, T value) {
        set(row, col, divide(get(row, col), value));
    }

    @Override
    public void addToElement(int row, int col, T value) {
        set(row, col, add(get(row, col), value));
    }

    @Override
    public void subtractFromElement(int row, int col, T value) {
        set(row, col, subtract(get(row, col), value));
    }

    @Override
    public void mapRow(int row, LinearMapper<T> mapper) {
        for (int i = 0; i < cols(); i++) {
            set(row, i, mapper.map(i, get(row, i)));
        }
    }

    @Override
    public void mapCol(int col, LinearMapper<T> mapper) {
        for (int i = 0; i < rows(); i++) {
            set(i, col, mapper.map(i, get(i, col)));
        }
    }

    @Override
    public void mapRowToRow(int source, int dest, LinearMapper<T> mapper) {
        setRow(dest, getRowMatrix(source).map((r, c, v) -> mapper.map(c, v)));
    }

    @Override
    public void mapColToCol(int source, int dest, LinearMapper<T> mapper) {
        setCol(dest, getColMatrix(source).map((r, c, v) -> mapper.map(r, v)));
    }

    @Override
    public T[] getRowArray(int row) {

        T[] data = (T[]) new Object[cols()];

        for (int c = 0; c < cols(); c++) {
            data[c] = get(row, c);
        }

        return data;
    }

    @Override
    public T[] getColArray(int col) {

        T[] data = (T[]) new Object[rows()];

        for (int r = 0; r < rows(); r++) {
            data[r] = get(r, col);
        }

        return data;
    }

    @Override
    public Matrix<T> getRowMatrix(int row) {
        return getSubMatrix(row, row, 0, cols() - 1);
    }

    @Override
    public Matrix<T> getColMatrix(int col) {
        return getSubMatrix(0, rows() - 1, col, col);
    }

    @Override
    public Matrix<T> multiply(Matrix<T> rhs) {

        if (!Matrix.canMultiply(this, rhs)) {
            throw new DimensionException(rhs, cols(), -1);
        }

        Matrix<T> result = create(rows(), rhs.cols());

        for (int r = 0; r < rows(); r++) {

            for (int c = 0; c < rhs.cols(); c++) {

                T value = zero;

                for (int k = 0; k < cols(); k++) {

                    value = add(value, multiply(get(r, k), rhs.get(k, c)));

                }

                result.set(r, c, value);

            }

        }

        return result;

    }

    @Override
    public Matrix<T> leftMultiply(Matrix<T> lhs) {
        return lhs.multiply(this);
    }

    @Override
    public Matrix<T> multiply(T rhs) {
        return map(v -> multiply(v, rhs));
    }

    @Override
    public Matrix<T> leftMultiply(T lhs) {
        return map(v -> multiply(lhs, v));
    }

    @Override
    public Matrix<T> elementMultiply(Matrix<T> rhs) {

        if (!Matrix.dimensionsMatch(this, rhs)) {
            throw new DimensionException(rhs, this);
        }

        return map((r, c, v) -> multiply(v, rhs.get(r, c)));

    }

    @Override
    public Matrix<T> leftElementMultiply(Matrix<T> lhs) {

        if (!Matrix.dimensionsMatch(this, lhs)) {
            throw new DimensionException(lhs, this);
        }

        return map((r, c, v) -> multiply(lhs.get(r, c), v));

    }

    @Override
    public Matrix<T> elementDivide(Matrix<T> rhs) {

        if (!Matrix.dimensionsMatch(this, rhs)) {
            throw new DimensionException(rhs, this);
        }

        return map((r, c, v) -> divide(v, rhs.get(r, c)));

    }

    @Override
    public Matrix<T> leftElementDivide(Matrix<T> lhs) {

        if (!Matrix.dimensionsMatch(this, lhs)) {
            throw new DimensionException(lhs, this);
        }

        return map((r, c, v) -> divide(lhs.get(r, c), v));

    }

    @Override
    public Matrix<T> divide(T rhs) {
        return map(v -> divide(v, rhs));
    }

    @Override
    public Matrix<T> leftDivide(T lhs) {
        return map(v -> divide(lhs, v));
    }

    @Override
    public Matrix<T> add(Matrix<T> rhs) {
        return map((r, c, v) -> add(v, rhs.get(r, c)));
    }

    @Override
    public Matrix<T> add(T rhs) {
        return map(v -> add(v, rhs));
    }

    @Override
    public Matrix<T> subtract(Matrix<T> rhs) {
        return map((r, c, v) -> subtract(v, rhs.get(r, c)));
    }

    @Override
    public Matrix<T> subtract(T rhs) {
        return map(v -> subtract(v, rhs));
    }

    @Override
    public Matrix<T> map(EntryMapper<T, T> mapper) {
        Matrix<T> result = create();
        forEach((r, c, v) -> result.set(r, c, mapper.map(r, c, v)));
        return result;
    }

    @Override
    public Matrix<T> map(GFunction<T, T> mapper) {
        return map((r, c, v) -> mapper.value(v));
    }

    @Override
    public void mapSelf(EntryMapper<T, T> mapper) {
        forEach((r, c, v) -> set(r, c, mapper.map(r, c, v)));
    }

    @Override
    public Matrix<T> copy() {
        return map(this::copy);
    }

    @Override
    public Matrix<T> getSubMatrix(int[] rows, int[] cols) {

        Matrix<T> subMatrix = create(rows.length, cols.length);

        for (int r = 0; r < rows.length; r++) {

            if (rows[r] >= rows()) {
                throw new SubMatrixException(this, rows, cols);
            }

            for (int c = 0; c < cols.length; c++) {

                if (cols[c] >= cols()) {
                    throw new SubMatrixException(this, rows, cols);
                }

                subMatrix.set(r, c, get(rows[r], cols[c]));

            }
        }

        return subMatrix;

    }

    @Override
    public Matrix<T> getSubMatrix(int startRow, int endRow, int startCol, int endCol) {
        return getSubMatrix(Util.makeCountingArray(startRow, endRow), Util.makeCountingArray(startCol, endCol));
    }

    @Override
    public Matrix<T> appendRows(Matrix<T> rows) {

        if (rows.cols() != cols()) {
            throw new DimensionException(rows, -1, cols());
        }

        Matrix<T> newMatrix = create(rows() + rows.rows(), cols());

        newMatrix.setSubMatrix(0, 0, this);
        newMatrix.setSubMatrix(rows(), 0, rows);

        return newMatrix;

    }

    @Override
    public Matrix<T> appendCols(Matrix<T> cols) {

        if (cols.rows() != rows()) {
            throw new DimensionException(cols, cols(), -1);
        }

        Matrix<T> newMatrix = create(rows(), cols() + cols.cols());

        newMatrix.setSubMatrix(0 ,0, this);
        newMatrix.setSubMatrix(0, cols(), cols);

        return newMatrix;

    }

    @Override
    public boolean isSingular() {
        return !(new FieldLUDecompositionImpl<>(backing)).getSolver().isNonSingular();
    }

    @Override
    public T getDeterminant() {

        if (!isSquare()) {
            throw new NonSquareException();
        }

        return (new FieldLUDecompositionImpl<>(backing)).getDeterminant().value;

    }

    @Override
    public T getTrace() {

        if (!isSquare()) {
            throw new NonSquareException();
        }

        T value = zero;

        for (int i = 0; i < rows(); i++) {
            value = add(value, get(i, i));
        }

        return value;

    }

    @Override
    public Matrix<T> invert() {

        if (!isSquare()) {
            throw new NonSquareException();
        }

        if (isSingular()) {
            throw new SingularException();
        }

        return create((new FieldLUDecompositionImpl<>(backing)).getSolver().getInverse());
    }

    @Override
    public Matrix<T> transpose() {
        Matrix<T> result = create(cols(), rows());
        forEach((r, c, v) -> result.set(c, r, v));
        return result;
    }

    @Override
    public Matrix<T> leftDivide(Matrix<T> rhs) {

        FieldMatrix<FieldElement> field;

        if (rhs instanceof AbstractMatrix) {
            field = ((AbstractMatrix<T>) rhs).backing;
        } else {
            field = create(rhs).backing;
        }

        return create(new FieldLUDecompositionImpl<>(backing).getSolver().solve(field));
    }

    @Override
    public QR<T> getQR() {
        return null;
    }

    @Override
    public LU<T> getLU() {

        FieldLUDecomposition<FieldElement> lu = new FieldLUDecompositionImpl<>(backing);

        return new LU<>() {
            @Override
            public Matrix<T> getL() {
                return create(lu.getL());
            }

            @Override
            public Matrix<T> getU() {
                return create(lu.getU());
            }

            @Override
            public Matrix<T> getP() {
                return create(lu.getP());
            }
        };

    }

    @Override
    public Matrix<T> getRowSums() {
        Matrix<T> sums = create(rows(), 1);
        forEach((r, c, v) -> sums.addToElement(r, 0, v));
        return sums;
    }

    @Override
    public Matrix<T> getColSums() {
        Matrix<T> sums = create(1, cols());
        forEach((r, c, v) -> sums.addToElement(0, c, v));
        return sums;
    }

    public static class LMatrix<T> extends AbstractMatrix<T> {

        private final Operation<T> add;
        private final Operation<T> sub;
        private final Operation<T> mult;
        private final Operation<T> div;
        private final Copier<T> copier;

        public LMatrix(T zero, T unity, Operation<T> add, Operation<T> sub, Operation<T> mult, Operation<T> div, Copier<T> copier, int rows, int cols) {
            super(zero, unity, rows, cols);

            this.add = add;
            this.sub = sub;
            this.mult = mult;
            this.div = div;
            this.copier = copier;
        }

        @Override
        protected T add(T a, T b) {
            return add.operate(a,b);
        }

        @Override
        protected T subtract(T a, T b) {
            return sub.operate(a,b);
        }

        @Override
        protected T multiply(T a, T b) {
            return mult.operate(a,b);
        }

        @Override
        protected T divide(T a, T b) {
            return div.operate(a,b);
        }

        @Override
        protected T copy(T a) {
            return copier.copy(a);
        }
    }

    private class FieldElement implements org.apache.commons.math.FieldElement<FieldElement> {

        public T value;

        private FieldElement(T value) {this.value = value;}


        @Override
        public FieldElement add(FieldElement a) {
            return new FieldElement(AbstractMatrix.this.add(value, a.value));
        }

        @Override
        public FieldElement subtract(FieldElement a) {
            return new FieldElement(AbstractMatrix.this.subtract(value, a.value));
        }

        @Override
        public FieldElement multiply(FieldElement a) {
            return new FieldElement(AbstractMatrix.this.multiply(value, a.value));
        }

        @Override
        public FieldElement divide(FieldElement a) throws ArithmeticException {
            return new FieldElement(AbstractMatrix.this.divide(value, a.value));
        }

        @Override
        public org.apache.commons.math.Field<FieldElement> getField() {
            return null;
        }
    }

    private class Field implements org.apache.commons.math.Field<FieldElement> {

        @Override
        public FieldElement getZero() {
            return new FieldElement(zero);
        }

        @Override
        public FieldElement getOne() {
            return new FieldElement(unity);
        }
    }

}
