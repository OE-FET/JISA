package JISA;

import JISA.Addresses.GPIBAddress;
import JISA.Addresses.StrAddress;
import JISA.Devices.*;
import JISA.Experiment.*;
import JISA.GUI.*;
import JISA.GUI.FXML.PlotWindow;
import JISA.VISA.VISA;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Random;
import java.util.Scanner;

public class Main extends GUI {

    public static void run() throws Exception {

        for (StrAddress a : VISA.getInstruments()) {
            System.out.println(a.getVISAAddress());
        }

    }

    public static void main(String[] args) {

        try {
            run();
        } catch (Exception e) {
            Util.exceptionHandler(e);
        }

    }

}
