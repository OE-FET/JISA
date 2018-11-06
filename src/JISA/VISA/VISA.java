package JISA.VISA;

import JISA.Addresses.InstrumentAddress;
import JISA.Addresses.StrAddress;
import JISA.GUI.GUI;
import JISA.Util;
import com.sun.jna.*;
import com.sun.jna.ptr.NativeLongByReference;
import javafx.application.Platform;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Static class for accessing the native VISA library in a more Java-friendly way
 */
public class VISA {

    private static ArrayList<Driver>     drivers      = new ArrayList<>();
    private static HashMap<Long, Driver> instrDrivers = new HashMap<>();
    private static HashMap<Long, Long>   instrIDs     = new HashMap<>();
    private static long                  counter      = 0;

    static {

        System.out.println("Attempting to load drivers.");

        try {
            System.out.print("Trying VISA driver...                \t");
            VISADriver.init();
            drivers.add(new VISADriver());
            System.out.println("Success.");
        } catch (VISAException ignored) {
            System.out.println("Nope.");
        }

        try {
            System.out.print("Trying Linux GPIB (libgpib) driver...\t");
            GPIBDriver.init();
            drivers.add(new GPIBDriver());
            System.out.println("Success.");
        } catch (VISAException ignored) {
            System.out.println("Nope.");
        }

        try {
            System.out.print("Trying NI-GPIB (ni4882) driver...    \t");
            NIGPIBDriver.init();
            drivers.add(new NIGPIBDriver());
            System.out.println("Success.");
        } catch (VISAException ignored) {
            System.out.println("Nope.");
        }

        try {
            System.out.print("Trying Serial driver...              \t");
            drivers.add(new SerialDriver());
            System.out.println("Success.");
        } catch (Exception | Error ignored) {
            System.out.println("Nope.");
        }

        if (drivers.size() == 0) {
            Util.sleep(500);
            System.err.println("ERROR: Could not load any drivers!");

            try {
                GUI.errorAlert("JISA Library", "No Drivers", "Could not load any drivers for instrument control!\n\nCheck your driver installation(s).");
            } catch (Exception | Error ignored) {}

            System.exit(1);
        } else {
            System.out.printf("Successfully loaded %d drivers.\n", drivers.size());
        }

    }

    public static void init() {}

    /**
     * Returns an array of all instrument addressed detected by VISA
     *
     * @return Array of instrument addresses
     *
     * @throws VISAException Upon error with VISA interface
     */
    public static StrAddress[] getInstruments() throws VISAException {

        HashMap<String, StrAddress> addresses = new HashMap<>();

        for (Driver driver : drivers) {
            for (StrAddress a : driver.search()) {
                boolean found = false;
                for (String s : addresses.keySet()) {
                    if (s.trim().equals(a.getVISAAddress().trim())) {
                        found = true;
                    }
                }
                if (!found) {
                    addresses.put(a.getVISAAddress(), a);
                }
            }
        }

        return addresses.values().toArray(new StrAddress[0]);

    }

    /**
     * Open the instrument with the given VISA resource address
     *
     * @param address Resource address
     *
     * @return Instrument handle
     *
     * @throws VISAException Upon error with VISA interface
     */
    public static long openInstrument(InstrumentAddress address) throws VISAException {

        long id    = -1;
        long index = -1;
        ArrayList<String> errors = new ArrayList<>();
        for (Driver d : drivers) {

            try {
                id = d.open(address);
                index = counter++;
                instrDrivers.put(index, d);
                instrIDs.put(index, id);
                break;
            } catch (VISAException e) {
                id = -1;
                errors.add(e.getMessage());
            }

        }

        if (id == -1) {
            System.err.println(String.join("Driver Errors:\n", errors));
            throw new VISAException("Could not open %s using any driver!", address.getVISAAddress());
        }

        return index;

    }

    /**
     * Writes to the given instrument, specified by instrument handle returned by openInstrument()
     *
     * @param instrument Instrument handle from openInstrument()
     * @param toWrite    String the write to the instrument
     *
     * @throws VISAException Upon error with VISA interface
     */
    public static void write(long instrument, String toWrite) throws VISAException {

        // Check that we have actually opened this device
        if (!instrIDs.containsKey(instrument)) {
            throw new VISAException("That instrument has not been opened!");
        }

        instrDrivers.get(instrument).write(
                instrIDs.get(instrument),
                toWrite
        );

    }

