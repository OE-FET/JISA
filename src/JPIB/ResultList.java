package JPIB;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class ResultList implements Iterable<Result> {

    private String[]          names;
    private String[]          units   = null;
    private int               cols;
    private ArrayList<Result> results = new ArrayList<>();

    public ResultList(String... names) {
        this.names = names;
        this.cols = this.names.length;
    }

    public void setUnits(String... units) {

        if (units.length != cols) {
            throw new IndexOutOfBoundsException("Number of columns does not match.");
        }

        this.units = units;

    }

    public void addData(Double... data) {

        if (data.length != cols) {
            throw new IndexOutOfBoundsException("Number of columns does not match.");
        }

        results.add(
                new Result(data)
        );

    }

    public void output(String delim, PrintStream stream) {
        output(delim, delim, "", stream);
    }

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

    public String getTitle(int col) {

        if (units == null) {
            return names[col];
        } else {
            return String.format("%s [%s]", names[col], units[col]);
        }

    }

    public void outputTable(PrintStream stream) {

        int[] widths = new int[cols];

        for (int i = 0; i < cols; i++) {

            int max = getTitle(i).length();

            for (Result r : this) {
                max = Math.max(max, String.format("%f", r.get(i)).length());
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
