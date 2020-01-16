package jisa.maths.matrices;

import jisa.Util;
import jisa.maths.functions.GFunction;
import jisa.maths.matrices.exceptions.*;
import org.apache.commons.math.linear.*;

public class RealMatrix implements Matrix<Double> {

    private final org.apache.commons.math.linear.RealMatrix backing;
    private       LUDecomposition                           lu = null;

    public static RealMatrix asRealMatrix(Matrix<Double> matrix) {

        if (matrix instanceof RealMatrix) {
            return (RealMatrix) matrix;
        } else {
            return new RealMatrix(matrix);
        }

    }

    public static RealMatrix asRow(Double... values) {
        return new RealMatrix(1, values.length, values);
    }

    public static RealMatrix asRow(double[] values) {
        return new RealMatrix(1, values.length, values);
    }

    public static RealMatrix asColumn(Double... values) {
        return new RealMatrix(values.length, 1, values);
    }

    public static RealMatrix asColumn(double[] values) {
        return new RealMatrix(values.length, 1, values);
    }

    public static RealMatrix identity(int size) {

        RealMatrix identity = new RealMatrix(size, size);
        identity.setDiagonal(1.0);
        return identity;

    }

    public static RealMatrix rotation2D(double angle) {
        return new RealMatrix(2, 2, Math.cos(angle), -Math.sin(angle), Math.sin(angle), Math.cos(angle));
    }

    public RealMatrix(int rows, int cols) {

        if (rows < 0 || cols < 0) {
            throw new MatrixException("Matrix dimensions cannot be negative.");
        }

        backing = MatrixUtils.createRealMatrix(rows, cols);
        setAll(0.0);

    }

    public RealMatrix(String text) {

        text = text.replace("[", "");
        text = text.replace("]", "");

        String[] rows = text.split(";");

        int nRows = rows.length;
        int nCols = rows[0].split(",").length;

        double[][] data = new double[nRows][nCols];

        for (int r = 0; r < nRows; r++) {

            String[] cols = rows[r].split(",");

            for (int c = 0; c < nCols; c++) {

                data[r][c] = Double.parseDouble(cols[c].trim());

            }

        }

        backing = MatrixUtils.createRealMatrix(data);

    }

    public RealMatrix(int rows, int cols, Double... values) {
        this(rows, cols);
        setAll(values);
    }

    public RealMatrix(int rows, int cols, double[] values) {
        this(rows, cols);
        setAll(values);
    }

    public RealMatrix(double[][] data) {
        backing = MatrixUtils.createRealMatrix(data);
    }

    public RealMatrix(org.apache.commons.math.linear.RealMatrix matrix) {
        this(matrix.getData());
    }

    public RealMatrix(Matrix<Double> matrix) {
        this(matrix.rows(), matrix.cols());
        setAll(matrix.getFlatData());
    }

    public static RealMatrix iterableToCol(Iterable<Double> iterable) {

        if (iterable instanceof RealMatrix) {
            if (((RealMatrix) iterable).cols() == 1) {
                return (RealMatrix) iterable;
            } else {
                return ((RealMatrix) iterable).reshape(((RealMatrix) iterable).size(), 1);
            }
        }

        int count = 0;
        for (double v : iterable) { count++; }

        RealMatrix matrix = new RealMatrix(count, 1);

        int i = 0;
        for (double v : iterable) {
            matrix.set(i++, 0, v);
        }

        return matrix;

    }

