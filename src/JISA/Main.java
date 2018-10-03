package JISA;

import JISA.Addresses.GPIBAddress;
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

public class Main extends GUI {

    public static void run() throws Exception {

        ITC503 itc503 = new ITC503(new GPIBAddress(0, 20));
        System.out.println(itc503.getIDN());

    }

    public static void main(String[] args) {

        try {
            run();
        } catch (Exception e) {
            Util.exceptionHandler(e);
        }

    }

}
