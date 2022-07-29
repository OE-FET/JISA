package jisa.visa;

import jisa.Util;
import jisa.addresses.Address;
import jisa.devices.interfaces.Instrument;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.*;

/**
 * Generic instrument encapsulation via VISA
 */
public class VISADevice implements Instrument {

    public final static int    DEFAULT_TIMEOUT = 13;
    public final static int    DEFAULT_EOI     = 1;
    public final static int    DEFAULT_EOS     = 0;
    public final static int    EOS_RETURN      = 5130;
    public final static int    LF_TERMINATOR   = 0x0A;
    public final static int    CR_TERMINATOR   = 0x0D;
    public final static int    CRLF_TERMINATOR = 0x0D0A;
    public final static String C_IDN           = "*IDN?";

    private final static List<WeakReference<VISADevice>> opened = new LinkedList<>();

    static {

        /*
         * Close any surviving connections when the JVM shuts down.
         */
        Util.addShutdownHook(() -> {

            for (WeakReference<VISADevice> reference : opened) {

                VISADevice device = reference.get();
                if (device != null) {
                    try {
                        device.close();
                    } catch (Exception ignored) {
                    }
                }

            }

        });

    }

    private final List<String> toRemove       = new LinkedList<>();
    private       Connection   connection;
    private       Address      address;
    private       String       terminator     = "";
    private       String       lastCommand    = null;
    private       String       lastRead       = null;
    private       int          readBufferSize = 1024;
    private       int          retryCount     = 3;
    private       int          timeout        = 2000;

    // logging utilities
    private Logger logger = null;
    private StreamHandler loggerHandler = null;
    private boolean loggerEnabled = false;



    public VISADevice(Address address) throws IOException {

        this(address, null);

    }

    /**
     * Opens the device at the specified address
     *
     * @param address Some form of InstrumentAddress (eg GPIBAddress, USBAddress etc)
     *
     * @throws IOException Upon communications error
     */
    public VISADevice(Address address, Class<? extends Driver> prefDriver) throws IOException {

        if (address == null) {
            return;
        }

        try {
            this.connection = VISA.openInstrument(address, prefDriver);
            this.address    = address;
        } catch (VISAException e) {
            throw new IOException(e.getMessage());
        }

        // Keep a weak reference to this
        opened.add(new WeakReference<>(this));

    }

    /**
     * Enable the logging functions for the device. The driver will log all the commands sent out and all the
     * readings received. Once enabled, the logging cannot be disabled (for now).
     *
     * @param loggerName Name of the logger. It should be distinct from other loggers and ideally shorter than
     *                   15 characters. If the name given is null, the class name will be used as a default.
     * @param logFileName Name of the file where the log will be written. If the name is null, the log will be printed
     *                    to the terminal.
     */
    public void enableLogger(String loggerName, String logFileName) {
        if (loggerEnabled)
            return;
        loggerEnabled = true;
        if (loggerName == null)
        {
            loggerName = this.getClass().getName();
        }
        logger = Logger.getLogger(loggerName);
        logger.setUseParentHandlers(false);
        if (logFileName == null)
        {
            loggerHandler = new ConsoleHandler();
            loggerHandler.setLevel(Level.INFO);
        }
        else
        {
            try {
                loggerHandler = new FileHandler(logFileName);
                loggerHandler.setLevel(Level.INFO);
            } catch (IOException e){
                System.out.print("Logger file handler initialization failed!");
                loggerEnabled = false;
                return;
            }
        }
        logger.addHandler(loggerHandler);
        loggerHandler.setFormatter(new CustomLoggerFormatter());
        logger.log(Level.INFO, "Logger enabled");
    }

    /**
     * Check if the logger is enabled.
     * @return true if the logger is enabled.
     */
    public boolean isLoggerEnabled()
    {
        return loggerEnabled;
    }

    /**
     * Add a new log entry. Safe to call even if the logger is not enabled. In this case, the function will do nothing.
     * @param level level of the log entry
     * @param message message of the log entry
     */
    public void addLog(Level level, String message)
    {
        if (loggerEnabled)
            logger.log(level, message);
    }

