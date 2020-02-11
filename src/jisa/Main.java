package jisa;

import javafx.application.Platform;
import jisa.addresses.Address;
import jisa.addresses.GPIBAddress;
import jisa.addresses.StrAddress;
import jisa.devices.K1234;
import jisa.devices.SMU;
import jisa.experiment.Combination;
import jisa.gui.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Main {

    private final static int CHOICE_SCAN = 0;
    private final static int CHOICE_ADDR = 1;
    private final static int CHOICE_HELP = 2;
    private final static int CHOICE_EXIT = 3;

    public static void main(String[] args) {

        try {

            RangeInput range = new RangeInput("Test", "V");

            range.show();

            System.in.read();

            Doc doc = new Doc("Help");

            doc.addImage(Main.class.getResource("gui/images/jisa.png"))
               .setAlignment(Doc.Align.CENTRE);

            doc.addHeading("Testing Utility")
               .setAlignment(Doc.Align.CENTRE);

            doc.addText("This is the built-in testing utility for JISA. Using this utility, you can:");

            doc.addList(false)
               .addItem("Scan for instruments, to see what instruments JISA can detect")
               .addItem("Enter address manually, to connect to an instrument with a known address")
               .addItem("Exit, to exit this utility");

            doc.addText("For more information regarding how to include and use this library in your project, take a look at the JISA wiki at:");

            doc.addLink("https://github.com/OE-FET/JISA/wiki", "https://github.com/OE-FET/JISA/wiki")
               .setAlignment(Doc.Align.CENTRE);

            while (true) {

                // Ask the user if they want to perform a test
                int result = GUI.choiceWindow(
                    "JISA",
                    "JISA Library - William Wood - 2018-2020",
                    "What would you like to do?",
                    "Scan for Instruments",
                    "Enter Address Manually",
                    "Help",
                    "Exit"
                );

                switch (result) {

                    case CHOICE_SCAN:

                        Address address = GUI.browseVISA();

                        if (address == null) {
                            break;
                        }

                        // Create the device shell, connect to the device and show
                        DeviceShell shell = new DeviceShell(address);
                        shell.connect();
                        shell.showAndWait();
                        break;

                    case CHOICE_ADDR:

                        String[] values = GUI.inputWindow(
                            "JISA",
                            "Input Address",
                            "Please type the VISA address to connect to...",
                            "Address"
                        );

                        if (values == null) {
                            break;
                        }

                        DeviceShell conShell = new DeviceShell(new StrAddress(values[0]));
                        conShell.connect();
                        conShell.showAndWait();
                        break;

                    case CHOICE_HELP:

                        doc.showAndWait();
                        break;

                    case CHOICE_EXIT:

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
