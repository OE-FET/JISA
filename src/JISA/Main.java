package JISA;

import JISA.Addresses.*;
import JISA.Devices.*;
import JISA.VISA.*;
import JISA.Control.*;

import java.io.IOException;

public class Main {

    private static void run() throws Exception {

        // Connect to the device
        K2450 smu1 = new K2450(new GPIBAddress(0, 30));

        // Perform a linear sweep from 0V to 20V in 10 steps with a 500 ms delay
        SMU.DataPoint[] points = smu1.performLinearSweep(
                SMU.Source.VOLTAGE,
                0,
                20,
                10,
                500,
                (count, V, I) -> {
                    // Output step number, voltage and current on each step
                    System.out.printf("%d\t%e\t%e\n", count, V, I);
                }
        );

    }

    public static void main(String[] args) {

        try {
            run();
        } catch (Exception e) {
            Util.exceptionHandler(e);
        }

    }


}