    public synchronized void clearBuffers() throws IOException {

        try {
            connection.clear();
        } catch (VISAException e) {
            throw new IOException(e.getMessage());
        }

    }


    /**
     * Continuously reads from the read buffer until there's nothing left to read. (Clears the read buffer for the more
     * stubborn of instruments)
     *
     * @throws IOException Upon communications error
     */
    public synchronized void manuallyClearReadBuffer() throws IOException {

        try {
            connection.setTimeout(250);
        } catch (VISAException e) {
            throw new IOException(e.getMessage());
        }

        while (true) {
            try {
                connection.readBytes(1);
            } catch (VISAException e) {
                break;
            }
            Util.sleep(25);
        }

        try {
            connection.setTimeout(timeout);
        } catch (VISAException e) {
            throw new IOException(e.getMessage());
        }

    }

    /**
     * What default number of bytes should we expect to get when reading from the device?
     *
     * @param bytes The number of bytes, yikes.
     */
    public synchronized void setReadBufferSize(int bytes) {
        readBufferSize = bytes;
    }

    public synchronized void setSerialParameters(int baudRate, int dataBits, Connection.Parity parity, Connection.StopBits stopBits, Connection.Flow flowControl) throws IOException {

        try {
            connection.setSerial(baudRate, dataBits, parity, stopBits, flowControl);
        } catch (VISAException e) {
            throw new IOException(e.getMessage());
        }

    }

    /**
     * Should we send an EOI signal at the end of writing to the device? Generally, this should be true and is by default
     * however older devices from more anarchic times (such as the 70s) may needs this disabling.
     *
     * @param flag Do we, don't we?
     *
     * @throws IOException Upon communications error
     */
    public synchronized void setEOI(boolean flag) throws IOException {

        try {
            connection.setEOI(flag);
        } catch (VISAException e) {
            throw new IOException(e.getMessage());
        }

    }

    /**
     * Sets which character we should expect to read from the device to indicate that it's done talking to us.
     *
     * @param character The character code
     *
     * @throws IOException Upon communications error
     */
    public synchronized void setReadTerminator(long character) throws IOException {

        try {
            connection.setReadTerminator(character);
        } catch (VISAException e) {
            throw new IOException(e.getMessage());
        }

    }

    /**
     * Sets which character we should expect to read from the device to indicate that it's done talking to us.
     *
     * @param character The character
     *
     * @throws IOException Upon communications error
     */
    public synchronized void setReadTerminator(String character) throws IOException {

        try {
            connection.setReadTerminator(character);
        } catch (VISAException e) {
            throw new IOException(e.getMessage());
        }

    }

    public synchronized void addAutoRemove(String... phrases) {
        toRemove.addAll(List.of(phrases));
    }

    /**
     * Sets the timeout, in milliseconds, for operations with the device
     *
     * @param timeoutMSec Timeout, milliseconds
     *
     * @throws IOException Upon communications error
     */
    public synchronized void setTimeout(int timeoutMSec) throws IOException {

        try {
            connection.setTimeout(timeoutMSec);
        } catch (VISAException e) {
            throw new IOException(e.getMessage());
        }

        timeout = timeoutMSec;

    }

    public synchronized void setRetryCount(int count) {
        retryCount = count;
    }

    /**
     * Returns the address used to connect to the device
     *
     * @return Address object
     */
    public Address getAddress() {
        return address;
    }

    /**
     * Set a termination character to tell the device when we've stopped talking to it
     *
     * @param term The character to use (eg "\n" or "\r")
     */
    public synchronized void setWriteTerminator(String term) {
        terminator = term;
    }

    /**
     * Write the given string to the device
     *
     * @param command The string to write
     * @param args    Any formatting arguments
     *
     * @throws IOException Upon communications error
     */
    public synchronized void write(String command, Object... args) throws IOException {

        String commandParsed = String.format(command, args).concat(terminator);

        lastCommand = commandParsed;

        try {
            connection.write(commandParsed);
        } catch (VISAException e) {
            throw new IOException(e.getMessage());
        }
        if (loggerEnabled)
            logger.log(Level.INFO, "write: " + commandParsed);

    }

