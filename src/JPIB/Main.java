package JPIB;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException, DeviceException {

        // Initialise the GPIB bus 0
        GPIB.initialise(0);

        // Attempt to connect to the ITC503 at address 20 on bus 0
        ITC503 itc = new ITC503(0, 20);

        // Query the instrument for its ID and print
        System.out.print("Temperature Controller: ");
        System.out.println(itc.query("V"));

        final long start = System.currentTimeMillis();

        // Take a temperature measurement every 500 ms until we get 10 measurements
        Trigger.onInterval(

                // Condition for loop to end
                (i) -> i >= 10,

                // Interval in milliseconds
                500,

                // What to do on each interval (i is an integer equal to the iteration number starting from 0)
                (i) -> {
                    System.out.printf(
                            "Measurement %02d: T1 = %f K @ t = %d ms\n",
                            i + 1,
                            itc.getTemperature(1),
                            System.currentTimeMillis() - start
                    );
                },

                // What to do when done
                () -> {
                    System.out.println("Done!");
                },

                // What to do when an exception is thrown
                (e) -> {
                    System.err.printf("There was an error: \"%s\"", e.getMessage());
                }

        );

    }
}
