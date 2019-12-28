package jisa.experiment;

import jisa.Util;
import jisa.gui.Clearable;
import jisa.maths.fits.Fit;
import jisa.maths.fits.Fitting;
import jisa.maths.functions.Function;
import jisa.maths.functions.PFunction;
import jisa.maths.matrices.Matrix;
import jisa.maths.matrices.RMatrix;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.function.Predicate;

public abstract class ResultTable implements Iterable<Result> {

    protected ArrayList<OnUpdate>  onUpdate     = new ArrayList<>();
    protected ArrayList<Clearable> toClear      = new ArrayList<>();
    protected ArrayList<Evaluable> extraColumns = new ArrayList<>();
    protected boolean              open         = true;
    protected ArrayList<Col>       columns      = new ArrayList<>();

    public ResultTable(Col... columns) {
        this.columns.addAll(Arrays.asList(columns));
    }

    public ResultTable(String... names) {

        for (String name : names) {
            columns.add(new Col(name));
        }

    }


    /**
     * Sets the units for each column in the result table
     *
     * @param units Units
     */
    public void setUnits(String... units) {

        for (int i = 0; i < Math.min(columns.size(), units.length); i++) {
            columns.get(i).setUnit(units[i]);
        }

        updateColumns();

    }

    public abstract void updateColumns();

    /**
     * Returns the name of the column with the given number.
     *
     * @param i Column number
     *
     * @return Name of the column
     */
    public String getName(int i) {
        return columns.get(i).getName();
    }

    public String getUnits(int i) {
        return columns.get(i).getUnit();
    }

    public Col getColumn(int i) {
        return columns.get(i);
    }

    public String[] getNames() {

        String[] names = new String[columns.size()];

        for (int i = 0; i < names.length; i++) {
            names[i] = getTitle(i);
        }

        return names;

    }

    public String getTitle(int i) {

        Col column = columns.get(i);

        if (column.hasUnit()) {
            return String.format("%s [%s]", column.getName(), column.getUnit());
        } else {
            return column.getName();
        }

    }

    public void addData(double... data) {

        if (!open) {
            throw new IllegalStateException("You cannot add data to a finalised ResultTable");
        }

        int i = 0;

        double[] fullData = new double[columns.size()];

        for (int j = 0; j < fullData.length; j++) {

            if (columns.get(j).isFunction()) {
                fullData[j] = 0.0;
            } else {
                fullData[j] = data[i];
                i++;
            }

        }
        Result row = new Result(fullData);

        for (int j = 0; j < fullData.length; j++) {

            if (columns.get(j).isFunction()) {
                try {
                    fullData[j] = columns.get(j).getFunction().evaluate(row);
                } catch (Throwable e) {
                    fullData[j] = Double.NaN;
                }
            }

        }

        addRow(row);

        for (OnUpdate r : (List<OnUpdate>) onUpdate.clone()) {
            r.run(row);
        }

    }

    public void addData(Matrix<Double> m) {

        if (m.cols() != getNumCols()) {
            return;
        }

        for (int i = 0; i < m.rows(); i++) {
            addData(Util.primitiveArray(m.getRowArray(i)));
        }

    }

    protected abstract void addRow(Result row);

    public void clear() {

        if (!open) {
            throw new IllegalStateException("You cannot remove data from a finalised ResultTable");
        }

        clearData();
        for (Clearable c : toClear) {
            c.clear();
        }

    }

    protected abstract void clearData();

    public abstract int getNumRows();

    public int getNumCols() {
        return columns.size();
    }

    public abstract Result getRow(int i);

    public abstract void removeRow(int i);

    public Result getLastResult() {
        return getRow(getNumRows() - 1);
    }

    public void addClearable(Clearable c) {
        toClear.add(c);
    }

    public OnUpdate addOnUpdate(OnUpdate o) {
        onUpdate.add(o);
        return o;
    }

    public void removeOnUpdate(OnUpdate o) {
        onUpdate.remove(o);
    }

