package JISA;

import JISA.Addresses.*;
import JISA.Devices.*;
import JISA.Experiment.*;
import JISA.GUI.*;
import JISA.VISA.VISA;
import javafx.application.Application;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Random;
import java.util.RandomAccess;

public class Main extends Application {

    private static void run() throws Exception {

        K2450 smu = new K2450(new GPIBAddress(0, 2));

        ResultList results = new ResultList("Voltage", "Current");
        results.setUnits("V", "nA");
        PlotWindow  plot  = PlotWindow.create("I-V Plot", results);
        TableWindow table = TableWindow.create("Data", results);
        GridWindow  grid  = GridWindow.create("Results");

        grid.addPane(table);
        grid.addPane(plot);

        grid.show();

        smu.performLinearSweep(
                SMU.Source.VOLTAGE,
                0,
                10,
                20,
                500,
                (i, p) -> {
                    results.addData(p.voltage, p.current * 1e9);
                }
        );

        results.outputTable();

        smu.turnOff();

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
