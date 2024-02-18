package jisa.results;

import jisa.Util;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;
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

        ResultStream stream = new ResultStream(file, path, ResultTable.parseColumnHeaderLine(header));

        if (attributes == null) {
            stream.addBefore(0, "% ATTRIBUTES: {}");
        } else {
            attributes.toMap().forEach((k, v) -> stream.setAttributeQuiet(k, v.toString()));
        }

        return stream;

    }

    private ResultStream(RandomAccessFile file, String path, Column<?>... columns) {
        super(columns);
        this.file = file;
        this.path = path;
    }

    public ResultStream(Column<?>... columns) throws IOException {
        super(columns);
        initialise(null);
    }

    public ResultStream(String file, Column<?>... columns) throws IOException {
        super(columns);
        initialise(file);
    }

    public ResultStream(String file, String... columns) throws IOException {
        this(file, Arrays.stream(columns).map(DoubleColumn::new).toArray(DoubleColumn[]::new));
    }

    private synchronized void initialise(String path) throws IOException {

        if (path != null) {

            // Make sure the directory we're wanting to write into exists.
            try {
                new File(path).getParentFile().mkdirs();
            } catch (Throwable ignored) {}
            
            this.path = path;

            file = new RandomAccessFile(path, "rw");

        } else {

            File tempFile = File.createTempFile("JISA", ".csv");
            tempFile.deleteOnExit();
            this.path = tempFile.getAbsolutePath();
            file      = new RandomAccessFile(tempFile, "rw");

        }

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


        try {

            File             temp     = File.createTempFile("JISA", ".csv");
            RandomAccessFile tempFile = new RandomAccessFile(temp, "rw");

            resetPosition();

            int    i    = 0;
            String line = getLine();

            do {

                if (i++ != lineNo) {
                    tempFile.writeBytes(line);
                } else {
                    tempFile.writeBytes(newLine);
                }

                tempFile.writeBytes("\n");

                line = getLine();

            } while (line != null);

            tempFile.seek(0);
            file.setLength(0);

            for (long j = 0; j < tempFile.length(); j++) {
                file.write(tempFile.read());
            }

            tempFile.close();
            temp.delete();

            resetPosition();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    protected synchronized void addBefore(int lineNo, String newLine) {

        try {

            File             temp     = File.createTempFile("JISA", ".csv");
            RandomAccessFile tempFile = new RandomAccessFile(temp, "rw");

            resetPosition();

            int    i    = 0;
            String line = getLine();

            do {

                if (i != lineNo) {

                    tempFile.writeBytes(line);

                } else {

                    tempFile.writeBytes(newLine);
                    tempFile.writeBytes("\n");
                    tempFile.writeBytes(line);

                }

                tempFile.writeBytes("\n");
                i++;

                line = getLine();

            } while (line != null);

            tempFile.seek(0);
            file.setLength(0);

            for (long j = 0; j < tempFile.length(); j++) {
                file.write(tempFile.read());
            }

            tempFile.close();
            temp.delete();

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
