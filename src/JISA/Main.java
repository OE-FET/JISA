package JISA;

import JISA.Experiment.*;
import JISA.GUI.FXML.PlotWindow;
import JISA.GUI.Plot;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    private static void run() throws Exception {



        ResultList list = new ResultList("Frequency","Set Amplitude","Measured Amplitude","Error");
        list.setUnits("Hz","V","V","%");

        Plot plot = new Plot("Results", list, 0, 3);
        plot.show();

        for (double f = 0.1; f <= 10; f += 0.2) {

            double ref = 50e-3;
            double mes = ref - (1/Math.sqrt(f)) * 0.5e-3;

            list.addData(
                    f,
                    ref,
                    mes,
                    100 * (Math.abs(ref - mes) / Math.max(ref,mes))
            );

        }

        list.output(",", System.out);

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
