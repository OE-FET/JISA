package JPIB;

import javax.xml.crypto.Data;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

public class Example {

    private static SR830 sr830 = null;

    private static final int GPIB_BUS      = 0;
    private static final int SR830_ADDRESS = 30;

    private static final double     MIN_FREQUENCY  = 0.5;   // 0.5 Hz
    private static final double     MAX_FREQUENCY  = 10.0;   // 1.0 Hz
    private static final double     FREQUENCY_STEP = 0.1;   // 0.1 Hz
    private static final double     AMPLITUDE      = 50e-3; // 50  mV
    private static final double     PREAMP_GAIN    = 5.0;
    private static       ResultList results;

    public static void run() {

        try {

            initialise();
            takeMeasurements();
            outputResults();

        } catch (Exception e) {
            Util.exceptionHandler(e);
        }

    }

    private static void initialise() throws Exception {

        System.out.print("Initialising the bus... ");

        // Clear the bus
        GPIB.initialise(GPIB_BUS);

        System.out.println("Done!");

        System.out.print("Connecting to SR830... ");

        // Connect to lock-in
        sr830 = new SR830(GPIB_BUS, SR830_ADDRESS);

        System.out.println("Done!");


        System.out.print("Configuring SR830... ");

        // Set to internal reference mode and 3s time constant
        sr830.setRefMode(SR830.RefMode.INTERNAL);
        sr830.setTimeConst(SR830.TimeConst.T_3s);

        System.out.println("Done!");

        System.out.print("Setting initial frequency... ");

        // Set the starting frequency, amplitude and phase
        sr830.setRefFrequency(MIN_FREQUENCY);
        sr830.setRefAmplitude(AMPLITUDE);
        sr830.setRefPhase(0.0);

        // Wait for everything to settle down
        sr830.waitForStableLock();

        System.out.println("Done!");

    }

    private static void takeMeasurements() throws Exception {

        // Setup the results container
        results = new ResultList("Frequency", "Set Amplitude", "Measured Amplitude", "Error");
        results.setUnits("Hz", "V", "V", "%");

        for (double f = MIN_FREQUENCY; f <= MAX_FREQUENCY; f += FREQUENCY_STEP) {

            System.out.printf("Waiting for stable %f Hz lock... ", f);

            sr830.setRefFrequency(f);
            sr830.waitForStableLock(0.1, 5000);

            System.out.println("Locked!");

            System.out.printf("Taking measurement for %f Hz... ", f);

            SR830.DataPacket data = sr830.getAll();
            double           amp  = sr830.getRefAmplitude() * PREAMP_GAIN;

            results.addData(
                    data.f,
                    amp,
                    data.r,
                    100D * Math.abs(data.r - amp) / amp
            );


            System.out.println("Done!");

        }

    }

    private static void outputResults() throws Exception {

        System.out.println("Measurements Complete.");
        System.out.println("");
        results.outputTable();
        results.outputMATLAB(System.getProperty("user.home") + "/output.m", "F", "A", "R", "E");

    }

}
