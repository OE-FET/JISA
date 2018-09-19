package JISA;

import JISA.Experiment.*;
import JISA.GUI.FXML.PlotWindow;
import JISA.GUI.Plot;
import JISA.GUI.Table;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.Random;

public class Main extends Application {

    private static void run() throws Exception {



        ResultList list = new ResultList("Frequency","Voltage","Current");
        list.setUnits("Hz","V","A");

        Table table = new Table("Results", list);
        table.show();

        Random rand = new Random();

        for (int i = 0; i < 10; i ++) {
            list.addData(
                    rand.nextDouble() * 100,
                    rand.nextDouble() * 100,
                    rand.nextDouble() * 100
            );
            Thread.sleep(100);
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
