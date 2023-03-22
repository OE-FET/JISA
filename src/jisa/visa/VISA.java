package jisa.visa;

import jisa.Util;
import jisa.addresses.Address;
import jisa.addresses.TCPIPAddress;
import jisa.visa.connections.Connection;
import jisa.visa.drivers.*;

import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Static class for accessing the native VISA library in a more Java-friendly way
 */
public class VISA {

    private interface DriverInit {
        Driver create() throws VISAException;
    }

    private final static Map<String, DriverInit> DRIVERS = Util.buildMap(map -> {

        map.put("RS VISA", RSVISADriver::new);
        map.put("AG VISA", AGVISADriver::new);
        map.put("NI VISA", NIVISADriver::new);
        map.put("Linux GPIB", GPIBDriver::new);
        map.put("NI GPIB", NIGPIBDriver::new);
        map.put("Serial", SerialDriver::new);
        map.put("TCP-IP", TCPIPDriver::new);

        map.put("Experimental USB", () -> {

            if (Paths.get(System.getProperty("user.home"), "jisa-usb.enable").toFile().exists()) {
                return new USBDriver();
            } else {
                throw new VISAException("Not Enabled");
            }

        });

    });

    private final static ArrayList<Driver>  loadedDrivers = new ArrayList<>();
    private final static Map<Class, Driver> lookup;

    static {

        Locale.setDefault(Locale.US);
        System.out.println("Attempting to load drivers:");

        int maxLength = DRIVERS.keySet().stream().mapToInt(String::length).max().orElse(0);

        DRIVERS.forEach((name, inst) -> {

            System.out.printf("- Loading %s Driver...", name);

            for (int i = name.length(); i < maxLength; i++) {
                System.out.print(" ");
            }

            try {
                loadedDrivers.add(inst.create());
                System.out.println(" [Success].");
            } catch (VISAException exception) {
                System.out.printf(" [Failed] (%s).%n", exception.getMessage());
            }

        });

        lookup = loadedDrivers.stream().collect(Collectors.toMap(Driver::getClass, d -> d));

        if (loadedDrivers.isEmpty()) {
            System.out.println("No drivers loaded.");
        } else {
            System.out.printf("Successfully loaded %d drivers.%n", loadedDrivers.size());
        }

    }

    public static void init() {
    }

    public static void resetDrivers() {

        for (Driver driver : loadedDrivers) {

            try {
                driver.reset();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    public <T extends Driver> T getDriver(Class<T> driverClass) {
        return (T) lookup.getOrDefault(driverClass, null);
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

        for (Driver driver : loadedDrivers) {

            try {
                addresses.addAll(
                    driver.search()
                          .stream()
                          .filter(a -> addresses.stream().noneMatch(b -> b.toString().trim().equalsIgnoreCase(a.toString().trim())))
                          .collect(Collectors.toUnmodifiableList())
                );
            } catch (Exception ignored) {}

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
        if (address instanceof TCPIPAddress) {

            try {
                connection = lookup.get(TCPIPDriver.class).open(address);
                return connection;
            } catch (Exception ignored) {}

        }

        boolean tried = false;

        // Try each driver in order
        for (Driver d : loadedDrivers) {

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
