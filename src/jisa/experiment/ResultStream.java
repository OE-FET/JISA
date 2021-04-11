package jisa.experiment;

import jisa.Util;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResultStream extends ResultTable {

    protected RandomAccessFile file;
    private   String           path;
    private   String[]         names;
    private   String[]         units       = null;
    private   JSONObject       attributes  = new JSONObject();
    private   int              cols;
    private   int              currentLine = 0;

    public ResultStream(String path, Col... columns) throws IOException {
        super(columns);
        init(path);
    }

    public ResultStream(String path, String... names) throws IOException {
        super(names);
        init(path);
    }

    public ResultStream(String path, RandomAccessFile file, JSONObject attributes, Col... columns) throws IOException {
        super(columns);
        this.path = path;
        this.file = file;
        this.file.seek(0);
    }

    public ResultStream(Col... columns) throws IOException {

        super(columns);

        File file = File.createTempFile("JISA-data-", ".csv");
        file.deleteOnExit();
        init(file.getAbsolutePath());

    }

    /**
     * Creates a ResultStream from a previously written CSV backing file.
     *
     * @param path Path to CSV file.
     *
     * @return ResultStream created from column headers in file.
     *
     * @throws IOException
     */
    public static ResultStream loadFile(String path) throws IOException {

        RandomAccessFile file = new RandomAccessFile(path, "rw");
        file.seek(0);

        String     header     = file.readLine();
        JSONObject attributes = header.startsWith("% ATTRIBUTES: ") ? new JSONObject(header.replaceFirst("% ATTRIBUTES: ", "")) : null;

        String[] columns = header.split(",");
        Col[]    cols    = new Col[columns.length];

        Pattern pattern = Pattern.compile("(.*)\\s\\[(.*)\\]");

        for (int i = 0; i < cols.length; i++) {

            Matcher matcher = pattern.matcher(columns[i]);
            Col     col     = matcher.find() ? new Col(matcher.group(1), matcher.group(2)) : new Col(columns[i]);

            cols[i] = col;

        }

        if (attributes == null) {

            ResultStream stream = new ResultStream(path, file, new JSONObject(), cols);
            stream.addBefore(0, "% ATTRIBUTES: {}");
            return stream;

        } else {
            return new ResultStream(path, file, attributes, cols);
        }

    }

    @Override
    public void setAttribute(String name, String value) {
        attributes.put(name, value);
        replaceLine(0, "% ATTRIBUTES: " + attributes.toString());
    }

    @Override
    public String getAttribute(String name) {
        return attributes.has(name) ? attributes.getString(name) : null;
    }

    @Override
    public Map<String, String> getAttributes() {

        HashMap<String, String> entries = new HashMap<>(attributes.length());

        for (String key : attributes.keySet()) {
            entries.put(key, attributes.getString(key));
        }

        return entries;

    }

    private synchronized void init(String path) throws IOException {

        System.out.println(path);

        // Make sure the directory we're wanting to write into exists.
        new File(path).getParentFile().mkdirs();
        this.path = path;

        file = new RandomAccessFile(path, "rw");
        file.setLength(0);
        file.seek(0);
        file.writeBytes("% ATTRIBUTES: " + attributes.toString() + "\n");
        file.writeBytes(String.join(",", getNames()));
        file.writeBytes("\n");

    }

    @Override
    public synchronized void updateColumns() {

        if (!open) {
            throw new IllegalStateException("You cannot alter a finalised ResultTable");
        }

        replaceLine(1, String.join(",", String.join(",", getNames())));

    }

    protected synchronized void replaceLine(int lineNo, String newLine) {

        StringBuilder newFile = new StringBuilder();

        try {

            resetPosition();

            int    i    = 0;
            String line = getLine();

            do {

                if (i++ != lineNo) {
                    newFile.append(line);
                } else {
                    newFile.append(newLine);
                }

                newFile.append("\n");

                line = getLine();

            } while (line != null);

            file.setLength(0);
            file.writeBytes(newFile.toString());

            resetPosition();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    protected synchronized void removeLine(int lineNo) {

        StringBuilder newFile = new StringBuilder();

        try {

            resetPosition();

            int    i    = 0;
            String line = getLine();

            do {

                if (i++ != lineNo) {
                    newFile.append(line);
                    newFile.append("\n");
                }

                line = getLine();

            } while (line != null);

            file.setLength(0);
            file.writeBytes(newFile.toString());

            resetPosition();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    protected synchronized void addBefore(int lineNo, String newLine) {

        StringBuilder newFile = new StringBuilder();

        try {

            resetPosition();

            int    i    = 0;
            String line = getLine();

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

                line = getLine();

            } while (line != null);

            file.setLength(0);
            file.writeBytes(newFile.toString());

            resetPosition();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected synchronized void addRow(Result row) {

        try {

            long current = file.getFilePointer();
            file.seek(file.length());
            file.writeBytes(row.getOutput(","));
            file.seek(current);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected synchronized void clearData() {

        try {

            file.setLength(0);
            file.seek(0);
            String[] titles = new String[getNumCols()];

            for (int i = 0; i < getNumCols(); i++) {
                titles[i] = getTitle(i);
            }

            file.writeBytes(String.join(",", titles));
            file.writeBytes("\n");

            resetPosition();


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public synchronized int getNumRows() {

        int count = 0;

        try {

            resetPosition();

            while (getLine() != null) {
                count++;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return count - 2;

    }

    protected synchronized void resetPosition() throws IOException {
        file.seek(0);
        currentLine = 0;
    }

    protected synchronized String getLine(int no) throws IOException {

        if (no < currentLine) {
            resetPosition();
        }

        for (int i = currentLine; i < no; i++) {
            getLine();
        }

        return getLine();

    }

    protected synchronized String getLine() throws IOException {
        currentLine++;
        return file.readLine();
    }

    @Override
    public synchronized Result getRow(int i) {

        try {

            String[] values = getLine(i + 2).split(",");
            double[] dVals  = new double[values.length];

            for (int j = 0; j < values.length; j++) {
                dVals[j] = Double.parseDouble(values[j]);
            }

            return new Result(this, dVals);

        } catch (IOException e) {

            e.printStackTrace();
            return null;

        }
    }

    public synchronized Set<Double> getValueSet(int column) {

        Set<Double> set = new HashSet<>();
        getColumns(column).forEach(set::add);
        return set;

    }

    @Override
    public synchronized void removeRow(int i) {
        removeLine(i);
    }

    @Override
    public synchronized void close() {

        try {
            file.close();
            file = new RandomAccessFile(path, "r");
        } catch (IOException e) {
            Util.exceptionHandler(e);
        }

    }

    @Override
    public Iterator<Result> iterator() {

        try {

            RandomAccessFile file = new RandomAccessFile(ResultStream.this.path, "r");

            if (file.readLine().startsWith("% ATTRIBUTES")) {
                file.readLine();
            }

            return new Iterator<Result>() {

                private final int rows = getNumRows();
                private int row = 0;

                @Override
                public boolean hasNext() {
                    return row < rows;
                }

                @Override
                public Result next() {

                    try {

                        return new Result(
                                ResultStream.this,
                                Arrays.stream(file.readLine().split(","))
                                      .mapToDouble(Double::parseDouble)
                                      .toArray()
                        );

                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    } finally {

                        row++;

                        if (!hasNext()) {
                            try {
                                file.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                    }

                }

            };

        } catch (IOException e) {

            e.printStackTrace();

            return new Iterator<Result>() {

                private final int rows = getNumRows();
                private int row = 0;

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

}
