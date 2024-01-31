package jisa.visa;

import com.sun.jna.Platform;
import jisa.Util;
import jisa.addresses.Address;
import jisa.addresses.TCPIPAddress;
import jisa.visa.connections.Connection;
import jisa.visa.drivers.*;
import jisa.visa.exceptions.ConnectionFailedException;
import jisa.visa.exceptions.NoCompatibleDriversException;
import jisa.visa.exceptions.VISAException;

import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Static class for accessing VISA-like communication libraries
 */
public class VISA {

    /**
     * Functional interface for instantiating driver classes
     */
    private interface DriverInit {
        Driver create() throws VISAException;
    }

    private final static List<Driver>       loadedDrivers = new LinkedList<>();
    private final static Map<Class, Driver> lookup;

    private final static Map<String, DriverInit> DRIVERS = Util.buildMap(drivers -> {

        drivers.put("RS VISA", RSVISADriver::new);
        drivers.put("AG VISA", AGVISADriver::new);
        drivers.put("NI VISA", NIVISADriver::new);
        drivers.put("Linux GPIB", LinuxGPIBDriver::new);
        drivers.put("NI GPIB", NIGPIBDriver::new);
        drivers.put("Serial", SerialDriver::new);
        drivers.put("TCP-IP", TCPIPDriver::new);

        drivers.put("Experimental USB", () -> {

            // Only use if Linux, no VISA driver loaded, or jisa-usb.enable file exists in home directory
            boolean isLinux   = Platform.isLinux();
            boolean noVISA    = loadedDrivers.stream().noneMatch(d -> d instanceof VISADriver);
            boolean isEnabled = Paths.get(System.getProperty("user.home"), "jisa-usb.enable").toFile().exists();

            if (isLinux || noVISA || isEnabled) {
                return new USBDriver();
            } else {
                throw new VISAException("Not Enabled");
            }

        });

    });

    static {

        Locale.setDefault(Locale.US);
        System.out.println("Attempting to load drivers:");

        // Need to know this to align text
        int maxLength = DRIVERS.keySet().stream().mapToInt(String::length).max().orElse(0);

        DRIVERS.forEach((name, inst) -> {

            System.out.printf("- Loading %s Driver...", name);

            for (int i = name.length(); i < maxLength; i++) {
                System.out.print(" ");
            }

            try {
                loadedDrivers.add(inst.create());
                System.out.println(" [Success]");
            } catch (VISAException exception) {
                System.out.printf(" [Failed]\t(%s)%n", exception.getMessage());
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

    /**
     * Resets all loaded drivers. This is liable to close all active connections.
     */
    public static void resetDrivers() {

        for (Driver driver : loadedDrivers) {

            try {
                driver.reset();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    /**
     * Returns the loaded driver with the specified class. Returns null if no such driver is loaded.
     *
     * @param driverClass The class of the driver to return
     * @param <T>         The class of the driver to return
     *
     * @return The matching driver object, or null.
     */
    public static <T extends Driver> T getDriver(Class<T> driverClass) {
        return (T) lookup.getOrDefault(driverClass, null);
    }

    /**
     * Returns a List of all instrument addressed detected by VISA
     *
     * @return List of instrument addresses
     */
    public static List<Address> listInstruments() {

        return loadedDrivers.stream()
                            .map(Driver::search)
                            .flatMap(List::stream)
                            .map(Address::getJISAString)
                            .map(String::trim)
                            .distinct()
                            .map(Address::parse)
                            .collect(Collectors.toUnmodifiableList());

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
     * @throws NoCompatibleDriversException If there are no VISA drivers loaded compatible with the given address
     * @throws ConnectionFailedException    If no available and compatible drivers were able to open a connection
     */
    public static Connection openInstrument(Address address, Class<? extends Driver> preferredDriver) throws VISAException {

        // If a preferred driver has been specified, try that first
        if (preferredDriver != null && lookup.containsKey(preferredDriver)) {
            try { return lookup.get(preferredDriver).open(address); } catch (Exception ignored) { }
        }

        // Workaround to use internal TCP-IP implementation since there seems to be issues with TCP-IP Sockets and NI-VISA
        if (address instanceof TCPIPAddress && lookup.containsKey(TCPIPDriver.class)) {
            try { return lookup.get(TCPIPDriver.class).open(address); } catch (Exception ignored) { }
        }

        // Get a list of all compatible VISA Driver objects
        List<Driver> compatible = loadedDrivers.stream()
                                               .filter(driver -> driver.worksWith(address))
                                               .collect(Collectors.toUnmodifiableList());

        if (compatible.isEmpty()) {
            throw new NoCompatibleDriversException(address);
        }

        // Map to record exceptions thrown by different drivers
        Map<Driver, Exception> errors = new LinkedHashMap<>();

        // Try each driver in order
        for (Driver driver : compatible) {

            try {
                return driver.open(address);     // If it worked, then let's use it!
            } catch (VISAException exception) {
                errors.put(driver, exception);   // If it didn't, record the error
            }

        }

        // If we get here, then nothing worked
        throw new ConnectionFailedException(address, errors);

    }

}
