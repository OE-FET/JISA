package JPIB;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * GPIB Driver class.
 *
 * Interfaces with native code (libJPIB.so) via JNI.
 *
 * JNI expects calls to be made from an object instance, so we instantiate one instance and store it statically so that
 * we may wrap static functions around it and pretend that this is a static class.
 */
public class GPIB {

    // JNI is designed around calls from an object instance (ie not static apparently)
    private static GPIB                                 instance;
    private static HashMap<Integer, ArrayList<Integer>> buses       = new HashMap<>();
    private static HashMap<Integer, Boolean>            initialised = new HashMap<>();

    /**
     * Load the native library and create our instance
     */
    static {
        System.loadLibrary("JPIB");
        instance = new GPIB();
    }

    /**
     * Callback for the native scan function
     *
     * @param bus busID
     * @param id  GPIB address of found device
     */
    public void addFound(int bus, int id) {

        if (!buses.containsKey(bus)) {
            buses.put(bus, new ArrayList<>());
        }

        buses.get(bus).add(id);

    }

    /**
     * JNI Call to initialise the bus
     *
     * @param bus Bus to initialise
     *
     * @throws IOException Upon communication error
     */
    private native void init(int bus) throws IOException;

    /**
     * JNI Call to scan for devices
     *
     * @param bus Which bus to scan
     *
     * @throws IOException Upon communication error
     */
    private native void scan(int bus) throws IOException;

    /**
     * JNI Call to open a device
     *
     * @param bus     Bus the device is on
     * @param address Address if the device on the bus
     * @param timeout What timeout should be used
     * @param EOI     Does this device send the EOI line at the end of talking?
     * @param EOS     What does this devices send to signal the end of a string?
     *
     * @return The device descriptor ID of the opened device
     *
     * @throws IOException Upon communication error
     */
    private native int open(int bus, int address, int timeout, int EOI, int EOS) throws IOException;

    /**
     * JNI call to write to the device.
     *
     * @param device  The device descriptor ID of the device
     * @param command The string to write
     *
     * @throws IOException Upon communication error
     */
    private native void write(int device, String command) throws IOException;

    /**
     * JNI call to address the device to talk and read its response.
     *
     * @param device The device descriptor ID of the device
     *
     * @return The response of the device
     *
     * @throws IOException Upon communication error
     */
    private native String read(int device) throws IOException;

    /**
     * Initialise a bus
     *
     * @param bus The bus to initialise
     *
     * @throws IOException
     */
    public static void initialise(int bus) throws IOException {
        instance.init(bus);
        initialised.put(bus, true);
    }

    /**
     * Has the given bus been initialised?
     *
     * @param bus The bus to check
     *
     * @return boolean
     */
    public static boolean isInitialised(int bus) {
        return initialised.getOrDefault(bus, false);
    }

    /**
     * Scan for devices on the given bus
     *
     * @param bus The bus to scan
     *
     * @throws IOException Upon communication error
     */
    public static void scanForDevices(int bus) throws IOException {

        if (buses.containsKey(bus)) {
            buses.get(bus).clear();
        } else {
            buses.put(bus, new ArrayList<>());
        }

        instance.scan(bus);

    }

    /**
     * Open the device on the given bus at the given address and return its unique descriptor ID.
     *
     * @param bus     The bus the device is on
     * @param address The address on the bus the device is using
     * @param timeout The timeout value for communications with the device
     * @param EOI     Does this device end communications with an EOI line?
     * @param EOS     The byte this device uses to signal the end of a string
     *
     * @return Device descriptor ID
     *
     * @throws IOException Upon communication error
     */
    public static int openDevice(int bus, int address, int timeout, int EOI, int EOS) throws IOException {
        return instance.open(bus, address, timeout, EOI, EOS);
    }

    /**
     * Write to the given device.
     *
     * @param device  The device descriptor ID of the device
     * @param command The string to write
     *
     * @throws IOException Upon communication error
     */
    public static void writeCommand(int device, String command) throws IOException {
        instance.write(device, command);
    }

    /**
     * Address the device to talk and read its response.
     *
     * @param device The device descriptor ID of the device to talk
     *
     * @return The response string
     *
     * @throws IOException Upon communication error
     */
    public static String readDevice(int device) throws IOException {
        return instance.read(device);
    }

    /**
     * Write to the device then address it to talk and read its response
     *
     * @param device  The device descriptor ID of the device to query
     * @param command The string to send
     *
     * @return The response of the device
     *
     * @throws IOException Upon communication error
     */
    public static String queryDevice(int device, String command) throws IOException {
        instance.write(device, command);
        return instance.read(device);
    }

    public static Integer[] getBuses() {
        return buses.keySet().toArray(new Integer[buses.size()]);
    }

    public static Integer[] getAddresses(int bus) {

        if (!buses.containsKey(bus)) {
            return new Integer[0];
        }

        return buses.get(bus).toArray(new Integer[buses.get(bus).size()]);

    }

    public static GPIBDevice[] getDevices(int bus) throws IOException {

        if (!buses.containsKey(bus)) {
            return new GPIBDevice[0];
        }

        int          count   = buses.get(bus).size();
        GPIBDevice[] devices = new GPIBDevice[count];

        for (int i = 0; i < count; i++) {
            int address = buses.get(bus).get(i);
            devices[i] = new GPIBDevice(bus, address);
        }

        return devices;

    }

}
