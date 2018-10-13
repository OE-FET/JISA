package JISA;

import JISA.Addresses.GPIBAddress;
import JISA.Addresses.StrAddress;
import JISA.Control.DCPowerLockInController;
import JISA.Devices.K2200;
import JISA.Devices.SR830;
import JISA.GUI.*;
import JISA.VISA.VISA;
import JISA.VISA.VISADevice;
import javafx.application.Platform;

import java.io.*;

public class Main extends GUI {


    public static void main(String[] args) {

        Progress     prog   = new Progress("JISA Library");
        StringWriter writer = new StringWriter();
        try {

            // Prepare the progress window
            prog.setStatus("Searching for devices...");
            prog.setProgress(-1, 1);

            // Ask the user if they want to perform a test
            boolean result = GUI.confirmWindow("JISA", "JISA Library", "JISA - William Wood - 2018\n\nPerform VISA test?");

            // If they press "Cancel", then exit.
            if (!result) {
                Platform.exit();
                return;
            }

            // Show the progress window whilst searching
            prog.show();

            // Search for devices
            for (StrAddress a : VISA.getInstruments()) {

                writer.append("* ");
                writer.append(a.getVISAAddress());

                VISADevice dev = new VISADevice(a);
                dev.setTimeout(500);
                dev.setRetryCount(1);

                writer.append(" \t ");

                // Try to get the instrument to identify itself
                try {
                    writer.append(dev.getIDN().replace("\n", "").replace("\r", ""));
                } catch (Exception e) {
                    writer.append("Unknown Instrument");
                }

                writer.append("\n\n");

            }


            prog.close();
            GUI.infoAlert("JISA", "Found Devices", writer.toString(), 1024);
            Platform.exit();

        } catch (Exception e) {
            Util.sleep(500);
            prog.close();
            StringWriter w = new StringWriter();
            w.append(e.getMessage());
            w.append("\n\n");
            PrintWriter pw = new PrintWriter(w);
            e.printStackTrace(pw);
            GUI.errorAlert("JISA Library", "Exception Encountered", w.toString(), 800);
            Platform.exit();
            System.exit(0);
        }

    }

}