    /**
     * Read from the given instrument, specified by instrument handle returned by openInstrument()
     *
     * @param instrument Instrument handle from openInstrument()
     * @param bufferSize Number of bytes to allocate for response
     *
     * @return The read string from the device
     *
     * @throws VISAException Upon error with VISA interface
     */
    public static String read(long instrument, int bufferSize) throws VISAException {

        // Check that we have actually opened this device
        if (!instrIDs.containsKey(instrument)) {
            throw new VISAException("That instrument has not been opened!");
        }

        return instrDrivers.get(instrument).read(
                instrIDs.get(instrument),
                bufferSize
        ).trim();

    }

    /**
     * Read from the given instrument, specified by instrument handle returned by openInstrument()
     *
     * @param instrument Instrument handle from openInstrument()
     *
     * @return The read string from the device
     *
     * @throws VISAException Upon error with VISA interface
     */
    public static String read(long instrument) throws VISAException {
        return read(instrument, 1024);
    }

    /**
     * Closes the connection to the given instrument, specified by instrument handle returned by openInstrument()
     *
     * @param instrument Instrument handle from openInstrument()
     *
     * @throws VISAException Upon error with VISA interface
     */
    public static void closeInstrument(long instrument) throws VISAException {

        // Check that we have actually opened this device
        if (!instrIDs.containsKey(instrument)) {
            throw new VISAException("That instrument has not been opened!");
        }

        instrDrivers.get(instrument).close(instrIDs.get(instrument));

    }

    /**
     * Sets whether to send EOI at the end of talking (mostly for GPIB)
     *
     * @param instrument Instrument handle from openInstrument()
     * @param set        Should it send?
     *
     * @throws VISAException Upon error with VISA interface
     */
    public static void setEOI(long instrument, boolean set) throws VISAException {
        // Check that we have actually opened this device
        if (!instrIDs.containsKey(instrument)) {
            throw new VISAException("That instrument has not been opened!");
        }

        instrDrivers.get(instrument).setEOI(instrIDs.get(instrument), set);
    }

    /**
     * Sets the timeout for read/write to/from instrument
     *
     * @param instrument  Instrument handle from openInstrument()
     * @param timeoutMSec Timeout in milliseconds
     *
     * @throws VISAException Upon error with VISA interface
     */
    public static void setTimeout(long instrument, long timeoutMSec) throws VISAException {
        // Check that we have actually opened this device
        if (!instrIDs.containsKey(instrument)) {
            throw new VISAException("That instrument has not been opened!");
        }

        instrDrivers.get(instrument).setTMO(instrIDs.get(instrument), timeoutMSec);
    }

    public static void setTerminationCharacter(long instrument, long eos) throws VISAException {
        // Check that we have actually opened this device
        if (!instrIDs.containsKey(instrument)) {
            throw new VISAException("That instrument has not been opened!");
        }

        instrDrivers.get(instrument).setEOS(instrIDs.get(instrument), eos);
    }

    public static void setSerialParameters(long instrument, int baudRate, int dataBits, Parity parity, StopBits stopBits, Flow flowControl) throws VISAException {
        // Check that we have actually opened this device
        if (!instrIDs.containsKey(instrument)) {
            throw new VISAException("That instrument has not been opened!");
        }

        instrDrivers.get(instrument).setSerial(instrIDs.get(instrument), baudRate, dataBits, parity, stopBits, flowControl);

    }

    public enum Parity {
        NONE(0),
        ODD(1),
        EVEN(2),
        MARK(3),
        SPACE(4);

        private int value;

        Parity(int v) {
            value = v;
        }

        public int toInt() {
            return value;
        }

    }

    public enum StopBits {
        ONE(10),
        ONE_HALF(15),
        TWO(20);

        private int value;

        StopBits(int v) {
            value = v;
        }

        public int toInt() {
            return value;
        }

    }

    public enum Flow {
        NONE(0),
        XON_XOFF(1),
        RTS_CTS(2),
        DTR_DSR(4);

        private int value;

        Flow(int v) {
            value = v;
        }

        public int toInt() {
            return value;
        }

    }

}
