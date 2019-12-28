package jisa.maths.matrices;

import jisa.Util;
import jisa.maths.functions.GFunction;
import jisa.maths.matrices.exceptions.*;
import org.apache.commons.math.linear.*;

public class RMatrix implements Matrix<Double> {

    private final RealMatrix      backing;
    private       LUDecomposition lu = null;

    public RMatrix(int rows, int cols) {

        if (rows < 0 || cols < 0) {
            throw new MatrixException("Matrix dimensions cannot be negative.");
        }

        backing = MatrixUtils.createRealMatrix(rows, cols);
        setAll(0.0);

    }

    public RMatrix(String text) {

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

    public RMatrix(int rows, int cols, Double... values) {
        this(rows, cols);
        setAll(values);
    }

    public RMatrix(double[][] data) {
        backing = MatrixUtils.createRealMatrix(data);
    }

    public RMatrix(RealMatrix matrix) {
        this(matrix.getData());
    }

    public RMatrix(Matrix<Double> matrix) {
        this(matrix.rows(), matrix.cols());
        setAll(matrix.getFlatData());
    }

    public static RMatrix iterableToCol(Iterable<Double> iterable) {

        if (iterable instanceof RMatrix) {
            if (((RMatrix) iterable).cols() == 1) {
                return (RMatrix) iterable;
            } else {
                return new RMatrix.Col(((RMatrix) iterable).getFlatData());
            }
        }

        int count = 0;
        for (double v : iterable) { count++; }

        RMatrix matrix = new RMatrix(count, 1);

        int i = 0;
        for (double v : iterable) {
            matrix.set(i++, 0, v);
        }

        return matrix;

    }

    public RealMatrix realMatrix() {
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
    public RMatrix getDiagonal() {

        if (!isSquare()) {
            throw new NonSquareException();
        }

        RMatrix result = new RMatrix(rows(), 1);

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
    public RMatrix getRowMatrix(int row) {
        checkIndices(row, 0);
        return getSubMatrix(row, row, 0, cols() - 1);
    }

    @Override
    public RMatrix getColMatrix(int col) {
        checkIndices(0, col);
        return getSubMatrix(0, rows() - 1, col, col);
    }

    @Override
    public RMatrix multiply(Matrix<Double> rhs) {

        if (!Matrix.canMultiply(this, rhs)) {
            throw new DimensionException(rhs, cols(), -1);
        }

        RMatrix result = new RMatrix(rows(), rhs.cols());

        for (int r = 0; r < rows(); r++) {

            for (int c = 0; c < rhs.cols(); c++) {

                for (int k = 0; k < cols(); k++) {
                    result.addToElement(r, c, get(r, k) * rhs.get(k, c));
                }

            }

        }

        return result;

    }

    public RMatrix multiply(RealMatrix rhs) {

        if (cols() != rhs.getRowDimension()) {
            throw new DimensionException(rhs.getRowDimension(), rhs.getColumnDimension(), cols(), -1);
        }

        return new RMatrix(backing.multiply(rhs));

    }

    @Override
    public RMatrix leftMultiply(Matrix<Double> lhs) {

        if (lhs instanceof RMatrix) {
            return ((RMatrix) lhs).multiply(this);
        } else {
            return new RMatrix(lhs.multiply(this));
        }

    }

    @Override
    public RMatrix multiply(Double rhs) {
        return map(v -> v * rhs);
    }

    @Override
    public RMatrix leftMultiply(Double lhs) {
        return map(v -> lhs * v);
    }

    @Override
    public RMatrix elementMultiply(Matrix<Double> rhs) {

        if (!Matrix.dimensionsMatch(this, rhs)) {
            throw new DimensionException(rhs, this);
        }

        return map((r, c, v) -> v * rhs.get(r, c));
    }

    @Override
    public RMatrix leftElementMultiply(Matrix<Double> lhs) {

        if (!Matrix.dimensionsMatch(this, lhs)) {
            throw new DimensionException(lhs, this);
        }

        return map((r, c, v) -> lhs.get(r, c) * v);
    }

    @Override
    public RMatrix elementDivide(Matrix<Double> rhs) {

        if (!Matrix.dimensionsMatch(this, rhs)) {
            throw new DimensionException(rhs, this);
        }

        return map((r, c, v) -> v / rhs.get(r, c));
    }

    @Override
    public RMatrix leftElementDivide(Matrix<Double> lhs) {

        if (!Matrix.dimensionsMatch(this, lhs)) {
            throw new DimensionException(lhs, this);
        }

        return map((r, c, v) -> lhs.get(r, c) / v);
    }

    @Override
    public RMatrix divide(Double rhs) {
        return map(v -> v / rhs);
    }

    @Override
    public RMatrix leftDivide(Double lhs) {
        return map(v -> lhs / v);
    }

    @Override
    public RMatrix add(Matrix<Double> rhs) {

        if (!Matrix.dimensionsMatch(this, rhs)) {
            throw new DimensionException(rhs, this);
        }

        return map((r, c, v) -> v + rhs.get(r, c));
    }

    @Override
    public RMatrix add(Double rhs) {
        return map(v -> v + rhs);
    }

    @Override
    public RMatrix subtract(Matrix<Double> rhs) {

        if (!Matrix.dimensionsMatch(this, rhs)) {
            throw new DimensionException(rhs, this);
        }

        return map((r, c, v) -> v - rhs.get(r, c));
    }

    @Override
    public RMatrix subtract(Double rhs) {
        return map(v -> v - rhs);
    }

    @Override
    public RMatrix map(EntryMapper<Double, Double> mapper) {

        RMatrix result = new RMatrix(rows(), cols());
        forEach((r, c, v) -> result.set(r, c, mapper.map(r, c, v)));
        return result;

    }

    @Override
    public RMatrix map(GFunction<Double, Double> mapper) {
        return map((r, c, v) -> mapper.value(v));
    }

    @Override
    public RMatrix copy() {
        return map(v -> v);
    }

    @Override
    public RMatrix getSubMatrix(int[] rows, int[] cols) {

        RMatrix subMatrix = new RMatrix(rows.length, cols.length);

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
    public RMatrix getSubMatrix(int startRow, int endRow, int startCol, int endCol) {
        return getSubMatrix(Util.makeCountingArray(startRow, endRow), Util.makeCountingArray(startCol, endCol));
    }

    @Override
    public RMatrix appendRows(Matrix<Double> rows) {

        if (rows.cols() != cols()) {
            throw new DimensionException(rows, -1, cols());
        }

        RMatrix newMatrix = new RMatrix(rows() + rows.rows(), cols());

        newMatrix.setSubMatrix(0, 0, this);
        newMatrix.setSubMatrix(rows(), 0, rows);

        return newMatrix;

    }

    @Override
    public RMatrix appendCols(Matrix<Double> cols) {

        if (cols.rows() != rows()) {
            throw new DimensionException(cols, cols(), -1);
        }

        RMatrix newMatrix = new RMatrix(rows(), cols() + cols.cols());

        newMatrix.setSubMatrix(0 ,0, this);
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
    public RMatrix invert() {

        if (!isSquare()) {
            throw new NonSquareException();
        }

        if (isSingular()) {
            throw new SingularException();
        }

        return new RMatrix(backing.inverse());

    }

    @Override
    public RMatrix transpose() {

        RMatrix result = new RMatrix(cols(), rows());
        forEach((r, c, v) -> result.set(c, r, v));
        return result;

    }

    @Override
    public RMatrix leftDivide(Matrix<Double> rhs) {

        if (isSingular()) {
            throw new SingularException();
        }

        if (rows() != rhs.rows()) {
            throw new DimensionException(rhs, rows(), -1);
        }

        if (rhs instanceof RMatrix) {
            return new RMatrix(backing.solve(((RMatrix) rhs).realMatrix()));
        } else {
            return new RMatrix(backing.solve(new RMatrix(rhs).realMatrix()));
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
                return new RMatrix(lu.getL());
            }

            @Override
            public Matrix<Double> getU() {
                return new RMatrix(lu.getU());
            }

            @Override
            public Matrix<Double> getP() {
                return new RMatrix(lu.getP());
            }
        };
    }

    @Override
    public RMatrix getRowSums() {

        RMatrix sums = new RMatrix(rows(), 1);
        forEach((r, c, v) -> sums.addToElement(r, 0, v));
        return sums;

    }

    @Override
    public RMatrix getColSums() {

        RMatrix sums = new RMatrix(1, cols());
        forEach((r, c, v) -> sums.addToElement(0, c, v));
        return sums;

    }

    public RMatrix getRowQuadratures() {

        RMatrix sums = new RMatrix(1, cols());
        forEach((r, c, v) -> sums.addToElement(0, c, v * v));
        sums.mapSelf(Math::sqrt);
        return sums;

    }

    public RMatrix getColQuadratures() {

        RMatrix sums = new RMatrix(1, cols());
        forEach((r, c, v) -> sums.addToElement(0, c, v * v));
        sums.mapSelf(Math::sqrt);
        return sums;

    }

    public static class Identity extends RMatrix {

        public Identity(int size) {
            super(size, size);
            setDiagonal(1.0);
        }

    }

    public static class Row extends RMatrix {

        public Row(Double... values) {
            super(1, values.length, values);
        }

        public Row(double... values) {
            super(1, values.length);
            for (int i = 0; i < values.length; i++) {
                set(0, i, values[i]);
            }
        }

    }

    public static class Col extends RMatrix {

        public Col(Double... values) {
            super(values.length, 1, values);
        }

        public Col(double... values) {
            super(values.length, 1);
            for (int i = 0; i < values.length; i++) {
                set(i, 0, values[i]);
            }
        }

    }

    public static class Rot2D extends RMatrix {

        public Rot2D(double theta) {

            super(2, 2,

                Math.cos(theta), -Math.sin(theta),
                Math.sin(theta), Math.cos(theta)

            );

        }

    }

    public static class QR implements jisa.maths.matrices.QR<Double> {

        private final QRDecomposition qr;

        public QR(QRDecomposition qr) {this.qr = qr;}

        @Override
        public RMatrix getQ() {
            return new RMatrix(qr.getQ());
        }

        @Override
        public RMatrix getR() {
            return new RMatrix(qr.getR());
        }

        @Override
        public RMatrix getQT() {
            return new RMatrix(qr.getQT());
        }

        @Override
        public RMatrix getH() {
            return new RMatrix(qr.getH());
        }

    }

}