    public org.apache.commons.math.linear.RealMatrix realMatrix() {
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
    public Double get(int row, int col) {
        checkIndices(row, col);
        return backing.getEntry(row, col);
    }

    @Override
    public Double[][] getData() {

        Double[][] data = new Double[rows()][cols()];

        for (int r = 0; r < rows(); r++) {

            for (int c = 0; c < cols(); c++) {
                data[r][c] = get(r, c);
            }

        }

        return data;

    }

    public RealMatrix divide(Matrix<Double> rhs) {
        return multiply(rhs.invert());
    }

    @Override
    public RealMatrix times(Matrix<Double> rhs) {
        return multiply(rhs);
    }

    @Override
    public RealMatrix times(Double rhs) {
        return multiply(rhs);
    }

    @Override
    public RealMatrix div(Matrix<Double> rhs) {
        return divide(rhs);
    }

    @Override
    public RealMatrix div(Double rhs) {
        return divide(rhs);
    }

    public RealMatrix rotate2D(double angle) {
        if (cols() == 2 && rows() != 2) {
            return rotation2D(angle).multiply(transpose()).transpose();
        } else {
            return rotation2D(angle).multiply(this);
        }
    }

    @Override
    public RealMatrix plus(Matrix<Double> rhs) {
        return add(rhs);
    }

    @Override
    public RealMatrix plus(Double rhs) {
        return add(rhs);
    }

    @Override
    public RealMatrix minus(Matrix<Double> rhs) {
        return subtract(rhs);
    }

    @Override
    public RealMatrix minus(Double rhs) {
        return subtract(rhs);
    }

    @Override
    public Double[] getFlatData() {

        Double[] data = new Double[size()];

        int i = 0;
        for (double v : this) {
            data[i++] = v;
        }

        return data;

    }

    @Override
    public RealMatrix getDiagonal() {

        if (!isSquare()) {
            throw new NonSquareException();
        }

        RealMatrix result = new RealMatrix(rows(), 1);

        for (int i = 0; i < rows(); i++) {
            result.set(i, 0, get(i, i));
        }

        return result;

    }

    @Override
    public void setDiagonal(Double... values) {

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
    public void setDiagonal(Double value) {

        if (!isSquare()) {
            throw new NonSquareException();
        }

        for (int i = 0; i < rows(); i++) {
            set(i, i, value);
        }

    }

    public void setRow(int row, double... values) {

        checkIndices(row, 0);

        if (values.length != cols()) {
            throw new DimensionException(values.length, cols());
        }

        int i = 0;
        for (int c = 0; c < cols(); c++) {
            set(row, c, values[i++]);
        }

    }

    public void setCol(int col, double... values) {

        checkIndices(0, col);

        if (values.length != rows()) {
            throw new DimensionException(values.length, rows());
        }

        int i = 0;
        for (int r = 0; r < rows(); r++) {
            set(r, col, values[i++]);
        }

    }

    public double[] getRow(int row) {
        checkIndices(row, 0);
        return backing.getRow(row);
    }

    public double[] getCol(int col) {
        checkIndices(0, col);
        return backing.getColumn(col);
    }

    public double getNorm() {
        return map(Math::abs).getRowSums().getMaxElement();
    }

    public double getMaxElement() {

        double max = Double.NEGATIVE_INFINITY;

        for (double v : this) {
            max = Math.max(max, v);
        }

        return max;

    }

    public double getMinElement() {

        double min = Double.POSITIVE_INFINITY;

        for (double v : this) {
            min = Math.min(min, v);
        }

        return min;

    }

    @Override
    public void set(int row, int col, Double value) {
        checkIndices(row, col);
        backing.setEntry(row, col, value);
    }

    @Override
    public void setAll(Double value) {
        forEach((r, c, v) -> set(r, c, value));
    }

    @Override
    public void setAll(Double... values) {

        if (values.length != size()) {
            throw new DimensionException(values.length, size());
        }

        int k = 0;

        for (int r = 0; r < rows(); r++) {

            for (int c = 0; c < cols(); c++) {

                set(r, c, values[k++]);

            }

        }

    }

    public void setAll(double[] values) {

        if (values.length != size()) {
            throw new DimensionException(values.length, size());
        }

        int k = 0;

        for (int r = 0; r < rows(); r++) {

            for (int c = 0; c < cols(); c++) {

                set(r, c, values[k++]);

            }

        }

    }

    public String toString() {

        String[] rows = new String[rows()];

        for (int r = 0; r < rows(); r++) {

            String[] cols = new String[cols()];

            for (int c = 0; c < cols(); c++) {
                cols[c] = get(r, c).toString();
            }

            rows[r] = String.join(", ", cols);

        }

        return "[ " + String.join("; ", rows) + " ]";

    }

    @Override
    public void mapElement(int row, int col, GFunction<Double, Double> mapper) {
        set(row, col, mapper.value(get(row, col)));
    }

    @Override
    public void multiplyElement(int row, int col, Double value) {
        set(row, col, get(row, col) * value);
    }

    @Override
    public void divideElement(int row, int col, Double value) {
        set(row, col, get(row, col) / value);
    }

    @Override
    public void addToElement(int row, int col, Double value) {
        set(row, col, get(row, col) + value);
    }

    @Override
    public void subtractFromElement(int row, int col, Double value) {
        set(row, col, get(row, col) - value);
    }

    @Override
    public void mapRow(int row, LinearMapper<Double> mapper) {

        checkIndices(row, 0);

        for (int i = 0; i < cols(); i++) {
            set(row, i, mapper.map(i, get(row, i)));
        }

    }

    @Override
    public void mapCol(int col, LinearMapper<Double> mapper) {

        checkIndices(0, col);

        for (int i = 0; i < rows(); i++) {
            set(i, col, mapper.map(i, get(i, col)));
        }

    }

    @Override
    public void mapRowToRow(int source, int dest, LinearMapper<Double> mapper) {

        checkIndices(source, 0);
        checkIndices(dest, 0);

        setRow(dest, getRowMatrix(source).map((r, c, v) -> mapper.map(c, v)));

    }

    @Override
    public void mapColToCol(int source, int dest, LinearMapper<Double> mapper) {

        checkIndices(0, source);
        checkIndices(0, dest);

        setCol(dest, getColMatrix(source).map((r, c, v) -> mapper.map(r, v)));

    }

    @Override
    public Double[] getRowArray(int row) {

        checkIndices(row, 0);

        Double[] data = new Double[cols()];

        for (int i = 0; i < cols(); i++) {
            data[i] = get(row, i);
        }

        return data;

    }

    @Override
    public Double[] getColArray(int col) {

        checkIndices(0, col);

        Double[] data = new Double[rows()];

        for (int i = 0; i < rows(); i++) {
            data[i] = get(i, col);
        }

        return data;

    }

    @Override
    public RealMatrix getRowMatrix(int row) {
        checkIndices(row, 0);
        return getSubMatrix(row, row, 0, cols() - 1);
    }

    @Override
    public RealMatrix getColMatrix(int col) {
        checkIndices(0, col);
        return getSubMatrix(0, rows() - 1, col, col);
    }

    @Override
    public RealMatrix multiply(Matrix<Double> rhs) {

        if (!Matrix.canMultiply(this, rhs)) {
            throw new DimensionException(rhs, cols(), -1);
        }

        RealMatrix result = new RealMatrix(rows(), rhs.cols());

        for (int r = 0; r < rows(); r++) {

            for (int c = 0; c < rhs.cols(); c++) {

                for (int k = 0; k < cols(); k++) {
                    result.addToElement(r, c, get(r, k) * rhs.get(k, c));
                }

            }

        }

        return result;

    }

    public RealMatrix multiply(org.apache.commons.math.linear.RealMatrix rhs) {

        if (cols() != rhs.getRowDimension()) {
            throw new DimensionException(rhs.getRowDimension(), rhs.getColumnDimension(), cols(), -1);
        }

        return new RealMatrix(backing.multiply(rhs));

    }

    @Override
    public RealMatrix leftMultiply(Matrix<Double> lhs) {
        return asRealMatrix(lhs.multiply(this));
    }

    @Override
    public RealMatrix multiply(Double rhs) {
        return map(v -> v * rhs);
    }

    @Override
    public RealMatrix leftMultiply(Double lhs) {
        return multiply(lhs);
    }

    @Override
    public RealMatrix elementMultiply(Matrix<Double> rhs) {

        if (Matrix.dimensionsMatch(this, rhs)) {
            return map((r, c, v) -> v * rhs.get(r, c));
        } else if (Matrix.rowMatrixMatch(this, rhs)) {
            return map((r, c, v) -> v * rhs.get(0, c));
        } else if (Matrix.colMatrixMatch(this, rhs)) {
            return map((r, c, v) -> v * rhs.get(r, 0));
        } else if (Matrix.isScalar(rhs)) {
            return multiply(rhs.get(0,0));
        } else {
            throw new DimensionException(rhs, this);
        }
    }

    @Override
    public RealMatrix leftElementMultiply(Matrix<Double> lhs) {
        return elementMultiply(lhs);
    }

    @Override
    public RealMatrix elementDivide(Matrix<Double> rhs) {

        if (Matrix.dimensionsMatch(this, rhs)) {
            return map((r, c, v) -> v / rhs.get(r, c));
        } else if (Matrix.rowMatrixMatch(this, rhs)) {
            return map((r, c, v) -> v / rhs.get(0, c));
        } else if (Matrix.colMatrixMatch(this, rhs)) {
            return map((r, c, v) -> v / rhs.get(r, 0));
        } else if (Matrix.isScalar(rhs)) {
            return divide(rhs.get(0,0));
        }  else {
            throw new DimensionException(rhs, this);
        }

    }

    @Override
    public RealMatrix leftElementDivide(Matrix<Double> lhs) {

        if (!Matrix.dimensionsMatch(this, lhs)) {
            throw new DimensionException(lhs, this);
        }

        return map((r, c, v) -> lhs.get(r, c) / v);
    }

    @Override
    public RealMatrix divide(Double rhs) {
        return map(v -> v / rhs);
    }

    @Override
    public RealMatrix leftDivide(Double lhs) {
        return map(v -> lhs / v);
    }

    @Override
    public RealMatrix add(Matrix<Double> rhs) {

        if (Matrix.dimensionsMatch(this, rhs)) {
            return map((r, c, v) -> v + rhs.get(r, c));
        } else if (Matrix.rowMatrixMatch(this, rhs)) {
            return map((r, c, v) -> v + rhs.get(0, c));
        } else if (Matrix.colMatrixMatch(this, rhs)) {
            return map((r, c, v) -> v + rhs.get(r, 0));
        } else if (Matrix.isScalar(rhs)) {
            return add(rhs.get(0,0));
        }  else {
            throw new DimensionException(rhs, this);
        }

    }

    @Override
    public RealMatrix add(Double rhs) {
        return map(v -> v + rhs);
    }

    @Override
    public RealMatrix subtract(Matrix<Double> rhs) {

        if (Matrix.dimensionsMatch(this, rhs)) {
            return map((r, c, v) -> v - rhs.get(r, c));
        } else if (Matrix.rowMatrixMatch(this, rhs)) {
            return map((r, c, v) -> v - rhs.get(0, c));
        } else if (Matrix.colMatrixMatch(this, rhs)) {
            return map((r, c, v) -> v - rhs.get(r, 0));
        } else if (Matrix.isScalar(rhs)) {
            return subtract(rhs.get(0,0));
        }  else {
            throw new DimensionException(rhs, this);
        }
    }

    @Override
    public RealMatrix subtract(Double rhs) {
        return map(v -> v - rhs);
    }

    @Override
    public RealMatrix map(EntryMapper<Double, Double> mapper) {

        RealMatrix result = new RealMatrix(rows(), cols());
        forEach((r, c, v) -> result.set(r, c, mapper.map(r, c, v)));
        return result;

    }

    @Override
    public RealMatrix map(GFunction<Double, Double> mapper) {
        return map((r, c, v) -> mapper.value(v));
    }

    @Override
    public RealMatrix copy() {
        return map(v -> v);
    }

    @Override
    public RealMatrix getSubMatrix(int[] rows, int[] cols) {

        RealMatrix subMatrix = new RealMatrix(rows.length, cols.length);

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
    public RealMatrix getSubMatrix(int startRow, int endRow, int startCol, int endCol) {
        return getSubMatrix(Util.makeCountingArray(startRow, endRow), Util.makeCountingArray(startCol, endCol));
    }

    @Override
    public RealMatrix appendRows(Matrix<Double> rows) {

        if (rows.cols() != cols()) {
            throw new DimensionException(rows, -1, cols());
        }

        RealMatrix newMatrix = new RealMatrix(rows() + rows.rows(), cols());

        newMatrix.setSubMatrix(0, 0, this);
        newMatrix.setSubMatrix(rows(), 0, rows);

        return newMatrix;

    }

    @Override
    public RealMatrix appendCols(Matrix<Double> cols) {

        if (cols.rows() != rows()) {
            throw new DimensionException(cols, cols(), -1);
        }

        RealMatrix newMatrix = new RealMatrix(rows(), cols() + cols.cols());

        newMatrix.setSubMatrix(0, 0, this);
        newMatrix.setSubMatrix(0, cols(), cols);

        return newMatrix;

    }

    @Override
    public boolean isSingular() {
        return backing.isSingular();
    }

    @Override
    public Double getDeterminant() {

        if (!isSquare()) {
            throw new NonSquareException();
        }

        return backing.getDeterminant();
    }

    @Override
    public Double getTrace() {

        if (!isSquare()) {
            throw new NonSquareException();
        }

        double value = 0;

        for (int i = 0; i < rows(); i++) {
            value += get(i, i);
        }

        return value;

    }

    @Override
    public RealMatrix invert() {

        if (!isSquare()) {
            throw new NonSquareException();
        }

        if (isSingular()) {
            throw new SingularException();
        }

        return new RealMatrix(backing.inverse());

    }

    @Override
    public RealMatrix transpose() {

        RealMatrix result = new RealMatrix(cols(), rows());
        forEach((r, c, v) -> result.set(c, r, v));
        return result;

    }

    @Override
    public RealMatrix reshape(int rows, int cols) {

        if (rows * cols != size()) {
            throw new DimensionException(rows * cols, size());
        }

        return new RealMatrix(rows, cols, getFlatData());

    }

    @Override
    public RealMatrix leftDivide(Matrix<Double> rhs) {

        if (isSingular()) {
            throw new SingularException();
        }

        if (rows() != rhs.rows()) {
            throw new DimensionException(rhs, rows(), -1);
        }

        if (rhs instanceof RealMatrix) {
            return new RealMatrix(backing.solve(((RealMatrix) rhs).realMatrix()));
        } else {
            return new RealMatrix(backing.solve(new RealMatrix(rhs).realMatrix()));
        }

    }

    @Override
    public QR getQR() {
        return new QR(new QRDecompositionImpl(backing));
    }

    @Override
    public LU<Double> getLU() {

        if (!isSquare()) {
            throw new NonSquareException();
        }

        LUDecomposition lu = new LUDecompositionImpl(backing);

        return new LU<>() {
            @Override
            public Matrix<Double> getL() {
                return new RealMatrix(lu.getL());
            }

            @Override
            public Matrix<Double> getU() {
                return new RealMatrix(lu.getU());
            }

            @Override
            public Matrix<Double> getP() {
                return new RealMatrix(lu.getP());
            }
        };
    }

    @Override
    public RealMatrix getRowSums() {

        RealMatrix sums = new RealMatrix(rows(), 1);
        forEach((r, c, v) -> sums.addToElement(r, 0, v));
        return sums;

    }

    @Override
    public RealMatrix getColSums() {

        RealMatrix sums = new RealMatrix(1, cols());
        forEach((r, c, v) -> sums.addToElement(0, c, v));
        return sums;

    }

    public RealMatrix getRowQuadratures() {

        RealMatrix sums = new RealMatrix(rows(), 1);
        forEach((r, c, v) -> sums.addToElement(r, 0, v * v));
        sums.mapSelf(Math::sqrt);
        return sums;

    }

    public RealMatrix getColQuadratures() {

        RealMatrix sums = new RealMatrix(1, cols());
        forEach((r, c, v) -> sums.addToElement(0, c, v * v));
        sums.mapSelf(Math::sqrt);
        return sums;

    }

    public static class QR implements jisa.maths.matrices.QR<Double> {

        private final QRDecomposition qr;

        public QR(QRDecomposition qr) {this.qr = qr;}

        @Override
        public RealMatrix getQ() {
            return new RealMatrix(qr.getQ());
        }

        @Override
        public RealMatrix getR() {
            return new RealMatrix(qr.getR());
        }

        @Override
        public RealMatrix getQT() {
            return new RealMatrix(qr.getQT());
        }

        @Override
        public RealMatrix getH() {
            return new RealMatrix(qr.getH());
        }

    }

}
