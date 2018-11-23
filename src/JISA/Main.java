package JISA;

import JISA.Addresses.GPIBAddress;
import JISA.Addresses.InstrumentAddress;
import JISA.Addresses.SerialAddress;
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

        // Start the GUI thread
        GUI.startGUI();
        try {

            Table table = new Table("TABLE");
            Plot  plot  = new Plot("PLOT", "PLOT", "PLOT");
            Grid  grid1  = new Grid("Page One", table, plot);

            Fields fields = new Fields("FIELDS!");
            Grid grid2 = new Grid("Page Two", fields);

            Tabs tabs = new Tabs("Tabs");

            tabs.addTab(grid1);
            tabs.addTab(grid2);

            tabs.show();

//            // Ask the user if they want to perform a test
//            boolean result = GUI.confirmWindow("JISA", "JISA Library", "JISA - William Wood - 2018\n\nPerform VISA test?");
//
//            // If they press "Cancel", then exit.
//            if (!result) {
//                Platform.exit();
//                return;
//            }
//
//            // Trigger VISA initialisation before we try browsing.
//            VISA.init();
//
//            // Keep going until they press cancel
//            while (true) {
//
//                InstrumentAddress address = GUI.browseVISA();
//
//                if (address == null) {
//                    Platform.exit();
//                    System.exit(0);
//                }
//
//                // Create the device shell, connect to the device and show
//                DeviceShell shell = new DeviceShell(address);
//                shell.connect();
//                shell.showAndWait();
//
//            }

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
