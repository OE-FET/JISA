package JISA;

import JISA.Addresses.GPIBAddress;
import JISA.Devices.K2450;
import JISA.Devices.SR830;
import JISA.Experiment.*;
import JISA.GUI.*;
import JISA.GUI.FXML.PlotWindow;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Random;

public class Main extends GUI {

    // Declare variable up here so that it's accessible in all methods
    static ResultList list;
    static Progress   bar;
    static Table      table;
    static Plot       plot;
    static Grid       grid;

    public static void run() throws Exception {

        list  = new ResultList("Frequency", "Voltage", "Current");
        list.setUnits("Hz", "V", "A");

        bar   = new Progress("Experiment Progress");
        table = new Table("Results", list);
        plot  = new Plot("Plot of Results", list, 1, 2);
        grid  = new Grid("Experiment Control", bar, table, plot);
        grid.show();
        grid.addToolbarButton("Start", Main::startExperiment);

    }

    public static void startExperiment() throws Exception {

        Random rand = new Random();

        // Take 10 readings
        for (int i = 0; i < 10; i++) {

            list.addData(
                    rand.nextDouble() * 100D,
                    rand.nextDouble() * 100D,
                    rand.nextDouble() * 100D
            );

            bar.setProgress(i+1,10);
            Thread.sleep(1000);

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
