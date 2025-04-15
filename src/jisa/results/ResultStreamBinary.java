package jisa.results;

import org.json.JSONObject;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.DeflaterOutputStream;

public class ResultStreamBinary extends ResultTable {

    private String           path;
    private RandomAccessFile file;
    private InputStream      input;
    private DataOutputStream output;
    private long             dataStart   = 0;
    private int              currentLine = 0;

    private ResultStreamBinary(RandomAccessFile file, String path, Column<?>... columns) {
        super(columns);
        this.file = file;
        this.path = path;
        input     = Channels.newInputStream(file.getChannel());
        output    = new DataOutputStream(new BufferedOutputStream(Channels.newOutputStream(file.getChannel())));
    }

    public ResultStreamBinary(Column<?>... columns) throws IOException {
        super(columns);
        initialise(null);
    }

    public ResultStreamBinary(String file, Column<?>... columns) throws IOException {
        super(columns);
        initialise(file);
    }

    public ResultStreamBinary(String file, String... columns) throws IOException {
        this(file, Arrays.stream(columns).map(DoubleColumn::new).toArray(DoubleColumn[]::new));
    }

    private synchronized void initialise(String path) throws IOException {

        if (path != null) {

            // Make sure the directory we're wanting to write into exists.
            try {
                new File(path).getParentFile().mkdirs();
            } catch (Throwable ignored) { }

            this.path = path;

            file = new RandomAccessFile(path, "rw");

        } else {

            File tempFile = File.createTempFile("JISA", ".bin");
            tempFile.deleteOnExit();
            this.path = tempFile.getAbsolutePath();
            file      = new RandomAccessFile(tempFile, "rw");

        }

        input  = Channels.newInputStream(file.getChannel());
        output = new DataOutputStream(new BufferedOutputStream(Channels.newOutputStream(file.getChannel())));

        file.setLength(0);
        file.seek(0);

        writeHeader();

    }

    protected synchronized void writeHeader() throws IOException {

        byte[] attributes = new JSONObject(getAttributes()).toString().getBytes(StandardCharsets.UTF_8);

        output.write(1);
        output.writeInt(attributes.length);
        output.write(attributes);

        for (Column<?> column : getColumns()) {

            byte[] name  = column.getName().getBytes(StandardCharsets.UTF_8);
            byte[] units = column.hasUnits() ? column.getUnits().getBytes(StandardCharsets.UTF_8) : new byte[0];
            byte[] type  = column.getType().getSimpleName().getBytes(StandardCharsets.UTF_8);

            output.write(2);
            output.writeInt(name.length);
            output.write(name);
            output.writeInt(units.length);
            output.write(units);
            output.writeInt(type.length);
            output.write(type);

        }

        output.write(3);

        dataStart = file.getFilePointer();

    }

    public synchronized void setAttribute(String key, String value) {
        super.setAttribute(key, value);
        try { updateHeader(); } catch (Exception ignored) { }
    }

    protected void setAttributeQuiet(String key, String value) {
        super.setAttribute(key, value);
    }

    @Override
    public synchronized Row getRow(int index) {

        try {
            return getLine(index);
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

            for (Column column : columns) {
                column.writeToStream(output, row.get(column));
            }

            file.seek(current);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public synchronized int getRowCount() {

        try {

            resetPosition();

            int  count  = 0;
            long length = file.length();

            while (file.getFilePointer() < length) {
                skipLine();
                count++;
            }

            return count;

        } catch (IOException e) {
            return 0;
        }

    }

    @Override
    protected synchronized void clearData() {

        try {

            file.setLength(0);
            file.seek(0);
            writeHeader();

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

        return new Iterator<Row>() {

            private final int rows = getRowCount();
            private       int row  = 0;

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

    protected synchronized void skipLine() throws IOException {

        for (Column column : columns) {
            column.skipBytes(input);
        }

        currentLine++;

    }

    protected synchronized Row readLine() throws IOException {

        Map<Column<?>, Object> result = new LinkedHashMap<>();

        for (Column column : columns) {
            result.put(column, column.readFromStream(input));
        }

        currentLine++;

        return new Row(result);

    }

    protected synchronized Row getLine(int index) throws IOException {

        if (index < currentLine) {
            resetPosition();
        }

        for (int i = currentLine; i < index; i++) {
            skipLine();
        }

        return readLine();

    }

    protected synchronized void resetPosition() throws IOException {
        file.seek(dataStart);
        currentLine = 0;
    }

    protected synchronized void updateHeader() throws IOException {

        File             temp     = File.createTempFile("JISA", ".csv");
        RandomAccessFile tempFile = new RandomAccessFile(temp, "rw");

        resetPosition();

        long length = file.length() - file.getFilePointer();

        for (long i = 0; i < length; i++) {
            tempFile.write(input.read());
        }

        tempFile.seek(0);
        file.setLength(0);

        writeHeader();

        for (long j = 0; j < tempFile.length(); j++) {
            output.write(tempFile.read());
        }

        tempFile.close();
        temp.delete();

        resetPosition();

    }

    public void compressAndClose() throws IOException {

        File                temp    = new File(path + ".temp");
        WritableByteChannel tempOut = Channels.newChannel(new DeflaterOutputStream(new BufferedOutputStream(new FileOutputStream(temp)), true));

        file.seek(0);
        file.getChannel().transferTo(0, file.length(), tempOut);

        file.close();
        File f = new File(path);
        f.delete();

        Files.move(temp.toPath(), f.toPath());
        tempOut.close();

    }

    public void close() {
        try {
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
