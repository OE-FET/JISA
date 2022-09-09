package jisa.logging;

import java.io.IOException;
import java.time.LocalDateTime;

public class Logger {

    private static Log CURRENT_LOG = null;

    public static void start(String path) {

        try {
            close();
            CURRENT_LOG = new Log(path);
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }


    public static void close() {

        try {

            if (CURRENT_LOG != null) {
                CURRENT_LOG.close();
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    public static Log getCurrentLog() {
        return CURRENT_LOG;
    }

    public static void addEntry(LogEntry entry) {

        try {
            CURRENT_LOG.addEntry(entry);
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    public static void addEntry(LocalDateTime time, LogEntry.Level level, String message) {

        try {
            CURRENT_LOG.addEntry(time, level, message);
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    public static void addEntry(LogEntry.Level level, String message) {

        try {
            CURRENT_LOG.addEntry(level, message);
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    public static void addMessage(String message) {
        try {
            CURRENT_LOG.addMessage(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addDebug(String message) {
        try {
            CURRENT_LOG.addDebug(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addWarning(String message) {
        try {
            CURRENT_LOG.addWarning(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addError(String message) {
        try {
            CURRENT_LOG.addError(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addFatal(String message) {
        try {
            CURRENT_LOG.addFatal(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
