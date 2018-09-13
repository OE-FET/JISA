package JISA;

import JISA.Addresses.*;
import JISA.Devices.*;
import JISA.VISA.*;
import JISA.Control.*;

import java.io.IOException;

public class Main {

    private static void run() throws Exception {


        SMU smu1 = new K2450(new GPIBAddress(0, 30));

        SMU.DataPoint[] points = smu1.performLinearSweep(
                SMU.Source.VOLTAGE,
                0,
                20,
                10,
                500
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
