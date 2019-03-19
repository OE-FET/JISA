package JISA;

import JISA.Addresses.Address;
import JISA.Addresses.StrAddress;
import JISA.Devices.DeviceException;
import JISA.Devices.DummyMCSMU;
import JISA.Devices.SMU;
import JISA.Experiment.*;
import JISA.GUI.DeviceShell;
import JISA.GUI.GUI;
import JISA.GUI.Plot;
import JISA.GUI.Series;
import JISA.VISA.VISA;
import javafx.application.Platform;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class Main {

    private final static int CHOICE_SCAN = 0;
    private final static int CHOICE_ADDR = 1;
    private final static int CHOICE_HELP = 2;
    private final static int CHOICE_EXIT = 3;

    public static void main(String[] args) throws IOException, DeviceException {

        // Start the GUI thread
        GUI.startGUI();

        ResultTable results = new ResultList("Time", "Voltage", "Current");
        Plot        plot    = new Plot("Results", "Time [s]", "Voltage [V]");
        Series      data    = plot.watchList(results, 0, 1, "Data", Color.BLACK);

        data.setLineWidth(0.0); // Hide the line, only show markers for raw data

        plot.show();

        // Take a measurement every second for 10 seconds
        for (double t = 0; t <= 10.0; t += 1.0) {
            results.addData(t, 4.3*Math.sin(t*0.8), 1);
        }

        PFunction toFit  = (x, p) -> p[0]*Math.sin(p[1]*x);
        Function  fitted = results.fit(0,1,toFit,0.5,0.5);
        Series    fit    = plot.plotFunction(fitted, 0, 10, 1000, "Fit", Color.RED);

        double[] coeffs   = fitted.getCoefficients();
        double   a = coeffs[0];
        double   b = coeffs[1];
        System.out.printf("a = %e, b = %e\n", a, b);
        System.in.read();

        try {
            VISA.init();

            while (true) {

                // Ask the user if they want to perform a test
                int result = GUI.choiceWindow(
                        "JISA",
                        "JISA Library - William Wood - 2018",
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
