package JISA.Experiment;

import JISA.GUI.Clearable;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public abstract class ResultTable implements Iterable<Result> {

    protected ArrayList<OnUpdate>  onUpdate     = new ArrayList<>();
    protected ArrayList<Clearable> toClear      = new ArrayList<>();
    protected ArrayList<Evaluable> extraColumns = new ArrayList<>();
    protected boolean              open         = true;

    /**
     * Sets the units for each column in the result table
     *
     * @param units Units
     */
    public abstract void setUnits(String... units);

    /**
     * Returns the name of the column with the given number.
     *
     * @param i Column number
     *
     * @return Name of the column
     */
    public abstract String getName(int i);

    public abstract String getUnits(int i);

    public abstract String[] getNames();

    public abstract boolean hasUnits();

    public String getTitle(int i) {

        if (hasUnits()) {
            return String.format("%s [%s]", getName(i), getUnits(i));
        } else {
            return getName(i);
        }

    }

    public void addData(double... data) {

        if (!open) {
            throw new IllegalStateException("You cannot add data to a finalised ResultTable");
        }

        Result row = new Result(data);
        addRow(row);

        for (OnUpdate r : (List<OnUpdate>) onUpdate.clone()) {
            r.run(row);
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

    public abstract int getNumCols();

    public abstract Result getRow(int i);

    public Result getLastResult() {
        return getRow(getNumRows() - 1);
    }

    public void addClearable(Clearable c) {
        toClear.add(c);
    }

    public void addOnUpdate(OnUpdate o) {
        onUpdate.add(o);
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

        if (hasUnits()) {
            String[] titles = new String[getNumCols()];

            for (int i = 0; i < getNumCols(); i++) {
                titles[i] = getTitle(i);
            }

            stream.print(String.join(delim, titles));

        } else {
            stream.print(String.join(delim, getNames()));
        }


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

    public List<XYPoint> getXYPoints(int xData, int yData, Predicate<Result> filter) {

        List<XYPoint> points = new LinkedList<>();

        for (Result r : this) {
            if (filter.test(r)) {
                points.add(new XYPoint(r.get(xData), r.get(yData)));
            }
        }

        return points;

    }

    public Function polyFit(int xData, int yData, Predicate<Result> filter, int degree) {
        return Maths.polyFit(getXYPoints(xData, yData, filter), degree);
    }

    public Function polyFit(int xData, int yData, int degree) {
        return polyFit(xData, yData, (r) -> true, degree);
    }

    public Function fit(int xData, int yData, Predicate<Result> filter, PFunction toFit, double... initialGuess) {
        return Maths.fit(getXYPoints(xData, yData, filter), toFit, initialGuess);
    }

    public Function fit(int xData, int yData, PFunction toFit, double... initialGuess) {
        return fit(xData, yData, (r) -> true, toFit, initialGuess);
    }

    public Function asFunction(int xData, int yData, Predicate<Result> filter) {
        return new DataFunction(getXYPoints(xData, yData, filter));
    }

    public Function asFunction(int xData, int yData) {
        return asFunction(xData, yData, (r) -> true);
    }

    public void finalise() {
        open = false;
        close();
    }

    public abstract void close();

    public ResultTable getColumns(int... columns) {
        return new SubTable(this, columns);
    }

    public interface OnUpdate {

        void run(Result row);

    }

    public interface Evaluable {

        double evaluate(Result r);

    }

}