    /**
     * Output the data as a formatted ASCII table to the given stream.
     *
     * @param stream Output stream
     */
    public void outputTable(PrintStream stream) {

        int cols = getNumCols();

        int[] widths = new int[cols];

        for (int i = 0; i < cols; i++) {

            int max = getTitle(i).length();

            for (Result r : this) {
                max = Math.max(max, String.format("%e", r.get(i)).length());
            }

            widths[i] = max;

        }

        stream.print("+");

        for (int w : widths) {

            for (int i = 0; i < w + 2; i++) {
                stream.print("=");
            }

            stream.print("+");

        }

        stream.print("\n");
        stream.print("|");

        for (int i = 0; i < cols; i++) {

            stream.print(" ");
            String title = getTitle(i);
            stream.print(title);

            for (int n = 0; n < widths[i] - title.length(); n++) {
                stream.print(" ");
            }

            stream.print(" |");

        }

        stream.print("\n+");

        for (int w : widths) {

            for (int i = 0; i < w + 2; i++) {
                stream.print("=");
            }

            stream.print("+");

        }

        stream.print("\n");

        for (Result r : this) {

            stream.print("|");

            for (int i = 0; i < cols; i++) {

                stream.print(" ");
                String title = String.format("%f", r.get(i));
                stream.print(title);

                for (int n = 0; n < widths[i] - title.length(); n++) {
                    stream.print(" ");
                }

                stream.print(" |");

            }

            stream.print("\n+");

            for (int w : widths) {

                for (int i = 0; i < w + 2; i++) {
                    stream.print("-");
                }

                stream.print("+");

            }

            stream.print("\n");

        }

    }

    /**
     * Output the data as a formatted ASCII table to the file with the given path.
     *
     * @param path File path
     *
     * @throws IOException Upon writing error
     */
    public void outputTable(String path) throws IOException {
        FileOutputStream f = new FileOutputStream(path);
        PrintStream      s = new PrintStream(f);
        outputTable(s);
        f.close();
        s.close();
    }

    public void outputTable() {
        outputTable(System.out);
    }

    public void outputMATLAB(PrintStream stream, String... variables) {

        if (variables.length != getNumCols()) {
            throw new IndexOutOfBoundsException("Number of columns does not match");
        }

        for (int i = 0; i < getNumCols(); i++) {

            ArrayList<String> numbers = new ArrayList<>();

            for (Result r : this) {
                numbers.add(String.format("%f", r.get(i)));
            }

            stream.printf("%s = [%s];\n", variables[i], String.join(" , ", numbers));

        }

    }

    public void outputMATLAB(String path, String... variables) throws IOException {
        FileOutputStream f = new FileOutputStream(path);
        PrintStream      s = new PrintStream(f);
        outputMATLAB(s);
        f.close();
        s.close();
    }

    public void output(String delim, PrintStream stream) {


        String[] titles = getNames();
        stream.print(String.join(delim, titles));
        stream.print("\n");

        for (Result r : this) {
            r.output(stream, delim);
        }

    }

    public void output(PrintStream stream) {
        output(",", stream);
    }

    public void output(String delim, String path) throws IOException {
        FileOutputStream f = new FileOutputStream(path);
        PrintStream      s = new PrintStream(f);
        output(delim, s);
        f.close();
        s.close();
    }

    public void output(String path) throws IOException {
        output(",", path);
    }

    public void output() {
        output(",", System.out);
    }

    public List<XYPoint> getXYPoints(int xData, int yData) {

        List<XYPoint> points = new LinkedList<>();

        for (Result r : this) {
            points.add(new XYPoint(r.get(xData), r.get(yData)));

        }

        return points;

    }

    public Fit polyFit(int xData, int yData, int degree) {
        return Fitting.polyFit(getColumns(xData), getColumns(yData), degree);
    }

    public Fit fit(int xData, int yData, PFunction toFit, double... initialGuess) {
        return Fitting.fit(getColumns(xData), getColumns(yData), toFit, initialGuess);
    }

