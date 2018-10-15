package JISA;

import JISA.Addresses.GPIBAddress;
import JISA.Addresses.InstrumentAddress;
import JISA.Addresses.StrAddress;
import JISA.Control.DCPowerLockInController;
import JISA.Devices.*;
import JISA.Experiment.ResultList;
import JISA.GUI.*;
import JISA.VISA.VISA;
import JISA.VISA.VISADevice;
import com.sun.jna.Native;
import javafx.application.Platform;
import javafx.scene.paint.Color;

import java.io.*;

public class Main {

    public static void test(String[] args) {

        try {
            run();
        } catch (Exception e) {
            Util.exceptionHandler(e);
        }

    }

    public static void run() throws Exception {

        GUI.startGUI();

        MCSMU smu = new DummyMCSMU();

        smu.setVoltage(0, 1);
        smu.setVoltage(1, 1);
        smu.setVoltage(2, 1);
        smu.setVoltage(3, 1);

        ResultList list  = smu.createSweepList();
        Plot       plot  = new Plot("Results", "Voltage [V]", "Current [A]");
        Table      table = new Table("Results", list);
        plot.watchList(list, 0, 1, "Channel 1", Color.RED);
        plot.watchList(list, 2, 3, "Channel 2", Color.GREEN);
        plot.watchList(list, 4, 5, "Channel 3", Color.BLUE);
        plot.watchList(list, 6, 7, "Channel 4", Color.ORANGE);
        MCSMU.Sweep sweep = smu.createNestedSweep();

        sweep.addLinearSweep(0, SMU.Source.VOLTAGE, 0, 10, 6, 0, false);
        sweep.addLinearSweep(1, SMU.Source.VOLTAGE, 1, 10, 11, 250, false);

        Grid grid = new Grid("Results", table, plot);
        grid.show();
        sweep.run(list);

        list.outputTable();

    }

    public static void main(String[] args) {

        GUI.startGUI();
        StringWriter writer = new StringWriter();
        try {


            // Ask the user if they want to perform a test
            boolean result = GUI.confirmWindow("JISA", "JISA Library", "JISA - William Wood - 2018\n\nPerform VISA test?");

            // If they press "Cancel", then exit.
            if (!result) {
                Platform.exit();
                return;
            }

            while (true) {

                InstrumentAddress address = GUI.browseVISA();

                if (address == null) {
                    Platform.exit();
                    System.exit(0);
                }

                DeviceShell shell = new DeviceShell(address);
                shell.connect();
                shell.showAndWait();

            }

        } catch (Exception | Error e) {
            Util.sleep(500);
            StringWriter w = new StringWriter();
            w.append(e.getMessage());
            w.append("\n\n");
            e.printStackTrace(new PrintWriter(w));
            GUI.errorAlert("JISA Library", "Exception Encountered", w.toString(), 800);
            Platform.exit();
            System.exit(0);
        }

    }

}
