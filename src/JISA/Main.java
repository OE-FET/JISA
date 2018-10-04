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
import java.util.Scanner;

public class Main extends GUI {

    public static void run() throws Exception {

        SMU smu = new K2450(new GPIBAddress(0, 2));

        smu.setTerminals(K2450.Terminals.FRONT);

        ResultList data = new ResultList("Voltage", "Current");
        ResultList res  = new ResultList("Voltage", "Resistance");
        data.setUnits("V", "A");
        res.setUnits("V", "Ohm");

        Table table = new Table("Table of Results", data);
        Plot  plot1 = new Plot("Plot of Results", data);
        Plot  plot2 = new Plot("Resistance", res);

        Grid grid = new Grid("Sweep", table, plot1, plot2);
        grid.show();

        smu.doLinearSweep(
                SMU.Source.CURRENT,
                0,
                50e-3,
                10,
                500,
                true,
                (i, p) -> {
                    if (i > 0) {
                        double lastV = data.getLastRow().get(0);
                        double lastI = data.getLastRow().get(1);
                        res.addData(0.5 * (lastV + p.voltage), (p.voltage - lastV) / (p.current - lastI));
                    }
                    data.addData(p.voltage, p.current);
                }
        );

        smu.turnOff();

        data.outputTable();

    }

    public static void main(String[] args) {

        try {
            run();
        } catch (Exception e) {
            Util.exceptionHandler(e);
        }

    }

}
