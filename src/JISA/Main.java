package JISA;

import JISA.Addresses.GPIBAddress;
import JISA.Addresses.InstrumentAddress;
import JISA.Addresses.StrAddress;
import JISA.Control.DCPowerLockInController;
import JISA.Control.SetGettable;
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

    public static void main(String[] args) {

        GUI.startGUI();

        ResultList list = new ResultList("Frequency", "Voltage", "Current");
        list.setUnits("Hz", "V", "A");

        Plot plot = new Plot("Results", list, 1, 2, 0);
        plot.show();

        for (double f = 0; f <= 10; f++) {

            for (double x = 0; x < 50; x += 5) {

                list.addData(f, x, f*x);
                Util.sleep(150);

            }

        }

    }

    public static void old(String[] args) {

        // Start the GUI thread
        GUI.startGUI();
        try {

            // Ask the user if they want to perform a test
            boolean result = GUI.confirmWindow("JISA", "JISA Library", "JISA - William Wood - 2018\n\nPerform VISA test?");

            // If they press "Cancel", then exit.
            if (!result) {
                Platform.exit();
                return;
            }

            // Trigger VISA initialisation before we try browsing.
            VISA.init();

            // Keep going until they press cancel
            while (true) {

                InstrumentAddress address = GUI.browseVISA();

                if (address == null) {
                    Platform.exit();
                    System.exit(0);
                }

                // Create the device shell, connect to the device and show
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
