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

    private static ArrayList<Driver> drivers = new ArrayList<>();
    private static long              counter = 0;

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

        try {
            System.out.print("Trying Raw TCP-IP driver...          \t");
            drivers.add(new RawTCPIPDriver());
            System.out.println("Success.");
        } catch (Exception | Error ignored) {
            System.out.println("Nope.");
        }

        if (drivers.size() == 0) {
            Util.sleep(500);
            System.err.println("ERROR: Could not load any drivers!");

            try {
                GUI.errorAlert("JISA Library", "No Drivers", "Could not load any drivers for instrument control!\n\nCheck your driver installation(s).");
            } catch (Exception | Error ignored) {
            }

            System.exit(1);
        } else {
            System.out.printf("Successfully loaded %d drivers.\n", drivers.size());
        }

    }

    public static void init() {
    }

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

        StrAddress[] toReturn = new StrAddress[addresses.size()];
        int          count    = 0;

        for (InstrumentAddress.Type t : InstrumentAddress.Type.values()) {

            for (StrAddress a : addresses.values()) {

                if (a.getType().equals(t)) {
                    toReturn[count] = a;
                    count++;
                }

            }

        }

        return toReturn;

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
    public static Connection openInstrument(InstrumentAddress address) throws VISAException {

        Connection        connection = null;
        ArrayList<String> errors     = new ArrayList<>();

        // Try each driver in order
        for (Driver d : drivers) {

            try {
                connection = d.open(address);
                break;                      // If it worked, then let's use it!
            } catch (VISAException e) {
                errors.add(e.getMessage());
            }

        }

        // If no drivers worked
        if (connection == null) {
            System.err.println(String.join("Driver Errors:\n", errors));
            throw new VISAException("Could not open %s using any driver!", address.getVISAAddress());
        }

        return connection;

    }

}
