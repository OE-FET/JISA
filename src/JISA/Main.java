package JISA;

import JISA.Addresses.StrAddress;
import JISA.GUI.*;
import JISA.VISA.VISA;
import JISA.VISA.VISADevice;
import javafx.application.Platform;

import java.io.StringWriter;

public class Main {

    public static void main(String[] args) {

        GUI.startGUI();
        Platform.setImplicitExit(false);
        Progress prog = new Progress("JISA Library");

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

            StringWriter writer = new StringWriter();

            // Search for devices
            for (StrAddress a : VISA.getInstruments()) {

                writer.append("* ");
                writer.append(a.getVISAAddress());

                VISADevice dev = new VISADevice(a);
                dev.setTimeout(500);

                writer.append(" \t ");

                // Try to get the instrument to identify itself
                try {
                    dev.write("*IDN?");
                    writer.append(dev.read(1).replace("\n", "").replace("\r", ""));
                } catch (Exception e) {
                    writer.append("Unknown Instrument");
                }

                writer.append("\n\n");

            }


            prog.close();
            GUI.infoAlert("JISA", "Found Devices", writer.toString());
            Platform.exit();

        } catch (Exception e) {
            prog.close();
            GUI.errorAlert("JISA Library", "Exception Encountered", e.getMessage());
            Platform.exit();
        }

    }

}
