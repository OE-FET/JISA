package JISA;

import JISA.Experiment.*;
import JISA.GUI.FXML.PlotWindow;
import JISA.GUI.Grid;
import JISA.GUI.Plot;
import JISA.GUI.Progress;
import JISA.GUI.Table;
import javafx.application.Application;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Random;

public class Main extends Application {

    private static void run() throws Exception {


        ResultList list = new ResultList("Frequency", "Voltage", "Current");
        list.setUnits("Hz", "V", "A");

        Progress progress = new Progress("Measuring...");
        Table    table    = new Table("Results", list);
        Plot     plot     = new Plot("My Plot Title", "X-Axis Label", "Y-Axis Label");
        plot.watchList(list, 1, 2, "Current", Color.RED);
        plot.watchList(list, 1, 0, "Frequency", Color.BLUE);
        plot.show();

        Random rand = new Random();

        for (int i = 1; i <= 10; i++) {
            list.addData(
                    rand.nextDouble() * 100,
                    (double) i,
                    rand.nextDouble() * 100
            );
            progress.setProgress(i, 10);
            Thread.sleep(500);
        }

        list.outputTable();

    }

    public static void main(String[] args) {


        try {
            run();
        } catch (Exception e) {
            Util.exceptionHandler(e);
        }

    }


    @Override
    public void start(Stage stage) {

    }
}
