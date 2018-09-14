package JISA;

import JISA.Addresses.GPIBAddress;
import JISA.Devices.K2450;
import JISA.Devices.SMU;
import JISA.Experiment.*;

public class Main {

    private static void run() throws Exception {

        SMU smu1 = new K2450(new GPIBAddress(0, 15));
        SMU smu2 = new K2450(new GPIBAddress(0, 16));
        SMU smu3 = new K2450(new GPIBAddress(0, 17));

        ResultList results = new ResultList("Vd", "Id", "Vg", "Ig");
        results.setUnits("V", "A", "V", "A");

        for (double Vd = 0; Vd <= 10; Vd += 2) {

            smu1.setVoltage(Vd);

            for (double Vg = 0; Vd >= -60; Vd -= 5) {

                smu2.setVoltage(Vg);

                Thread.sleep(500);

                results.addData(
                        smu1.getVoltage(),
                        smu1.getCurrent(),
                        smu2.getVoltage(),
                        smu2.getCurrent()
                );

            }

        }

        results.output("fileName.csv");

    }

    public static void main(String[] args) {

        try {
            run();
        } catch (Exception e) {
            Util.exceptionHandler(e);
        }

    }


}
