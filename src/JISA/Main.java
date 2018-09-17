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

        K2450 smu = new K2450(new GPIBAddress(0, 15));

        double voltage = smu.getVoltage();
        double current = smu.getCurrent();

        SMU.DataPoint[] points = smu.performLinearSweep(SMU.Source.VOLTAGE, 0, 10, 5, 500);

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