    public Function asFunction(int xData, int yData) {
        return new DataFunction(getXYPoints(xData, yData));
    }

    public void finalise() {
        open = false;
        close();
    }

    public abstract void close();

    public RMatrix toMatrix() {

        RMatrix result = new RMatrix(getNumRows(), getNumCols());

        for (int i = 0; i < getNumRows(); i++) {
            Result r = getRow(i);
            result.setRow(i, r.getData());
        }

        return result;

    }

    public RMatrix getColumns(int... columns) {

        RMatrix result = new RMatrix(Math.max(1, getNumRows()), Math.max(1, columns.length));

        int i = 0;
        for (Result r : this) {

            for (int j = 0; j < columns.length; j++) {
                result.set(i, j, r.get(columns[j]));
            }

            i++;

        }

        return result;

    }

    public RMatrix getRows(int... rows) {

        RMatrix result = new RMatrix(rows.length, getNumCols());

        for (int i = 0; i < rows.length; i++) {

            Result r = getRow(rows[i]);

            for (int j = 0; j < getNumCols(); j++) {
                result.set(i, j, r.get(j));
            }

        }

        return result;

    }

    public ResultTable filteredCopy(Predicate<Result> filter) {

        ResultTable newCopy = new ResultList(columns.toArray(new Col[0]));

        for (Result row : this) {

            if (filter.test(row)) {
                newCopy.addRow(row);
            }

        }

        return newCopy;

    }

    public Set<Double> getUniqueValues(int column) {

        Set<Double> valueSet = new HashSet<>();

        for (Result r : this) {

            valueSet.add(r.get(column));

        }

        return valueSet;

    }

    public ResultTable filtered(Predicate<Result> filter) {

        return new ResultTable(columns.toArray(new Col[0])) {

            @Override
            public void updateColumns() {
                ResultTable.this.updateColumns();
            }

            public OnUpdate addOnUpdate(OnUpdate onUpdate) {

                return ResultTable.this.addOnUpdate(r -> {
                    if (filter.test(r)) {
                        onUpdate.run(r);
                    }
                });

            }

            public void addData(double... data) {
                ResultTable.this.addData(data);
            }

            @Override
            protected void addRow(Result row) {
                ResultTable.this.addRow(row);
            }

            @Override
            protected void clearData() {

                for (int i = ResultTable.this.getNumRows() - 1; i >= 0; i--) {

                    if (filter.test(ResultTable.this.getRow(i))) {
                        ResultTable.this.removeRow(i);
                    }

                }

            }

            @Override
            public int getNumRows() {

                int count = 0;

                for (Result row : this) {
                    count++;
                }

                return count;

            }

            @Override
            public Result getRow(int i) {
                return ResultTable.this.getRow(getRealRowIndex(i));
            }

            private int getRealRowIndex(int i) {

                int j = -1;

                for (int r = 0; r < ResultTable.this.getNumRows(); r++) {

                    if (filter.test(ResultTable.this.getRow(r))) {
                        j++;
                    }

                    if (j == i) {
                        return r;
                    }

                }

                throw new IndexOutOfBoundsException("Index out of bounds.");

            }

            @Override
            public void removeRow(int i) {
                ResultTable.this.removeRow(getRealRowIndex(i));
            }

            @Override
            public void close() {

            }

            @Override
            public Iterator<Result> iterator() {

                return new Iterator<Result>() {

                    private int i = getRealRowIndex(0);
                    private int j = 0;
                    private boolean hasNext = true;

                    @Override
                    public boolean hasNext() {
                        return hasNext;
                    }

                    @Override
                    public Result next() {
                        Result row = ResultTable.this.getRow(i);
                        try {
                            i = getRealRowIndex(++j);
                        } catch (Exception e) {
                            hasNext = false;
                        }
                        return row;
                    }

                };

            }

        };

    }

    public interface OnUpdate {

        void run(Result row);

    }

    public interface Evaluable {

        double evaluate(Result r);

    }

}
