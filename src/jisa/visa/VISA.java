package jisa.visa;

import jisa.addresses.Address;
import jisa.addresses.StrAddress;
import jisa.gui.GUI;
import jisa.Util;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Static class for accessing the native VISA library in a more Java-friendly way
 */
public class VISA {

    private static ArrayList<Driver>      drivers = new ArrayList<>();
    private static HashMap<Class, Driver> lookup  = new HashMap<>();
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

        for (Driver d : drivers) {
            lookup.put(d.getClass(), d);
        }

        if (drivers.isEmpty()) {
            Util.sleep(500);
            Util.errLog.println("ERROR: Could not load any drivers!");

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
                    if (s.trim().equals(a.toString().trim())) {
                        found = true;
                    }
                }
                if (!found) {
                    addresses.put(a.toString(), a);
                }
            }
        }

        StrAddress[] toReturn = new StrAddress[addresses.size()];
        int          count    = 0;

        for (Address.Type t : Address.Type.values()) {

            for (StrAddress a : addresses.values()) {

                if (a.getType().equals(t)) {
                    toReturn[count] = a;
                    count++;
                }

            }

        }

        return toReturn;

    }

    public static Connection openInstrument(Address address) throws VISAException {
        return openInstrument(address, null);
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
    public static Connection openInstrument(Address address, Class<? extends Driver> preferredDriver) throws VISAException {

        Connection        connection = null;
        ArrayList<String> errors     = new ArrayList<>();

        if (preferredDriver != null && lookup.containsKey(preferredDriver)) {

            try {
                connection = lookup.get(preferredDriver).open(address);
                return connection;
            } catch (VISAException ignored) { }

        }

        // Try each driver in order
        for (Driver d : drivers) {

            try {
                connection = d.open(address);
                break;                      // If it worked, then let's use it!
            } catch (VISAException e) {
                errors.add(String.format("* %s: %s", d.getClass().getSimpleName(), e.getMessage()));
            }

        }

        // If no drivers worked
        if (connection == null) {
            throw new VISAException("Could not open %s using any driver:\n%s", address.toString(), String.join("\n", errors));
        }

        return connection;

    }

}
