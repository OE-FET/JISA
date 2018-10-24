package JISA.Experiment;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * Table-like structure for holding numerical data
 */
public class ResultList implements Iterable<Result> {

    private String[]            names;
    private String[]            units    = null;
    private int                 cols;
    private ArrayList<Result>   results  = new ArrayList<>();
    private ArrayList<Runnable> onUpdate = new ArrayList<>();

    /**
     * Merges multiple lists, side-by-side, into a single list.
     *
     * @param lists Lists to merge
     *
     * @return The merged list
     */
    public static ResultList mergeLists(ResultList... lists) {

        if (lists.length == 0) {
            return null;
        }

        ArrayList<String> columns = new ArrayList<>();
        ArrayList<String> units   = new ArrayList<>();
        int               maxRows = 0;

        for (ResultList list : lists) {
            columns.addAll(Arrays.asList(list.names));
            if (list.units == null) {
                for (String name : list.names) {
                    units.add("");
                }
            } else {
                units.addAll(Arrays.asList(list.units));
            }

            maxRows = Math.max(maxRows, list.getNumRows());

        }

        ResultList newList = new ResultList(columns.toArray(new String[0]));
        newList.setUnits(units.toArray(new String[0]));

        ArrayList<Double> data = new ArrayList<>();
        for (int i = 0; i < maxRows; i++) {

            data.clear();

            for (ResultList list : lists) {

                Result r = list.getRow(i);

                if (r == null) {
                    for (String name : list.names) {
                        data.add(0.0);
                    }
                } else {
                    data.addAll(Arrays.asList(r.getData()));
                }

            }

            newList.addData(data.toArray(new Double[0]));

        }

        return newList;

    }

    /**
     * Creates a ResultList with the specified columns.
     *
     * @param names The names of the columns
     */
    public ResultList(String... names) {
        this.names = names;
        this.cols = this.names.length;
    }

    /**
     * Set action to perform each time the list is updated.
     *
     * @param onUpdate Runnable to run when updated
     */
    public void setOnUpdate(Runnable onUpdate) {
        this.onUpdate.add(onUpdate);
    }

    private void doUpdate() {
        for (Runnable r : onUpdate) {
            r.run();
        }
    }

    /**
     * Sets the units of each column.
     *
     * @param units The units for the columns
     */
    public void setUnits(String... units) {

        if (units.length != cols) {
            throw new IndexOutOfBoundsException("Number of columns does not match.");
        }

        this.units = units;
        doUpdate();

    }

    /**
     * Add a new row of data.
     *
     * @param data Row of data
     */
    public void addData(Double... data) {

        if (data.length != cols) {
            throw new IndexOutOfBoundsException("Number of columns does not match.");
        }

        results.add(
                new Result(data)
        );

        doUpdate();

    }

    /**
     * Output as a CSV file to the specified file.
     *
     * @param filePath Output file path
     *
     * @throws IOException Upon writing error
     */
    public void output(String filePath) throws IOException {

        FileOutputStream f = new FileOutputStream(filePath);
        PrintStream      s = new PrintStream(f);
        output(",", s);
        f.close();
        s.close();

    }

    /**
     * Output as a delimiter-separated string to the given stream.
     *
     * @param delim  Delimiter
     * @param stream Output stream
     */
    public void output(String delim, PrintStream stream) {
        output(delim, delim, "", stream);
    }

    /**
     * Output as a delimiter-separated string, with separated header delimiter and line-start to the given stream.
     *
     * @param delim       Delimiter
     * @param headerDelim Delimiter for headers
     * @param headerStart Starting character(s) for headers
     * @param stream      Output stream
     */
    public void output(String delim, String headerDelim, String headerStart, PrintStream stream) {

        stream.print(headerStart);

        if (units == null) {
            stream.print(String.join(headerDelim, names));
        } else {
            String[] titles = new String[cols];

            for (int i = 0; i < cols; i++) {

                titles[i] = String.format("%s [%s]", names[i], units[i]);

            }

            stream.print(String.join(headerDelim, titles));

        }


        stream.print("\n");

        for (Result r : this) {
            r.output(stream, delim);
        }

    }

    /**
     * Returns the row of data, as a Result object, with the given index.
     *
     * @param i Index
     *
     * @return Row with index, i
     */
    public Result getRow(int i) {

        if (i >= results.size()) {
            return null;
        } else {
            return results.get(i);
        }

    }

    /**
     * Returns the last row of data that was added.
     *
     * @return Last row
     */
    public Result getLastRow() {
        return results.get(results.size() - 1);
    }

    /**
     * Returns the number of columns in the list.
     *
     * @return Number of columns
     */
    public int getNumCols() {
        return names.length;
    }

    /**
     * Returns the number of rows in the list.
     *
     * @return Number of rows
     */
    public int getNumRows() {
        return results.size();
    }

    /**
     * Returns the fully formatted title (including units) of the specified column.
     *
     * @param col Column index
     *
     * @return Title of column with units
     */
    public String getTitle(int col) {

        if (units == null) {
            return names[col];
        } else {
            return String.format("%s [%s]", names[col], units[col]);
        }

    }

    public String getUnits(int col) {

        return units[col];

    }

    /**
     * Output the data as a formatted ASCII table to the given stream.
     *
     * @param stream Output stream
     */
    public void outputTable(PrintStream stream) {

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

    /**
     * Output the data as a formatted ASCII table to the standard out stream (ie terminal window).
     */
    public void outputTable() {
        outputTable(System.out);
    }

    /**
     * Output a MATLAB script to the given stream which, when run, will load the data in this table into the specified
     * MATLAB variables.
     *
     * @param stream    Output stream
     * @param variables Variable names to load into (in column order)
     */
    public void outputMATLAB(PrintStream stream, String... variables) {

        if (variables.length != cols) {
            throw new IndexOutOfBoundsException("Number of columns does not match");
        }

        for (int i = 0; i < cols; i++) {

            ArrayList<String> numbers = new ArrayList<>();

            for (Result r : this) {
                numbers.add(String.format("%f", r.get(i)));
            }

            stream.printf("%s = [%s];\n", variables[i], String.join(" , ", numbers));

        }

    }

    /**
     * Output a MATLAB script to the given stream which, when run, will load the data in this table into the specified
     * MATLAB variables.
     *
     * @param path      Output file path
     * @param variables Variable names to load into (in column order)
     */
    public void outputMATLAB(String path, String... variables) throws IOException {
        FileOutputStream f = new FileOutputStream(path);
        PrintStream      s = new PrintStream(f);
        outputMATLAB(s, variables);
        f.close();
        s.close();
    }

    @Override
    public Iterator<Result> iterator() {
        return results.iterator();
    }

    @Override
    public void forEach(Consumer<? super Result> action) {
        results.forEach(action);
    }

    @Override
    public Spliterator<Result> spliterator() {
        return results.spliterator();
    }
}
