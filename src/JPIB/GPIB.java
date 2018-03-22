package JPIB;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class GPIB {

    private static GPIB instance;
    private static HashMap<Integer, ArrayList<Integer>> buses = new HashMap<>();
    private static HashMap<Integer, Boolean> initialised = new HashMap<>();

    static {
        System.loadLibrary("JPIB");
    }

    public void addFound(int bus, int id) {

        if (!buses.containsKey(bus)) {
            buses.put(bus, new ArrayList<>());
        }

        buses.get(bus).add(id);

    }

    private native void init(int bus) throws IOException;

    private native void scan(int bus) throws IOException;

    private native int open(int bus, int address, int timeout, int EOI, int EOS) throws IOException;

    private native void write(int device, String command) throws IOException;

    private native String read(int device) throws IOException;

    public static void initialise(int bus) throws IOException {
        instance = new GPIB();
        instance.init(bus);
        initialised.put(bus, true);
    }

    public static boolean isInitialised(int bus) {

        return initialised.getOrDefault(bus, false);

    }

    public static void scanForDevices(int bus) throws IOException {

        if (buses.containsKey(bus)) {
            buses.get(bus).clear();
        } else {
            buses.put(bus, new ArrayList<>());
        }

        instance.scan(bus);

    }

    public static int openDevice(int bus, int address, int timeout, int EOI, int EOS) throws IOException {
        return instance.open(bus, address, timeout, EOI, EOS);
    }

    public static void writeCommand(int device, String command) throws IOException {
        instance.write(device, command);
    }

    public static String readDevice(int device) throws IOException {
        return instance.read(device);
    }

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

        int count = buses.get(bus).size();
        GPIBDevice[] devices = new GPIBDevice[count];

        for (int i = 0; i < count; i ++) {
            int address = buses.get(bus).get(i);
            devices[i] = new GPIBDevice(bus, address);
        }

        return devices;

    }

}
