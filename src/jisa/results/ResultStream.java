package jisa.results;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ResultStream extends ResultTable {

    private String           path;
    private RandomAccessFile file;
    private int              currentLine = 0;

    public static ResultStream loadFile(String path) throws IOException {

        RandomAccessFile file = new RandomAccessFile(path, "rw");
        file.seek(0);

        String header = file.readLine();

        JSONObject attributes = null;

        if (header.startsWith("% ATTRIBUTES: ")) {
            attributes = new JSONObject(header.replaceFirst("% ATTRIBUTES: ", ""));
            header     = file.readLine();
        }

        ResultStream stream = new ResultStream(ResultTable.parseColumnHeaderLine(header));

        stream.path = path;
        stream.file = file;

        if (attributes == null) {
            stream.addBefore(0, "% ATTRIBUTES: {}");
        } else {
            attributes.toMap().forEach((k,v) -> stream.setAttributeQuiet(k, v.toString()));
        }

        return stream;

    }

    private ResultStream(Column<?>... columns) {
        super(columns);
    }

    public ResultStream(String file, Column<?>... columns) throws IOException {
        super(columns);
        initialise(file);
    }

    public ResultStream(String file, String... columns) throws IOException {
        this(file, Arrays.stream(columns).map(DoubleColumn::new).toArray(DoubleColumn[]::new));
    }

    private synchronized void initialise(String path) throws IOException {

        // Make sure the directory we're wanting to write into exists.
        new File(path).getParentFile().mkdirs();
        this.path = path;

        file = new RandomAccessFile(path, "rw");
        file.setLength(0);
        file.seek(0);
        file.writeBytes(getAttributeLine());
        file.writeBytes("\n");
        file.writeBytes(getColumnHeaderLine());
        file.writeBytes("\n");

    }

    public synchronized void setAttribute(String key, String value) {
        super.setAttribute(key, value);
        replaceLine(0, getAttributeLine());
    }

    protected void setAttributeQuiet(String key, String value) {
        super.setAttribute(key, value);
    }

    @Override
    public synchronized Row getRow(int index) {

        try {
            return parseCSVLine(getLine(index + 2));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    @Override
    protected synchronized void addRowData(Row row) {

        try {

            long current = file.getFilePointer();
            file.seek(file.length());
            file.writeBytes(getCSVLine(row));
            file.writeBytes("\n");
            file.seek(current);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public synchronized int getRowCount() {

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

    @Override
    protected synchronized void clearData() {

        try {

            file.setLength(0);
            file.seek(0);
            file.writeBytes(getAttributeLine());
            file.writeBytes("\n");
            file.writeBytes(getColumnHeaderLine());
            file.writeBytes("\n");

            resetPosition();


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public Stream<Row> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    @Override
    public Iterator<Row> iterator() {

        try {

            RandomAccessFile file = new RandomAccessFile(path, "r");

            if (file.readLine().startsWith("% ATTRIBUTES")) {
                file.readLine();
            }

            return new Iterator<Row>() {

                private final int rows = getRowCount();
                private int row = 0;

                @Override
                public boolean hasNext() {
                    return row < rows;
                }

                @Override
                public Row next() {

                    try {

                        return parseCSVLine(file.readLine());

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

            return new Iterator<Row>() {

                private final int rows = getRowCount();
                private int row = 0;

                @Override
                public boolean hasNext() {
                    return row < rows;
                }

                @Override
                public Row next() {
                    return getRow(row++);
                }

            };

        }

    }

    protected synchronized String getLine() throws IOException {
        currentLine++;
        return file.readLine();
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

    protected synchronized void resetPosition() throws IOException {
        file.seek(0);
        currentLine = 0;
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

    public void close() {
        try {
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
