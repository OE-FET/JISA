package JPIB;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {

    private static SR830  sr830;
    private static ITC503 itc;
    private static K2200  power;
    private static K236   smu;

    private static final int                 GPIB_BUS       = 0;
    private static final int                 SR830_ADDRESS  = 30;
    private static final int                 K2200_ADDRESS  = 22;
    private static final int                 ITC503_ADDRESS = 20;
    private static final int                 K236_ADDRESS   = 17;
    private static final ArrayList<Double[]> results        = new ArrayList<>();

    enum Exc {

        IO_EXCEPTION(IOException.class),
        DEVICE_EXCEPTION(DeviceException.class),
        INTERRUPTED_EXCEPTION(InterruptedException.class),
        UNKNOWN_EXCEPTION(Exception.class);

        private static HashMap<Class, Exc> lookup = new HashMap<>();

        static {
            for (Exc e: Exc.values()) {
                lookup.put(e.getClazz(), e);
            }
        }

        static Exc fromClass(Class c) {
            return lookup.getOrDefault(c, UNKNOWN_EXCEPTION);
        }

        private Class clazz;

        Exc(Class c) {
            clazz = c;
        }

        Class getClazz() {
            return clazz;
        }

    }

    public static void exceptionHandler(Exception e) {

        Exc exc = Exc.fromClass(e.getClass());

        switch (exc) {

            case IO_EXCEPTION:
                System.err.printf("Communication error: \"%s\"\n", e.getMessage());
                break;
            case DEVICE_EXCEPTION:
                System.err.printf("Device error: \"%s\"\n", e.getMessage());
                break;
            case INTERRUPTED_EXCEPTION:
                System.err.printf("Waiting error: \"%s\"\n", e.getMessage());
                break;
            default:
                System.err.printf("Unknown error: \"%s\"\n", e.getMessage());
                break;
        }

        System.exit(1);

    }

    public static void main(String[] args) {

        try {
            initialise();
        } catch (Exception e) {
            exceptionHandler(e);
        }

    }

    private static void initialise() throws Exception {

        // Clear the bus
        GPIB.initialise(GPIB_BUS);

        // Connect to our instruments
        sr830 = new SR830(GPIB_BUS, SR830_ADDRESS);
        itc = new ITC503(GPIB_BUS, ITC503_ADDRESS);
        power = new K2200(GPIB_BUS, K2200_ADDRESS);
        smu = new K236(GPIB_BUS, K236_ADDRESS);

        // Set the reference mode and control mode of the SR830 and ITC503 respectively
        sr830.setRefMode(SR830.RefMode.EXTERNAL);
        itc.setMode(ITC503.Mode.REMOTE_LOCKED);
        smu.setSourceFunction(K236.Source.CURRENT, K236.Function.DC);

        System.out.print("Waiting for stable temperature...");
        // Set ITC temperature to 100 K and wait until the temperature is stable (within 10%)
        itc.onStableTemperature(1, 292.6, 5000, 100, 10, Main::powerUp);


    }

    private static void powerUp() throws IOException {

        System.out.println(" Done!");
        System.out.print("Waiting for stable voltage...");

        // Set voltage and wait for it to be stable before executing the next function
        final double voltage = 2.5;

        power.setVoltage(voltage);
        power.turnOn();

        power.onStableVoltage(voltage, Main::measure);

    }

    private static void measure() {

        System.out.println(" Done!");
        System.out.println("Taking measurements...");

        final int numMeasurements = 25;
        final int interval        = 1000;

        // Take 10 measurements, 500 ms apart, then move to the outputResults() function
        Asynch.onInterval(
                (i) -> i >= numMeasurements,
                interval,
                (i) -> {
                    results.add(new Double[]{power.getVoltage(), itc.getTemperature(1), sr830.getR(), sr830.getT()});
                    System.out.printf("Measurement %d/%d\n", i+1, numMeasurements);
                },
                Main::outputResults,
                (e) -> {
                    e.printStackTrace();
                    System.exit(1);
                }
        );

    }

    private static void outputResults() throws IOException {

        power.turnOff();

        // Output each measurement set on a new line
        System.out.println("Results:");
        System.out.println("Measurement \t Voltage [V] \t Temperature [K] \t R [V] \t\t T [deg]");

        int count = 1;

        for (Double[] row : results) {

            System.out.printf("%03d \t\t\t %f \t\t %f \t\t %f \t %f\n", count, row[0], row[1], row[2], row[3]);
            count++;

        }

    }

}
