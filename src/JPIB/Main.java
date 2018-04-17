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

    private static final double START_FREQUENCY = 0.5;
    private static final double STEP_FREQUENCY  = 0.1;
    private static final double END_FREQUENCY   = 2.5;
    private static       double currentStep;

    enum Exc {

        IO_EXCEPTION(IOException.class),
        DEVICE_EXCEPTION(DeviceException.class),
        INTERRUPTED_EXCEPTION(InterruptedException.class),
        UNKNOWN_EXCEPTION(Exception.class);

        private static HashMap<Class, Exc> lookup = new HashMap<>();

        static {
            for (Exc e : Exc.values()) {
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
        System.out.print("Intialising bus...");
        GPIB.initialise(GPIB_BUS);
        System.out.println(" Done!");

        // Connect to our instruments
        System.out.print("Connecting to instruments...");
        sr830 = new SR830(GPIB_BUS, SR830_ADDRESS);
        itc = new ITC503(GPIB_BUS, ITC503_ADDRESS);
        power = new K2200(GPIB_BUS, K2200_ADDRESS);
        smu = new K236(GPIB_BUS, K236_ADDRESS);
        System.out.println(" Done!");

        // Set the reference mode and control mode of the SR830 and ITC503 respectively

        System.out.print("Setting up SR830...");
        sr830.setRefMode(SR830.RefMode.INTERNAL);
        sr830.setTimeConst(SR830.TimeConst.T_3s);

        currentStep = START_FREQUENCY;

        sr830.setRefFrequency(currentStep);
        sr830.setRefAmplitude(50e-3);
        sr830.setRefPhase(0);
        System.out.println(" Done!");

        System.out.print("Waiting for stable lock...");
        sr830.onStableLock(
                0.1,
                5000,
                100,
                Main::step,
                Main::exceptionHandler
        );

    }

    private static void step() throws Exception {

        System.out.println(" Done!");
        System.out.printf("Taking measurement for %f Hz...", currentStep);

        results.add(new Double[]{
                sr830.getRefFrequency(),
                sr830.getRefAmplitude(),
                sr830.getR()
        });

        System.out.println(" Done!");

        if (currentStep < END_FREQUENCY) {

            currentStep += STEP_FREQUENCY;
            sr830.setRefFrequency(currentStep);
            System.out.print("Waiting for stable lock...");
            sr830.onStableLock(
                    1.0,
                    5000,
                    100,
                    Main::step,
                    Main::exceptionHandler
            );

        } else {
            outputResults();
        }

    }

    private static void outputResults() {

        System.out.println("Measurements Complete.");
        System.out.println("======================");
        System.out.println("       RESULTS:       ");
        System.out.println("======================");
        System.out.println("");
        System.out.println("F [Hz] \t\t A [V] \t\t R [V] \t\t E [%]");
        System.out.println("");

        for (Double[] values : results) {

            System.out.printf("%f \t %f \t %f \t %f\n", values[0], values[1], values[2], 100D * Math.abs(values[2] - values[1]) / values[1]);

        }

    }

}
