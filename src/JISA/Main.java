package JISA;

import JISA.Addresses.InstrumentAddress;
import JISA.Addresses.StrAddress;
import JISA.GUI.DeviceShell;
import JISA.GUI.GUI;
import JISA.VISA.VISA;
import javafx.application.Platform;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Main {

    private final static int CHOICE_SCAN = 0;
    private final static int CHOICE_ADDR = 1;
    private final static int CHOICE_HELP = 2;
    private final static int CHOICE_EXIT = 3;

    public static void main(String[] args) {

        // Start the GUI thread
        GUI.startGUI();

        try {
            VISA.init();

            while (true) {

                // Ask the user if they want to perform a test
                int result = GUI.choiceWindow("JISA", "JISA Library - William Wood - 2018", "What would you like to do?", "Scan for Instruments", "Enter Address Manually", "Help", "Exit");

                switch (result) {

                    case CHOICE_SCAN:
                        InstrumentAddress address = GUI.browseVISA();

                        if (address == null) {
                            break;
                        }

                        // Create the device shell, connect to the device and show
                        DeviceShell shell = new DeviceShell(address);
                        shell.connect();
                        shell.showAndWait();
                        break;

                    case CHOICE_ADDR:
                        String[] values = GUI.inputWindow("JISA", "Input Address", "Please type the VISA address to connect to...", "Address");

                        if (values == null) {
                            break;
                        }

                        DeviceShell conShell = new DeviceShell(new StrAddress(values[0]));
                        conShell.connect();
                        conShell.showAndWait();
                        break;

                    case CHOICE_HELP:
                        GUI.infoAlert("JISA", "Help", "You can use this built-in utility to test that JISA works.\n\n" +
                                "Use \"Scan for Instruments\" to see what instruments are visible to JISA.\n\n" +
                                "Use \"Enter Address Manually\" if you know the VISA address to connect to.", 650);
                        break;

                    case CHOICE_EXIT:
                        GUI.stopGUI();
                        System.exit(0);
                        break;


                }

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
