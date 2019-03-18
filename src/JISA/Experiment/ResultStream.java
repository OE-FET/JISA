package JISA.Experiment;

import java.io.*;
import java.util.Iterator;

public class ResultStream extends ResultTable {

    private   String           path;
    protected RandomAccessFile file;
    private   String[]         names;
    private   String[]         units = null;
    private   int              cols;

    public ResultStream(String path, String... names) throws IOException {

        this.path = path;
        this.names = names;
        cols = this.names.length;
        file = new RandomAccessFile(path, "rw");
        file.setLength(0);
        file.seek(0);

        String[] titles = new String[getNumCols()];

        for (int i = 0; i < getNumCols(); i++) {
            titles[i] = getTitle(i);
        }

        file.writeBytes(String.join(",", titles));
        file.writeBytes("\n");

    }

    @Override
    public void setUnits(String... units) {

        if (units.length != cols) {
            return;
        }

        this.units = units;

        String[] titles = new String[getNumCols()];

        for (int i = 0; i < getNumCols(); i++) {
            titles[i] = getTitle(i);
        }

        replaceLine(0, String.join(",", titles));

    }

    protected void replaceLine(int lineNo, String newLine) {

        StringBuilder newFile = new StringBuilder();

        try {
            int i = 0;
            file.seek(0);
            String line;

            line = file.readLine();
            do {

                if (i != lineNo) {
                    newFile.append(line);
                } else {
                    newFile.append(newLine);
                }
                newFile.append("\n");
                i++;

                line = file.readLine();
            } while (line != null);

            file.setLength(0);
            file.writeBytes(newFile.toString());

        } catch (IOException ignored) {
        }

    }

    protected void addBefore(int lineNo, String newLine) {

        StringBuilder newFile = new StringBuilder();

        try {
            int i = 0;
            file.seek(0);
            String line;

            line = file.readLine();
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

        } catch (IOException ignored) {
        }

    }

    @Override
    public String getName(int i) {
        return names[i];
    }

    @Override
    public String getUnits(int i) {
        return units[i];
    }

    @Override
    public String[] getNames() {
        return names.clone();
    }

    @Override
    public boolean hasUnits() {
        return units != null;
    }

    @Override
    protected void addRow(Result row) {
        try {
            file.seek(file.length());
            file.writeBytes(row.getOutput(","));
        } catch (IOException ignored) {

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
        } catch (IOException ignored) {

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

        } catch (IOException ignored) {
        }

        return count - 1;

    }

    @Override
    public int getNumCols() {
        return cols;
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
                dVals[j] = Double.valueOf(values[j]);
            }
            return new Result(dVals);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public Iterator<Result> iterator() {

        return new Iterator<Result>() {

            int rows = getNumRows();
            int row = 0;

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
