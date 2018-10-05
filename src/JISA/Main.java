package JISA;

import JISA.Addresses.GPIBAddress;
import JISA.Addresses.StrAddress;
import JISA.Devices.*;
import JISA.Experiment.*;
import JISA.GUI.*;
import JISA.GUI.FXML.PlotWindow;
import JISA.VISA.VISA;
import JISA.VISA.VISADevice;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.StringWriter;
import java.util.Random;
import java.util.Scanner;

public class Main extends GUI {

    public static void run() throws Exception {

        Progress prog = new Progress("JISA Library");
        prog.setStatus("Searching for devices...");
        prog.setProgress(-1,1);

        GUI.infoAlert("JISA", "JISA Library", "JISA - William Wood - 2018\nPress okay to perform test.");
        prog.show();

        StringWriter writer = new StringWriter();
        for (StrAddress a : VISA.getInstruments()) {
            writer.append("* ");
            writer.append(a.getVISAAddress());
            VISADevice dev = new VISADevice(a);
            dev.setTimeout(500);
            writer.append(" - ");
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
            Util.exceptionHandler(e);
        }

    }

}
