package jisa.experiment;

import jisa.Util;
import jisa.gui.Clearable;
import jisa.maths.fits.*;
import jisa.maths.functions.Function;
import jisa.maths.functions.PFunction;
import jisa.maths.matrices.Matrix;
import jisa.maths.matrices.RealMatrix;
import org.json.JSONObject;

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

    public abstract void setAttribute(String name, String value);

    public void setAttribute(String name, double value) {
        setAttribute(name, String.format("%s", value));
    }

    public abstract String getAttribute(String name);

    public double getAttributeDouble(String name) {
        return Double.parseDouble(getAttribute(name));
    }

    public abstract Map<String, String> getAttributes();

    public int getColumnFromString(String name) {

        name = name.trim();

        int i = 0;
        for (Col col : columns) {
            if (col.getName().trim().equals(name.trim())) {
                return i;
            }
            i++;
        }

        return -1;

    }

    public int getColumnFromCol(Col column) {
        return columns.indexOf(column);
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

    protected abstract void updateColumns();

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
        Result row = new Result(this, fullData);

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

        int size = onUpdate.size();
        for (int k = 0; k < size; k++) {
            onUpdate.get(k).run(row);
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

        stream.println();
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

        stream.println();

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

            stream.println();
            stream.print("+");

            for (int w : widths) {

                for (int i = 0; i < w + 2; i++) {
                    stream.print("-");
                }

                stream.print("+");

            }

            stream.println();

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


        String[]   titles = getNames();
        stream.println("% ATTRIBUTES: " + (new JSONObject(getAttributes())).toString());
        stream.println(String.join(delim, titles));

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

    public LinearFit linearFit(int xData, int yData) {
        return Fitting.linearFit(this, xData, yData);
    }

    public PolyFit polyFit(int xData, int yData, int degree) {
        return Fitting.polyFit(this, xData, yData, degree);
    }

    public GaussianFit gaussianFit(int xData, int yData) {
        return Fitting.gaussianFit(this, xData, yData);
    }

    public CosFit cosFit(int xData, int yData) {
        return Fitting.cosFit(this, xData, yData);
    }

    public Fit fit(int xData, int yData, PFunction toFit, double... initialGuess) {
        return Fitting.fit(this, xData, yData, toFit, initialGuess);
    }

    public Function asFunction(int xData, int yData) {
        return new DataFunction(getXYPoints(xData, yData));
    }

    public void finalise() {
        open = false;
        onUpdate.clear();
        close();
    }

    public abstract void close();

    public RealMatrix toMatrix() {

        RealMatrix result = new RealMatrix(getNumRows(), getNumCols());

        for (int i = 0; i < getNumRows(); i++) {
            Result r = getRow(i);
            result.setRow(i, r.getData());
        }

        return result;

    }

    public RealMatrix getColumns(int... columns) {

        RealMatrix result = new RealMatrix(Math.max(1, getNumRows()), Math.max(1, columns.length));

        int i = 0;
        for (Result r : this) {

            for (int j = 0; j < columns.length; j++) {
                result.set(i, j, r.get(columns[j]));
            }

            i++;

        }

        return result;

    }

    public RealMatrix getRows(int... rows) {

        RealMatrix result = new RealMatrix(rows.length, getNumCols());

        for (int i = 0; i < rows.length; i++) {

            Result r = getRow(rows[i]);

            for (int j = 0; j < getNumCols(); j++) {
                result.set(i, j, r.get(j));
            }

        }

        return result;

    }

    public double getMax(Evaluable value) {

        if (getNumRows() < 1) {
            throw new IllegalStateException("Cannot find maximum in empty table!");
        }

        double max = Double.NEGATIVE_INFINITY;

        for (Result row : this) {
            max = Math.max(max, value.evaluate(row));
        }

        return max;

    }

    public double getMax(int column) {
        return getMax(r -> r.get(column));
    }

    public double getMin(Evaluable value) {

        if (getNumRows() < 1) {
            throw new IllegalStateException("Cannot find minimum in empty table!");
        }

        double min = Double.POSITIVE_INFINITY;

        for (Result row : this) {
            min = Math.min(min, value.evaluate(row));
        }

        return min;

    }

    public double getMin(int column) {
        return getMin(r -> r.get(column));
    }

    public double getMean(Evaluable value) {

        if (getNumRows() < 1) {
            throw new IllegalStateException("Cannot find mean in empty table!");
        }

        double sum   = 0;
        int    count = 0;

        for (Result row : this) {
            sum += value.evaluate(row);
            count++;
        }

        return sum / count;

    }

    public double getMean(int column) {
        return getMean(r -> r.get(column));
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

    public ResultTable sortedCopy(Evaluable sortBy) {

        List<Map.Entry<Integer, Double>> toSort = new ArrayList<>(getNumRows());

        int i = 0;
        for (Result row : this) {
            toSort.add(Map.entry(i++, sortBy.evaluate(row)));
        }

        toSort.sort(Map.Entry.comparingByValue());

        ResultTable newCopy = new ResultList(columns.toArray(new Col[0]));

        for (Map.Entry<Integer, Double> entry : toSort) {
            newCopy.addRow(getRow(entry.getKey()));
        }

        return newCopy;

    }

    public FwdBwd splitTwoWaySweep(Evaluable swept) {

        int         mode      = -2;
        double      lastValue = swept.evaluate(getRow(0));
        ResultTable forward   = new ResultList(columns.toArray(new Col[0]));
        ResultTable backward  = new ResultList(columns.toArray(new Col[0]));

        ResultTable current = forward;

        current.addRow(getRow(0));

        for (int i = 1; i < getNumRows(); i++) {

            Result row   = getRow(i);
            double value = swept.evaluate(row);

            if (mode == -2) {

                mode = Double.compare(value, lastValue);
                current.addRow(row);

            } else {

                int newMode = Double.compare(value, lastValue);

                if (newMode != mode) {
                    current = backward;
                }

                current.addRow(row);
                mode = newMode;

            }

            lastValue = value;

        }

        return new FwdBwd(forward, backward);

    }

    public ResultTable sortedCopy(int column) {
        return sortedCopy(r -> r.get(column));
    }

    public ResultTable flippedCopy() {

        ResultTable newCopy = new ResultList(columns.toArray(new Col[0]));

        for (int i = getNumRows() - 1; i >= 0; i--) {
            newCopy.addRow(getRow(i));
        }

        return newCopy;

    }

    public Set<Double> getUniqueValues(Evaluable column) {

        Set<Double> valueSet = new HashSet<>();

        for (Result r : this) {

            valueSet.add(column.evaluate(r));

        }

        return valueSet;

    }

    public Set<Double> getUniqueValues(int column) {
        return getUniqueValues(r -> r.get(column));
    }

    public Map<Double, ResultTable> split(Evaluable splitBy) {

        Set<Double>              values = getUniqueValues(splitBy);
        Map<Double, ResultTable> map    = new TreeMap<>();

        for (double value : values) {
            map.put(value, filteredCopy(r -> splitBy.evaluate(r) == value));
        }

        return map;

    }

    public Map<Double, ResultTable> split(int column) {
        return split(r -> r.get(column));
    }

    public interface OnUpdate {

        void run(Result row);

    }

    public interface Evaluable {

        double evaluate(Result r);

    }

    public static class FwdBwd {

        public final ResultTable forward;
        public final ResultTable backward;


        public FwdBwd(ResultTable forward, ResultTable backward) {
            this.forward  = forward;
            this.backward = backward;
        }

    }

}
