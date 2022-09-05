package jisa.visa;

import jisa.Util;
import jisa.addresses.Address;
import jisa.gui.GUI;
import jisa.visa.connections.Connection;
import jisa.visa.drivers.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Static class for accessing the native VISA library in a more Java-friendly way
 */
public class VISA {

    private final static ArrayList<Driver>      drivers = new ArrayList<>();
    private final static HashMap<Class, Driver> lookup  = new HashMap<>();

    static {
        Locale.setDefault(Locale.US);

        System.out.println("Attempting to load drivers.");

        try {
            System.out.print("Trying NI VISA driver...             \t");
            NIVISADriver.init();
            drivers.add(new NIVISADriver());
            System.out.println("Success.");
        } catch (VISAException ignored) {
            System.out.println("Nope.");
        }

        try {
            System.out.print("Trying RS VISA driver...             \t");
            RSVISADriver.init();
            drivers.add(new RSVISADriver());
            System.out.println("Success.");
        } catch (VISAException ignored) {
            System.out.println("Nope.");
        }

        try {
            System.out.print("Trying Agilent VISA driver...        \t");
            AgilentVISADriver.init();
            drivers.add(new AgilentVISADriver());
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
            drivers.add(new TCPIPDriver());
            System.out.println("Success.");
        } catch (Exception | Error ignored) {
            System.out.println("Nope.");
        }

        try {
            System.out.print("Trying USB-TMC driver...             \t");
            drivers.add(new USBTMCDriver());
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
    public static List<Address> listInstruments() throws VISAException {

        List<Address> addresses = new LinkedList<>();

        for (Driver driver : drivers) {

            addresses.addAll(
                driver.search()
                      .stream()
                      .filter(a -> addresses.stream().noneMatch(b -> b.toString().trim().equalsIgnoreCase(a.toString().trim())))
                      .collect(Collectors.toUnmodifiableList())
            );

        }

        return addresses;

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
            } catch (Exception ignored) {}

        }

        // Workaround to use internal TCP-IP implementation since there seems to be issues with TCP-IP Sockets and NI-VISA
        if (address.getType() == Address.Type.TCPIP) {

            try {
                connection = lookup.get(TCPIPDriver.class).open(address);
                return connection;
            } catch (Exception ignored) {}

        }

        boolean tried = false;

        // Try each driver in order
        for (Driver d : drivers) {

            if (d.worksWith(address)) {

                tried = true;

                try {
                    connection = d.open(address);
                    break;                      // If it worked, then let's use it!
                } catch (VISAException e) {
                    errors.add(String.format("* %s: %s", d.getClass().getSimpleName(), e.getMessage()));
                }

            }

        }

        if (!tried) {
            throw new VISAException("No drivers available that support connecting to %s", address.toString());
        }

        // If no drivers worked
        if (connection == null) {
            throw new VISAException("Could not open %s using any driver%n%s", address.toString(), String.join("\n", errors));
        }

        return connection;

    }

}
