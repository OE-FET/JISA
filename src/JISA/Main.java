package JISA;

import JISA.Addresses.StrAddress;
import JISA.GUI.*;
import JISA.VISA.VISA;
import JISA.VISA.VISADevice;

import java.io.StringWriter;

public class Main {

    public static void run() throws Exception {

        GUI.startGUI();

        Progress prog = new Progress("JISA Library");
        prog.setStatus("Searching for devices...");
        prog.setProgress(-1, 1);

        boolean result = GUI.confirmWindow("JISA", "JISA Library", "JISA - William Wood - 2018\n\nPerform VISA test?");

        if (!result) {
            System.exit(0);
        }

        prog.show();

        StringWriter writer = new StringWriter();
        for (StrAddress a : VISA.getInstruments()) {
            writer.append("* ");
            writer.append(a.getVISAAddress());
            VISADevice dev = new VISADevice(a);
            dev.setTimeout(500);
            writer.append(" \t ");
            try {
                writer.append(dev.getIDN().replace("\n", "").replace("\r", ""));
            } catch (Exception e) {
                writer.append("Unknown Instrument");
            }
            writer.append("\n\n");
        }


        prog.hide();
        GUI.infoAlert("JISA", "Found Devices", writer.toString());
        prog.close();

    }

    public static void main(String[] args) {

        try {
            run();
        } catch (Exception e) {
            GUI.errorAlert("JISA Library", "Exception Encountered", e.getMessage());
            System.exit(1);
        }

    }

}
