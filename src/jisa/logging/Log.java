package jisa.logging;

import java.io.*;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Iterator;

public class Log implements Iterable<LogEntry> {

    private final RandomAccessFile file;
    private final String           path;

    public Log(String path) throws IOException {
        (new File(path)).getParentFile().mkdirs();
        this.file = new RandomAccessFile(path, "rw");
        this.path = path;
        file.seek(file.length());
    }

    public void addEntry(LogEntry entry) throws IOException {
        file.writeBytes(entry.toString());
    }

    public void addEntry(LocalDateTime time, LogEntry.Level level, String message) throws IOException {
        addEntry(new LogEntry(time, level, message));
    }

    public void addEntry(LogEntry.Level level, String message) throws IOException {
        addEntry(new LogEntry(level, message));
    }

    public void addMessage(String message) throws IOException {
        addEntry(LogEntry.Level.MESSAGE, message);
    }

    public void addDebug(String message) throws IOException {
        addEntry(LogEntry.Level.DEBUG, message);
    }

    public void addWarning(String message) throws IOException {
        addEntry(LogEntry.Level.WARNING, message);
    }

    public void addError(String message) throws IOException {
        addEntry(LogEntry.Level.ERROR, message);
    }

    public void addFatal(String message) throws IOException {
        addEntry(LogEntry.Level.FATAL, message);
    }

    public void close() throws IOException {
        file.close();
    }

    @Override
    public Iterator<LogEntry> iterator() {

        final BufferedReader reader;

        try {
            reader = new BufferedReader(new FileReader(path));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        return reader.lines().map(LogEntry::fromString).iterator();

    }

}
