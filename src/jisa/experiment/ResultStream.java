package jisa.experiment;

import jisa.Util;

import java.io.*;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResultStream extends ResultTable {

    protected RandomAccessFile file;
    private   String[]         names;
    private   String[]         units = null;
    private   int              cols;

    public static ResultStream loadFile(String path) throws IOException {

        RandomAccessFile file = new RandomAccessFile(path, "rw");
        file.seek(0);

        String   header  = file.readLine();
        String[] columns = header.split(",");
        Col[]    cols    = new Col[columns.length];

        Pattern pattern = Pattern.compile("(.*)\\s\\[(.*)\\]");

        for (int i = 0; i < cols.length; i++) {

            Matcher matcher = pattern.matcher(columns[i]);
            Col     col;
            if (matcher.find()) {
                col = new Col(matcher.group(1), matcher.group(2));
            } else {
                col = new Col(columns[i]);
            }

            cols[i] = col;

        }

        return new ResultStream(file, cols);

    }

    public ResultStream(String path, Col... columns) throws IOException {
        super(columns);
        init(path);
    }

    public ResultStream(String path, String... names) throws IOException {
        super(names);
        init(path);
    }

    public ResultStream(RandomAccessFile file, Col... columns) throws IOException {
        super(columns);
        this.file = file;
        this.file.seek(0);
    }

    private void init(String path) throws IOException {

        file = new RandomAccessFile(path, "rw");
        file.setLength(0);
        file.seek(0);
        file.writeBytes(String.join(",", getNames()));
        file.writeBytes("\n");

    }

    @Override
    public void updateColumns() {

        if (!open) {
            throw new IllegalStateException("You cannot alter a finalised ResultTable");
        }

        replaceLine(0, String.join(",", String.join(",", getNames())));

    }

    protected void replaceLine(int lineNo, String newLine) {

        StringBuilder newFile = new StringBuilder();

        try {

            file.seek(0);

            int    i    = 0;
            String line = file.readLine();

            do {

                if (i++ != lineNo) {
                    newFile.append(line);
                } else {
                    newFile.append(newLine);
                }

                newFile.append("\n");

                line = file.readLine();

            } while (line != null);

            file.setLength(0);
            file.writeBytes(newFile.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    protected void removeLine(int lineNo) {

        StringBuilder newFile = new StringBuilder();

        try {

            file.seek(0);

            int    i    = 0;
            String line = file.readLine();

            do {

                if (i++ != lineNo) {
                    newFile.append(line);
                    newFile.append("\n");
                }

                line = file.readLine();

            } while (line != null);

            file.setLength(0);
            file.writeBytes(newFile.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    protected void addBefore(int lineNo, String newLine) {

        StringBuilder newFile = new StringBuilder();

        try {

            file.seek(0);

            int    i    = 0;
            String line = file.readLine();

            do {

                if (i != lineNo) {

                    newFile.append(line);

                } else {

                    newFile.append(newLine);
                    newFile.append("\n");
                    newFile.append(line);

                }

                newFile.append("\n");
                i++;

                line = file.readLine();

            } while (line != null);

            file.setLength(0);
            file.writeBytes(newFile.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void addRow(Result row) {

        try {

            file.seek(file.length());
            file.writeBytes(row.getOutput(","));

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void clearData() {

        try {

            file.setLength(0);
            file.seek(0);
            String[] titles = new String[getNumCols()];

            for (int i = 0; i < getNumCols(); i++) {
                titles[i] = getTitle(i);
            }

            file.writeBytes(String.join(",", titles));
            file.writeBytes("\n");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public int getNumRows() {

        int count = 0;

        try {

            file.seek(0);

            while (file.readLine() != null) {
                count++;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return count - 1;

    }

    @Override
    public Result getRow(int i) {

        try {

            file.seek(0);
            file.readLine();

            for (int j = 0; j < i; j++) {
                file.readLine();
            }

            String[] values = file.readLine().split(",");
            double[] dVals  = new double[values.length];

            for (int j = 0; j < values.length; j++) {
                dVals[j] = Double.parseDouble(values[j]);
            }

            return new Result(dVals);

        } catch (IOException e) {

            e.printStackTrace();
            return null;

        }
    }

    @Override
    public void removeRow(int i) {
        removeLine(i);
    }

    @Override
    public void close() {

        try {
            file.close();
        } catch (IOException e) {
            Util.exceptionHandler(e);
        }

    }

    @Override
    public Iterator<Result> iterator() {

        return new Iterator<Result>() {

            int rows = getNumRows();
            int row  = 0;

            @Override
            public boolean hasNext() {
                return row < rows;
            }

            @Override
            public Result next() {
                return getRow(row++);
            }

        };

    }

}
