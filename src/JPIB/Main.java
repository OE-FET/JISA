package JPIB;

import java.io.IOException;
import java.util.ArrayList;

public class Main {

    private static SR830  sr830;
    private static ITC503 itc;
    private static K2200  power;

    private static final int                 GPIB_BUS       = 0;
    private static final int                 SR830_ADDRESS  = 30;
    private static final int                 K2200_ADDRESS  = 22;
    private static final int                 ITC503_ADDRESS = 20;
    private static final ArrayList<Double[]> results        = new ArrayList<>();

    public static void main(String[] args) {

        try {

            initialise();

        } catch (IOException e) {
            System.err.printf("Communication error: \"%s\"\n", e.getMessage());
        } catch (DeviceException e) {
            System.err.printf("Device error: \"%s\"\n", e.getMessage());
        } catch (InterruptedException e) {
            System.err.printf("Waiting error: \"%s\"\n", e.getMessage());
        } catch (Exception e) {
            System.err.printf("Unknown error: \"%s\"\n", e.getMessage());
        }

    }

    private static void initialise() throws Exception {

        // Clear the bus
        GPIB.initialise(GPIB_BUS);

        // Connect to our instruments
        sr830 = new SR830(GPIB_BUS, SR830_ADDRESS);
        itc = new ITC503(GPIB_BUS, ITC503_ADDRESS);
        power = new K2200(GPIB_BUS, K2200_ADDRESS);

        // Set the reference mode and control mode of the SR830 and ITC503 respectively
        sr830.setRefMode(SR830.RefMode.EXTERNAL);
        itc.setMode(ITC503.Mode.REMOTE_LOCKED);

        // Set ITC temperature to 100 K and wait until the temperature is stable (within 10%)
        itc.setTemperature(100.0);
        itc.onStableTemperature(1, 100.0, 10, Main::powerUp);


    }

    private static void powerUp() throws IOException {

        // Set voltage and wait for it to be stable before executing the next function
        final double voltage = 2.5;

        power.setVoltage(voltage);
        power.turnOn();

        power.onStableVoltage(voltage, Main::measure);

    }

    private static void measure() {

        final int numMeasurements = 10;
        final int interval        = 500;

        // Take 10 measurements, 500 ms apart, then move to the outputResults() function
        Asynch.onInterval(
                (i) -> i >= numMeasurements,
                interval,
                (i) -> {
                    results.add(new Double[]{power.getVoltage(), itc.getTemperature(1), sr830.getRefFrequency()});
                },
                Main::outputResults,
                (e) -> {
                    e.printStackTrace();
                    System.exit(1);
                }
        );

    }

    private static void outputResults() {

        // Output each measurement set on a new line
        System.out.println("Results:");
        System.out.println("Measurement \t Voltage [V] \t Temperature [K] \t Frequency [Hz]");

        int count = 1;

        for (Double[] row : results) {

            System.out.printf("%03d \t %f \t %f \t %f\n", count, row[0], row[1], row[2]);
            count++;

        }

    }

}
