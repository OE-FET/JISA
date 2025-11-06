package jisa;

import jisa.addresses.Address;
import jisa.addresses.SerialAddress;
import jisa.devices.translator.PriorProScan;
import jisa.gui.*;

import java.time.LocalDateTime;

public class Main {

    private final static int CHOICE_SCAN = 0;
    private final static int CHOICE_ADDR = 1;
    private final static int CHOICE_HELP = 2;
    private final static int CHOICE_EXIT = 3;

    public static void main(String[] args) {

        try {

            Form form1 = new Form("Form 1");
            Form form2 = new Form("Form 2");
            Form form3 = new Form("Form 3");
            Form form4 = new Form("Form 4");
            Form form5 = new Form("Form 5");
            Form form6 = new Form("Form 6");
            Form form7 = new Form("Form 7");
            Form form8 = new Form("Form 8");

            Grid grid1 = new Grid(form1, form2, form3, form4, form5, form6, form7, form8);

            grid1.setMinColumnWidth(500.0);

            grid1.showAndWait();

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
                    String.format("JISA Library - William Wood - 2018-%d", LocalDateTime.now().getYear()),
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

                        String[] values = GUI.inputWindow("JISA", "Input Address", "Please type the VISA address to connect to...", "Address");

                        if (values == null) {
                            break;
                        }

                        DeviceShell conShell = new DeviceShell(Address.parse(values[0]));
                        conShell.connect();
                        conShell.showAndWait();
                        break;


                    case CHOICE_HELP:

                        doc.showAsAlert();
                        break;


                    case CHOICE_EXIT:

                        System.exit(0);
                        break;


                }

            }

        } catch (Exception | Error e) {

            GUI.showException(e);
            GUI.stopGUI();
            System.exit(0);

        }

    }

}
