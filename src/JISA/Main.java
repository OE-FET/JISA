package JISA;

import JISA.Addresses.GPIBAddress;
import JISA.Devices.*;
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

        list = new ResultList("Voltage 1", "Current 1", "Voltage 2", "Current 2", "Voltage 3", "Current 3", "Voltage 4", "Current 4");
        list.setUnits("V", "A", "V", "A", "V", "A", "V", "A");
        plot = new Plot("Plot!", "Voltage [V]", "Current [A]");
        plot.watchList(list, 0, 1, "Channel 1", Color.RED);
        plot.watchList(list, 2, 3, "Channel 2", Color.GREEN);
        plot.watchList(list, 4, 5, "Channel 3", Color.BLUE);
        plot.watchList(list, 6, 7, "Channel 4", Color.ORANGE);
        table = new Table("Table!", list);
        bar = new Progress("Progress");
        grid = new Grid("Yay", plot, table);
        grid.show();

        MCSMU combined = new DummyMCSMU();

        MCSMU.Sweep sweep = combined.createNestedSweep();

        sweep.addLinearSweep(0, SMU.Source.VOLTAGE, 0, 10, 30, 0, false);
        sweep.addLinearSweep(1, SMU.Source.VOLTAGE, 0, 10, 5, 0, false);
        sweep.addLinearSweep(2, SMU.Source.VOLTAGE, 0, 10, 2, 0, false);
        sweep.addLinearSweep(3, SMU.Source.VOLTAGE, 0, 10, 6, 750, true);

        MCIVPoint[] points = sweep.run(list);

    }

    public static void startExperiment() throws Exception {

        Random rand = new Random();

        // Take 10 readings
        for (int i = 0; i < 10; i++) {

            list.addData(
                    rand.nextDouble() * 100D,
                    (double) i,
                    rand.nextDouble() * 100D
            );

            bar.setProgress(i + 1, 10);
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
