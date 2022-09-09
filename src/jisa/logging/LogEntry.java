package jisa.logging;

import java.text.DateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogEntry {

    public static final Pattern LINE_PATTERN = Pattern.compile("^\\[(.*?)\\]\\s+\\(([A-Z])\\)\\s+(.*?)$");

    public static LogEntry fromString(String line) {

        try {

            Matcher matcher = LINE_PATTERN.matcher(line);

            if (matcher.matches()) {

                return new LogEntry(
                    LocalDateTime.parse(matcher.group(1), DateTimeFormatter.ISO_DATE_TIME),
                    Level.fromCode(matcher.group(2)),
                    matcher.group(3)
                );

            } else {
                return null;
            }

        } catch (Exception e) {
            return null;
        }

    }

    private final LocalDateTime time;
    private final Level         level;
    private final String        message;

    public LogEntry(Level level, String message) {
        this(LocalDateTime.now(), level, message);
    }

    public LogEntry(LocalDateTime time, Level level, String message) {
        this.time    = time;
        this.level   = level;
        this.message = message;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public Level getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }

    public String toString() {

        return String.format(
            "[%s]\t(%s)\t%s\n",
            time.truncatedTo(ChronoUnit.SECONDS).format(DateTimeFormatter.ISO_DATE_TIME),
            level.getCode(),
            message.replace("\r\n", " ").replace("\n\r", " ").replace("\n", " ").replace("\r", " ")
        );

    }

    public enum Level {

        DEBUG("D"),
        MESSAGE("M"),
        WARNING("W"),
        ERROR("E"),
        FATAL("F");

        public static Level fromCode(String code) {
            return Arrays.stream(values()).filter(e -> e.getCode().equals(code)).findFirst().orElse(null);
        }

        private final String code;

        Level(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

    }

}
