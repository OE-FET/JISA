package JPIB;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {

    private static SR830  sr830;
    private static ITC503 itc;
    private static K2200  power;
    private static K236   smu;
    private static K2450  k2450;

    private static final int                 GPIB_BUS           = 0;
    private static final int                 SR830_ADDRESS      = 30;
    private static final int                 K2200_ADDRESS      = 22;
    private static final int                 ITC503_ADDRESS     = 20;
    private static final int                 K236_ADDRESS       = 17;
    private static final int                 K2450_ADDRESS      = 2;
    private static final ArrayList<Double[]> results            = new ArrayList<>();
    private static final double              START_FREQUENCY    = 0.5;
    private static final double              STEP_FREQUENCY     = 0.1;
    private static final double              END_FREQUENCY      = 2.5;
    private static final double              LOCKIN_ERROR_PCT   = 0.1;
    private static final int                 LOCKIN_STABLE_TIME = 5000;
    private static double currentStep;

    public static void main(String[] args) {

        try {
            initialise();
        } catch (Exception e) {
            Util.exceptionHandler(e);
        }

    }

    private static void testK2450() throws IOException, DeviceException, InterruptedException {

        GPIB.initialise(GPIB_BUS);

        k2450 = new K2450(GPIB_BUS, K2450_ADDRESS);
        k2450.setSource(K2450.Source.VOLTAGE);
        k2450.setVoltage(50e-3);
        k2450.turnOn();

        System.out.println("Voltage [mV] \t Current [nA]");

        for (int i = 0; i < 11; i++) {
            k2450.setVoltage(50e-3 + (1e-3 * i));
            Thread.sleep(1000);
            System.out.printf("%.2f \t\t\t %.2f\n", k2450.getVoltage()*1e3, k2450.getCurrent() * 1e9);
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
//        itc = new ITC503(GPIB_BUS, ITC503_ADDRESS);
//        power = new K2200(GPIB_BUS, K2200_ADDRESS);
//        smu = new K236(GPIB_BUS, K236_ADDRESS);
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
                LOCKIN_ERROR_PCT,
                LOCKIN_STABLE_TIME,
                100,
                Main::step,
                Util::exceptionHandler
        );

    }

    private static void step() throws Exception {

        System.out.println(" Done!");
        System.out.printf("Taking measurement for %f Hz...", currentStep);

        results.add(new Double[]{
                sr830.getRefFrequency(),
                sr830.getRefAmplitude() * 5.0,
                sr830.getR()
        });

        System.out.println(" Done!");

        if (currentStep < END_FREQUENCY) {

            currentStep += STEP_FREQUENCY;
            sr830.setRefFrequency(currentStep);
            System.out.print("Waiting for stable lock...");
            sr830.onStableLock(
                    LOCKIN_ERROR_PCT,
                    LOCKIN_STABLE_TIME,
                    100,
                    Main::step
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
