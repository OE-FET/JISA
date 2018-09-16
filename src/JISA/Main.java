package JISA;

import JISA.Addresses.GPIBAddress;
import JISA.Devices.*;
import JISA.Experiment.*;
import JISA.GUI.*;
import javafx.application.Application;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Random;
import java.util.RandomAccess;

public class Main extends Application {


    static ResultList     list   = new ResultList("x", "sin(x)", "cos(x)", "sqrt(x)", "log(x)");
    static GridWindow     window = GridWindow.create("Experiment");
    static ProgressWindow prog   = ProgressWindow.create("");
    static TaskListWindow tList  = TaskListWindow.create("List");
    static PlotWindow     plot1  = PlotWindow.create("Sin", list, 0, 1);
    static PlotWindow     plot2  = PlotWindow.create("Cos", list, 0, 2);
    static TableWindow    table  = TableWindow.create("Table", list);
    static String         path;
    static double         minX   = 0;
    static double         maxX   = 4 * Math.PI;
    static double         step   = Math.PI / 10;

    private static void run() throws Exception {

        InputWindow inputs = InputWindow.create("Define the Parameters", true, Main::doExperiment);

        inputs.addDouble("Min X", (v) -> {
            minX = v;
        });

        inputs.addDouble("Step Size", (v) -> {
            step = v;
        });

        inputs.addDouble("Max X", (v) -> {
            maxX = v;
        });

        inputs.addFileSave("Output File", (v) -> {
            path = v;
        });

        window.addPane(inputs);
        window.addPane(prog);
        window.addPane(table);
        window.addPane(plot1);
        window.addPane(plot2);
        window.show();

        prog.setTitleText("Experiment Ready");
        prog.setStatusText("Click start to begin...");

    }

    public static void okay() {
        System.out.println(path);
    }

    public static void doExperiment() throws Exception {
        prog.setTitleText("Running Experiment");
        prog.setStatusText("Taking measurements...");

        for (double d = minX; d <= maxX; d += step) {

            prog.setProgress(d, maxX);

            list.addData(
                    d,
                    Math.sin(d),
                    Math.cos(d),
                    Math.sqrt(d),
                    d > 0 ? Math.log(d) : Math.log(0.0001)
            );

            Thread.sleep(1500);

        }


        prog.setTitleText("DONE");
        prog.setStatusText("yay!");
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
