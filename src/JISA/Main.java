package JISA;

import JISA.Addresses.Address;
import JISA.Addresses.StrAddress;
import JISA.Devices.SMU;
import JISA.Experiment.Function;
import JISA.Experiment.ResultList;
import JISA.Experiment.ResultTable;
import JISA.GUI.*;
import JISA.Maths.Maths;
import JISA.Maths.Matrix;
import JISA.VISA.VISA;
import javafx.application.Platform;
import javafx.scene.paint.Color;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Main {

    private final static int CHOICE_SCAN = 0;
    private final static int CHOICE_ADDR = 1;
    private final static int CHOICE_HELP = 2;
    private final static int CHOICE_EXIT = 3;

    public static void main(String[] args) {

        try {

            InstrumentConfig<SMU> smu1 = new InstrumentConfig.SMU("SMU 1");
            InstrumentConfig<SMU> smu2 = new InstrumentConfig.SMU("SMU 2");

            (new Grid(smu1, smu2)).show();

            Dashboard               dash     = new Dashboard("Dashboard");
            Dashboard.Category<SMU> category1 = dash.addInstrument("Source-Drain", smu1);

            category1.addMeasurement("Source-Drain Voltage", "V", SMU::getVoltage);
            category1.addMeasurement("Drain Current", "A", SMU::getCurrent);

            Dashboard.Category<SMU> category2 = dash.addInstrument("Source-Gate", smu2);

            category2.addMeasurement("Gate Voltage", "V", SMU::getVoltage);
            category2.addMeasurement("Gate Current", "A", SMU::getCurrent);

            dash.show();
            dash.setMaximised(true);

            System.in.read();

            VISA.init();

            while (true) {

                // Ask the user if they want to perform a test
                int result = GUI.choiceWindow(
                        "JISA",
                        "JISA Library - William Wood - 2018-2019",
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

                        MarkDown md = new MarkDown("Help");

                        md.addLine("## JISA Testing Utility");
                        md.addLine("");
                        md.addLine("This is the built-in testing utility for JISA.");
                        md.addLine("");
                        md.addLine("Using this utility, you can:");
                        md.addLine("");
                        md.addLine("* `\"Scan for Instruments\"` to see what instruments JISA can detect");
                        md.addLine("");
                        md.addLine(
                                "* `\"Enter Address Manually\"` if you want to connect to an instrument with a known address");
                        md.addLine("");
                        md.addLine("* `\"Exit\"` to exit this utility");
                        md.addLine("");
                        md.addLine(
                                "For more information regarding how to include and use this library in your project, take a look at the `JISA` wiki at:");
                        md.addLine("");
                        md.addLine("https://github.com/OE-FET/JISA/wiki");
                        md.addLine("");
                        md.addLine("_Close this window or press `OK` to return to menu_");

                        md.showAndWait();

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