    public synchronized void writeBytes(byte[] bytes) throws IOException {

        try {
            connection.writeBytes(bytes);
        } catch (VISAException e) {
            throw new IOException(e.getMessage());
        }

    }

    /**
     * Read a string from the device
     *
     * @return The string returned by the device
     *
     * @throws IOException Upon communications error
     */
    public synchronized String read() throws IOException {
        return read(retryCount);
    }

    /**
     * Read a string from the device
     *
     * @param attempts Number of failed attempts to read before throwing an exception
     *
     * @return The string returned by the device
     *
     * @throws IOException Upon communications error
     */
    public synchronized String read(int attempts) throws IOException {

        int count = 0;

        // Try n times
        while (true) {

            try {

                lastRead = connection.read(readBufferSize);

                for (String remove : toRemove) {
                    lastRead = lastRead.replace(remove, "");
                }

                break;

            } catch (VISAException e) {

                count++;
                if (count >= attempts) {
                    throw new IOException(e.getMessage());
                }

                System.out.printf("Retrying read from \"%s\", reason: %s%n", address.toString(), e.getMessage());

            }

        }

        if (loggerEnabled)
            logger.log(Level.INFO, "read:  " + lastRead);

        return lastRead;

    }

    public synchronized byte[] readBytes(int numBytes) throws IOException {

        try {
            return connection.readBytes(numBytes);
        } catch (VISAException e) {
            throw new IOException(e.getMessage());
        }

    }

    /**
     * Read a double from the device
     *
     * @return The number returned by the device
     *
     * @throws IOException Upon communications error
     */
    public synchronized double readDouble() throws IOException {
        return Double.parseDouble(read().replace("\n", "").replace("\r", "").trim());
    }

    /**
     * Read an integer from the device
     *
     * @return Integer read from the device
     *
     * @throws IOException Upon communications error
     */
    public synchronized int readInt() throws IOException {
        return Integer.parseInt(read().replace("\n", "").replace("\r", "").trim());
    }

    /**
     * Write the given string, then immediately read the response as a double
     *
     * @param command String to write
     * @param args    Formatting arguments
     *
     * @return Numerical response
     *
     * @throws IOException Upon communications error
     */
    public synchronized double queryDouble(String command, Object... args) throws IOException {
        write(command, args);
        return readDouble();
    }

    /**
     * Write the given string, then immediately read the response as an integer
     *
     * @param command String to write
     * @param args    Formatting arguments
     *
     * @return Numerical response
     *
     * @throws IOException Upon communications error
     */
    public synchronized int queryInt(String command, Object... args) throws IOException {
        write(command, args);
        return readInt();
    }

    /**
     * Write the given string, then immediately read the response
     *
     * @param command String to write
     * @param args    Formatting arguments
     *
     * @return String response
     *
     * @throws IOException Upon communications error
     */
    public synchronized String query(String command, Object... args) throws IOException {
        write(command, args);
        return read();
    }

    /**
     * Sends the standard identifications query to the device (*IDN?)
     *
     * @return The resposne of the device
     *
     * @throws IOException Upon communications error
     */
    public synchronized String getIDN() throws IOException {
        return query(C_IDN);
    }

    /**
     * Close the connection to the device
     *
     * @throws IOException Upon communications error
     */
    public synchronized void close() throws IOException {

        try {
            connection.close();
        } catch (VISAException e) {
            throw new IOException(e.getMessage());
        }

    }

    private static class CustomLoggerFormatter extends SimpleFormatter
    {
        // formatter for the optional logging functionality.
        private static final String format = "[%1$tF %1$tT] [%2$-7s] [%3$-15.15s] %4$s %n";

        @Override
        public synchronized String format(LogRecord lr) {
            return String.format(format,
                    new Date(lr.getMillis()),
                    lr.getLevel().getLocalizedName(),
                    lr.getLoggerName(),
                    lr.getMessage()
            );
        }
    }

}
