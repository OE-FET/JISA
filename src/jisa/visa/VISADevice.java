package jisa.visa;

import jisa.Util;
import jisa.addresses.Address;
import jisa.devices.DeviceException;
import jisa.devices.interfaces.Instrument;
import jisa.visa.connections.*;
import jisa.visa.drivers.Driver;
import jisa.visa.exceptions.VISAException;
import kotlin.jvm.JvmClassMappingKt;
import kotlin.reflect.KClass;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Generic instrument encapsulation via VISA
 */
public class VISADevice implements Instrument {

    public final static int                      DEFAULT_TIMEOUT = 13;
    public final static int                      DEFAULT_EOI     = 1;
    public final static int                      DEFAULT_EOS     = 0;
    public final static int                      EOS_RETURN      = 5130;
    public final static int                      LF_TERMINATOR   = 0x0A;
    public final static int                      CR_TERMINATOR   = 0x0D;
    public final static int                      CRLF_TERMINATOR = 0x0D0A;
    public final static String                   C_IDN           = "*IDN?";
    private             ScheduledExecutorService scheduler       = null;
    private             int                      ioInterval      = 0;
    private final       Semaphore                ioPermits       = new Semaphore(1);
    private final       Runnable                 ioWait          = ioPermits::release;
    private             boolean                  writeWait       = false;
    private             boolean                  readWait        = false;

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
                    } catch (Exception ignored) {}

                }

            }

        });

    }

    private final List<String> toRemove       = new LinkedList<>();
    private final Connection   connection;
    private final Address      address;
    private       String       terminator     = "";
    private       String       lastCommand    = null;
    private       String       lastRead       = null;
    private       int          readBufferSize = 1024;
    private       int          retryCount     = 3;
    private       int          timeout        = 2000;

    public VISADevice(Address address) throws IOException {

        this(address, null);

    }

    /**
     * Opens the device at the specified address
     *
     * @param address    Some form of InstrumentAddress (eg GPIBAddress, USBAddress etc)
     * @param prefDriver Preferred driver to try first
     *
     * @throws IOException Upon communications error
     */
    public VISADevice(Address address, Class<? extends Driver> prefDriver) throws IOException {

        if (address == null) {

            this.connection = null;
            this.address    = null;

            return;

        }
        
        this.connection = VISA.openInstrument(address, prefDriver);
        this.address    = address;

        // Keep a weak reference to this
        opened.add(new WeakReference<>(this));

    }

    public Connection getConnection() {
        return connection;
    }

    public synchronized <T extends Connection> void config(Class<T> type, ConfigRun<T> run) throws IOException, DeviceException {

        if (type.isAssignableFrom(getConnection().getClass())) {
            run.config((T) getConnection());
        }

    }

    public synchronized <T extends Connection> void config(KClass<T> type, ConfigRun<T> run) throws IOException, DeviceException {
        config(JvmClassMappingKt.getJavaClass(type), run);
    }

    public synchronized void configGPIB(ConfigRun<GPIBConnection> run) throws IOException, DeviceException {
        config(GPIBConnection.class, run);
    }

    public synchronized void configSerial(ConfigRun<SerialConnection> run) throws IOException, DeviceException {
        config(SerialConnection.class, run);
    }

    public synchronized void configTCPIP(ConfigRun<TCPIPConnection> run) throws IOException, DeviceException {
        config(TCPIPConnection.class, run);
    }

    public synchronized void configLXI(ConfigRun<LXIConnection> run) throws IOException, DeviceException {
        config(LXIConnection.class, run);
    }

    public synchronized void configUSB(ConfigRun<USBConnection> run) throws IOException, DeviceException {
        config(USBConnection.class, run);
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
     * stubborn of instruments). Do not use on GPIB instruments programmed to respond to TALK requests (it will never
     * terminate).
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

            Util.sleep(Math.max(25, ioInterval));

        }

        try {
            connection.setTimeout(timeout);
        } catch (VISAException e) {
            throw new IOException(e.getMessage());
        }

    }

    /**
     * Sets whether this VISADevice object should wait a minimum amount of time between successive read/write
     * operations.
     *
     * @param interval The minimum interval, in milliseconds (0 will disable this feature)
     * @param read     Whether this wait should apply to read operations
     * @param write    Whether this wait should apply to write operations
     */
    public synchronized void setIOLimit(int interval, boolean read, boolean write) {

        if (interval < 0) {
            throw new IllegalArgumentException("Minimum I/O interval cannot be negative.");
        }

        if (interval > 0 && scheduler == null) {
            scheduler = Executors.newSingleThreadScheduledExecutor();
        } else if (interval == 0 && scheduler != null) {
            scheduler.shutdown();
            scheduler = null;
        }

        ioInterval = interval;
        readWait   = read;
        writeWait  = write;

    }

    /**
     * What default number of bytes should we expect to get when reading from the device?
     *
     * @param bytes The number of bytes, yikes.
     */
    public synchronized void setReadBufferSize(int bytes) {
        readBufferSize = bytes;
    }

    public synchronized void addAutoRemove(String... phrases) {
        toRemove.addAll(List.of(phrases));
    }

    public synchronized void setRetryCount(int count) {
        retryCount = count;
    }

    public synchronized void setTimeout(int msec) throws IOException {
        this.timeout = msec;
        getConnection().setTimeout(msec);
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

    public synchronized void setReadTerminator(String term) throws VISAException {
        getConnection().setReadTerminator(term);
    }

    public synchronized void setReadTerminator(long term) throws VISAException {
        getConnection().setReadTerminator(term);
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

        String  commandParsed = String.format(command, args).concat(terminator);
        boolean wait          = writeWait && ioInterval > 0;

        if (wait) {
            ioPermits.acquireUninterruptibly();
        }

        lastCommand = commandParsed;

        try {
            connection.write(commandParsed);
        } catch (VISAException e) {
            throw new IOException(e.getMessage());
        } finally {

            if (wait) {
                scheduler.schedule(ioWait, ioInterval, TimeUnit.MILLISECONDS);
            }

        }

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

        int           count = 0;
        final boolean wait  = readWait && ioInterval > 0;

        // Try n times
        while (true) {

            if (wait) {
                ioPermits.acquireUninterruptibly();
            }

            try {

                lastRead = connection.read(readBufferSize);

                for (String remove : toRemove) {
                    lastRead = lastRead.replace(remove, "");
                }

                break;

            } catch (VISAException e) {

                count++;
                if (count >= attempts) {
                    throw e;
                }

                System.out.printf("Retrying read from \"%s\", reason: %s%n", address.toString(), e.getMessage());

            } finally {

                if (wait) {
                    scheduler.schedule(ioWait, ioInterval, TimeUnit.MILLISECONDS);
                }

            }

        }

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

    @Override
    public String getName() {
        return "VISA Device";
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

    /**
     * Method to check limits of instruments values, throws DeviceException exceeded.
     *
     * @param valueName parameter to be checked (e.g. Voltage range)
     * @param value     value to set
     * @param lower     lower limit
     * @param upper     upper limit
     * @param unit      unit of value
     */
    protected void checkLimit(String valueName, Number value, Number lower, Number upper, String unit) throws DeviceException {

        if (!Util.isBetween(value, lower, upper)) {
            throw new DeviceException("%s = %e %s is out of range (%e to %e)", valueName, value, unit, lower, upper);
        }

    }

    protected void checkLimit(String valueName, Number value, Number lower, Number upper) throws DeviceException {

        if (!Util.isBetween(value, lower, upper)) {
            throw new DeviceException("%s = %e is out of range (%e to %e)", valueName, value, lower, upper);
        }

    }

    public interface ConfigRun<T extends Connection> {

        void config(T connection) throws DeviceException, IOException;

    }

}
